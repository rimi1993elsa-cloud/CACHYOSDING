package org.cachyos.controlcenter.input.intent;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import org.cachyos.controlcenter.core.action.ActionId;

/** Deterministic offline German intent router. It emits only registered identifiers. */
public final class GermanIntentRouter {
  private static final Pattern NON_WORD = Pattern.compile("[^a-z0-9]+");
  private static final Set<String> QUESTION_WORDS =
      Set.of("warum", "wie", "was", "wann", "wo", "welche", "welcher", "welches", "wer", "wieso");

  private final IntentApplicationCatalog applications;
  private final Map<String, ActionRule> actions = new LinkedHashMap<>();
  private final Map<String, String> navigation = new LinkedHashMap<>();

  public GermanIntentRouter(IntentApplicationCatalog applications) {
    this.applications = Objects.requireNonNull(applications, "applications");
    registerAction(
        ActionId.OPEN_FIREFOX,
        false,
        "Firefox öffnen",
        "oeffne firefox",
        "starte firefox",
        "firefox oeffnen");
    registerAction(
        ActionId.OPEN_FILE_MANAGER,
        false,
        "Dateimanager öffnen",
        "oeffne dateimanager",
        "starte dateimanager",
        "dateien oeffnen");
    registerAction(
        ActionId.OPEN_TERMINAL,
        false,
        "Terminal öffnen",
        "oeffne terminal",
        "starte terminal",
        "terminal oeffnen");
    registerAction(
        ActionId.LOCK_SCREEN,
        true,
        "Bildschirm sperren",
        "bildschirm sperren",
        "sperre den bildschirm",
        "rechner sperren");
    registerAction(
        ActionId.NETWORK_SCAN_WIFI,
        false,
        "WLANs neu suchen",
        "wlan suchen",
        "wlans suchen",
        "wlan scannen",
        "netzwerke suchen");
    registerAction(
        ActionId.AUDIO_TEST_TONE,
        false,
        "Testton abspielen",
        "testton abspielen",
        "spiele testton",
        "ton testen");

    registerNavigation("overview", "uebersicht", "zeige uebersicht", "zur uebersicht");
    registerNavigation("system", "system", "zeige system", "systeminformationen");
    registerNavigation("network", "netzwerk", "zeige netzwerk", "netzwerk oeffnen");
    registerNavigation("audio", "audio", "zeige audio", "audio oeffnen");
    registerNavigation("applications", "programme", "zeige programme", "programme oeffnen");
    registerNavigation("voice", "sprache", "zeige sprache", "spracheingabe");
    registerNavigation("settings", "einstellungen", "zeige einstellungen");
  }

  public IntentResult route(String input) {
    String normalized = normalize(input);
    if (normalized.isBlank()) {
      return IntentResult.passive(IntentKind.UNKNOWN, 0, "Bitte gib einen Text ein.");
    }

    ActionRule direct = actions.get(normalized);
    if (direct != null) {
      return IntentResult.action(
          direct.id(), Map.of(), 1, direct.confirmationRequired(), direct.label());
    }

    String target = navigation.get(normalized);
    if (target != null) {
      return IntentResult.navigation(target, 1);
    }

    IntentResult application = routeApplication(normalized);
    if (application != null) {
      return application;
    }

    String firstWord = normalized.split(" ", 2)[0];
    if (QUESTION_WORDS.contains(firstWord) || input != null && input.strip().endsWith("?")) {
      return IntentResult.passive(
          IntentKind.QUESTION, 0.9, "Diese Eingabe ist eine Frage für den optionalen KI-Chat.");
    }
    return IntentResult.passive(IntentKind.UNKNOWN, 0, "Kein eindeutiger lokaler Befehl erkannt.");
  }

  public static String normalize(String input) {
    String value = Objects.requireNonNullElse(input, "").toLowerCase(Locale.GERMAN).strip();
    value = value.replace("ä", "ae").replace("ö", "oe").replace("ü", "ue").replace("ß", "ss");
    value = Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
    return NON_WORD.matcher(value).replaceAll(" ").strip().replaceAll("\\s+", " ");
  }

  private IntentResult routeApplication(String normalized) {
    String requested = applicationName(normalized);
    if (requested == null || requested.isBlank()) {
      return null;
    }
    List<RegisteredApplication> exact = new ArrayList<>();
    List<RegisteredApplication> partial = new ArrayList<>();
    for (RegisteredApplication application : applications.applications()) {
      String name = normalize(application.name());
      if (name.equals(requested)) {
        exact.add(application);
      } else if (name.startsWith(requested) || requested.startsWith(name)) {
        partial.add(application);
      }
    }
    List<RegisteredApplication> candidates = exact.isEmpty() ? partial : exact;
    if (candidates.size() == 1) {
      RegisteredApplication match = candidates.getFirst();
      return IntentResult.action(
          ActionId.APPLICATION_LAUNCH,
          Map.of("applicationId", match.id()),
          exact.isEmpty() ? 0.8 : 1,
          false,
          match.name() + " öffnen");
    }
    if (candidates.size() > 1) {
      return IntentResult.passive(
          IntentKind.AMBIGUOUS,
          0.5,
          "Mehrere registrierte Anwendungen passen. Bitte gib den vollständigen Namen ein.");
    }
    return IntentResult.passive(
        IntentKind.UNKNOWN, 0, "Keine sicher katalogisierte Anwendung mit diesem Namen gefunden.");
  }

  private static String applicationName(String normalized) {
    for (String prefix : List.of("oeffne ", "starte ")) {
      if (normalized.startsWith(prefix)) {
        return normalized.substring(prefix.length()).strip();
      }
    }
    return null;
  }

  private void registerAction(
      ActionId id, boolean confirmationRequired, String label, String... phrases) {
    for (String phrase : phrases) {
      actions.put(normalize(phrase), new ActionRule(id, confirmationRequired, label));
    }
  }

  private void registerNavigation(String target, String... phrases) {
    for (String phrase : phrases) {
      navigation.put(normalize(phrase), target);
    }
  }

  private record ActionRule(ActionId id, boolean confirmationRequired, String label) {}
}
