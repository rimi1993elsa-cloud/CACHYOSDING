package org.cachyos.controlcenter.platform.process;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

class PathExecutableLookupTest {
  @Test
  void rejectsPathsAndCommandSyntaxAsExecutableNames() {
    PathExecutableLookup lookup = new PathExecutableLookup(Map.of("PATH", "/usr/bin"), false);

    assertTrue(lookup.find("../bin/sh").isEmpty());
    assertTrue(lookup.find("sh -c").isEmpty());
    assertTrue(lookup.find("firefox;rm").isEmpty());
  }
}
