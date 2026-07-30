package org.cachyos.controlcenter.platform.storage;

import org.cachyos.controlcenter.helper.api.HelperReply;
import org.cachyos.controlcenter.helper.api.PrivilegedHelper;
import org.cachyos.controlcenter.modules.snapshots.SnapshotGateway;
import org.cachyos.controlcenter.modules.snapshots.SnapshotResult;
import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder;

public final class DbusSnapshotGateway implements SnapshotGateway {
  private final boolean linux;

  public DbusSnapshotGateway(boolean linux) {
    this.linux = linux;
  }

  @Override
  public boolean available() {
    return linux;
  }

  @Override
  public SnapshotResult create(String description) {
    return call(helper -> helper.createSnapshot(description));
  }

  @Override
  public SnapshotResult delete(int id) {
    return call(helper -> helper.deleteSnapshot(id));
  }

  private SnapshotResult call(java.util.function.Function<PrivilegedHelper, String> operation) {
    if (!linux) {
      return new SnapshotResult(false, "Helper-Aufruf abgelehnt");
    }
    try (DBusConnection connection = DBusConnectionBuilder.forSystemBus().build()) {
      PrivilegedHelper helper =
          connection.getRemoteObject(
              PrivilegedHelper.BUS_NAME, PrivilegedHelper.OBJECT_PATH, PrivilegedHelper.class);
      HelperReply reply = HelperReply.decode(operation.apply(helper));
      return new SnapshotResult(reply.successful(), reply.message());
    } catch (Exception exception) {
      return new SnapshotResult(false, "Privilegierter Helper ist nicht erreichbar");
    }
  }
}
