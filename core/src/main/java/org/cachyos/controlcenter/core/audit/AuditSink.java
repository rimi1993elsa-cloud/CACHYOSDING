package org.cachyos.controlcenter.core.audit;

/** Local audit destination. */
@FunctionalInterface
public interface AuditSink {
  void record(ActionAuditEvent event);
}
