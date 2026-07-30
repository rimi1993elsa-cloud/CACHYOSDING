package org.cachyos.controlcenter.helper;

import java.util.Objects;
import org.cachyos.controlcenter.helper.api.HelperReply;
import org.cachyos.controlcenter.helper.api.PrivilegedHelper;
import org.freedesktop.dbus.DBusCallInfo;
import org.freedesktop.dbus.connections.AbstractConnection;

final class DbusPrivilegedHelper implements PrivilegedHelper {
  private final PrivilegedHelperService service;

  DbusPrivilegedHelper(PrivilegedHelperService service) {
    this.service = Objects.requireNonNull(service, "service");
  }

  @Override
  public String installPackage(String packageName) {
    return encode(service.installPackage(sender(), bounded(packageName, 128)));
  }

  @Override
  public String removePackage(String packageName) {
    return encode(service.removePackage(sender(), bounded(packageName, 128)));
  }

  @Override
  public String setFirewallEnabled(boolean enabled) {
    return encode(service.setFirewallEnabled(sender(), enabled));
  }

  @Override
  public String controlSystemService(String unitName, String operation) {
    return encode(
        service.controlSystemService(sender(), bounded(unitName, 160), bounded(operation, 16)));
  }

  @Override
  public String createSnapshot(String description) {
    return encode(service.createSnapshot(sender(), bounded(description, 120)));
  }

  @Override
  public String deleteSnapshot(int snapshotId) {
    return encode(service.deleteSnapshot(sender(), snapshotId));
  }

  @Override
  public String signalProcess(long processId, int signal) {
    return encode(service.signalProcess(sender(), processId, signal));
  }

  @Override
  public String setProcessPriority(long processId, int priority) {
    return encode(service.setProcessPriority(sender(), processId, priority));
  }

  @Override
  public boolean isRemote() {
    return false;
  }

  @Override
  public String getObjectPath() {
    return OBJECT_PATH;
  }

  private static String sender() {
    DBusCallInfo info = AbstractConnection.getCallInfo();
    return info == null ? "" : info.getSource();
  }

  private static String bounded(String value, int limit) {
    return value != null && value.length() <= limit ? value : "";
  }

  private static String encode(HelperReply reply) {
    return reply.encode();
  }
}
