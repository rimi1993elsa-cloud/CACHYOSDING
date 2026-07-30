package org.cachyos.controlcenter.platform.processes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.cachyos.controlcenter.modules.processes.ProcessBackend;
import org.cachyos.controlcenter.modules.processes.ProcessEntry;

public final class LinuxProcessBackend implements ProcessBackend {
  private static final Set<String> CRITICAL =
      Set.of("systemd", "kthreadd", "dbus-daemon", "systemd-logind", "systemd-udevd");
  private final boolean linux;

  public LinuxProcessBackend(boolean linux) {
    this.linux = linux;
  }

  @Override
  public List<ProcessEntry> inspect() {
    if (!linux) {
      return List.of();
    }
    return ProcessHandle.allProcesses()
        .limit(20_000)
        .map(LinuxProcessBackend::entry)
        .sorted(Comparator.comparingLong(ProcessEntry::residentBytes).reversed())
        .toList();
  }

  private static ProcessEntry entry(ProcessHandle process) {
    ProcessHandle.Info info = process.info();
    boolean commandHidden = info.command().isEmpty();
    String command =
        info.command()
            .map(Path::of)
            .map(Path::getFileName)
            .map(Path::toString)
            .orElseGet(() -> readComm(process.pid()));
    long pid = process.pid();
    long cpu = info.totalCpuDuration().map(java.time.Duration::toMillis).orElse(0L);
    ProcValues proc = proc(pid);
    return new ProcessEntry(
        pid,
        command,
        info.user().orElse("unbekannt"),
        cpu,
        proc.residentBytes(),
        proc.priority(),
        pid <= 2 || commandHidden || command.startsWith("[") || CRITICAL.contains(command));
  }

  private static String readComm(long pid) {
    try {
      return Files.readString(Path.of("/proc", Long.toString(pid), "comm")).trim();
    } catch (IOException exception) {
      return "unbekannt";
    }
  }

  private static ProcValues proc(long pid) {
    Path statm = Path.of("/proc", Long.toString(pid), "statm");
    Path stat = Path.of("/proc", Long.toString(pid), "stat");
    try {
      long pages = Long.parseLong(Files.readString(statm).trim().split("\\s+")[1]);
      String[] fields = Files.readString(stat).trim().split("\\s+");
      int priority = fields.length > 18 ? Integer.parseInt(fields[18]) : 0;
      return new ProcValues(pages * 4096, priority);
    } catch (IOException | NumberFormatException | IndexOutOfBoundsException exception) {
      return new ProcValues(0, 0);
    }
  }

  private record ProcValues(long residentBytes, int priority) {}
}
