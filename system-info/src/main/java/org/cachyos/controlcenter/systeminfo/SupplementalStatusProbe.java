package org.cachyos.controlcenter.systeminfo;

/** Platform boundary for bounded read-only dashboard commands. */
@FunctionalInterface
public interface SupplementalStatusProbe {
  SupplementalStatus read(CapabilityRegistry capabilities);
}
