package org.cachyos.controlcenter.platform.process;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.cachyos.controlcenter.core.action.ActionId;
import org.cachyos.controlcenter.systeminfo.OperatingSystemFamily;
import org.junit.jupiter.api.Test;

class DesktopCommandResolverTest {
  private static final Path ROOT =
      Path.of(System.getProperty("java.io.tmpdir")).toAbsolutePath().resolve("fake-root");
  private static final Path HOME = ROOT.resolve("home/tester");

  @Test
  void resolvesLinuxActionsToFixedArgumentLists() {
    ExecutableLookup lookup =
        name ->
            Optional.ofNullable(
                Map.of(
                        "firefox", ROOT.resolve("usr/bin/firefox"),
                        "dolphin", ROOT.resolve("usr/bin/dolphin"),
                        "konsole", ROOT.resolve("usr/bin/konsole"),
                        "loginctl", ROOT.resolve("usr/bin/loginctl"))
                    .get(name));
    DesktopCommandResolver resolver =
        new DesktopCommandResolver(OperatingSystemFamily.LINUX, HOME, Map.of(), lookup);

    assertEquals(
        List.of(ROOT.resolve("usr/bin/firefox").toString()),
        resolver.resolve(ActionId.OPEN_FIREFOX).orElseThrow().commandLine());
    assertEquals(
        List.of(ROOT.resolve("usr/bin/dolphin").toString(), HOME.toString()),
        resolver.resolve(ActionId.OPEN_FILE_MANAGER).orElseThrow().commandLine());
    assertEquals(
        List.of(ROOT.resolve("usr/bin/konsole").toString()),
        resolver.resolve(ActionId.OPEN_TERMINAL).orElseThrow().commandLine());
    assertEquals(
        List.of(ROOT.resolve("usr/bin/loginctl").toString(), "lock-session"),
        resolver.resolve(ActionId.LOCK_SCREEN).orElseThrow().commandLine());
  }

  @Test
  void returnsUnavailableWhenExecutableIsMissing() {
    DesktopCommandResolver resolver =
        new DesktopCommandResolver(
            OperatingSystemFamily.LINUX, HOME, Map.of(), ignored -> Optional.empty());

    assertTrue(resolver.resolve(ActionId.OPEN_FIREFOX).isEmpty());
  }
}
