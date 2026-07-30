package org.cachyos.controlcenter.core.action;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.cachyos.controlcenter.core.module.ManagerModule;

/** Mutable-at-bootstrap, read-only-at-runtime action allowlist. */
public final class ActionRegistry {
  private final Map<ActionId, ActionRegistration> registrations = new LinkedHashMap<>();

  public synchronized void register(ActionRegistration registration) {
    if (registrations.putIfAbsent(registration.id(), registration) != null) {
      throw new IllegalStateException("Duplicate action id: " + registration.id());
    }
  }

  public synchronized void registerModule(ManagerModule module) {
    for (ActionId actionId : module.actions()) {
      register(
          new ActionRegistration(
              actionId,
              module.displayName() + ": " + actionId,
              module.capabilities().privilegedActions(),
              module::execute));
    }
  }

  public synchronized Optional<ActionHandler> find(ActionId id) {
    ActionRegistration registration = registrations.get(id);
    return registration == null ? Optional.empty() : Optional.of(registration.handler());
  }

  public synchronized boolean isPrivileged(ActionId id) {
    ActionRegistration registration = registrations.get(id);
    return registration != null && registration.privileged();
  }

  public synchronized Set<ActionId> actionIds() {
    return Set.copyOf(registrations.keySet());
  }
}
