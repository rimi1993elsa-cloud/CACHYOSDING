package org.cachyos.controlcenter.modules.services;

public record ServiceUnit(
    String name,
    ServiceScope scope,
    String loadState,
    String activeState,
    String subState,
    String description) {}
