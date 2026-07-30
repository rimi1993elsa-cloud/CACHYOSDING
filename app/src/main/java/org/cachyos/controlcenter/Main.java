package org.cachyos.controlcenter;

import javafx.application.Application;

/** Stable entry point used by Gradle and desktop packaging. */
public final class Main {
  private Main() {}

  public static void main(String[] args) {
    Application.launch(ControlCenterApplication.class, args);
  }
}
