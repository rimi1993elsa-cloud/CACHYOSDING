package org.cachyos.controlcenter.ai.knowledge;

import java.net.URI;
import java.time.Duration;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/** Allowlisted official documentation source. */
public record KnowledgeSource(String id, String title, URI uri, Duration maximumAge) {
  private static final Pattern ID = Pattern.compile("[a-z][a-z0-9-]{2,39}");
  private static final Set<String> ALLOWED_HOSTS = Set.of("wiki.cachyos.org", "wiki.archlinux.org");

  public KnowledgeSource {
    id = Objects.requireNonNull(id, "id");
    title = Objects.requireNonNull(title, "title").strip();
    uri = Objects.requireNonNull(uri, "uri");
    maximumAge = Objects.requireNonNull(maximumAge, "maximumAge");
    if (!ID.matcher(id).matches()
        || title.isBlank()
        || !"https".equalsIgnoreCase(uri.getScheme())
        || !ALLOWED_HOSTS.contains(uri.getHost())
        || uri.getUserInfo() != null
        || uri.getPort() != -1
        || maximumAge.isNegative()
        || maximumAge.isZero()) {
      throw new IllegalArgumentException("Unsafe knowledge source");
    }
  }
}
