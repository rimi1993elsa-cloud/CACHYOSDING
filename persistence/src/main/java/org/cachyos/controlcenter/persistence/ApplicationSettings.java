package org.cachyos.controlcenter.persistence;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/** Exportable settings only. Secrets are deliberately absent from this schema. */
public record ApplicationSettings(
    Set<String> enabledModules,
    List<String> quickButtons,
    boolean microphoneEnabled,
    String microphoneId,
    boolean onlineAiEnabled,
    String aiProvider,
    String aiModel,
    int monthlyBudgetCents,
    boolean shareDocumentation,
    boolean shareDiagnostics,
    boolean shareHardware,
    boolean shareSystemContext,
    boolean storeChatHistory) {
  private static final Set<String> MODULES =
      Set.of(
          "system",
          "network",
          "audio",
          "applications",
          "packages",
          "security",
          "hardware",
          "storage",
          "snapshots",
          "services",
          "processes",
          "display",
          "power",
          "boot",
          "diagnostics",
          "voice",
          "ai");
  private static final Set<String> BUTTONS =
      Set.of("firefox", "file-manager", "terminal", "lock-screen");
  private static final Set<String> PROVIDERS = Set.of("openai", "offline");
  private static final Set<String> AI_MODELS =
      Set.of("gpt-5.6-sol", "gpt-5.6-terra", "gpt-5.6-luna");
  private static final Pattern DEVICE = Pattern.compile("[A-Za-z0-9 _.:/-]{0,160}");

  public ApplicationSettings {
    enabledModules = Set.copyOf(enabledModules == null ? Set.of() : enabledModules);
    quickButtons = List.copyOf(quickButtons == null ? List.of() : quickButtons);
    microphoneId = microphoneId == null ? "" : microphoneId.strip();
    aiProvider = aiProvider == null ? "offline" : aiProvider.strip();
    aiModel = aiModel == null ? "gpt-5.6-sol" : aiModel.strip();
    if (!MODULES.containsAll(enabledModules)
        || !BUTTONS.containsAll(quickButtons)
        || quickButtons.size() > BUTTONS.size()
        || !DEVICE.matcher(microphoneId).matches()
        || !PROVIDERS.contains(aiProvider)
        || !AI_MODELS.contains(aiModel)
        || monthlyBudgetCents < 0
        || monthlyBudgetCents > 100_000) {
      throw new IllegalArgumentException("Ungültige oder unbegrenzte Einstellung");
    }
  }

  /** Backward-compatible constructor for version-1 settings call sites and imports. */
  public ApplicationSettings(
      Set<String> enabledModules,
      List<String> quickButtons,
      boolean microphoneEnabled,
      String microphoneId,
      boolean onlineAiEnabled,
      String aiProvider,
      int monthlyBudgetCents,
      boolean shareDocumentation,
      boolean shareDiagnostics,
      boolean shareHardware,
      boolean shareSystemContext,
      boolean storeChatHistory) {
    this(
        enabledModules,
        quickButtons,
        microphoneEnabled,
        microphoneId,
        onlineAiEnabled,
        aiProvider,
        "gpt-5.6-sol",
        monthlyBudgetCents,
        shareDocumentation,
        shareDiagnostics,
        shareHardware,
        shareSystemContext,
        storeChatHistory);
  }

  public static ApplicationSettings defaults() {
    return new ApplicationSettings(
        MODULES,
        List.of("firefox", "file-manager", "terminal", "lock-screen"),
        false,
        "",
        true,
        "openai",
        "gpt-5.6-sol",
        500,
        true,
        false,
        false,
        false,
        false);
  }
}
