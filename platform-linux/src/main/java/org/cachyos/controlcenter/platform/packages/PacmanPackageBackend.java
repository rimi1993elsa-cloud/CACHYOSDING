package org.cachyos.controlcenter.platform.packages;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.cachyos.controlcenter.modules.packages.PackageAction;
import org.cachyos.controlcenter.modules.packages.PackageBackend;
import org.cachyos.controlcenter.modules.packages.PackageDetails;
import org.cachyos.controlcenter.modules.packages.PackageEntry;
import org.cachyos.controlcenter.modules.packages.PackageNames;
import org.cachyos.controlcenter.modules.packages.PackageSnapshot;
import org.cachyos.controlcenter.systeminfo.Capability;
import org.cachyos.controlcenter.systeminfo.CapabilityRegistry;

public final class PacmanPackageBackend implements PackageBackend {
  private static final Path PACMAN = Path.of("/usr/bin/pacman");
  private static final Path LOCK = Path.of("/var/lib/pacman/db.lck");
  private static final Path CACHE = Path.of("/var/cache/pacman/pkg");
  private static final Duration TIMEOUT = Duration.ofSeconds(20);

  private final boolean available;
  private final PacmanCommandRunner runner;
  private final Path lockPath;
  private final Path cachePath;

  public PacmanPackageBackend(CapabilityRegistry capabilities) {
    this(
        capabilities.status(Capability.PACMAN).available(), new PacmanCommandRunner(), LOCK, CACHE);
  }

  PacmanPackageBackend(
      boolean available, PacmanCommandRunner runner, Path lockPath, Path cachePath) {
    this.available = available;
    this.runner = runner;
    this.lockPath = lockPath;
    this.cachePath = cachePath;
  }

  @Override
  public boolean available() {
    return available;
  }

  @Override
  public boolean locked() {
    return Files.exists(lockPath, LinkOption.NOFOLLOW_LINKS);
  }

  @Override
  public PackageSnapshot snapshot() {
    if (!available) {
      return PackageSnapshot.unavailable("Pacman ist auf diesem System nicht verfügbar");
    }
    List<PackageEntry> installed = parseQuery(run(List.of("-Q")).lines(), true);
    List<PackageEntry> updates = parseQuery(run(List.of("-Qu")).lines(), true);
    List<String> orphans =
        run(List.of("-Qdtq")).lines().stream().filter(PackageNames::valid).sorted().toList();
    return new PackageSnapshot(
        true,
        locked(),
        installed,
        updates,
        orphans,
        cacheBytes(),
        Instant.now(),
        locked() ? "Eine andere Pacman-Transaktion ist aktiv" : "Pacman bereit");
  }

  @Override
  public List<PackageEntry> search(String query) {
    requireQuery(query);
    PacmanCommandRunner.CommandOutput output = run(List.of("-Ss", "--", query));
    return output.exitCode() == 0 ? parseSearch(output.lines()) : List.of();
  }

  @Override
  public Optional<PackageDetails> details(String packageName) {
    requirePackage(packageName);
    PacmanCommandRunner.CommandOutput installed = run(List.of("-Qi", "--", packageName));
    List<String> lines =
        installed.exitCode() == 0
            ? installed.lines()
            : run(List.of("-Si", "--", packageName)).lines();
    if (lines.isEmpty()) {
      return Optional.empty();
    }
    String name = field(lines, "Name");
    String version = field(lines, "Version");
    String description = field(lines, "Description");
    String architecture = field(lines, "Architecture");
    String repository = field(lines, "Repository");
    String dependencies = field(lines, "Depends On");
    return Optional.of(
        new PackageDetails(
            new PackageEntry(
                name,
                version,
                repository.isBlank() ? "local" : repository,
                description,
                installed.exitCode() == 0),
            architecture,
            parseSize(field(lines, "Installed Size")),
            dependencies.equals("None") || dependencies.isBlank()
                ? List.of()
                : List.of(dependencies.split("\\s+"))));
  }

