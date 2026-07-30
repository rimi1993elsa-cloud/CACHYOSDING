package org.cachyos.controlcenter.helper;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.cachyos.controlcenter.helper.api.HelperErrorCode;
import org.cachyos.controlcenter.helper.api.HelperReply;

final class PrivilegedHelperService implements AutoCloseable {
  private final HelperAuthorizer authorizer;
  private final HelperAuditSink audit;
  private final HelperExecutor executor;
  private final Duration timeout;
  private final ExecutorService worker;

  PrivilegedHelperService(
      HelperAuthorizer authorizer,
      HelperAuditSink audit,
      HelperExecutor executor,
      Duration timeout) {
    this.authorizer = Objects.requireNonNull(authorizer, "authorizer");
    this.audit = Objects.requireNonNull(audit, "audit");
    this.executor = Objects.requireNonNull(executor, "executor");
    this.timeout = Objects.requireNonNull(timeout, "timeout");
    worker =
        Executors.newSingleThreadExecutor(Thread.ofPlatform().name("privileged-helper").factory());
  }

  HelperReply installPackage(String sender, String packageName) {
    if (!HelperValidation.packageName(packageName)) {
      return rejected(sender, HelperAction.PACKAGE_MANAGE);
    }
    return execute(sender, HelperAction.PACKAGE_MANAGE, () -> executor.installPackage(packageName));
  }

  HelperReply removePackage(String sender, String packageName) {
    if (!HelperValidation.packageName(packageName)) {
      return rejected(sender, HelperAction.PACKAGE_MANAGE);
    }
    return execute(sender, HelperAction.PACKAGE_MANAGE, () -> executor.removePackage(packageName));
  }

  HelperReply setFirewallEnabled(String sender, boolean enabled) {
    return execute(
        sender, HelperAction.FIREWALL_MANAGE, () -> executor.setFirewallEnabled(enabled));
  }

  HelperReply controlSystemService(String sender, String unitName, String operation) {
    if (!HelperValidation.unitName(unitName) || !HelperValidation.serviceOperation(operation)) {
      return rejected(sender, HelperAction.SERVICE_MANAGE);
    }
    return execute(
        sender,
        HelperAction.SERVICE_MANAGE,
        () -> executor.controlSystemService(unitName, operation));
  }

  HelperReply createSnapshot(String sender, String description) {
    if (!HelperValidation.snapshotDescription(description)) {
      return rejected(sender, HelperAction.SNAPSHOT_MANAGE);
    }
    return execute(
        sender, HelperAction.SNAPSHOT_MANAGE, () -> executor.createSnapshot(description));
  }

  HelperReply deleteSnapshot(String sender, int snapshotId) {
    if (!HelperValidation.snapshotId(snapshotId)) {
      return rejected(sender, HelperAction.SNAPSHOT_MANAGE);
    }
    return execute(sender, HelperAction.SNAPSHOT_MANAGE, () -> executor.deleteSnapshot(snapshotId));
  }

  HelperReply signalProcess(String sender, long processId, int signal) {
    if (!HelperValidation.processId(processId) || !HelperValidation.signal(signal)) {
      return rejected(sender, HelperAction.PROCESS_MANAGE);
    }
    return execute(
        sender, HelperAction.PROCESS_MANAGE, () -> executor.signalProcess(processId, signal));
  }

  HelperReply setProcessPriority(String sender, long processId, int priority) {
    if (!HelperValidation.processId(processId) || !HelperValidation.priority(priority)) {
      return rejected(sender, HelperAction.PROCESS_MANAGE);
    }
    return execute(
        sender,
        HelperAction.PROCESS_MANAGE,
        () -> executor.setProcessPriority(processId, priority));
  }

  private HelperReply rejected(String sender, HelperAction action) {
    audit.record(sender, action, HelperErrorCode.INVALID_ARGUMENT.name());
    return new HelperReply(HelperErrorCode.INVALID_ARGUMENT, "Ungültige Parameter");
  }

  private HelperReply execute(String sender, HelperAction action, Callable<Integer> operation) {
    if (!HelperValidation.sender(sender) || !authorizer.authorize(sender, action)) {
      audit.record(sender, action, HelperErrorCode.NOT_AUTHORIZED.name());
      return new HelperReply(HelperErrorCode.NOT_AUTHORIZED, "Autorisierung abgelehnt");
    }
    Future<Integer> future = worker.submit(operation);
    HelperReply reply;
    try {
      int exitCode = future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
      reply =
          exitCode == 0
              ? new HelperReply(HelperErrorCode.OK, "Aktion abgeschlossen")
              : new HelperReply(
                  HelperErrorCode.EXECUTION_FAILED, "Systemwerkzeug meldet Fehler " + exitCode);
    } catch (TimeoutException exception) {
      future.cancel(true);
      reply = new HelperReply(HelperErrorCode.TIMEOUT, "Zeitlimit überschritten");
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      reply = new HelperReply(HelperErrorCode.INTERNAL_ERROR, "Helper unterbrochen");
    } catch (ExecutionException exception) {
      reply = new HelperReply(HelperErrorCode.EXECUTION_FAILED, "Systemaktion fehlgeschlagen");
    }
    audit.record(sender, action, reply.code().name());
    return reply;
  }

  @Override
  public void close() {
    worker.shutdownNow();
  }
}
