package org.cachyos.controlcenter.modules.network;

/** Narrow NetworkManager port implemented by the Linux adapter. */
public interface NetworkBackend {
  NetworkSnapshot readSnapshot();

  NetworkOperationResult scanWifi();

  NetworkOperationResult setWifiEnabled(boolean enabled);

  NetworkOperationResult activateProfile(String profileUuid);

  NetworkOperationResult disconnectDevice(String deviceName);
}
