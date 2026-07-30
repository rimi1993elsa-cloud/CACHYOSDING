package org.cachyos.controlcenter.platform.display;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.cachyos.controlcenter.modules.display.DisplayBackend;
import org.cachyos.controlcenter.modules.display.DisplayResult;
import org.cachyos.controlcenter.modules.display.DisplayState;
import org.cachyos.controlcenter.modules.display.GraphicsInfo;
import org.cachyos.controlcenter.modules.display.MonitorInfo;
import org.cachyos.controlcenter.platform.status.FixedCommandReader;

/** KDE/Wayland-first display adapter. X11 tools are deliberately not used. */
public final class LinuxDisplayBackend implements DisplayBackend {
  private static final Duration TIMEOUT = Duration.ofSeconds(8);
  private final boolean linux;
  private final ObjectMapper mapper = new ObjectMapper();

  public LinuxDisplayBackend(boolean linux) {
    this.linux = linux;
  }

  @Override
  public DisplayState inspect() {
    if (!linux) {
      return unavailable("Linux-Anzeigeerkennung nicht verfügbar.");
    }
    boolean wayland = "wayland".equalsIgnoreCase(System.getenv("XDG_SESSION_TYPE"));
    List<MonitorInfo> monitors = readKscreen();
    Optional<Path> backlight = backlightDevice();
    int brightness = backlight.map(LinuxDisplayBackend::readBrightness).orElse(0);
    GraphicsInfo graphics = readGraphics();
    boolean nightTool = Files.isExecutable(Path.of("/usr/bin/qdbus6"));
    boolean nightMode = nightTool && readNightMode();
    return new DisplayState(
        true,
        wayland,
        monitors,
        brightness,
        backlight.isPresent() && Files.isExecutable(Path.of("/usr/bin/brightnessctl")),
        nightMode,
        nightTool,
        graphics,
        wayland
            ? "KDE/Wayland-Fähigkeiten dynamisch erkannt."
            : "Keine Wayland-Sitzung; Funktionen können eingeschränkt sein.");
  }

  @Override
  public DisplayResult setBrightness(int percent) {
    if (!Files.isExecutable(Path.of("/usr/bin/brightnessctl")) || backlightDevice().isEmpty()) {
      return new DisplayResult(false, "Keine steuerbare Hintergrundbeleuchtung erkannt.");
    }
    return run(
        Path.of("/usr/bin/brightnessctl"),
        List.of("set", "--", Integer.toString(percent) + "%"),
        "Helligkeit auf " + percent + " % gesetzt.");
  }

  @Override
  public DisplayResult setNightMode(boolean enabled) {
    Path qdbus = Path.of("/usr/bin/qdbus6");
    if (!Files.isExecutable(qdbus)) {
      return new DisplayResult(false, "KDE-Nachtmodus-Schnittstelle ist nicht verfügbar.");
    }
    return run(
        qdbus,
        List.of(
            "org.kde.KWin",
            "/org/kde/KWin/NightLight",
            "org.kde.KWin.NightLight.setEnabled",
            Boolean.toString(enabled)),
        enabled ? "Nachtmodus aktiviert." : "Nachtmodus deaktiviert.");
  }

  List<MonitorInfo> parseKscreen(String json) {
    try {
      List<MonitorInfo> result = new ArrayList<>();
      JsonNode root = mapper.readTree(json);
      for (JsonNode output : root.path("outputs")) {
        JsonNode currentMode = output.path("currentMode");
        String mode =
            currentMode.path("size").path("width").asInt()
                + "×"
                + currentMode.path("size").path("height").asInt()
                + " @ "
                + currentMode.path("refreshRate").asDouble()
                + " Hz";
        result.add(
            new MonitorInfo(
                output.path("name").asText("Unbekannt"),
                output.path("enabled").asBoolean(),
                output.path("priority").asInt() == 1,
                mode,
                output.path("scale").asDouble(1.0)));
      }
      return List.copyOf(result);
    } catch (IOException exception) {
      return List.of();
    }
  }

  private List<MonitorInfo> readKscreen() {
    return FixedCommandReader.read(Path.of("/usr/bin/kscreen-doctor"), List.of("-j"), TIMEOUT)
        .map(lines -> parseKscreen(String.join("\n", lines)))
        .orElse(List.of());
  }

