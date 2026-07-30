package org.cachyos.controlcenter.modules.audio;

/** Event stream emitted by the audio server without polling. */
public interface AudioEvents extends AutoCloseable {
  void subscribe(Runnable listener);

  @Override
  void close();
}
