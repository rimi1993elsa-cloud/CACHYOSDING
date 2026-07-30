package org.cachyos.controlcenter.core.audit;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Thread-safe Phase 2 audit log; SQLite persistence follows in a later phase. */
public final class InMemoryAuditLog implements AuditSink {
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
