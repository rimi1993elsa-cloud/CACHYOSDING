package org.cachyos.controlcenter.persistence;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;

/** Small SQLite boundary with a fixed schema and hardened local file creation. */
final class SqliteDatabase {
  private static final Set<PosixFilePermission> PRIVATE_DIRECTORY =
      Set.of(
          PosixFilePermission.OWNER_READ,
          PosixFilePermission.OWNER_WRITE,
          PosixFilePermission.OWNER_EXECUTE);
  private final Path file;
  private final String url;

  SqliteDatabase(Path file) {
    this.file = file.toAbsolutePath().normalize();
    Path parent = this.file.getParent();
    if (parent == null
        || Files.isSymbolicLink(parent)
        || (Files.exists(this.file, LinkOption.NOFOLLOW_LINKS)
            && (!Files.isRegularFile(this.file, LinkOption.NOFOLLOW_LINKS)
                || Files.isSymbolicLink(this.file)))) {
      throw new IllegalArgumentException("Unsicherer SQLite-Pfad");
    }
    try {
      Files.createDirectories(parent);
      try {
        Files.setPosixFilePermissions(parent, PRIVATE_DIRECTORY);
      } catch (UnsupportedOperationException ignored) {
        // Development hosts without POSIX permissions are supported.
      }
      Class.forName("org.sqlite.JDBC");
    } catch (IOException | ClassNotFoundException exception) {
      throw new IllegalStateException("SQLite konnte nicht initialisiert werden", exception);
    }
    url = "jdbc:sqlite:" + this.file;
    migrate();
  }

  Connection open() throws SQLException {
    Connection connection = DriverManager.getConnection(url);
    try (Statement statement = connection.createStatement()) {
      statement.execute("PRAGMA foreign_keys = ON");
      statement.execute("PRAGMA busy_timeout = 5000");
    }
    return connection;
  }

  Path file() {
    return file;
  }

  private void migrate() {
    try (Connection connection = open();
        Statement statement = connection.createStatement()) {
      statement.execute("PRAGMA journal_mode = WAL");
      statement.execute(
          """
          CREATE TABLE IF NOT EXISTS schema_migrations (
            version INTEGER PRIMARY KEY,
            applied_at TEXT NOT NULL
          )
          """);
      statement.execute(
          """
          CREATE TABLE IF NOT EXISTS settings (
            key TEXT PRIMARY KEY,
            value TEXT NOT NULL,
            updated_at TEXT NOT NULL
          )
          """);
      statement.execute(
          """
          CREATE TABLE IF NOT EXISTS module_preferences (
            module_id TEXT PRIMARY KEY,
            enabled INTEGER NOT NULL CHECK(enabled IN (0, 1))
          )
          """);
      statement.execute(
          """
          CREATE TABLE IF NOT EXISTS quick_actions (
            position INTEGER PRIMARY KEY,
            action_id TEXT NOT NULL UNIQUE
          )
          """);
      statement.execute(
          """
          CREATE TABLE IF NOT EXISTS action_history (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            occurred_at TEXT NOT NULL,
            action_id TEXT NOT NULL,
            input_source TEXT NOT NULL,
            result TEXT NOT NULL,
            duration_ms INTEGER NOT NULL CHECK(duration_ms >= 0),
            privileged INTEGER NOT NULL CHECK(privileged IN (0, 1))
          )
          """);
      statement.execute(
          """
          CREATE TABLE IF NOT EXISTS diagnostic_runs (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            occurred_at TEXT NOT NULL,
            category TEXT NOT NULL,
            status TEXT NOT NULL,
            sanitized_summary TEXT NOT NULL
          )
          """);
      statement.execute(
          """
          CREATE TABLE IF NOT EXISTS chat_sessions (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            started_at TEXT NOT NULL
          )
          """);
      statement.execute(
          """
          CREATE TABLE IF NOT EXISTS chat_messages (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            session_id INTEGER NOT NULL REFERENCES chat_sessions(id) ON DELETE CASCADE,
            occurred_at TEXT NOT NULL,
            role TEXT NOT NULL CHECK(role IN ('user', 'assistant')),
            content TEXT NOT NULL
          )
          """);
      statement.execute(
          """
          CREATE TABLE IF NOT EXISTS knowledge_sources (
            id TEXT PRIMARY KEY,
            uri TEXT NOT NULL,
            fetched_at TEXT
          )
          """);
      statement.execute(
          """
          CREATE TABLE IF NOT EXISTS knowledge_documents (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            source_id TEXT NOT NULL REFERENCES knowledge_sources(id) ON DELETE CASCADE,
            content_hash TEXT NOT NULL,
            content TEXT NOT NULL
          )
          """);
      statement.execute(
          """
          CREATE TABLE IF NOT EXISTS knowledge_chunks (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            document_id INTEGER NOT NULL REFERENCES knowledge_documents(id) ON DELETE CASCADE,
            position INTEGER NOT NULL,
            content TEXT NOT NULL
          )
          """);
      statement.execute(
          """
          CREATE TABLE IF NOT EXISTS ai_usage (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            occurred_at TEXT NOT NULL,
            model TEXT NOT NULL,
            input_tokens INTEGER NOT NULL CHECK(input_tokens >= 0),
            output_tokens INTEGER NOT NULL CHECK(output_tokens >= 0),
            estimated_millicents INTEGER NOT NULL CHECK(estimated_millicents >= 0)
          )
          """);
      statement.execute(
          "INSERT OR IGNORE INTO schema_migrations(version, applied_at) VALUES"
              + " (1, datetime('now'))");
    } catch (SQLException exception) {
      throw new IllegalStateException("SQLite-Schema konnte nicht migriert werden", exception);
    }
  }
}
