package org.cachyos.controlcenter.modules.storage;

public record StorageDevice(
    String path, String type, long sizeBytes, String fileSystem, String mountPoint, String model) {}
