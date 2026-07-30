package org.cachyos.controlcenter.modules.services;

import java.util.List;

public record ServiceState(boolean available, List<ServiceUnit> units, String message) {
  public ServiceState {
    units = List.copyOf(units);
  }
}
