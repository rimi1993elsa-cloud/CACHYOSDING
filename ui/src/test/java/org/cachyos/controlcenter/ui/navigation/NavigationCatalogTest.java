package org.cachyos.controlcenter.ui.navigation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import org.junit.jupiter.api.Test;

class NavigationCatalogTest {
  @Test
  void exposesEveryRouteExactlyOnce() {
    NavigationCatalog catalog = new NavigationCatalog();

    assertEquals(NavigationId.values().length, catalog.entries().size());
    assertEquals(
        EnumSet.allOf(NavigationId.class),
        catalog.entries().stream()
            .map(NavigationEntry::id)
            .collect(() -> EnumSet.noneOf(NavigationId.class), EnumSet::add, EnumSet::addAll));
  }

  @Test
  void enablesOnlyPagesWithImplementedContent() {
    NavigationCatalog catalog = new NavigationCatalog();

    assertTrue(entry(catalog, NavigationId.OVERVIEW).enabled());
    assertTrue(entry(catalog, NavigationId.SYSTEM).enabled());
    assertTrue(entry(catalog, NavigationId.NETWORK).enabled());
    assertTrue(entry(catalog, NavigationId.AUDIO).enabled());
    assertTrue(entry(catalog, NavigationId.APPLICATIONS).enabled());
    assertTrue(entry(catalog, NavigationId.VOICE).enabled());
    assertTrue(entry(catalog, NavigationId.AI_ASSISTANT).enabled());
    assertTrue(entry(catalog, NavigationId.DIAGNOSTICS).enabled());
    assertTrue(entry(catalog, NavigationId.SETTINGS).enabled());
    assertTrue(entry(catalog, NavigationId.PACKAGES).enabled());
    assertEquals("Verfügbar", entry(catalog, NavigationId.PACKAGES).availability());
  }

  private static NavigationEntry entry(NavigationCatalog catalog, NavigationId id) {
    return catalog.entries().stream()
        .filter(candidate -> candidate.id() == id)
        .findFirst()
        .orElseThrow();
  }
}
