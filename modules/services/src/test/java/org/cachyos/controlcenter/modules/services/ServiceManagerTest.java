package org.cachyos.controlcenter.modules.services;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class ServiceManagerTest {
  @Test
  void rejectsInjectedUnitAndPreservesScope() {
    TrackingGateway gateway = new TrackingGateway();
    try (ServiceManager manager =
        new ServiceManager(
            new ServiceBackend() {
              public ServiceState inspect() {
                return new ServiceState(true, List.of(), "");
              }

              public List<String> logs(ServiceScope scope, String unitName) {
                return List.of();
              }
            },
            gateway)) {
      assertFalse(
          manager
              .execute(ServiceScope.SYSTEM, "ssh.service;id", ServiceOperation.START)
              .join()
              .successful());
      assertFalse(gateway.called);
      assertTrue(
          manager
              .execute(ServiceScope.USER, "pipewire.service", ServiceOperation.RESTART)
              .join()
              .successful());
      assertTrue(gateway.user);
    }
  }

  private static final class TrackingGateway implements ServiceGateway {
    private boolean called;
    private boolean user;

    @Override
    public ServiceResult execute(ServiceScope scope, String unitName, ServiceOperation operation) {
      called = true;
      user = scope == ServiceScope.USER;
      return new ServiceResult(true, "ok");
    }
  }
}
