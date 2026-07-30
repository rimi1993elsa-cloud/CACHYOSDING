package org.cachyos.controlcenter.xdg;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;

class XdgPathsTest {
  @Test
  void usesXdgDirectoriesWhenTheyAreAbsolute() {
    Path root = Path.of(System.getProperty("java.io.tmpdir")).toAbsolutePath();

    XdgPaths paths =
        XdgPaths.resolve(
            root.resolve("home"),
            Map.of(
                "XDG_CONFIG_HOME", root.resolve("cfg").toString(),
                "XDG_DATA_HOME", root.resolve("data").toString(),
                "XDG_CACHE_HOME", root.resolve("cache").toString()));

    assertEquals(root.resolve("cfg/cachyos-control-center"), paths.configDirectory());
    assertEquals(root.resolve("data/cachyos-control-center"), paths.dataDirectory());
    assertEquals(root.resolve("cache/cachyos-control-center"), paths.cacheDirectory());
  }

  @Test
  void ignoresUnsafeRelativeXdgDirectories() {
    Path home = Path.of(System.getProperty("java.io.tmpdir")).toAbsolutePath().resolve("home");

    XdgPaths paths = XdgPaths.resolve(home, Map.of("XDG_CONFIG_HOME", "../relative"));

    assertEquals(home.resolve(".config/cachyos-control-center"), paths.configDirectory());
  }
}
