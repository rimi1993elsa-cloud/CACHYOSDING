package org.cachyos.controlcenter.platform.security;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.cachyos.controlcenter.modules.security.ListeningPort;
import org.cachyos.controlcenter.modules.security.SecurityBackend;
import org.cachyos.controlcenter.modules.security.SecurityCheck;
import org.cachyos.controlcenter.modules.security.SecuritySnapshot;
import org.cachyos.controlcenter.modules.security.SecurityStatus;

public final class LinuxSecurityBackend implements SecurityBackend {
  private static final Duration TIMEOUT = Duration.ofSeconds(12);
  private final SecurityCommandReader reader;
  private final boolean linux;

  public LinuxSecurityBackend() {
    this(System.getProperty("os.name", "").toLowerCase(java.util.Locale.ROOT).contains("linux"));
  }

  public LinuxSecurityBackend(boolean linux) {
    this(linux, new SecurityCommandReader());
  }

  LinuxSecurityBackend(boolean linux, SecurityCommandReader reader) {
    this.linux = linux;
    this.reader = reader;
  }

  @Override
  public SecuritySnapshot inspect() {
    if (!linux) {
      return new SecuritySnapshot(
          false,
          false,
          List.of(),
          List.of(),
          Instant.now(),
          "Linux-Sicherheitsstatus ist auf dieser Plattform nicht verfügbar");
    }
    List<SecurityCheck> checks = new ArrayList<>();
    String firewallState = systemdState("firewalld.service");
    boolean firewallEnabled = firewallState.equals("active");
    checks.add(
        check(
            "firewall",
            "Firewall",
            firewallEnabled ? SecurityStatus.GOOD : SecurityStatus.WARNING,
            firewallState.isBlank() ? "Status nicht ermittelbar" : "firewalld: " + firewallState,
            firewallEnabled ? "" : "Firewall prüfen und bei Bedarf aktivieren."));

    List<ListeningPort> ports =
        reader
            .read(Path.of("/usr/bin/ss"), List.of("-H", "-lntu"), TIMEOUT)
            .map(LinuxSecurityBackend::parsePorts)
            .orElse(List.of());
    checks.add(
        check(
            "ports",
            "Lauschende Ports",
            ports.isEmpty() ? SecurityStatus.UNKNOWN : SecurityStatus.GOOD,
            ports.isEmpty() ? "Keine Daten oder keine Ports" : ports.size() + " lokale Listener",
            "Nicht benötigte Netzwerkdienste deaktivieren."));

    String sshState = systemdState("sshd.service");
    checks.add(
        check(
            "ssh",
            "SSH",
            sshState.equals("active") ? SecurityStatus.WARNING : SecurityStatus.GOOD,
            sshState.isBlank() ? "Status nicht ermittelbar" : "sshd: " + sshState,
            sshState.equals("active") ? "SSH-Konfiguration und Schlüsselanmeldung prüfen." : ""));

    Optional<List<String>> failures =
        reader.read(
            Path.of("/usr/bin/journalctl"),
            List.of(
                "-b",
                "-u",
                "sshd.service",
                "--no-pager",
                "--grep",
                "Failed password|authentication failure"),
            TIMEOUT);
    int failureCount = failures.map(List::size).orElse(0);
    checks.add(
        check(
            "login-failures",
            "Fehlgeschlagene Logins",
            failures.isEmpty()
                ? SecurityStatus.UNKNOWN
                : failureCount > 0 ? SecurityStatus.WARNING : SecurityStatus.GOOD,
            failures.isEmpty() ? "Journal nicht lesbar" : failureCount + " Einträge seit Boot",
            failureCount > 0 ? "Ursprung im Systemjournal prüfen." : ""));

    Optional<List<String>> updates =
        reader.read(Path.of("/usr/bin/pacman"), List.of("-Qu"), TIMEOUT, Set.of(0, 1));
    checks.add(
        check(
            "updates",
            "Sicherheitsrelevante Updates",
            updates.isEmpty()
                ? SecurityStatus.UNKNOWN
                : updates.orElseThrow().isEmpty() ? SecurityStatus.GOOD : SecurityStatus.WARNING,
            updates.isEmpty()
                ? "Lokaler Update-Stand nicht ermittelbar"
                : updates.orElseThrow().size() + " Updates im lokalen Datenbankstand",
            "CachyOS vollständig aktualisieren; Arch-Pakete haben keinen separaten Security-Kanal."));

    boolean efi = Files.isDirectory(Path.of("/sys/firmware/efi"), LinkOption.NOFOLLOW_LINKS);
    Optional<List<String>> secureBoot =
        reader.read(Path.of("/usr/bin/bootctl"), List.of("status"), TIMEOUT);
    boolean secureBootEnabled =
        secureBoot.orElse(List.of()).stream()
            .anyMatch(
                line -> line.contains("Secure Boot: enabled") || line.contains("Secure Boot: yes"));
    checks.add(
        check(
            "secure-boot",
            "Secure Boot",
            !efi
                ? SecurityStatus.UNKNOWN
                : secureBootEnabled ? SecurityStatus.GOOD : SecurityStatus.WARNING,
            !efi
                ? "System nicht im UEFI-Modus oder Status unbekannt"
                : "Aktiv: " + secureBootEnabled,
            secureBootEnabled ? "" : "Firmware- und Bootloader-Unterstützung prüfen."));

    Optional<List<String>> appArmor =
        reader.read(Path.of("/usr/bin/aa-status"), List.of("--json"), TIMEOUT);
    boolean appArmorLoaded =
        appArmor.orElse(List.of()).stream().anyMatch(line -> line.contains("\"profiles\""));
    checks.add(
        check(
            "apparmor",
            "AppArmor",
            appArmorLoaded ? SecurityStatus.GOOD : SecurityStatus.UNKNOWN,
            appArmorLoaded
                ? "Profile werden vom Kernel gemeldet"
                : "Keine geladenen Profile belegt",
            "AppArmor nur als aktiv betrachten, wenn Status und Profile geladen sind."));

    checks.add(permissionCheck());
    return new SecuritySnapshot(
        true, firewallEnabled, checks, ports, Instant.now(), "Lokale Einzelbefunde ohne Score");
  }

