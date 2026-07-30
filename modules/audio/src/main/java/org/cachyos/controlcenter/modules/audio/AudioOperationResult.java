package org.cachyos.controlcenter.modules.audio;

/** Result from the narrow local audio boundary. */
public record AudioOperationResult(boolean success, boolean available, String message) {
  public static AudioOperationResult success(String message) {
    return new AudioOperationResult(true, true, message);
  }

  public static AudioOperationResult failed(String message) {
    return new AudioOperationResult(false, true, message);
  }

  public static AudioOperationResult unavailable(String message) {
    return new AudioOperationResult(false, false, message);
  }
}
