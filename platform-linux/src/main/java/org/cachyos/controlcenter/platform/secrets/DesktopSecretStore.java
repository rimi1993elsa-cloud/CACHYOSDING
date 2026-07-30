package org.cachyos.controlcenter.platform.secrets;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.cachyos.controlcenter.ai.provider.EnvironmentSecretStore;
import org.cachyos.controlcenter.ai.provider.SecretStore;

/**
 * Reads OpenAI credentials from Secret Service/libsecret, with an environment-only dev fallback.
 */
public final class DesktopSecretStore implements SecretStore {
  private static final int MAXIMUM_SECRET_BYTES = 8_192;
  private final Path secretTool;
  private final Duration timeout;
  private final SecretStore fallback;

  public DesktopSecretStore() {
    this(Path.of("/usr/bin/secret-tool"), Duration.ofSeconds(5), new EnvironmentSecretStore());
  }

  DesktopSecretStore(Path secretTool, Duration timeout, SecretStore fallback) {
    this.secretTool = secretTool.toAbsolutePath().normalize();
    this.timeout = timeout;
    this.fallback = fallback;
  }

  @Override
  public Optional<char[]> readSecret(String key) {
    if (!"openai-api-key".equals(key)) {
      return Optional.empty();
    }
    Optional<char[]> desktopSecret = readDesktopSecret();
    return desktopSecret.isPresent() ? desktopSecret : fallback.readSecret(key);
  }

  private Optional<char[]> readDesktopSecret() {
    if (!Files.isRegularFile(secretTool) || !Files.isExecutable(secretTool)) {
      return Optional.empty();
    }
    Process process = null;
    try {
      process =
          new ProcessBuilder(
                  java.util.List.of(
                      secretTool.toString(),
                      "lookup",
                      "application",
                      "cachyos-control-center",
                      "key",
                      "openai-api-key"))
              .redirectError(ProcessBuilder.Redirect.DISCARD)
              .start();
      if (!process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
        process.destroyForcibly();
        return Optional.empty();
      }
      byte[] bytes = process.getInputStream().readNBytes(MAXIMUM_SECRET_BYTES + 1);
      if (process.exitValue() != 0 || bytes.length == 0 || bytes.length > MAXIMUM_SECRET_BYTES) {
        return Optional.empty();
      }
      String value = new String(bytes, StandardCharsets.UTF_8).strip();
      return value.isBlank() ? Optional.empty() : Optional.of(value.toCharArray());
    } catch (IOException exception) {
      return Optional.empty();
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      return Optional.empty();
    } finally {
      if (process != null && process.isAlive()) {
        process.destroyForcibly();
      }
    }
  }
}
