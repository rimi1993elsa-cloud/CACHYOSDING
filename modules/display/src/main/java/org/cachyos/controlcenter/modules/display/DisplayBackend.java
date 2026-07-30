package org.cachyos.controlcenter.modules.display;

public interface DisplayBackend {
  DisplayState inspect();

  DisplayResult setBrightness(int percent);

  DisplayResult setNightMode(boolean enabled);
}
