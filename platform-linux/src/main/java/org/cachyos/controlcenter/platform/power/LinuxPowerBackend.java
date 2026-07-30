package org.cachyos.controlcenter.platform.power;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.cachyos.controlcenter.modules.power.PowerBackend;
import org.cachyos.controlcenter.modules.power.PowerProfile;
import org.cachyos.controlcenter.modules.power.PowerResult;
import org.cachyos.controlcenter.modules.power.PowerState;
import org.cachyos.controlcenter.platform.status.FixedCommandReader;

/** Dynamic Linux power adapter using power-profiles-daemon and systemd/logind policy. */
public final class LinuxPowerBackend implements PowerBackend {
  private static final Duration TIMEOUT = Duration.ofSeconds(8);
  private static final Pattern PROFILE = Pattern.compile("^\\s*\\*?\\s*([a-z][a-z0-9-]+):?\\s*$");
  private final boolean linux;

  public LinuxPowerBackend(boolean linux) {
    this.linux = linux;
  }

  @Override
  public PowerState inspect() {
    if (!linux) {
      return new PowerState(false, false, 0, "", List.of(), false, false, "Nur unter Linux.");
    }
    Battery battery = battery();
    List<String> profileLines =
        FixedCommandReader.read(Path.of("/usr/bin/powerprofilesctl"), List.of("list"), TIMEOUT)
            .orElse(List.of());
    List<PowerProfile> profiles = parseProfiles(profileLines);
    String sleepStates = read(Path.of("/sys/power/state"));
    boolean canSuspend = sleepStates.contains("mem");
    boolean canHibernate =
        sleepStates.contains("disk") && !read(Path.of("/sys/power/disk")).isBlank();
    return new PowerState(
        true,
        battery.present(),
        battery.percent(),
        battery.status(),
        profiles,
        canSuspend,
        canHibernate,
        "Fähigkeiten aus Kernel und verfügbaren Diensten erkannt.");
  }

  @Override
  public PowerResult setProfile(String profile) {
    return run(
        Path.of("/usr/bin/powerprofilesctl"),
        List.of("set", profile),
        "Energieprofil „" + profile + "“ aktiviert.");
  }

  @Override
  public PowerResult suspend() {
    return run(Path.of("/usr/bin/systemctl"), List.of("suspend"), "Suspend eingeleitet.");
  }

  @Override
  public PowerResult hibernate() {
    return run(Path.of("/usr/bin/systemctl"), List.of("hibernate"), "Hibernate eingeleitet.");
  }

  List<PowerProfile> parseProfiles(List<String> lines) {
    List<PowerProfile> result = new ArrayList<>();
    for (String line : lines) {
      Matcher matcher = PROFILE.matcher(line);
      if (matcher.matches()) {
        result.add(new PowerProfile(matcher.group(1), line.stripLeading().startsWith("*")));
      }
    }
    return List.copyOf(result);
  }

  private Battery battery() {
    Path root = Path.of("/sys/class/power_supply");
    if (!Files.isDirectory(root)) {
      return new Battery(false, 0, "Nicht vorhanden");
    }
    try (DirectoryStream<Path> entries = Files.newDirectoryStream(root, "BAT*")) {
      for (Path entry : entries) {
        int percent = parsePercent(read(entry.resolve("capacity")));
        return new Battery(true, percent, read(entry.resolve("status")));
      }
    } catch (IOException | SecurityException ignored) {
      // No readable battery is a normal desktop capability state.
    }
    return new Battery(false, 0, "Nicht vorhanden");
  }

  private static int parsePercent(String value) {
    try {
      return Math.clamp(Integer.parseInt(value.trim()), 0, 100);
    } catch (NumberFormatException exception) {
      return 0;
    }
  }

  private static String read(Path path) {
    try {
      return Files.readString(path).trim();
    } catch (IOException | SecurityException exception) {
      return "";
    }
  }

  private static PowerResult run(Path executable, List<String> arguments, String success) {
    if (!Files.isExecutable(executable)) {
      return new PowerResult(false, "Benötigtes Werkzeug ist nicht verfügbar.");
    }
    try {
      List<String> command = new ArrayList<>();
      command.add(executable.toString());
      command.addAll(arguments);
      Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
      if (!process.waitFor(TIMEOUT.toSeconds(), TimeUnit.SECONDS)) {
        process.destroyForcibly();
        return new PowerResult(false, "Energieaktion hat das Zeitlimit überschritten.");
      }
      return process.exitValue() == 0
          ? new PowerResult(true, success)
          : new PowerResult(false, "Energieaktion wurde von systemd/logind abgelehnt.");
    } catch (IOException | InterruptedException exception) {
      Thread.currentThread().interrupt();
      return new PowerResult(false, "Energieaktion konnte nicht ausgeführt werden.");
    }
  }

  private record Battery(boolean present, int percent, String status) {}
}
