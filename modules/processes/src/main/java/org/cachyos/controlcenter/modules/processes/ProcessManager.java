package org.cachyos.controlcenter.modules.processes;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ProcessManager implements AutoCloseable {
  private final ProcessBackend backend;
  private final ProcessGateway gateway;
  private final Map<Long, ProcessEntry> known = new ConcurrentHashMap<>();
  private final ExecutorService worker =
      Executors.newSingleThreadExecutor(Thread.ofPlatform().name("process-manager").factory());

  public ProcessManager(ProcessBackend backend, ProcessGateway gateway) {
    this.backend = backend;
    this.gateway = gateway;
  }

  public CompletableFuture<List<ProcessEntry>> inspect() {
    return CompletableFuture.supplyAsync(
        () -> {
          List<ProcessEntry> entries = backend.inspect();
          known.clear();
          entries.forEach(entry -> known.put(entry.pid(), entry));
          return entries;
        },
        worker);
  }

  public CompletableFuture<ProcessResult> terminate(long pid) {
    return signal(pid, 15, "");
  }

  public CompletableFuture<ProcessResult> kill(long pid, String typedPid) {
    return signal(pid, 9, typedPid);
  }

  public CompletableFuture<ProcessResult> setPriority(long pid, int priority) {
    ProcessEntry entry = known.get(pid);
    if (entry == null || entry.critical() || priority < -20 || priority > 19) {
      return CompletableFuture.completedFuture(new ProcessResult(false, "Prozessaktion abgelehnt"));
    }
    return CompletableFuture.supplyAsync(() -> gateway.priority(pid, priority), worker);
  }

  private CompletableFuture<ProcessResult> signal(long pid, int signal, String confirmation) {
    ProcessEntry entry = known.get(pid);
    if (entry == null
        || entry.critical()
        || (signal == 9 && !Long.toString(pid).equals(confirmation))) {
      return CompletableFuture.completedFuture(new ProcessResult(false, "Prozesssignal abgelehnt"));
    }
    return CompletableFuture.supplyAsync(() -> gateway.signal(pid, signal), worker);
  }

  @Override
  public void close() {
    known.clear();
    worker.shutdownNow();
  }
}
