package org.cachyos.controlcenter.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import org.cachyos.controlcenter.core.action.ActionId;
import org.cachyos.controlcenter.core.action.ActionStatus;
import org.cachyos.controlcenter.core.action.InputSource;
import org.cachyos.controlcenter.core.audit.ActionAuditEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SqliteAuditLogTest {
  @TempDir java.nio.file.Path temporary;

  @Test
  void survivesReopeningAndCanBeCleared() {
    java.nio.file.Path file = temporary.resolve("control-center.sqlite3");
    SqliteAuditLog audit = new SqliteAuditLog(file);
    audit.record(
        new ActionAuditEvent(
            Instant.now(),
            ActionId.OPEN_FIREFOX,
            InputSource.BUTTON,
            ActionStatus.SUCCESS,
            12,
            false));

    SqliteAuditLog reopened = new SqliteAuditLog(file);

    assertEquals(ActionId.OPEN_FIREFOX, reopened.events().getFirst().actionId());
    reopened.clear();
    assertTrue(reopened.events().isEmpty());
  }
}