  @Override
  public List<String> preview(PackageAction action, String packageName) {
    requirePackage(packageName);
    List<String> arguments = new ArrayList<>();
    arguments.add(action == PackageAction.INSTALL ? "-Sp" : "-Rp");
    arguments.add("--print-format");
    arguments.add("%n\t%v\t%s");
    arguments.add("--");
    arguments.add(packageName);
    PacmanCommandRunner.CommandOutput output = run(arguments);
    return output.exitCode() == 0
        ? output.lines().stream().filter(line -> !line.isBlank()).limit(500).toList()
        : List.of();
  }

  private PacmanCommandRunner.CommandOutput run(List<String> arguments) {
    return runner.run(PACMAN, arguments, TIMEOUT);
  }

  private long cacheBytes() {
    if (!Files.isDirectory(cachePath, LinkOption.NOFOLLOW_LINKS)
        || Files.isSymbolicLink(cachePath)) {
      return 0;
    }
    try (var files = Files.list(cachePath)) {
      return files
          .filter(path -> Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS))
          .limit(20_000)
          .mapToLong(PacmanPackageBackend::size)
          .sum();
    } catch (IOException exception) {
      return 0;
    }
  }

  static List<PackageEntry> parseQuery(List<String> lines, boolean installed) {
    return lines.stream()
        .map(String::trim)
        .filter(line -> !line.isBlank())
        .map(
            line -> {
              String[] parts = line.split("\\s+", 3);
              return new PackageEntry(
                  parts[0], parts.length > 1 ? parts[1] : "", "local", "", installed);
            })
        .toList();
  }

  static List<PackageEntry> parseSearch(List<String> lines) {
    List<PackageEntry> result = new ArrayList<>();
    for (int index = 0; index < lines.size(); index++) {
      String header = lines.get(index).trim();
      if (header.isBlank() || Character.isWhitespace(lines.get(index).charAt(0))) {
        continue;
      }
      String[] headerParts = header.split("\\s+", 3);
      String[] identity = headerParts[0].split("/", 2);
      if (identity.length != 2 || !PackageNames.valid(identity[1])) {
        continue;
      }
      String description =
          index + 1 < lines.size() && !lines.get(index + 1).isBlank()
              ? lines.get(index + 1).trim()
              : "";
      result.add(
          new PackageEntry(
              identity[1],
              headerParts.length > 1 ? headerParts[1] : "",
              identity[0],
              description,
              header.contains("[installed")));
    }
    return List.copyOf(result);
  }

  private static String field(List<String> lines, String name) {
    String prefix = name + " ";
    return lines.stream()
        .filter(line -> line.startsWith(prefix) && line.contains(":"))
        .map(line -> line.substring(line.indexOf(':') + 1).trim())
        .findFirst()
        .orElse("");
  }

  private static long parseSize(String value) {
    String[] parts = value.replace(',', '.').split("\\s+");
    if (parts.length < 2) {
      return 0;
    }
    try {
      double amount = Double.parseDouble(parts[0]);
      double multiplier =
          switch (parts[1].toLowerCase(Locale.ROOT)) {
            case "kib" -> 1024;
            case "mib" -> 1024 * 1024;
            case "gib" -> 1024 * 1024 * 1024;
            default -> 1;
          };
      return (long) (amount * multiplier);
    } catch (NumberFormatException exception) {
      return 0;
    }
  }

  private static long size(Path path) {
    try {
      return Files.size(path);
    } catch (IOException exception) {
      return 0;
    }
  }

  private static void requirePackage(String packageName) {
    if (!PackageNames.valid(packageName)) {
      throw new IllegalArgumentException("Ungültiger Paketname");
    }
  }

  private static void requireQuery(String query) {
    if (!PackageNames.validQuery(query)) {
      throw new IllegalArgumentException("Ungültige Paketsuche");
    }
  }
}
