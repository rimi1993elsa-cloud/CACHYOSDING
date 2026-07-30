package org.cachyos.controlcenter.modules.storage;

import java.nio.file.Path;

public record LargeFile(Path path, long sizeBytes) {}
