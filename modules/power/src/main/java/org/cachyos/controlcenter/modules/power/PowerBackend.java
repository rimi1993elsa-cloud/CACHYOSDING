package org.cachyos.controlcenter.modules.power;

public interface PowerBackend {
  PowerState inspect();

  PowerResult setProfile(String profile);

  PowerResult suspend();

  PowerResult hibernate();
}
