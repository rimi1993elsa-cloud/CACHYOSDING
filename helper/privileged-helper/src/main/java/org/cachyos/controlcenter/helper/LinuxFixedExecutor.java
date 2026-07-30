package org.cachyos.controlcenter.helper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

final class LinuxFixedExecutor implements HelperExecutor {
  private static final Duration PROCESS_TIMEOUT = Duration.ofMinutes(5);
  private static final Set<String> CRITICAL_PROCESSES =
      Set.of(
          "systemd",
          "init",
          "kthreadd",
          "dbus-daemon",
          "systemd-logind",
          "systemd-journald",
          "systemd-udevd",
          "polkitd");

  @Override
  public int installPackage(String packageName) throws Exception {
    require(HelperValidation.packageName(packageName));
    if (pacmanLocked()) {
      return 75;
    }
    return run(List.of("/usr/bin/pacman", "--noconfirm", "-S", "--", packageName));
  }

  @Override
  public int removePackage(String packageName) throws Exception {
    require(HelperValidation.packageName(packageName));
    if (pacmanLocked()) {
      return 75;
    }
    return run(List.of("/usr/bin/pacman", "--noconfirm", "-Rns", "--", packageName));
  }

  @Override
  public int setFirewallEnabled(boolean enabled) throws Exception {
    String operation = enabled ? "enable" : "disable";
    String runtimeOperation = enabled ? "start" : "stop";
    int persistent = run(List.of("/usr/bin/systemctl", operation, "firewalld.service"));
    return persistent == 0
        ? run(List.of("/usr/bin/systemctl", runtimeOperation, "firewalld.service"))
        : persistent;
  }

  @Override
  public int controlSystemService(String unitName, String operation) throws Exception {
    require(HelperValidation.unitName(unitName));
    require(HelperValidation.serviceOperation(operation));
    return run(List.of("/usr/bin/systemctl", operation, "--", unitName));
  }

  @Override
  public int createSnapshot(String description) throws Exception {
    require(HelperValidation.snapshotDescription(description));
    return run(List.of("/usr/bin/snapper", "create", "--description", description));
  }

  @Override
  public int deleteSnapshot(int snapshotId) throws Exception {
    require(HelperValidation.snapshotId(snapshotId));
    return run(List.of("/usr/bin/snapper", "delete", Integer.toString(snapshotId)));
  }

  @Override
  public int signalProcess(long processId, int signal) throws Exception {
    require(HelperValidation.processId(processId));
    require(HelperValidation.signal(signal));
    require(!criticalProcess(processId));
    return run(
        List.of("/usr/bin/kill", signal == 9 ? "-KILL" : "-TERM", "--", Long.toString(processId)));
  }

  @Override
  public int setProcessPriority(long processId, int priority) throws Exception {
    require(HelperValidation.processId(processId));
    require(HelperValidation.priority(priority));
    require(!criticalProcess(processId));
    return run(
        List.of(
            "/usr/bin/renice",
            "--priority",
            Integer.toString(priority),
            "--pid",
            Long.toString(processId)));
  }

  private static void require(boolean valid) {
    if (!valid) {
      throw new IllegalArgumentException("Helper validation rejected an argument");
    }
  }

  private static int run(List<String> command) throws IOException, InterruptedException {
    Process process =
        new ProcessBuilder(command)
            .redirectInput(ProcessBuilder.Redirect.from(nullDevice()))
            .redirectOutput(ProcessBuilder.Redirect.DISCARD)
            .redirectError(ProcessBuilder.Redirect.DISCARD)
            .start();
    if (!process.waitFor(PROCESS_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS)) {
      process.destroyForcibly();
      throw new IOException("System process timed out");
    }
    return process.exitValue();
  }

  private static java.io.File nullDevice() {
    return new java.io.File("/dev/null");
  }

  private static boolean pacmanLocked() {
    return Files.exists(Path.of("/var/lib/pacman/db.lck"), LinkOption.NOFOLLOW_LINKS);
  }

  private static boolean criticalProcess(long processId) {
    Path comm = Path.of("/proc", Long.toString(processId), "comm");
    if (!Files.isRegularFile(comm, LinkOption.NOFOLLOW_LINKS) || Files.isSymbolicLink(comm)) {
      return true;
    }
    try {
      return CRITICAL_PROCESSES.contains(Files.readString(comm).strip());
    } catch (IOException | SecurityException exception) {
      return true;
    }
  }
}
