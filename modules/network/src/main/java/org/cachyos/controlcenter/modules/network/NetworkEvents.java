package org.cachyos.controlcenter.modules.network;

/** Event stream emitted by NetworkManager without polling. */
public interface NetworkEvents extends AutoCloseable {
  void subscribe(Runnable listener);

  @Override
  void close();
}
