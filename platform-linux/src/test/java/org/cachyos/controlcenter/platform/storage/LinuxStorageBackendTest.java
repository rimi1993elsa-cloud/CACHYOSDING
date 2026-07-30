package org.cachyos.controlcenter.platform.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class LinuxStorageBackendTest {
  @Test
  void parsesNestedLsblkAndQuotedSnapperCsv() {
    LinuxStorageBackend backend = new LinuxStorageBackend(true);
    var devices =
        backend.parseDevices(
            "{\"blockdevices\":[{\"path\":\"/dev/nvme0n1\",\"type\":\"disk\",\"size\":100,"
                + "\"children\":[{\"path\":\"/dev/nvme0n1p1\",\"type\":\"part\",\"size\":50}]}]}");
    assertEquals(2, devices.size());
    assertEquals("hello, world", LinuxSnapshotBackend.csv("1,x,\"hello, world\"").get(2));
  }
}
