package org.cachyos.controlcenter.platform.security;

import org.cachyos.controlcenter.helper.api.HelperReply;
import org.cachyos.controlcenter.helper.api.PrivilegedHelper;
import org.cachyos.controlcenter.modules.security.SecurityMutationGateway;
import org.cachyos.controlcenter.modules.security.SecurityOperationResult;
import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder;

public final class DbusSecurityMutationGateway implements SecurityMutationGateway {
  private final boolean linux;

  public DbusSecurityMutationGateway(boolean linux) {
    this.linux = linux;
  }

  @Override
  public boolean available() {
    return linux;
  }

  @Override
  public SecurityOperationResult setFirewallEnabled(boolean enabled) {
    if (!linux) {
      return new SecurityOperationResult(false, "Helper-Aufruf abgelehnt");
    }
    try (DBusConnection connection = DBusConnectionBuilder.forSystemBus().build()) {
      PrivilegedHelper helper =
          connection.getRemoteObject(
              PrivilegedHelper.BUS_NAME, PrivilegedHelper.OBJECT_PATH, PrivilegedHelper.class);
      HelperReply reply = HelperReply.decode(helper.setFirewallEnabled(enabled));
      return new SecurityOperationResult(reply.successful(), reply.message());
    } catch (Exception exception) {
      return new SecurityOperationResult(false, "Privilegierter Helper ist nicht erreichbar");
    }
  }
}
