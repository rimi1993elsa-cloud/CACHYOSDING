package org.cachyos.controlcenter.platform.process;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.cachyos.controlcenter.core.action.ActionId;
import org.cachyos.controlcenter.systeminfo.OperatingSystemFamily;

/** Maps allowlisted desktop actions to fixed platform command structures. */
public final class DesktopCommandResolver {
  private final OperatingSystemFamily operatingSystem;
  private final Path homeDirectory;
  private final Map<String, String> environment;
  private final ExecutableLookup executableLookup;

  public DesktopCommandResolver(
      OperatingSystemFamily operatingSystem,
      Path homeDirectory,
      Map<String, String> environment,
      ExecutableLookup executableLookup) {
    this.operatingSystem = Objects.requireNonNull(operatingSystem, "operatingSystem");
    this.homeDirectory = requireAbsolute(homeDirectory, "homeDirectory");
    this.environment = Map.copyOf(Objects.requireNonNull(environment, "environment"));
    this.executableLookup = Objects.requireNonNull(executableLookup, "executableLookup");
  }

  public Optional<CommandSpec> resolve(ActionId actionId) {
    Objects.requireNonNull(actionId, "actionId");
    return switch (operatingSystem) {
      case LINUX -> resolveLinux(actionId);
      case WINDOWS -> resolveWindows(actionId);
      case MACOS, OTHER -> Optional.empty();
    };
  }

  private Optional<CommandSpec> resolveLinux(ActionId actionId) {
    if (actionId.equals(ActionId.OPEN_FIREFOX)) {
      return command("firefox", List.of());
    }
    if (actionId.equals(ActionId.OPEN_FILE_MANAGER)) {
      Optional<CommandSpec> dolphin = command("dolphin", List.of(homeDirectory.toString()));
      return dolphin.isPresent() ? dolphin : command("xdg-open", List.of(homeDirectory.toString()));
    }
    if (actionId.equals(ActionId.OPEN_TERMINAL)) {
      for (String terminal : List.of("konsole", "foot", "alacritty")) {
        Optional<CommandSpec> command = command(terminal, List.of());
        if (command.isPresent()) {
          return command;
        }
      }
      return Optional.empty();
    }
    if (actionId.equals(ActionId.LOCK_SCREEN)) {
      return command("loginctl", List.of("lock-session"));
    }
    return Optional.empty();
  }

  private Optional<CommandSpec> resolveWindows(ActionId actionId) {
    if (actionId.equals(ActionId.OPEN_FIREFOX)) {
      return command("firefox", List.of());
    }
    Path windowsDirectory =
        Path.of(environment.getOrDefault("SystemRoot", "C:\\Windows")).toAbsolutePath().normalize();
    if (actionId.equals(ActionId.OPEN_FILE_MANAGER)) {
      return fixedWindowsCommand(
          windowsDirectory.resolve("explorer.exe"), List.of(homeDirectory.toString()));
    }
    if (actionId.equals(ActionId.OPEN_TERMINAL)) {
      Optional<CommandSpec> windowsTerminal = command("wt", List.of());
      return windowsTerminal.isPresent()
          ? windowsTerminal
          : fixedWindowsCommand(windowsDirectory.resolve("System32/cmd.exe"), List.of());
    }
    if (actionId.equals(ActionId.LOCK_SCREEN)) {
      return fixedWindowsCommand(
          windowsDirectory.resolve("System32/rundll32.exe"), List.of("user32.dll,LockWorkStation"));
    }
    return Optional.empty();
  }

  private Optional<CommandSpec> command(String executableName, List<String> arguments) {
    return executableLookup.find(executableName).map(path -> new CommandSpec(path, arguments));
  }

  private static Optional<CommandSpec> fixedWindowsCommand(
      Path executable, List<String> arguments) {
    return Files.isRegularFile(executable)
        ? Optional.of(new CommandSpec(executable.toAbsolutePath(), arguments))
        : Optional.empty();
  }

  private static Path requireAbsolute(Path path, String name) {
    Objects.requireNonNull(path, name);
    if (!path.isAbsolute()) {
      throw new IllegalArgumentException(name + " must be absolute");
    }
    return path.normalize();
  }
}
