package org.cachyos.controlcenter.platform.applications;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.cachyos.controlcenter.modules.applications.ApplicationBackend;
import org.cachyos.controlcenter.modules.applications.ApplicationEntry;
import org.cachyos.controlcenter.modules.applications.ApplicationOperationResult;
import org.cachyos.controlcenter.platform.process.CommandSpec;
import org.cachyos.controlcenter.platform.process.ExecutableLookup;
import org.cachyos.controlcenter.platform.process.JvmProcessStarter;
import org.cachyos.controlcenter.platform.process.PathExecutableLookup;
import org.cachyos.controlcenter.platform.status.FixedCommandReader;
import org.cachyos.controlcenter.systeminfo.Capability;
import org.cachyos.controlcenter.systeminfo.CapabilityRegistry;

/** XDG desktop catalog that launches only previously parsed, shell-free entries. */
public final class DesktopApplicationBackend implements ApplicationBackend {
  private static final long MAX_DESKTOP_FILE_BYTES = 1024 * 1024;
  private static final int MAX_APPLICATIONS = 5_000;
  private static final Duration TIMEOUT = Duration.ofSeconds(8);
  private final List<Path> applicationDirectories;
  private final ExecutableLookup executableLookup;
  private final JvmProcessStarter processStarter = new JvmProcessStarter();
  private final Optional<Path> pacman;
  private final Map<String, CommandSpec> commands = new HashMap<>();
  private final Map<String, Path> desktopFiles = new HashMap<>();

  public DesktopApplicationBackend(CapabilityRegistry capabilities) {
    this(System.getenv(), capabilities);
  }

  DesktopApplicationBackend(Map<String, String> environment, CapabilityRegistry capabilities) {
    executableLookup = new PathExecutableLookup(environment, false);
    pacman = capabilities.status(Capability.PACMAN).executable();
    applicationDirectories = applicationDirectories(environment);
  }

  @Override
  public synchronized List<ApplicationEntry> loadApplications() {
    LinkedHashMap<String, ApplicationEntry> entries = new LinkedHashMap<>();
    commands.clear();
    desktopFiles.clear();
    for (Path directory : applicationDirectories) {
      if (!Files.isDirectory(directory)) {
        continue;
      }
      try (var files = Files.list(directory)) {
        for (Path file :
            files
                .filter(path -> path.getFileName().toString().endsWith(".desktop"))
                .filter(path -> Files.isRegularFile(path) && !Files.isSymbolicLink(path))
                .limit(MAX_APPLICATIONS)
                .toList()) {
          parse(file).ifPresent(entry -> entries.putIfAbsent(entry.id(), entry));
          if (entries.size() >= MAX_APPLICATIONS) {
            break;
          }
        }
      } catch (IOException ignored) {
        // Other XDG application directories remain usable.
      }
    }
    return entries.values().stream()
        .sorted(Comparator.comparing(ApplicationEntry::name, String.CASE_INSENSITIVE_ORDER))
        .toList();
  }

  @Override
  public synchronized ApplicationOperationResult launch(String applicationId) {
    CommandSpec command = commands.get(applicationId);
    if (command == null) {
      loadApplications();
      command = commands.get(applicationId);
    }
    if (command == null) {
      return ApplicationOperationResult.unavailable(
          "Die Anwendung ist nicht mehr installiert oder nicht sicher startbar.");
    }
    try {
      processStarter.start(command);
      return ApplicationOperationResult.success("Anwendung wurde gestartet.");
    } catch (IOException exception) {
      return ApplicationOperationResult.failed("Die Anwendung konnte nicht gestartet werden.");
    }
  }

  @Override
  public synchronized Optional<String> findPackage(String applicationId) {
    Path desktopFile = desktopFiles.get(applicationId);
    if (desktopFile == null || pacman.isEmpty()) {
      return Optional.empty();
    }
    return FixedCommandReader.read(pacman.get(), List.of("-Qoq", desktopFile.toString()), TIMEOUT)
        .stream()
        .flatMap(List::stream)
        .map(String::trim)
        .filter(value -> value.matches("[A-Za-z0-9@._+:-]{1,255}"))
        .findFirst();
  }

