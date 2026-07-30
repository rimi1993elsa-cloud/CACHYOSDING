package org.cachyos.controlcenter.modules.diagnostics;

import java.util.Objects;
import java.util.regex.Pattern;

/** Removes common personal and secret values from diagnostic text. */
public final class DiagnosticSanitizer {
  private static final Pattern EMAIL =
      Pattern.compile("(?i)\\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}\\b");
  private static final Pattern HOME = Pattern.compile("(?i)(?:/home/|C:\\\\Users\\\\)[^/\\\\\\s]+");
  private static final Pattern PRIVATE_IPV4 =
      Pattern.compile(
          "\\b(?:10\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}"
              + "|192\\.168\\.\\d{1,3}\\.\\d{1,3}"
              + "|172\\.(?:1[6-9]|2\\d|3[01])\\.\\d{1,3}\\.\\d{1,3})\\b");
  private static final Pattern TOKEN =
      Pattern.compile("(?i)\\b(?:sk-[A-Za-z0-9_-]{12,}|(?:token|api[_ -]?key)\\s*[:=]\\s*\\S+)");
  private static final Pattern HOSTNAME =
      Pattern.compile("(?im)^(?:hostname|static hostname)\\s*[:=]\\s*\\S+");
  private static final Pattern HARDWARE_IDENTIFIER =
      Pattern.compile(
          "(?i)\\b(?:[0-9a-f]{2}:){5}[0-9a-f]{2}\\b"
              + "|\\b[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}"
              + "-[89ab][0-9a-f]{3}-[0-9a-f]{12}\\b");
  private static final Pattern SERIAL =
      Pattern.compile("(?im)^(?:serial|serial number|machine id)\\s*[:=]\\s*\\S+");
  private static final int MAXIMUM_LENGTH = 16_000;

  private DiagnosticSanitizer() {}

  public static String sanitize(String text) {
    String safe = Objects.requireNonNullElse(text, "").replace('\0', ' ');
    safe = EMAIL.matcher(safe).replaceAll("<email>");
    safe = HOME.matcher(safe).replaceAll("/home/<user>");
    safe = PRIVATE_IPV4.matcher(safe).replaceAll("<private-ip>");
    safe = TOKEN.matcher(safe).replaceAll("<secret>");
    safe = HOSTNAME.matcher(safe).replaceAll("hostname: <host>");
    safe = HARDWARE_IDENTIFIER.matcher(safe).replaceAll("<hardware-id>");
    safe = SERIAL.matcher(safe).replaceAll("serial: <hardware-id>");
    safe = safe.replaceAll("[\\p{Cntrl}&&[^\\n\\t]]", "");
    return safe.substring(0, Math.min(safe.length(), MAXIMUM_LENGTH)).strip();
  }
}
