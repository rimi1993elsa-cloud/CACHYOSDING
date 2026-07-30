package org.cachyos.controlcenter.xdg;

import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

/** XDG-compliant application directories with safe home-directory fallbacks. */
public record XdgPaths(Path configDirectory, Path dataDirectory, Path cacheDirectory) {
  private static final String APPLICATION_DIRECTORY = "cachyos-control-center";

  public XdgPaths {
    configDirectory = absolute(configDirectory, "configDirectory");
    dataDirectory = absolute(dataDirectory, "dataDirectory");
    cacheDirectory = absolute(cacheDirectory, "cacheDirectory");
  }

  public static XdgPaths detect() {
    return resolve(Path.of(System.getProperty("user.home")), System.getenv());
  }

  public static XdgPaths resolve(Path homeDirectory, Map<String, String> environment) {
    Path home = absolute(homeDirectory, "homeDirectory");
    Objects.requireNonNull(environment, "environment");
    return new XdgPaths(
        basePath(environment.get("XDG_CONFIG_HOME"), home.resolve(".config"))
            .resolve(APPLICATION_DIRECTORY),
        basePath(environment.get("XDG_DATA_HOME"), home.resolve(".local/share"))
            .resolve(APPLICATION_DIRECTORY),
        basePath(environment.get("XDG_CACHE_HOME"), home.resolve(".cache"))
            .resolve(APPLICATION_DIRECTORY));
  }

  private static Path basePath(String configuredPath, Path fallback) {
    if (configuredPath == null || configuredPath.isBlank()) {
      return fallback;
    }
    Path path = Path.of(configuredPath);
    return path.isAbsolute() ? path : fallback;
  }

  private static Path absolute(Path path, String name) {
    Objects.requireNonNull(path, name);
    if (!path.isAbsolute()) {
      throw new IllegalArgumentException(name + " must be absolute");
    }
    return path.normalize();
  }
}
