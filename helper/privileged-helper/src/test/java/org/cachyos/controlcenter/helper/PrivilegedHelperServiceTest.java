package org.cachyos.controlcenter.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.cachyos.controlcenter.helper.api.HelperErrorCode;
import org.junit.jupiter.api.Test;

class PrivilegedHelperServiceTest {
  private static final String SENDER = ":1.42";

  @Test
  void rejectsManipulatedArgumentsBeforeAuthorizationOrExecution() {
    RecordingExecutor executor = new RecordingExecutor();
    List<String> audit = new ArrayList<>();
    try (PrivilegedHelperService service =
        new PrivilegedHelperService(
            (sender, action) -> true,
            (sender, action, outcome) -> audit.add(outcome),
            executor,
            Duration.ofSeconds(1))) {
      assertEquals(
          HelperErrorCode.INVALID_ARGUMENT,
          service.installPackage(SENDER, "ok;touch /tmp/pwned").code());
      assertEquals(
          HelperErrorCode.INVALID_ARGUMENT,
          service.controlSystemService(SENDER, "../../ssh.service", "start").code());
      assertEquals(
          HelperErrorCode.INVALID_ARGUMENT,
          service.controlSystemService(SENDER, "ssh.service", "start;id").code());
      assertEquals(
          HelperErrorCode.INVALID_ARGUMENT, service.createSnapshot(SENDER, "$(id)").code());
      assertEquals(HelperErrorCode.INVALID_ARGUMENT, service.signalProcess(SENDER, 2, 1).code());
    }
    assertFalse(executor.called);
    assertEquals(5, audit.size());
  }

  @Test
  void requiresPolkitAuthorizationForValidAction() {
    RecordingExecutor executor = new RecordingExecutor();
    try (PrivilegedHelperService service =
        new PrivilegedHelperService(
            (sender, action) -> false,
            (sender, action, outcome) -> {},
            executor,
            Duration.ofSeconds(1))) {
      assertEquals(
          HelperErrorCode.NOT_AUTHORIZED, service.installPackage(SENDER, "linux-cachyos").code());
    }
    assertFalse(executor.called);
  }

  @Test
  void executesOnlyTypedAuthorizedActionAndAuditsOutcome() {
    RecordingExecutor executor = new RecordingExecutor();
    List<String> audit = new ArrayList<>();
    try (PrivilegedHelperService service =
        new PrivilegedHelperService(
            (sender, action) -> sender.equals(SENDER),
            (sender, action, outcome) -> audit.add(action + ":" + outcome),
            executor,
            Duration.ofSeconds(1))) {
      assertTrue(service.installPackage(SENDER, "linux-cachyos").successful());
    }
    assertTrue(executor.called);
    assertEquals(List.of("PACKAGE_MANAGE:OK"), audit);
  }

  @Test
  void cancelsOperationAtTimeout() {
    RecordingExecutor executor = new RecordingExecutor();
    executor.delay = true;
    try (PrivilegedHelperService service =
        new PrivilegedHelperService(
            (sender, action) -> true,
            (sender, action, outcome) -> {},
            executor,
            Duration.ofMillis(20))) {
      assertEquals(HelperErrorCode.TIMEOUT, service.installPackage(SENDER, "linux-cachyos").code());
    }
  }

  private static final class RecordingExecutor implements HelperExecutor {
    private boolean called;
    private boolean delay;

    @Override
    public int installPackage(String packageName) throws InterruptedException {
      called = true;
      if (delay) {
        Thread.sleep(5_000);
      }
      return 0;
    }

    @Override
    public int removePackage(String packageName) {
      called = true;
      return 0;
    }

    @Override
    public int setFirewallEnabled(boolean enabled) {
      called = true;
      return 0;
    }

    @Override
    public int controlSystemService(String unitName, String operation) {
      called = true;
      return 0;
    }

    @Override
    public int createSnapshot(String description) {
      called = true;
      return 0;
    }

    @Override
    public int deleteSnapshot(int snapshotId) {
      called = true;
      return 0;
    }

    @Override
    public int signalProcess(long processId, int signal) {
      called = true;
      return 0;
    }

    @Override
    public int setProcessPriority(long processId, int priority) {
      called = true;
      return 0;
    }
  }
}
