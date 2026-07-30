package org.cachyos.controlcenter;

import org.cachyos.controlcenter.systeminfo.PlatformInfo;

/** Explicit container for application-wide Phase 0 services. */
public record AppContext(PlatformInfo platformInfo, LifecycleManager lifecycleManager) {}
