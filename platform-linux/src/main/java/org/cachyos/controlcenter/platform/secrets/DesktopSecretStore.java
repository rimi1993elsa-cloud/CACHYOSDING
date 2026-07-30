package org.cachyos.controlcenter.platform.secrets;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.cachyos.controlcenter.ai.provider.EnvironmentSecretStore;
import org.cachyos.controlcenter.ai.provider.SecretOperationResult;
import org.cachyos.controlcenter.ai.provider.SecretStore;

/**
 * Reads OpenAI credentials from Secret Service/libsecret, with an environment-only dev fallback.
 */
public final class DesktopSecretStore implements SecretStore {
  private static final int MAXIMUM_SECRET_BYTES = 8_192;
  private final Path secretTool;
  private final Duration timeout;
  private final SecretStore fallback;
  private volatile Boolean cachedAvailability;

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
    if (desktopSecret.isPresent()) {
      cachedAvailability = true;
      return desktopSecret;
    }
    Optional<char[]> fallbackSecret = fallback.readSecret(key);
    cachedAvailability = fallbackSecret.isPresent();
    return fallbackSecret;
  }

  @Override
  public boolean containsSecret(String key) {
    if (!"openai-api-key".equals(key)) {
      return false;
    }
    Boolean cached = cachedAvailability;
    return cached != null ? cached : SecretStore.super.containsSecret(key);
  }

  @Override
  public SecretOperationResult storeSecret(String key, char[] value) {
    if (!"openai-api-key".equals(key)
        || value == null
        || value.length < 20
        || value.length > MAXIMUM_SECRET_BYTES
        || !Files.isRegularFile(secretTool)
        || !Files.isExecutable(secretTool)) {
      if (value != null) {
        Arrays.fill(value, '\0');
      }
      return SecretOperationResult.failure(
          "Secret Service ist nicht verfügbar oder der Schlüssel ist ungültig.");
    }
    Process process = null;
    try {
      process =
          new ProcessBuilder(
                  java.util.List.of(
                      secretTool.toString(),
                      "store",
                      "--label=CachyOS Control Center – OpenAI",
                      "application",
                      "cachyos-control-center",
                      "key",
                      "openai-api-key"))
              .redirectError(ProcessBuilder.Redirect.DISCARD)
              .start();
      try (var writer =
          new java.io.OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8)) {
        writer.write(value);
        writer.write(System.lineSeparator());
      }
      if (!process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
        process.destroyForcibly();
        return SecretOperationResult.failure("KWallet/Secret Service antwortet nicht.");
      }
      if (process.exitValue() == 0) {
        cachedAvailability = true;
        return SecretOperationResult.success("API-Schlüssel wurde sicher gespeichert.");
      }
      return SecretOperationResult.failure("KWallet/Secret Service hat das Speichern abgelehnt.");
    } catch (IOException exception) {
      return SecretOperationResult.failure("API-Schlüssel konnte nicht gespeichert werden.");
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      return SecretOperationResult.failure("Speichern wurde abgebrochen.");
    } finally {
      Arrays.fill(value, '\0');
      if (process != null && process.isAlive()) {
        process.destroyForcibly();
      }
    }
  }

  @Override
  public SecretOperationResult deleteSecret(String key) {
    if (!"openai-api-key".equals(key)
        || !Files.isRegularFile(secretTool)
        || !Files.isExecutable(secretTool)) {
      return SecretOperationResult.failure("Secret Service ist nicht verfügbar.");
    }
    Process process = null;
    try {
      process =
          new ProcessBuilder(
                  java.util.List.of(
                      secretTool.toString(),
                      "clear",
                      "application",
                      "cachyos-control-center",
                      "key",
                      "openai-api-key"))
              .redirectError(ProcessBuilder.Redirect.DISCARD)
              .start();
      if (!process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
        process.destroyForcibly();
        return SecretOperationResult.failure("KWallet/Secret Service antwortet nicht.");
      }
      if (process.exitValue() == 0) {
        cachedAvailability = fallback.containsSecret(key);
        return SecretOperationResult.success("API-Schlüssel wurde gelöscht.");
      }
      return SecretOperationResult.failure("Es war kein gespeicherter API-Schlüssel vorhanden.");
    } catch (IOException exception) {
      return SecretOperationResult.failure("API-Schlüssel konnte nicht gelöscht werden.");
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      return SecretOperationResult.failure("Löschen wurde abgebrochen.");
    } finally {
      if (process != null && process.isAlive()) {
        process.destroyForcibly();
      }
    }
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
