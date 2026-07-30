package org.cachyos.controlcenter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns lifecycle state and later background resources. */
public final class LifecycleManager implements AutoCloseable {
  private static final Logger LOGGER = LoggerFactory.getLogger(LifecycleManager.class);
  private final AtomicBoolean closed = new AtomicBoolean();
  private final CopyOnWriteArrayList<AutoCloseable> resources = new CopyOnWriteArrayList<>();

  public <T extends AutoCloseable> T manage(T resource) {
    if (closed.get()) {
      throw new IllegalStateException("Lifecycle already closed");
    }
    resources.add(resource);
    return resource;
  }

  public void applicationStarted() {
    LOGGER.info("Application started without elevated privileges");
  }

  @Override
  public void close() {
    if (closed.compareAndSet(false, true)) {
      List<AutoCloseable> closingOrder = new ArrayList<>(resources);
      Collections.reverse(closingOrder);
      for (AutoCloseable resource : closingOrder) {
        try {
          resource.close();
        } catch (Exception exception) {
          LOGGER.warn("Resource shutdown failed type={}", resource.getClass().getName(), exception);
        }
      }
      LOGGER.info("Application stopped");
    }
  }
}
