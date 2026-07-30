package org.cachyos.controlcenter;

import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns lifecycle state and later background resources. */
public final class LifecycleManager implements AutoCloseable {
  private static final Logger LOGGER = LoggerFactory.getLogger(LifecycleManager.class);
  private final AtomicBoolean closed = new AtomicBoolean();

  public void applicationStarted() {
    LOGGER.info("Application started without elevated privileges");
  }

  @Override
  public void close() {
    if (closed.compareAndSet(false, true)) {
      LOGGER.info("Application stopped");
    }
  }
}