  private Optional<ApplicationEntry> parse(Path file) {
    try {
      if (Files.size(file) > MAX_DESKTOP_FILE_BYTES) {
        return Optional.empty();
      }
      Map<String, String> values = desktopValues(Files.readAllLines(file));
      if (!"Application".equals(values.get("Type"))
          || "true".equalsIgnoreCase(values.get("Hidden"))
          || "true".equalsIgnoreCase(values.get("NoDisplay"))
          || "true".equalsIgnoreCase(values.get("Terminal"))) {
        return Optional.empty();
      }
      Optional<CommandSpec> command = DesktopExecParser.parse(values.get("Exec"), executableLookup);
      if (command.isEmpty()) {
        return Optional.empty();
      }
      String id = stableId(file);
      Path normalized = file.toAbsolutePath().normalize();
      commands.put(id, command.get());
      desktopFiles.put(id, normalized);
      String name = values.getOrDefault("Name[de]", values.get("Name"));
      String comment = values.getOrDefault("Comment[de]", values.getOrDefault("Comment", ""));
      return Optional.of(
          new ApplicationEntry(
              id,
              name,
              comment,
              resolveIcon(values.get("Icon")),
              normalized,
              Optional.empty(),
              false));
    } catch (IOException | RuntimeException exception) {
      return Optional.empty();
    }
  }

  static Map<String, String> desktopValues(List<String> lines) {
    Map<String, String> values = new HashMap<>();
    boolean desktopEntry = false;
    for (String line : lines) {
      if (line.startsWith("[") && line.endsWith("]")) {
        desktopEntry = "[Desktop Entry]".equals(line);
        continue;
      }
      if (!desktopEntry || line.startsWith("#")) {
        continue;
      }
      int separator = line.indexOf('=');
      if (separator > 0) {
        values.putIfAbsent(line.substring(0, separator), line.substring(separator + 1));
      }
    }
    return Map.copyOf(values);
  }

  private static List<Path> applicationDirectories(Map<String, String> environment) {
    List<Path> directories = new ArrayList<>();
    Path dataHome =
        Path.of(
            environment.getOrDefault(
                "XDG_DATA_HOME",
                Path.of(System.getProperty("user.home"), ".local", "share").toString()));
    if (dataHome.isAbsolute()) {
      directories.add(dataHome.resolve("applications").normalize());
    }
    String dataDirs = environment.getOrDefault("XDG_DATA_DIRS", "/usr/local/share:/usr/share");
    for (String value : dataDirs.split(":")) {
      if (!value.isBlank()) {
        Path directory = Path.of(value);
        if (directory.isAbsolute()) {
          directories.add(directory.resolve("applications").normalize());
        }
      }
    }
    return List.copyOf(directories);
  }

  private static Optional<Path> resolveIcon(String icon) {
    if (icon == null || icon.isBlank()) {
      return Optional.empty();
    }
    Path supplied = Path.of(icon);
    if (supplied.isAbsolute()) {
      return Files.isRegularFile(supplied) ? Optional.of(supplied.normalize()) : Optional.empty();
    }
    List<Path> candidates =
        List.of(
            Path.of("/usr/share/pixmaps", icon + ".png"),
            Path.of("/usr/share/icons/hicolor/64x64/apps", icon + ".png"),
            Path.of("/usr/share/icons/hicolor/128x128/apps", icon + ".png"),
            Path.of("/usr/share/icons/hicolor/256x256/apps", icon + ".png"));
    return candidates.stream().filter(Files::isRegularFile).findFirst();
  }

  private static String stableId(Path file) {
    try {
      byte[] digest =
          MessageDigest.getInstance("SHA-256")
              .digest(
                  file.toAbsolutePath().normalize().toString().getBytes(StandardCharsets.UTF_8));
      StringBuilder value = new StringBuilder(16);
      for (int index = 0; index < 8; index++) {
        value.append(String.format(java.util.Locale.ROOT, "%02x", digest[index]));
      }
      return value.toString();
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 unavailable", exception);
    }
  }
}
