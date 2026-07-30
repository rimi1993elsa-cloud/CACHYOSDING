package org.cachyos.controlcenter.persistence;

import java.time.Instant;

public record ChatHistoryEntry(Instant timestamp, String role, String text) {
  public ChatHistoryEntry {
    if (timestamp == null
        || role == null
        || (!role.equals("user") && !role.equals("assistant"))
        || text == null
        || text.isBlank()
        || text.length() > 8_000) {
      throw new IllegalArgumentException("Ungültiger Verlaufseintrag");
    }
    text = text.strip();
  }
}
