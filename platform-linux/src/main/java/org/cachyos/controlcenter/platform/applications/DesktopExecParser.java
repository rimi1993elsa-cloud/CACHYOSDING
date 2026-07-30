package org.cachyos.controlcenter.platform.applications;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import org.cachyos.controlcenter.platform.process.CommandSpec;
import org.cachyos.controlcenter.platform.process.ExecutableLookup;

/** Parses Desktop Entry Exec syntax into a shell-free command specification. */
final class DesktopExecParser {
  private static final Set<String> SHELLS =
      Set.of("sh", "bash", "dash", "zsh", "fish", "csh", "tcsh", "env");

  private DesktopExecParser() {}

  static Optional<CommandSpec> parse(String exec, ExecutableLookup lookup) {
    List<String> tokens = tokenize(exec);
    if (tokens.isEmpty()) {
      return Optional.empty();
    }
    String executableToken = tokens.getFirst();
    String basename;
    try {
      basename = Path.of(executableToken).getFileName().toString().toLowerCase(Locale.ROOT);
    } catch (RuntimeException exception) {
      return Optional.empty();
    }
    if (SHELLS.contains(basename)) {
      return Optional.empty();
    }
    Optional<Path> executable;
    Path candidate = Path.of(executableToken);
    if (candidate.isAbsolute()) {
      executable =
          Files.isRegularFile(candidate) && Files.isExecutable(candidate)
              ? Optional.of(candidate.normalize())
              : Optional.empty();
    } else {
      executable = lookup.find(executableToken);
    }
    if (executable.isEmpty()) {
      return Optional.empty();
    }
    List<String> arguments =
        tokens.subList(1, tokens.size()).stream()
            .filter(token -> !containsFieldCode(token))
            .map(token -> token.replace("%%", "%"))
            .toList();
    return Optional.of(new CommandSpec(executable.get(), arguments));
  }

  static List<String> tokenize(String exec) {
    if (exec == null || exec.isBlank() || exec.indexOf('\0') >= 0) {
      return List.of();
    }
    List<String> tokens = new ArrayList<>();
    StringBuilder current = new StringBuilder();
    boolean quoted = false;
    boolean escaped = false;
    for (int index = 0; index < exec.length(); index++) {
      char value = exec.charAt(index);
      if (escaped) {
        current.append(value);
        escaped = false;
      } else if (value == '\\') {
        escaped = true;
      } else if (value == '"') {
        quoted = !quoted;
      } else if (Character.isWhitespace(value) && !quoted) {
        addToken(tokens, current);
      } else {
        current.append(value);
      }
    }
    if (escaped || quoted) {
      return List.of();
    }
    addToken(tokens, current);
    return List.copyOf(tokens);
  }

  private static boolean containsFieldCode(String token) {
    for (int index = 0; index + 1 < token.length(); index++) {
      if (token.charAt(index) == '%' && token.charAt(index + 1) != '%') {
        return true;
      }
      if (token.charAt(index) == '%' && token.charAt(index + 1) == '%') {
        index++;
      }
    }
    return false;
  }

  private static void addToken(List<String> tokens, StringBuilder current) {
    if (!current.isEmpty()) {
      tokens.add(current.toString());
      current.setLength(0);
    }
  }
}
