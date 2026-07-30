package org.cachyos.controlcenter.modules.storage;

import java.nio.file.Path;
import java.util.List;

public interface StorageBackend {
  StorageSnapshot inspect();

  List<LargeFile> findLargeFiles(Path root);
}
