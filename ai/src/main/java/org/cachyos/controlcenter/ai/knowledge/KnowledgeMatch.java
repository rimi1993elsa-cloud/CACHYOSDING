package org.cachyos.controlcenter.ai.knowledge;

import java.net.URI;
import java.time.Instant;

/** Ranked excerpt with explicit source attribution. */
public record KnowledgeMatch(String title, URI uri, String excerpt, Instant fetchedAt, int score) {}
