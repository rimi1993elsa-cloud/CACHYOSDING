package org.cachyos.controlcenter.core.audit;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Thread-safe volatile audit log for tests and non-persistent fallbacks. */
public final class InMemoryAuditLog implements AuditLog {
  private final CopyOnWriteArrayList<ActionAuditEvent> events = new CopyOnWriteArrayList<>();

  @Override
  public void record(ActionAuditEvent event) {
    events.add(event);
  }

  public List<ActionAuditEvent> events() {
    return List.copyOf(events);
  }

  public void clear() {
    events.clear();
  }
}
