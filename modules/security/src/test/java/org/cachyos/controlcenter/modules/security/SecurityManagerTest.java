package org.cachyos.controlcenter.modules.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class SecurityManagerTest {
  @Test
  void inspectionIsReadOnlyAndMutationUsesOnlyGateway() {
    TrackingGateway gateway = new TrackingGateway();
    SecurityBackend backend =
        () -> new SecuritySnapshot(true, false, List.of(), List.of(), Instant.now(), "ok");
    try (SecurityManager manager = new SecurityManager(backend, gateway)) {
      assertFalse(manager.inspect().join().firewallEnabled());
      assertFalse(gateway.called);
      assertTrue(manager.setFirewallEnabled(true).join().successful());
      assertTrue(gateway.called);
    }
  }

  private static final class TrackingGateway implements SecurityMutationGateway {
    private boolean called;

    @Override
    public boolean available() {
      return true;
    }

    @Override
    public SecurityOperationResult setFirewallEnabled(boolean enabled) {
      called = true;
      return new SecurityOperationResult(enabled, "ok");
    }
  }
}
