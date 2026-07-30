package org.cachyos.controlcenter.ui.navigation;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

/** Routes only to registered local pages; unknown or unfinished pages are rejected. */
public final class ContentRouter {
  private final Map<NavigationId, Node> pages;
  private final StackPane view = new StackPane();

  public ContentRouter(Map<NavigationId, Node> pages) {
    Objects.requireNonNull(pages, "pages");
    this.pages = new EnumMap<>(pages);
  }

  public void navigate(NavigationId id) {
    Node page = pages.get(Objects.requireNonNull(id, "id"));
    if (page == null) {
      throw new IllegalArgumentException("No page registered for " + id);
    }
    view.getChildren().setAll(page);
  }

  public StackPane view() {
    return view;
  }
}
