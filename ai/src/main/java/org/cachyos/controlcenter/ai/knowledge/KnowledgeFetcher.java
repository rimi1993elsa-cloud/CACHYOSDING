package org.cachyos.controlcenter.ai.knowledge;

import java.io.IOException;

/** Fetch port used by the cache and deterministic tests. */
@FunctionalInterface
public interface KnowledgeFetcher {
  KnowledgeDocument fetch(KnowledgeSource source) throws IOException, InterruptedException;
}
