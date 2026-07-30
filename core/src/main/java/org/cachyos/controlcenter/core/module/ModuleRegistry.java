package org.cachyos.controlcenter.core.module;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Bootstrap registry for manager modules. */
public final class ModuleRegistry {
  private final Map<ModuleId, ManagerModule> modules = new LinkedHashMap<>();

  public synchronized void register(ManagerModule module) {
    if (modules.putIfAbsent(module.id(), module) != null) {
      throw new IllegalStateException("Duplicate module id: " + module.id().value());
    }
  }

  public synchronized Optional<ManagerModule> find(ModuleId id) {
    return Optional.ofNullable(modules.get(id));
  }

  public synchronized List<ManagerModule> modules() {
    return List.copyOf(modules.values());
  }
}
