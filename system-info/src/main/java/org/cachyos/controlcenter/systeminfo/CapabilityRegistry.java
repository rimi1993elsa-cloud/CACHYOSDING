package org.cachyos.controlcenter.systeminfo;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/** Immutable snapshot of optional tool availability. */
public final class CapabilityRegistry {
  private final Map<Capability, CapabilityStatus> statuses;

  private CapabilityRegistry(Map<Capability, CapabilityStatus> statuses) {
    this.statuses = Map.copyOf(statuses);
  }

  public static CapabilityRegistry detect(
      Map<String, String> environment, OperatingSystemFamily family) {
    List<Path> directories =
        Pattern.compile(Pattern.quote(File.pathSeparator))
            .splitAsStream(environment.getOrDefault("PATH", ""))
            .filter(value -> !value.isBlank())
            .map(Path::of)
            .filter(Path::isAbsolute)
            .map(Path::normalize)
            .distinct()
            .toList();
    boolean windows = family == OperatingSystemFamily.WINDOWS;
    EnumMap<Capability, CapabilityStatus> detected = new EnumMap<>(Capability.class);
    for (Capability capability : Capability.values()) {
      Optional<Path> executable =
          capability.executableNames().stream()
              .map(name -> findExecutable(directories, name, windows))
              .flatMap(Optional::stream)
              .findFirst();
      String reason =
          executable.isPresent()
              ? "Verfügbar"
              : "Nicht gefunden. Optionales Paket: " + capability.installHint();
      detected.put(
          capability, new CapabilityStatus(capability, executable.isPresent(), executable, reason));
    }
    return new CapabilityRegistry(detected);
  }

  public CapabilityStatus status(Capability capability) {
    return statuses.get(capability);
  }

  public boolean available(Capability capability) {
    return status(capability).available();
  }

  public Map<Capability, CapabilityStatus> statuses() {
    return statuses;
  }

  private static Optional<Path> findExecutable(
      List<Path> directories, String name, boolean windows) {
    for (Path directory : directories) {
      Optional<Path> direct = existing(directory.resolve(name));
      if (direct.isPresent()) {
        return direct;
      }
      if (windows && !name.toLowerCase(Locale.ROOT).endsWith(".exe")) {
        Optional<Path> executable = existing(directory.resolve(name + ".exe"));
        if (executable.isPresent()) {
          return executable;
        }
      }
    }
    return Optional.empty();
  }

  private static Optional<Path> existing(Path path) {
    return Files.isRegularFile(path) && Files.isExecutable(path)
        ? Optional.of(path.toAbsolutePath().normalize())
        : Optional.empty();
  }
}
