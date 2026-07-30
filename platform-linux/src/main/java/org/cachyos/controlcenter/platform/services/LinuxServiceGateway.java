package org.cachyos.controlcenter.platform.services;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import org.cachyos.controlcenter.helper.api.HelperReply;
import org.cachyos.controlcenter.helper.api.PrivilegedHelper;
import org.cachyos.controlcenter.modules.services.ServiceGateway;
import org.cachyos.controlcenter.modules.services.ServiceManager;
import org.cachyos.controlcenter.modules.services.ServiceOperation;
import org.cachyos.controlcenter.modules.services.ServiceResult;
import org.cachyos.controlcenter.modules.services.ServiceScope;
import org.cachyos.controlcenter.platform.status.FixedCommandReader;
import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder;

public final class LinuxServiceGateway implements ServiceGateway {
  private final boolean linux;

  public LinuxServiceGateway(boolean linux) {
    this.linux = linux;
  }

  @Override
  public ServiceResult execute(ServiceScope scope, String unitName, ServiceOperation operation) {
    if (!linux || !ServiceManager.valid(unitName)) {
      return new ServiceResult(false, "Dienstaktion abgelehnt");
    }
    String verb = operation.name().toLowerCase(java.util.Locale.ROOT);
    return scope == ServiceScope.USER ? user(unitName, verb) : system(unitName, verb);
  }

  private static ServiceResult user(String unitName, String verb) {
    boolean success =
        FixedCommandReader.read(
                Path.of("/usr/bin/systemctl"),
                List.of("--user", verb, "--", unitName),
                Duration.ofSeconds(30))
            .isPresent();
    return new ServiceResult(
        success, success ? "Benutzerdienst geändert" : "Benutzerdienstaktion fehlgeschlagen");
  }

  private static ServiceResult system(String unitName, String verb) {
    try (DBusConnection connection = DBusConnectionBuilder.forSystemBus().build()) {
      PrivilegedHelper helper =
          connection.getRemoteObject(
              PrivilegedHelper.BUS_NAME, PrivilegedHelper.OBJECT_PATH, PrivilegedHelper.class);
      HelperReply reply = HelperReply.decode(helper.controlSystemService(unitName, verb));
      return new ServiceResult(reply.successful(), reply.message());
    } catch (Exception exception) {
      return new ServiceResult(false, "Privilegierter Helper ist nicht erreichbar");
    }
  }
}
