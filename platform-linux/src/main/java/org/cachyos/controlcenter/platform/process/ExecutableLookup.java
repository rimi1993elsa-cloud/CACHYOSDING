package org.cachyos.controlcenter.platform.process;

import java.nio.file.Path;
import java.util.Optional;

/** Resolves a fixed executable name to an absolute path. */
@FunctionalInterface
public interface ExecutableLookup {
  Optional<Path> find(String executableName);
}
