package org.cachyos.controlcenter.ai.knowledge;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

/** Symlink-resistant JSON cache under a caller-supplied XDG cache root. */
public final class KnowledgeCache {
  private final Path directory;
  private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

  public KnowledgeCache(Path cacheRoot) {
    directory = cacheRoot.toAbsolutePath().normalize().resolve("knowledge");
  }

  public Optional<KnowledgeDocument> read(KnowledgeSource source) {
    Path file = file(source);
    try {
      if (!Files.isRegularFile(file)
          || Files.isSymbolicLink(file)
          || Files.size(file) > 1_200_000) {
        return Optional.empty();
      }
    } catch (IOException exception) {
      return Optional.empty();
    }
    try {
      KnowledgeDocument document = mapper.readValue(file.toFile(), KnowledgeDocument.class);
      return source.id().equals(document.sourceId()) && source.uri().equals(document.uri())
          ? Optional.of(document)
          : Optional.empty();
    } catch (IOException | RuntimeException exception) {
      return Optional.empty();
    }
  }

  public void write(KnowledgeSource source, KnowledgeDocument document) throws IOException {
    if (!source.id().equals(document.sourceId()) || !source.uri().equals(document.uri())) {
      throw new IllegalArgumentException("Document does not match source");
    }
    if (Files.exists(directory) && Files.isSymbolicLink(directory)) {
      throw new IOException("Knowledge cache directory must not be a symlink");
    }
    Files.createDirectories(directory);
    Path target = file(source);
    if (Files.exists(target) && Files.isSymbolicLink(target)) {
      throw new IOException("Knowledge cache file must not be a symlink");
    }
    Path temporary = Files.createTempFile(directory, source.id() + "-", ".tmp");
    try {
      mapper.writeValue(temporary.toFile(), document);
      try {
        Files.move(
            temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
      } catch (AtomicMoveNotSupportedException exception) {
        Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
      }
    } finally {
      Files.deleteIfExists(temporary);
    }
  }

  private Path file(KnowledgeSource source) {
    return directory.resolve(source.id() + ".json");
  }
}
