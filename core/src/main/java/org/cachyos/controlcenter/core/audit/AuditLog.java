package org.cachyos.controlcenter.core.audit;

import java.util.List;

/** Queryable and clearable local audit trail. */
public interface AuditLog extends AuditSink {
  List<ActionAuditEvent> events();

  void clear();
}
