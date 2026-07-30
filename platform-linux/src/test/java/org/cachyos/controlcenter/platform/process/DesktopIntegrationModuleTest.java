package org.cachyos.controlcenter.platform.process;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.cachyos.controlcenter.core.action.ActionId;
import org.cachyos.controlcenter.core.action.ActionRejectedException;
import org.cachyos.controlcenter.core.action.ActionRequest;
import org.cachyos.controlcenter.core.action.ActionStatus;
import org.cachyos.controlcenter.core.action.InputSource;
import org.cachyos.controlcenter.systeminfo.OperatingSystemFamily;
import org.junit.jupiter.api.Test;

class DesktopIntegrationModuleTest {
  private static final Path ROOT =
      Path.of(System.getProperty("java.io.tmpdir")).toAbsolutePath().resolve("fake-root");

  @Test
  void launchesOnlyResolvedFixedCommand() {
    DesktopCommandResolver resolver =
        new DesktopCommandResolver(
            OperatingSystemFamily.LINUX,
            ROOT.resolve("home/tester"),
            Map.of(),
            name ->
                "firefox".equals(name)
                    ? Optional.of(ROOT.resolve("usr/bin/firefox"))
                    : Optional.empty());
    AtomicReference<CommandSpec> launched = new AtomicReference<>();
    DesktopIntegrationModule module =
        new DesktopIntegrationModule(
            resolver,
            command -> {
              launched.set(command);
              return new ProcessLaunchResult(42);
            });

    assertEquals(
        ActionStatus.SUCCESS,
        module.execute(ActionRequest.fromButton(ActionId.OPEN_FIREFOX)).status());
    assertEquals(
        ROOT.resolve("usr/bin/firefox").toString(), launched.get().commandLine().getFirst());
    assertEquals(1, launched.get().commandLine().size());
  }

  @Test
  void rejectsInjectedParametersBeforeProcessBoundary() {
    DesktopCommandResolver resolver =
        new DesktopCommandResolver(
            OperatingSystemFamily.LINUX,
            ROOT.resolve("home/tester"),
            Map.of(),
            ignored -> Optional.of(ROOT.resolve("usr/bin/firefox")));
    DesktopIntegrationModule module =
        new DesktopIntegrationModule(resolver, command -> new ProcessLaunchResult(1));
    ActionRequest request =
        new ActionRequest(
            ActionId.OPEN_FIREFOX,
            InputSource.TEXT,
            Map.of("argument", "--profile; rm -rf"),
            Instant.now());

    assertThrows(ActionRejectedException.class, () -> module.execute(request));
  }
}
