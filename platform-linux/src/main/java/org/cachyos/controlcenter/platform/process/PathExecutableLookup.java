package org.cachyos.controlcenter.platform.process;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/** Safe PATH lookup that rejects separators and other command syntax. */
public final class PathExecutableLookup implements ExecutableLookup {
  private static final Pattern NAME = Pattern.compile("[A-Za-z0-9._+-]+");
  private final List<Path> searchDirectories;
  private final boolean windows;

  public PathExecutableLookup(Map<String, String> environment, boolean windows) {
    this.windows = windows;
    String pathValue = environment.getOrDefault("PATH", "");
    searchDirectories =
        Pattern.compile(Pattern.quote(java.io.File.pathSeparator))
            .splitAsStream(pathValue)
            .filter(value -> !value.isBlank())
            .map(Path::of)
            .filter(Path::isAbsolute)
            .map(Path::normalize)
            .distinct()
            .toList();
  }

  @Override
  public Optional<Path> find(String executableName) {
    if (executableName == null || !NAME.matcher(executableName).matches()) {
      return Optional.empty();
    }
    for (Path directory : searchDirectories) {
      Optional<Path> direct = existing(directory.resolve(executableName));
      if (direct.isPresent()) {
        return direct;
      }
      if (windows && !executableName.toLowerCase(Locale.ROOT).endsWith(".exe")) {
        Optional<Path> executable = existing(directory.resolve(executableName + ".exe"));
        if (executable.isPresent()) {
          return executable;
        }
      }
    }
    return Optional.empty();
  }

  private static Optional<Path> existing(Path candidate) {
    return Files.isRegularFile(candidate) && Files.isExecutable(candidate)
        ? Optional.of(candidate.toAbsolutePath().normalize())
        : Optional.empty();
  }
}
