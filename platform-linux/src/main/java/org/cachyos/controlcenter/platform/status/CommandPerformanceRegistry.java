package org.cachyos.controlcenter.platform.status;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.List;

/** Bounded parameter-free timing evidence for diagnosing slow platform probes. */
public final class CommandPerformanceRegistry {
  private static final int MAXIMUM_SAMPLES = 100;
  private static final ArrayDeque<CommandTiming> SAMPLES = new ArrayDeque<>();

  private CommandPerformanceRegistry() {}

  static synchronized void record(String executable, Duration duration, boolean success) {
    SAMPLES.addLast(new CommandTiming(Instant.now(), executable, duration, success));
    while (SAMPLES.size() > MAXIMUM_SAMPLES) {
      SAMPLES.removeFirst();
    }
  }

  public static synchronized List<CommandTiming> samples() {
    return List.copyOf(SAMPLES);
  }

  public static synchronized void clear() {
    SAMPLES.clear();
  }

  public record CommandTiming(
      Instant timestamp, String executable, Duration duration, boolean success) {}
}
