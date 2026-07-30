package org.cachyos.controlcenter.platform.security;

import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

final class SecurityCommandReader {
  private static final int MAX_LINES = 10_000;

  Optional<List<String>> read(Path executable, List<String> arguments, Duration timeout) {
    return read(executable, arguments, timeout, Set.of(0));
  }

  Optional<List<String>> read(
      Path executable, List<String> arguments, Duration timeout, Set<Integer> acceptedExitCodes) {
    if (!java.nio.file.Files.isExecutable(executable)) {
      return Optional.empty();
    }
    List<String> command = new ArrayList<>(arguments.size() + 1);
    command.add(executable.toString());
    command.addAll(arguments);
    try {
      ProcessBuilder builder = new ProcessBuilder(command).redirectErrorStream(true);
      Map<String, String> environment = builder.environment();
      environment.put("LC_ALL", "C");
      environment.put("LANG", "C");
      Process process = builder.start();
      CompletableFuture<List<String>> lines =
          CompletableFuture.supplyAsync(
              () -> {
                try (var stream = process.inputReader().lines()) {
                  return stream.limit(MAX_LINES).toList();
                }
              });
      if (!process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
        process.destroyForcibly();
        lines.cancel(true);
        return Optional.empty();
      }
      return acceptedExitCodes.contains(process.exitValue())
          ? Optional.of(lines.get(1, TimeUnit.SECONDS))
          : Optional.empty();
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      return Optional.empty();
    } catch (Exception exception) {
      return Optional.empty();
    }
  }
}
