package org.cachyos.controlcenter.modules.audio;

/** PipeWire/PulseAudio port with typed operations only. */
public interface AudioBackend {
  AudioSnapshot readSnapshot();

  AudioOperationResult setOutputVolume(String deviceName, int percent);

  AudioOperationResult setInputVolume(String deviceName, int percent);

  AudioOperationResult setOutputMute(String deviceName, boolean muted);

  AudioOperationResult setInputMute(String deviceName, boolean muted);

  AudioOperationResult setDefaultOutput(String deviceName);

  AudioOperationResult setDefaultInput(String deviceName);

  AudioOperationResult playTestTone();
}