  static List<ListeningPort> parsePorts(List<String> lines) {
    List<ListeningPort> result = new ArrayList<>();
    for (String line : lines) {
      String[] fields = line.trim().split("\\s+");
      if (fields.length < 5) {
        continue;
      }
      String endpoint = fields[4];
      int separator = endpoint.lastIndexOf(':');
      if (separator < 0 || separator == endpoint.length() - 1) {
        continue;
      }
      try {
        int port = Integer.parseInt(endpoint.substring(separator + 1));
        if (port > 0 && port <= 65_535) {
          result.add(
              new ListeningPort(
                  fields[0],
                  endpoint.substring(0, separator),
                  port,
                  fields.length > 5 ? fields[5] : ""));
        }
      } catch (NumberFormatException ignored) {
        // Kernel output may contain service names; those rows stay unreported.
      }
    }
    return List.copyOf(result);
  }

  private String systemdState(String unit) {
    return reader
        .read(
            Path.of("/usr/bin/systemctl"),
            List.of("show", "--property=ActiveState", "--value", "--", unit),
            TIMEOUT)
        .flatMap(lines -> lines.stream().findFirst())
        .orElse("");
  }

  private static SecurityCheck permissionCheck() {
    List<Path> critical =
        List.of(Path.of("/etc/passwd"), Path.of("/etc/shadow"), Path.of("/etc/sudoers"));
    List<String> unsafe = new ArrayList<>();
    for (Path path : critical) {
      if (!Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS) || Files.isSymbolicLink(path)) {
        unsafe.add(path + " fehlt oder ist ein Symlink");
        continue;
      }
      try {
        Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(path);
        if (permissions.contains(PosixFilePermission.OTHERS_WRITE)
            || permissions.contains(PosixFilePermission.GROUP_WRITE)) {
          unsafe.add(path + " ist gruppen-/weltbeschreibbar");
        }
      } catch (UnsupportedOperationException | IOException exception) {
        unsafe.add(path + " nicht prüfbar");
      }
    }
    return check(
        "permissions",
        "Kritische Dateirechte",
        unsafe.isEmpty() ? SecurityStatus.GOOD : SecurityStatus.CRITICAL,
        unsafe.isEmpty() ? "Keine unsicheren Schreibrechte erkannt" : String.join("; ", unsafe),
        unsafe.isEmpty() ? "" : "Dateirechte mit Paketstandard vergleichen und korrigieren.");
  }

  private static SecurityCheck check(
      String id, String title, SecurityStatus status, String evidence, String recommendation) {
    return new SecurityCheck(id, title, status, evidence, recommendation);
  }
}
