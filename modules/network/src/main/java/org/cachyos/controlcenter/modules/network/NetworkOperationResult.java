package org.cachyos.controlcenter.modules.network;

/** Secret-free result returned by the NetworkManager boundary. */
public record NetworkOperationResult(boolean success, boolean available, String message) {
  public static NetworkOperationResult success(String message) {
    return new NetworkOperationResult(true, true, message);
  }

  public static NetworkOperationResult failed(String message) {
    return new NetworkOperationResult(false, true, message);
  }

  public static NetworkOperationResult unavailable(String message) {
    return new NetworkOperationResult(false, false, message);
  }
}
