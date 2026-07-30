package org.cachyos.controlcenter.platform.boot;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.cachyos.controlcenter.modules.boot.BootBackend;
import org.cachyos.controlcenter.modules.boot.BootResult;
import org.cachyos.controlcenter.modules.boot.BootSnapshot;
import org.cachyos.controlcenter.modules.boot.KernelInfo;
import org.cachyos.controlcenter.modules.boot.SlowBootUnit;
import org.cachyos.controlcenter.platform.status.FixedCommandReader;

/** Read-only boot inspection plus a fixed CachyOS GUI launcher. */
public final class LinuxBootBackend implements BootBackend {
  private static final Duration TIMEOUT = Duration.ofSeconds(12);
  private static final Pattern KERNEL_PACKAGE =
      Pattern.compile("^(linux(?:-cachyos)?(?:-[a-z0-9]+)?)(?:\\s+)(\\S+)$");
  private static final Pattern BLAME = Pattern.compile("^\\s*(\\S+)\\s+([A-Za-z0-9@_.:-]+)$");
  private static final List<Path> KERNEL_MANAGERS =
      List.of(
          Path.of("/usr/bin/cachyos-kernel-manager"),
          Path.of("/usr/bin/cachyos-kernel-manager-qt"));
  private final boolean linux;

  public LinuxBootBackend(boolean linux) {
    this.linux = linux;
  }

  @Override
  public BootSnapshot inspect() {
    if (!linux) {
      return new BootSnapshot(
          false, "", List.of(), "Nicht verfügbar", "", "", List.of(), false, "Nur unter Linux.");
    }
    String active = read(Path.of("/proc/sys/kernel/osrelease"));
    List<KernelInfo> kernels =
        FixedCommandReader.read(Path.of("/usr/bin/pacman"), List.of("-Q"), TIMEOUT)
            .map(lines -> parseKernels(lines, active))
            .orElse(List.of());
    String bootManager = detectBootManager();
    String parameters = read(Path.of("/proc/cmdline"));
    String bootDuration =
        FixedCommandReader.read(
                Path.of("/usr/bin/systemd-analyze"), List.of("time", "--no-pager"), TIMEOUT)
            .map(lines -> String.join(" ", lines))
            .orElse("Nicht verfügbar");
    List<SlowBootUnit> slowUnits =
        FixedCommandReader.read(
                Path.of("/usr/bin/systemd-analyze"), List.of("blame", "--no-pager"), TIMEOUT)
            .map(this::parseBlame)
            .orElse(List.of());
    return new BootSnapshot(
        true,
        active,
        kernels,
        bootManager,
        parameters,
        bootDuration,
        slowUnits,
        kernelManager().isPresent(),
        "Bootdaten sind ausschließlich lesend.");
  }

  @Override
  public BootResult launchKernelManager() {
    Optional<Path> executable = kernelManager();
    if (executable.isEmpty()) {
      return new BootResult(false, "CachyOS Kernel Manager ist nicht installiert.");
    }
    try {
      new ProcessBuilder(executable.orElseThrow().toString()).start();
      return new BootResult(true, "CachyOS Kernel Manager wurde gestartet.");
    } catch (IOException exception) {
      return new BootResult(false, "CachyOS Kernel Manager konnte nicht gestartet werden.");
    }
  }

  List<KernelInfo> parseKernels(List<String> lines, String activeKernel) {
    List<KernelInfo> result = new ArrayList<>();
    for (String line : lines) {
      Matcher matcher = KERNEL_PACKAGE.matcher(line);
      if (matcher.matches() && !matcher.group(1).endsWith("-headers")) {
        String packageName = matcher.group(1);
        String version = matcher.group(2);
        boolean active =
            activeKernel.equals(version)
                || activeKernel.startsWith(version + "-")
                || (activeKernel.contains("cachyos") && packageName.contains("cachyos"));
        result.add(new KernelInfo(packageName, version, active));
      }
    }
    return List.copyOf(result);
  }

  List<SlowBootUnit> parseBlame(List<String> lines) {
    List<SlowBootUnit> result = new ArrayList<>();
    for (String line : lines.stream().limit(20).toList()) {
      Matcher matcher = BLAME.matcher(line);
      if (matcher.matches()) {
        result.add(new SlowBootUnit(matcher.group(1), matcher.group(2)));
      }
    }
    return List.copyOf(result);
  }

  private String detectBootManager() {
    if (Files.isDirectory(Path.of("/boot/loader/entries"))
        || Files.isRegularFile(Path.of("/boot/loader/loader.conf"))) {
      return "systemd-boot";
    }
    if (Files.isRegularFile(Path.of("/boot/grub/grub.cfg"))
        || Files.isDirectory(Path.of("/etc/grub.d"))) {
      return "GRUB";
    }
    if (Files.isDirectory(Path.of("/sys/firmware/efi/efivars"))) {
      return "UEFI (Bootmanager nicht eindeutig)";
    }
    return "Nicht erkannt";
  }

  private Optional<Path> kernelManager() {
    return KERNEL_MANAGERS.stream().filter(Files::isExecutable).findFirst();
  }

  private static String read(Path path) {
    try {
      return Files.readString(path).trim();
    } catch (IOException | SecurityException exception) {
      return "";
    }
  }
}
