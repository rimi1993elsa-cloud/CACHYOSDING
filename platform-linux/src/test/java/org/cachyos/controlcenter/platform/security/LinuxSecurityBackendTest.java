package org.cachyos.controlcenter.platform.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class LinuxSecurityBackendTest {
  @Test
  void parsesIpv4AndIpv6ListeningPortsWithoutShell() {
    var ports =
        LinuxSecurityBackend.parsePorts(
            List.of("tcp LISTEN 0 4096 127.0.0.1:631", "udp UNCONN 0 0 [::]:5353 users:(mdns)"));
    assertEquals(2, ports.size());
    assertEquals(631, ports.getFirst().port());
    assertEquals(5353, ports.getLast().port());
  }

  @Test
  void ignoresMalformedOrNamedEndpoints() {
    var ports =
        LinuxSecurityBackend.parsePorts(
            List.of("tcp LISTEN 0 1 *:ssh", "broken", "tcp LISTEN 0 1 *:70000"));
    assertTrue(ports.isEmpty());
  }
}
