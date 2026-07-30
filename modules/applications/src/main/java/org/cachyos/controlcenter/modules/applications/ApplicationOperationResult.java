package org.cachyos.controlcenter.modules.applications;

/** Result from the desktop application boundary. */
public record ApplicationOperationResult(boolean success, boolean available, String message) {
  public static ApplicationOperationResult success(String message) {
    return new ApplicationOperationResult(true, true, message);
  }

  public static ApplicationOperationResult failed(String message) {
    return new ApplicationOperationResult(false, true, message);
  }

  public static ApplicationOperationResult unavailable(String message) {
    return new ApplicationOperationResult(false, false, message);
  }
}
