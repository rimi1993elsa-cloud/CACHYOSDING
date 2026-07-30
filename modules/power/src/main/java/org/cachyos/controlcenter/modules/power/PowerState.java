package org.cachyos.controlcenter.modules.power;

import java.util.List;

public record PowerState(
    boolean available,
    boolean batteryPresent,
    int batteryPercent,
    String batteryStatus,
    List<PowerProfile> profiles,
    boolean canSuspend,
    boolean canHibernate,
    String message) {}
