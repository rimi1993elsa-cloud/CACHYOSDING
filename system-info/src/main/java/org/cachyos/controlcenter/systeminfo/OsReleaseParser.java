package org.cachyos.controlcenter.systeminfo;

import java.util.LinkedHashMap;
import java.util.Map;

/** Strict line-oriented parser for os-release key/value data. */
public final class OsReleaseParser {
  private OsReleaseParser() {}

  public static Map<String, String> parse(String content) {
    Map<String, String> values = new LinkedHashMap<>();
    for (String line : content.lines().toList()) {
      String trimmed = line.trim();
      if (trimmed.isEmpty() || trimmed.startsWith("#")) {
        continue;
      }
      int separator = trimmed.indexOf('=');
      if (separator <= 0) {
        continue;
      }
      String key = trimmed.substring(0, separator);
      if (!key.matches("[A-Z][A-Z0-9_]*")) {
        continue;
      }
      values.put(key, unquote(trimmed.substring(separator + 1).trim()));
    }
    return Map.copyOf(values);
  }

  private static String unquote(String value) {
    if (value.length() >= 2) {
      char first = value.charAt(0);
      char last = value.charAt(value.length() - 1);
      if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
        return value.substring(1, value.length() - 1).replace("\\\"", "\"").replace("\\\\", "\\");
      }
    }
    return value;
  }
}
