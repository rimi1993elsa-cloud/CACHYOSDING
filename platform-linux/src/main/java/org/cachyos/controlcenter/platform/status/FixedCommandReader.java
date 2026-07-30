package org.cachyos.controlcenter.platform.status;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/** Executes fixed, read-only argument lists with bounded output and timeout. */
public final class FixedCommandReader {
  private static final int MAX_LINES = 5_000;

  private FixedCommandReader() {}

  public static Optional<List<String>> read(
      Path executable, List<String> fixedArguments, Duration timeout) {
    long started = System.nanoTime();
    boolean success = false;
    List<String> command = new ArrayList<>(fixedArguments.size() + 1);
    command.add(executable.toAbsolutePath().normalize().toString());
    command.addAll(fixedArguments);
    Process process = null;
    try {
      process = new ProcessBuilder(command).redirectErrorStream(true).start();
      Process runningProcess = process;
      CompletableFuture<List<String>> output =
          CompletableFuture.supplyAsync(
              () -> {
                try (var lines = runningProcess.inputReader().lines()) {
                  return lines.limit(MAX_LINES).toList();
                }
              });
      if (!process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
        process.destroyForcibly();
        output.cancel(true);
        return Optional.empty();
      }
      if (process.exitValue() != 0) {
        return Optional.empty();
      }
      List<String> lines = output.get(1, TimeUnit.SECONDS);
      success = true;
      return Optional.of(lines);
    } catch (InterruptedException exception) {
      if (process != null) {
        process.destroyForcibly();
      }
      Thread.currentThread().interrupt();
      return Optional.empty();
    } catch (IOException exception) {
      return Optional.empty();
    } catch (java.util.concurrent.ExecutionException
        | java.util.concurrent.TimeoutException exception) {
      return Optional.empty();
    } finally {
      CommandPerformanceRegistry.record(
          executable.getFileName().toString(),
          Duration.ofNanos(System.nanoTime() - started),
          success);
    }
  }
}
