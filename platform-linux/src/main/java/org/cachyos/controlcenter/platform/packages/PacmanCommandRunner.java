package org.cachyos.controlcenter.platform.packages;

import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

class PacmanCommandRunner {
  private static final int MAX_LINES = 20_000;

  CommandOutput run(Path executable, List<String> arguments, Duration timeout) {
    List<String> command = new ArrayList<>(arguments.size() + 1);
    command.add(executable.toAbsolutePath().normalize().toString());
    command.addAll(arguments);
    try {
      ProcessBuilder builder = new ProcessBuilder(command).redirectErrorStream(true);
      Map<String, String> environment = builder.environment();
      environment.put("LC_ALL", "C");
      environment.put("LANG", "C");
      Process process = builder.start();
      CompletableFuture<List<String>> output =
          CompletableFuture.supplyAsync(
              () -> {
                try (var lines = process.inputReader().lines()) {
                  return lines.limit(MAX_LINES).toList();
                }
              });
      if (!process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
        process.destroyForcibly();
        output.cancel(true);
        return new CommandOutput(-1, List.of());
      }
      return new CommandOutput(process.exitValue(), output.get(1, TimeUnit.SECONDS));
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      return new CommandOutput(-1, List.of());
    } catch (Exception exception) {
      return new CommandOutput(-1, List.of());
    }
  }

  record CommandOutput(int exitCode, List<String> lines) {
    CommandOutput {
      lines = List.copyOf(lines);
    }

    boolean successOrEmpty() {
      return exitCode == 0 || exitCode == 1;
    }
  }
}
