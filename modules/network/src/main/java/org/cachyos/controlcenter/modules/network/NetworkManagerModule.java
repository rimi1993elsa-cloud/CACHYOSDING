package org.cachyos.controlcenter.modules.network;

import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.cachyos.controlcenter.core.action.ActionId;
import org.cachyos.controlcenter.core.action.ActionRequest;
import org.cachyos.controlcenter.core.action.ActionResult;
import org.cachyos.controlcenter.core.module.ManagerModule;
import org.cachyos.controlcenter.core.module.ModuleCapabilities;
import org.cachyos.controlcenter.core.module.ModuleId;
import org.cachyos.controlcenter.core.module.ModuleSnapshot;
import org.cachyos.controlcenter.core.validation.ActionValidators;

/** Validates all dynamic identifiers before crossing into NetworkManager. */
public final class NetworkManagerModule implements ManagerModule {
  private static final ModuleId MODULE_ID = new ModuleId("network");
  private static final Pattern UUID =
      Pattern.compile(
          "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");
  private static final Pattern DEVICE = Pattern.compile("[A-Za-z0-9_.:-]{1,32}");
  private static final Set<ActionId> ACTIONS =
      Set.of(
          ActionId.NETWORK_SCAN_WIFI,
          ActionId.NETWORK_WIFI_ON,
          ActionId.NETWORK_WIFI_OFF,
          ActionId.NETWORK_ACTIVATE_PROFILE,
          ActionId.NETWORK_DISCONNECT_DEVICE);

  private final NetworkBackend backend;

  public NetworkManagerModule(NetworkBackend backend) {
    this.backend = backend;
  }

  public NetworkSnapshot networkSnapshot() {
    return backend.readSnapshot();
  }

  @Override
  public ModuleId id() {
    return MODULE_ID;
  }

  @Override
  public String displayName() {
    return "Netzwerk";
  }

  @Override
  public ModuleCapabilities capabilities() {
    return new ModuleCapabilities(true, true, false);
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
      return ActionResult.rejected("Unbekannte Netzwerkaktion.", "Unsupported action id");
    }
    NetworkOperationResult result;
    if (request.actionId().equals(ActionId.NETWORK_SCAN_WIFI)) {
      ActionValidators.requireNoParameters(request.parameters());
      result = backend.scanWifi();
    } else if (request.actionId().equals(ActionId.NETWORK_WIFI_ON)) {
      ActionValidators.requireNoParameters(request.parameters());
      result = backend.setWifiEnabled(true);
    } else if (request.actionId().equals(ActionId.NETWORK_WIFI_OFF)) {
      ActionValidators.requireNoParameters(request.parameters());
      result = backend.setWifiEnabled(false);
    } else if (request.actionId().equals(ActionId.NETWORK_ACTIVATE_PROFILE)) {
      result = backend.activateProfile(requireOnly(request.parameters(), "profileUuid", UUID));
    } else {
      result = backend.disconnectDevice(requireOnly(request.parameters(), "deviceName", DEVICE));
    }
    if (result.success()) {
      return ActionResult.success(result.message());
    }
    return result.available()
        ? ActionResult.failed(result.message(), "NetworkManager operation failed")
        : ActionResult.unavailable(result.message());
  }

  private static String requireOnly(Map<String, String> parameters, String key, Pattern pattern) {
    if (parameters.size() != 1 || !parameters.containsKey(key)) {
      throw new IllegalArgumentException("Invalid network parameters");
    }
    String value = parameters.get(key);
    if (value == null || !pattern.matcher(value).matches()) {
      throw new IllegalArgumentException("Invalid network identifier");
    }
    return value;
  }
}
