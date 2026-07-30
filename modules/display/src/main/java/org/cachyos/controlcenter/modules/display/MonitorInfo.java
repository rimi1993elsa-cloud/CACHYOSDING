package org.cachyos.controlcenter.modules.display;

public record MonitorInfo(
    String name, boolean enabled, boolean primary, String mode, double scale) {}
