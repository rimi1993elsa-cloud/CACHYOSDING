package org.cachyos.controlcenter.core.audit;

import java.time.Instant;
import java.util.Objects;
import org.cachyos.controlcenter.core.action.ActionId;
import org.cachyos.controlcenter.core.action.ActionStatus;
import org.cachyos.controlcenter.core.action.InputSource;

/** Parameter-free audit metadata safe for local retention. */
public record ActionAuditEvent(
    Instant timestamp,
    ActionId actionId,
    InputSource source,
    ActionStatus result,
    long durationMillis,
    boolean privileged) {
  public ActionAuditEvent {
    Objects.requireNonNull(timestamp, "timestamp");
    Objects.requireNonNull(actionId, "actionId");
    Objects.requireNonNull(source, "source");
    Objects.requireNonNull(result, "result");
    if (durationMillis < 0) {
      throw new IllegalArgumentException("durationMillis must not be negative");
    }
  }
}
