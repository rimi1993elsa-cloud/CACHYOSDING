package org.cachyos.controlcenter.helper;

enum HelperAction {
  PACKAGE_MANAGE("org.cachyos.controlcenter.package-manage"),
  FIREWALL_MANAGE("org.cachyos.controlcenter.firewall-manage"),
  SERVICE_MANAGE("org.cachyos.controlcenter.service-manage"),
  SNAPSHOT_MANAGE("org.cachyos.controlcenter.snapshot-manage"),
  PROCESS_MANAGE("org.cachyos.controlcenter.process-manage");

  private final String polkitId;

  HelperAction(String polkitId) {
    this.polkitId = polkitId;
  }

  String polkitId() {
    return polkitId;
  }
}
