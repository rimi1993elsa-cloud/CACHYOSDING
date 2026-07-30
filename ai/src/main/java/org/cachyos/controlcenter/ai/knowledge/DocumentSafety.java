package org.cachyos.controlcenter.ai.knowledge;

import java.util.Locale;
import java.util.Set;

/** Neutralizes common embedded instruction phrases before retrieval. */
public final class DocumentSafety {
  private static final Set<String> INSTRUCTION_MARKERS =
      Set.of(
          "ignore previous",
          "ignore all previous",
          "system prompt",
          "developer message",
          "disregard instructions",
          "ignoriere vorherige",
          "ignoriere alle regeln");

  private DocumentSafety() {}

  public static String sanitize(String text) {
    StringBuilder safe = new StringBuilder();
    for (String line : text.replace('\0', ' ').lines().toList()) {
      String lower = line.toLowerCase(Locale.ROOT);
      boolean suspicious = INSTRUCTION_MARKERS.stream().anyMatch(lower::contains);
      if (suspicious) {
        safe.append("[mögliche eingebettete Anweisung entfernt]");
      } else {
        safe.append(line.replaceAll("[\\p{Cntrl}&&[^\\n\\t]]", " "));
      }
      safe.append('\n');
    }
    return safe.toString().strip();
  }
}
