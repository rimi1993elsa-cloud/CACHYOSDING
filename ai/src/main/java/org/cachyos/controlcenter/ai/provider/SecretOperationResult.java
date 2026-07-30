package org.cachyos.controlcenter.ai.provider;

/** User-facing result of a desktop secret-service operation. */
public record SecretOperationResult(boolean success, String message) {
  public SecretOperationResult {
    message = message == null ? "" : message.strip();
  }

  public static SecretOperationResult success(String message) {
    return new SecretOperationResult(true, message);
  }

  public static SecretOperationResult failure(String message) {
    return new SecretOperationResult(false, message);
  }
}
