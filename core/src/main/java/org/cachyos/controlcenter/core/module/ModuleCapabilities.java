package org.cachyos.controlcenter.core.module;

/** Coarse module trust and feature capabilities. */
public record ModuleCapabilities(
    boolean readStatus, boolean localActions, boolean privilegedActions) {}
