package org.cachyos.controlcenter.helper.api;

import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.interfaces.DBusInterface;

@DBusInterfaceName("org.cachyos.ControlCenter.Helper1")
public interface PrivilegedHelper extends DBusInterface {
  String BUS_NAME = "org.cachyos.ControlCenter.Helper1";
  String OBJECT_PATH = "/org/cachyos/ControlCenter/Helper1";

  String installPackage(String packageName);

  String removePackage(String packageName);

  String setFirewallEnabled(boolean enabled);

  String controlSystemService(String unitName, String operation);

  String createSnapshot(String description);

  String deleteSnapshot(int snapshotId);

  String signalProcess(long processId, int signal);

  String setProcessPriority(long processId, int priority);
}
