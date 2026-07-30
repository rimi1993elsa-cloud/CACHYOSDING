package org.cachyos.controlcenter.helper;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import org.cachyos.controlcenter.helper.api.PrivilegedHelper;
import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder;

public final class HelperMain {
  private HelperMain() {}

  public static void main(String[] args) throws Exception {
    try (PrivilegedHelperService service =
            new PrivilegedHelperService(
                new PolkitAuthorizer(),
                new StructuredAuditSink(),
                new LinuxFixedExecutor(),
                Duration.ofMinutes(6));
        DBusConnection connection = DBusConnectionBuilder.forSystemBus().build()) {
      connection.requestBusName(PrivilegedHelper.BUS_NAME);
      connection.exportObject(PrivilegedHelper.OBJECT_PATH, new DbusPrivilegedHelper(service));
      new CountDownLatch(1).await();
    }
  }
}
