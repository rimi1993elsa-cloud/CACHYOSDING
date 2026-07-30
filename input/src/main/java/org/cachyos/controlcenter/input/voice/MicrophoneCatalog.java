package org.cachyos.controlcenter.input.voice;

import java.util.ArrayList;
import java.util.List;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;

/** Enumerates capture-capable mixers without opening any microphone. */
public final class MicrophoneCatalog {
  public static final AudioFormat RECORDING_FORMAT = new AudioFormat(16_000, 16, 1, true, false);

  public List<MicrophoneDescriptor> availableMicrophones() {
    List<MicrophoneDescriptor> microphones = new ArrayList<>();
    Mixer.Info[] infos = AudioSystem.getMixerInfo();
    DataLine.Info target = new DataLine.Info(TargetDataLine.class, RECORDING_FORMAT);
    for (int index = 0; index < infos.length; index++) {
      Mixer mixer = AudioSystem.getMixer(infos[index]);
      if (mixer.isLineSupported(target)) {
        microphones.add(new MicrophoneDescriptor(Integer.toString(index), infos[index].getName()));
      }
    }
    return List.copyOf(microphones);
  }

  public TargetDataLine open(MicrophoneDescriptor descriptor)
      throws javax.sound.sampled.LineUnavailableException {
    int index;
    try {
      index = Integer.parseInt(descriptor.id());
    } catch (NumberFormatException exception) {
      throw new IllegalArgumentException("Invalid microphone id", exception);
    }
    Mixer.Info[] infos = AudioSystem.getMixerInfo();
    if (index < 0 || index >= infos.length) {
      throw new IllegalArgumentException("Microphone no longer available");
    }
    Mixer mixer = AudioSystem.getMixer(infos[index]);
    DataLine.Info target = new DataLine.Info(TargetDataLine.class, RECORDING_FORMAT);
    TargetDataLine line = (TargetDataLine) mixer.getLine(target);
    line.open(RECORDING_FORMAT);
    return line;
  }
}
