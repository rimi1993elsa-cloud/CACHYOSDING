package org.cachyos.controlcenter.platform.processes;

import org.cachyos.controlcenter.helper.api.HelperReply;
import org.cachyos.controlcenter.helper.api.PrivilegedHelper;
import org.cachyos.controlcenter.modules.processes.ProcessGateway;
import org.cachyos.controlcenter.modules.processes.ProcessResult;
import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder;

public final class DbusProcessGateway implements ProcessGateway {
  private final boolean linux;

  public DbusProcessGateway(boolean linux) {
    this.linux = linux;
  }

  public ProcessResult signal(long pid, int signal) {
    return call(helper -> helper.signalProcess(pid, signal));
  }

  public ProcessResult priority(long pid, int priority) {
    return call(helper -> helper.setProcessPriority(pid, priority));
  }

  private ProcessResult call(java.util.function.Function<PrivilegedHelper, String> operation) {
    if (!linux) {
      return new ProcessResult(false, "Helper-Aufruf abgelehnt");
    }
    try (DBusConnection connection = DBusConnectionBuilder.forSystemBus().build()) {
      PrivilegedHelper helper =
          connection.getRemoteObject(
              PrivilegedHelper.BUS_NAME, PrivilegedHelper.OBJECT_PATH, PrivilegedHelper.class);
      HelperReply reply = HelperReply.decode(operation.apply(helper));
      return new ProcessResult(reply.successful(), reply.message());
    } catch (Exception exception) {
      return new ProcessResult(false, "Privilegierter Helper ist nicht erreichbar");
    }
  }
}
