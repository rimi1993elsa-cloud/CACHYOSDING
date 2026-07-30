package org.cachyos.controlcenter.ai.knowledge;

import java.net.URI;
import java.time.Instant;
import java.util.Objects;

/** Cached plain text from one allowlisted official source. */
public record KnowledgeDocument(
    String sourceId, String title, URI uri, Instant fetchedAt, String text) {
  public KnowledgeDocument {
    sourceId = Objects.requireNonNull(sourceId, "sourceId");
    title = Objects.requireNonNull(title, "title");
    uri = Objects.requireNonNull(uri, "uri");
    fetchedAt = Objects.requireNonNull(fetchedAt, "fetchedAt");
    text = Objects.requireNonNull(text, "text");
    if (text.isBlank() || text.length() > 1_000_000) {
      throw new IllegalArgumentException("Knowledge text is empty or too large");
    }
  }
}
