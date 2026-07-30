package org.cachyos.controlcenter.modules.boot;

public interface BootBackend {
  BootSnapshot inspect();

  BootResult launchKernelManager();
}
