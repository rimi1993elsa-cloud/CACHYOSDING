package org.cachyos.controlcenter.systeminfo;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

/** Availability and safe explanation for one capability. */
public record CapabilityStatus(
    Capability capability, boolean available, Optional<Path> executable, String reason) {
  public CapabilityStatus {
    Objects.requireNonNull(capability, "capability");
    executable = Objects.requireNonNull(executable, "executable");
    reason = Objects.requireNonNull(reason, "reason");
    if (available != executable.isPresent()) {
      throw new IllegalArgumentException("Availability and executable must agree");
    }
  }
}
