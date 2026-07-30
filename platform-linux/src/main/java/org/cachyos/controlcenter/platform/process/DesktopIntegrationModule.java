package org.cachyos.controlcenter.platform.process;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import org.cachyos.controlcenter.core.action.ActionId;
import org.cachyos.controlcenter.core.action.ActionRequest;
import org.cachyos.controlcenter.core.action.ActionResult;
import org.cachyos.controlcenter.core.module.ManagerModule;
import org.cachyos.controlcenter.core.module.ModuleCapabilities;
import org.cachyos.controlcenter.core.module.ModuleId;
import org.cachyos.controlcenter.core.module.ModuleSnapshot;
import org.cachyos.controlcenter.core.validation.ActionValidators;
import org.cachyos.controlcenter.systeminfo.OperatingSystemFamily;

/** Four safe unprivileged desktop actions backed by fixed commands. */
public final class DesktopIntegrationModule implements ManagerModule {
  private static final ModuleId MODULE_ID = new ModuleId("desktop-integration");
  private static final Set<ActionId> ACTIONS =
      Set.of(
          ActionId.OPEN_FIREFOX,
          ActionId.OPEN_FILE_MANAGER,
          ActionId.OPEN_TERMINAL,
          ActionId.LOCK_SCREEN);

  private final DesktopCommandResolver commandResolver;
  private final ProcessStarter processStarter;

  public DesktopIntegrationModule(
      DesktopCommandResolver commandResolver, ProcessStarter processStarter) {
    this.commandResolver = commandResolver;
    this.processStarter = processStarter;
  }

  public static DesktopIntegrationModule createDefault(
      OperatingSystemFamily operatingSystemFamily) {
    boolean windows = operatingSystemFamily == OperatingSystemFamily.WINDOWS;
    Path home = Path.of(System.getProperty("user.home")).toAbsolutePath().normalize();
    Map<String, String> environment = System.getenv();
    ExecutableLookup lookup = new PathExecutableLookup(environment, windows);
    return new DesktopIntegrationModule(
        new DesktopCommandResolver(operatingSystemFamily, home, environment, lookup),
        new JvmProcessStarter());
  }

  @Override
  public ModuleId id() {
    return MODULE_ID;
  }

  @Override
  public String displayName() {
    return "Desktop-Integration";
  }

  @Override
  public ModuleCapabilities capabilities() {
    return new ModuleCapabilities(false, true, false);
  }

  @Override
  public ModuleSnapshot loadSnapshot() {
    return ModuleSnapshot.empty();
  }

  @Override
  public Set<ActionId> actions() {
    return ACTIONS;
  }

  @Override
  public ActionResult execute(ActionRequest request) {
    if (!supports(request.actionId())) {
      return ActionResult.rejected(
          "Diese Aktion wird vom Desktop-Modul nicht unterstützt.", "Unsupported action id");
    }
    ActionValidators.requireNoParameters(request.parameters());
    CommandSpec command = commandResolver.resolve(request.actionId()).orElse(null);
    if (command == null) {
      return ActionResult.unavailable("Das benötigte Desktop-Programm ist nicht verfügbar.");
    }
    try {
      processStarter.start(command);
      return ActionResult.success(successMessage(request.actionId()));
    } catch (IOException exception) {
      return ActionResult.failed(
          "Das Desktop-Programm konnte nicht gestartet werden.",
          exception.getClass().getSimpleName());
    }
  }

  private static String successMessage(ActionId actionId) {
    if (actionId.equals(ActionId.LOCK_SCREEN)) {
      return "Bildschirmsperre wurde angefordert.";
    }
    return "Desktop-Programm wurde gestartet.";
  }
}
