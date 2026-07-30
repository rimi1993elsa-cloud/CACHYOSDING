package org.cachyos.controlcenter.ai.knowledge;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/** Cached local lexical retrieval over allowlisted official documentation. */
public final class KnowledgeService implements AutoCloseable {
  private static final int CHUNK_LENGTH = 1_200;
  private static final int CHUNK_OVERLAP = 160;
  private final List<KnowledgeSource> sources;
  private final KnowledgeCache cache;
  private final KnowledgeFetcher fetcher;
  private final ExecutorService executor;
  private final AtomicReference<List<KnowledgeDocument>> documents =
      new AtomicReference<>(List.of());

  public KnowledgeService(
      List<KnowledgeSource> sources, KnowledgeCache cache, KnowledgeFetcher fetcher) {
    this.sources = List.copyOf(sources);
    this.cache = cache;
    this.fetcher = fetcher;
    executor =
        Executors.newSingleThreadExecutor(
            runnable -> {
              Thread thread = new Thread(runnable, "knowledge-refresh");
              thread.setDaemon(true);
              return thread;
            });
    loadCached();
  }

  public CompletableFuture<Integer> refreshStale() {
    return CompletableFuture.supplyAsync(
        () -> {
          List<KnowledgeDocument> refreshed = new ArrayList<>(documents.get());
          int updated = 0;
          for (KnowledgeSource source : sources) {
            KnowledgeDocument current = find(refreshed, source.id());
            if (current != null
                && current.fetchedAt().plus(source.maximumAge()).isAfter(Instant.now())) {
              continue;
            }
            try {
              KnowledgeDocument document = fetcher.fetch(source);
              cache.write(source, document);
              refreshed.removeIf(item -> item.sourceId().equals(source.id()));
              refreshed.add(document);
              updated++;
            } catch (IOException exception) {
              // Existing cache remains authoritative during offline operation.
            } catch (InterruptedException exception) {
              Thread.currentThread().interrupt();
              break;
            }
          }
          documents.set(List.copyOf(refreshed));
          return updated;
        },
        executor);
  }

  public List<KnowledgeMatch> search(String query, int limit) {
    if (query == null || query.isBlank() || limit < 1 || limit > 10) {
      return List.of();
    }
    Set<String> terms =
        java.util.Arrays.stream(query.toLowerCase(Locale.GERMAN).split("[^\\p{L}\\p{N}]+"))
            .filter(term -> term.length() >= 3)
            .collect(Collectors.toUnmodifiableSet());
    if (terms.isEmpty()) {
      return List.of();
    }
    List<KnowledgeMatch> matches = new ArrayList<>();
    for (KnowledgeDocument document : documents.get()) {
      for (String chunk : chunks(document.text())) {
        String lower = chunk.toLowerCase(Locale.GERMAN);
        int score = terms.stream().mapToInt(term -> occurrences(lower, term)).sum();
        if (score > 0) {
          matches.add(
              new KnowledgeMatch(
                  document.title(), document.uri(), chunk, document.fetchedAt(), score));
        }
      }
    }
    return matches.stream()
        .sorted(
            Comparator.comparingInt(KnowledgeMatch::score)
                .reversed()
                .thenComparing(KnowledgeMatch::title))
        .limit(limit)
        .toList();
  }

  public int documentCount() {
    return documents.get().size();
  }

  @Override
  public void close() {
    executor.shutdownNow();
  }

  private void loadCached() {
    documents.set(sources.stream().flatMap(source -> cache.read(source).stream()).toList());
  }

  private static KnowledgeDocument find(List<KnowledgeDocument> values, String sourceId) {
    return values.stream()
        .filter(document -> document.sourceId().equals(sourceId))
        .findFirst()
        .orElse(null);
  }

  private static List<String> chunks(String text) {
    List<String> result = new ArrayList<>();
    int start = 0;
    while (start < text.length()) {
      int end = Math.min(text.length(), start + CHUNK_LENGTH);
      if (end < text.length()) {
        int boundary = text.lastIndexOf(' ', end);
        if (boundary > start + CHUNK_LENGTH / 2) {
          end = boundary;
        }
      }
      result.add(text.substring(start, end).strip());
      if (end == text.length()) {
        break;
      }
      start = Math.max(start + 1, end - CHUNK_OVERLAP);
    }
    return result;
  }

  private static int occurrences(String text, String term) {
    int count = 0;
    int offset = 0;
    while ((offset = text.indexOf(term, offset)) >= 0) {
      count++;
      offset += term.length();
    }
    return count;
  }
}
