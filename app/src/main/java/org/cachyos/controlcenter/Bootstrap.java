package org.cachyos.controlcenter;

import org.cachyos.controlcenter.systeminfo.PlatformDetector;

/** Creates the unprivileged application context. */
public final class Bootstrap {
  private Bootstrap() {}

  public static AppContext createContext() {
    return new AppContext(PlatformDetector.detect(), new LifecycleManager());
  }
}
