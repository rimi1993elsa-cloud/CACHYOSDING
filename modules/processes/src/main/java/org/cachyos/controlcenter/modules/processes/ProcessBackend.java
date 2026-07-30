package org.cachyos.controlcenter.modules.processes;

import java.util.List;

public interface ProcessBackend {
  List<ProcessEntry> inspect();
}
