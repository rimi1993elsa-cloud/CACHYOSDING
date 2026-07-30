package org.cachyos.controlcenter.modules.storage;

public record MountEntry(String source, String target, String fileSystem, String options) {}
