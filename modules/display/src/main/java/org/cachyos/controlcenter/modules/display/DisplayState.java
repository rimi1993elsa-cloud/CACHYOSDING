package org.cachyos.controlcenter.modules.display;

import java.util.List;

public record DisplayState(
    boolean available,
    boolean wayland,
    List<MonitorInfo> monitors,
    int brightnessPercent,
    boolean brightnessAdjustable,
    boolean nightMode,
    boolean nightModeAdjustable,
    GraphicsInfo graphics,
    String message) {}
