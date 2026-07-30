package org.cachyos.controlcenter.input.intent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.cachyos.controlcenter.core.action.ActionId;
import org.junit.jupiter.api.Test;

class GermanIntentRouterTest {
  private final GermanIntentRouter router =
      new GermanIntentRouter(
          () ->
              List.of(
                  new RegisteredApplication("0123456789abcdef", "KDE Connect"),
                  new RegisteredApplication("fedcba9876543210", "KDE Connect SMS"),
                  new RegisteredApplication("1111111111111111", "GIMP")));

  @Test
  void normalizesGermanTextAndPunctuation() {
    assertEquals("oeffne uebersicht", GermanIntentRouter.normalize("  ÖFFNE, Übersicht! "));
  }

  @Test
  void routesFixedOfflineCommand() {
    IntentResult result = router.route("Bitte nicht: irrelevant");
    assertEquals(IntentKind.UNKNOWN, result.kind());

    result = router.route("Öffne Firefox");
    assertEquals(IntentKind.ACTION, result.kind());
    assertEquals(ActionId.OPEN_FIREFOX, result.actionId().orElseThrow());
    assertEquals(Map.of(), result.parameters());
  }

  @Test
  void marksLockAsRequiringConfirmation() {
    IntentResult result = router.route("Sperre den Bildschirm");
    assertEquals(IntentKind.ACTION, result.kind());
    assertTrue(result.confirmationRequired());
  }

  @Test
  void routesNavigationWithoutAction() {
    IntentResult result = router.route("Zeige Netzwerk");
    assertEquals(IntentKind.NAVIGATION, result.kind());
    assertEquals("network", result.navigationTarget().orElseThrow());
    assertTrue(result.actionId().isEmpty());
  }

  @Test
  void launchesOnlyExactlyCataloguedApplicationId() {
    IntentResult result = router.route("Starte GIMP");
    assertEquals(IntentKind.ACTION, result.kind());
    assertEquals(ActionId.APPLICATION_LAUNCH, result.actionId().orElseThrow());
    assertEquals(Map.of("applicationId", "1111111111111111"), result.parameters());
  }

  @Test
  void ambiguousApplicationDoesNotProduceAction() {
    IntentResult result = router.route("Öffne KDE");
    assertEquals(IntentKind.AMBIGUOUS, result.kind());
    assertTrue(result.actionId().isEmpty());
  }

  @Test
  void questionNeverProducesAction() {
    IntentResult result = router.route("Warum ist mein WLAN langsam?");
    assertEquals(IntentKind.QUESTION, result.kind());
    assertTrue(result.actionId().isEmpty());
  }

  @Test
  void shellTextIsNotACommand() {
    IntentResult result = router.route("rm -rf /");
    assertEquals(IntentKind.UNKNOWN, result.kind());
    assertTrue(result.actionId().isEmpty());
  }
}
