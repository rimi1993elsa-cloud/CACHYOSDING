package org.cachyos.controlcenter.persistence;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.cachyos.controlcenter.core.action.ActionId;
import org.cachyos.controlcenter.core.action.ActionStatus;
import org.cachyos.controlcenter.core.action.InputSource;
import org.cachyos.controlcenter.core.audit.ActionAuditEvent;
import org.cachyos.controlcenter.core.audit.AuditLog;

/** Persistent, parameter-free action audit trail. */
public final class SqliteAuditLog implements AuditLog {
  private static final int MAXIMUM_EVENTS = 500;
  private final SqliteDatabase database;

  public SqliteAuditLog(java.nio.file.Path databaseFile) {
    database = new SqliteDatabase(databaseFile);
  }

  @Override
  public synchronized void record(ActionAuditEvent event) {
    String sql =
        """
        INSERT INTO action_history(
          occurred_at, action_id, input_source, result, duration_ms, privileged
        ) VALUES (?, ?, ?, ?, ?, ?)
        """;
    try (var connection = database.open();
        PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setString(1, event.timestamp().toString());
      statement.setString(2, event.actionId().value());
      statement.setString(3, event.source().name());
      statement.setString(4, event.result().name());
      statement.setLong(5, event.durationMillis());
      statement.setInt(6, event.privileged() ? 1 : 0);
      statement.executeUpdate();
      try (PreparedStatement trim =
          connection.prepareStatement(
              "DELETE FROM action_history WHERE id NOT IN"
                  + " (SELECT id FROM action_history ORDER BY id DESC LIMIT ?)")) {
        trim.setInt(1, MAXIMUM_EVENTS);
        trim.executeUpdate();
      }
    } catch (SQLException exception) {
      throw new IllegalStateException("Audit-Eintrag konnte nicht gespeichert werden", exception);
    }
  }

  @Override
  public synchronized List<ActionAuditEvent> events() {
    List<ActionAuditEvent> result = new ArrayList<>();
    String sql =
        """
        SELECT occurred_at, action_id, input_source, result, duration_ms, privileged
        FROM action_history ORDER BY id DESC LIMIT ?
        """;
    try (var connection = database.open();
        PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setInt(1, MAXIMUM_EVENTS);
      try (ResultSet rows = statement.executeQuery()) {
        while (rows.next()) {
          result.add(
              new ActionAuditEvent(
                  java.time.Instant.parse(rows.getString(1)),
                  ActionId.of(rows.getString(2)),
                  InputSource.valueOf(rows.getString(3)),
                  ActionStatus.valueOf(rows.getString(4)),
                  rows.getLong(5),
                  rows.getInt(6) != 0));
        }
      }
      return List.copyOf(result);
    } catch (SQLException | IllegalArgumentException exception) {
      throw new IllegalStateException("Audit-Einträge konnten nicht gelesen werden", exception);
    }
  }

  @Override
  public synchronized void clear() {
    try (var connection = database.open();
        PreparedStatement statement = connection.prepareStatement("DELETE FROM action_history")) {
      statement.executeUpdate();
    } catch (SQLException exception) {
      throw new IllegalStateException("Audit konnte nicht gelöscht werden", exception);
    }
  }
}
