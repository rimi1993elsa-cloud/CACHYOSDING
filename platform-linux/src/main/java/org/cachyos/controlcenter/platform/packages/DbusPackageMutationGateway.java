package org.cachyos.controlcenter.platform.packages;

import org.cachyos.controlcenter.helper.api.HelperReply;
import org.cachyos.controlcenter.helper.api.PrivilegedHelper;
import org.cachyos.controlcenter.modules.packages.PackageAction;
import org.cachyos.controlcenter.modules.packages.PackageMutationGateway;
import org.cachyos.controlcenter.modules.packages.PackageNames;
import org.cachyos.controlcenter.modules.packages.PackageOperationResult;
import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder;

public final class DbusPackageMutationGateway implements PackageMutationGateway {
  private final boolean linux;

  public DbusPackageMutationGateway(boolean linux) {
    this.linux = linux;
  }

  @Override
  public boolean available() {
    return linux;
  }

  @Override
  public PackageOperationResult execute(PackageAction action, String packageName) {
    if (!linux || !PackageNames.valid(packageName)) {
      return new PackageOperationResult(false, "Helper-Aufruf abgelehnt");
    }
    try (DBusConnection connection = DBusConnectionBuilder.forSystemBus().build()) {
      PrivilegedHelper helper =
          connection.getRemoteObject(
              PrivilegedHelper.BUS_NAME, PrivilegedHelper.OBJECT_PATH, PrivilegedHelper.class);
      String encoded =
          action == PackageAction.INSTALL
              ? helper.installPackage(packageName)
              : helper.removePackage(packageName);
      HelperReply reply = HelperReply.decode(encoded);
      return new PackageOperationResult(reply.successful(), reply.message());
    } catch (Exception exception) {
      return new PackageOperationResult(false, "Privilegierter Helper ist nicht erreichbar");
    }
  }
}
