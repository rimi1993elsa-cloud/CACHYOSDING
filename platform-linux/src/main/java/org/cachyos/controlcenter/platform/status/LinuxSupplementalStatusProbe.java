package org.cachyos.controlcenter.platform.status;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import org.cachyos.controlcenter.systeminfo.Capability;
import org.cachyos.controlcenter.systeminfo.CapabilityRegistry;
import org.cachyos.controlcenter.systeminfo.SupplementalStatus;
import org.cachyos.controlcenter.systeminfo.SupplementalStatusProbe;

/** Reads update and failed-service counts through fixed optional Linux tools. */
public final class LinuxSupplementalStatusProbe implements SupplementalStatusProbe {
  private static final Duration TIMEOUT = Duration.ofSeconds(8);

  @Override
  public SupplementalStatus read(CapabilityRegistry capabilities) {
    return new SupplementalStatus(
        count(capabilities.status(Capability.PACMAN).executable(), List.of("-Qu")),
        count(
            capabilities.status(Capability.SYSTEMCTL).executable(),
            List.of("--failed", "--no-legend", "--plain", "--no-pager")));
  }

  private static OptionalInt count(Optional<Path> executable, List<String> arguments) {
    if (executable.isEmpty()) {
      return OptionalInt.empty();
    }
    return FixedCommandReader.read(executable.get(), arguments, TIMEOUT).stream()
        .mapToInt(LinuxSupplementalStatusProbe::countNonBlank)
        .findFirst();
  }

  static int countNonBlank(List<String> lines) {
    return (int) lines.stream().map(String::trim).filter(line -> !line.isEmpty()).count();
  }
}