  private GraphicsInfo readGraphics() {
    String gpu =
        FixedCommandReader.read(Path.of("/usr/bin/lspci"), List.of("-D"), TIMEOUT)
            .flatMap(
                lines ->
                    lines.stream()
                        .filter(
                            line ->
                                line.contains("VGA compatible controller")
                                    || line.contains("3D controller"))
                        .findFirst())
            .orElse("Nicht erkannt");
    String driver = readFirst(Path.of("/usr/bin/lspci"), List.of("-k"), "Kernel driver in use:");
    String vulkan =
        FixedCommandReader.read(Path.of("/usr/bin/vulkaninfo"), List.of("--summary"), TIMEOUT)
            .map(lines -> firstContaining(lines, "driverName"))
            .orElse("vulkaninfo nicht verfügbar");
    String openGl =
        FixedCommandReader.read(Path.of("/usr/bin/eglinfo"), List.of("-B"), TIMEOUT)
            .map(lines -> firstContaining(lines, "OpenGL core profile version"))
            .orElse("eglinfo nicht verfügbar");
    return new GraphicsInfo(gpu, driver, vulkan, openGl);
  }

  private boolean readNightMode() {
    return FixedCommandReader.read(
            Path.of("/usr/bin/qdbus6"),
            List.of("org.kde.KWin", "/org/kde/KWin/NightLight", "org.kde.KWin.NightLight.running"),
            TIMEOUT)
        .flatMap(lines -> lines.stream().findFirst())
        .map(Boolean::parseBoolean)
        .orElse(false);
  }

  private Optional<Path> backlightDevice() {
    Path root = Path.of("/sys/class/backlight");
    if (!Files.isDirectory(root)) {
      return Optional.empty();
    }
    try (DirectoryStream<Path> devices = Files.newDirectoryStream(root)) {
      for (Path device : devices) {
        if (!Files.isSymbolicLink(device.resolve("brightness"))
            && Files.isRegularFile(device.resolve("brightness"))
            && Files.isRegularFile(device.resolve("max_brightness"))) {
          return Optional.of(device);
        }
      }
    } catch (IOException | SecurityException ignored) {
      // Missing kernel capability is represented as unavailable.
    }
    return Optional.empty();
  }

  private static int readBrightness(Path device) {
    try {
      long current = Long.parseLong(Files.readString(device.resolve("brightness")).trim());
      long maximum = Long.parseLong(Files.readString(device.resolve("max_brightness")).trim());
      return maximum <= 0 ? 0 : Math.clamp(Math.round(current * 100.0 / maximum), 0, 100);
    } catch (IOException | NumberFormatException exception) {
      return 0;
    }
  }

  private static String readFirst(Path command, List<String> arguments, String token) {
    return FixedCommandReader.read(command, arguments, TIMEOUT)
        .map(lines -> firstContaining(lines, token))
        .orElse("Nicht erkannt");
  }

  private static String firstContaining(List<String> lines, String token) {
    return lines.stream().filter(line -> line.contains(token)).findFirst().orElse("Nicht erkannt");
  }

  private static DisplayResult run(Path executable, List<String> arguments, String success) {
    if (!Files.isExecutable(executable)) {
      return new DisplayResult(false, "Benötigtes Werkzeug ist nicht verfügbar.");
    }
    try {
      List<String> command = new ArrayList<>();
      command.add(executable.toString());
      command.addAll(arguments);
      Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
      if (!process.waitFor(TIMEOUT.toSeconds(), TimeUnit.SECONDS)) {
        process.destroyForcibly();
        return new DisplayResult(false, "Anzeigeaktion hat das Zeitlimit überschritten.");
      }
      return process.exitValue() == 0
          ? new DisplayResult(true, success)
          : new DisplayResult(false, "Anzeigeaktion wurde vom Desktop abgelehnt.");
    } catch (IOException | InterruptedException exception) {
      Thread.currentThread().interrupt();
      return new DisplayResult(false, "Anzeigeaktion konnte nicht ausgeführt werden.");
    }
  }

  private static DisplayState unavailable(String message) {
    return new DisplayState(
        false, false, List.of(), 0, false, false, false, new GraphicsInfo("", "", "", ""), message);
  }
}
