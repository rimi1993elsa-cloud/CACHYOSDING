package org.cachyos.controlcenter.modules.applications;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
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

/** Application catalog with session favorites and ID-only launching. */
public final class ApplicationManagerModule implements ManagerModule {
  private static final ModuleId MODULE_ID = new ModuleId("applications");
  private static final Pattern ID = Pattern.compile("[a-f0-9]{16}");
  private final ApplicationBackend backend;
  private final Set<String> favorites = new HashSet<>();

  public ApplicationManagerModule(ApplicationBackend backend) {
    this.backend = backend;
  }

  public synchronized List<ApplicationEntry> applications() {
    List<ApplicationEntry> applications = new ArrayList<>();
    for (ApplicationEntry entry : backend.loadApplications()) {
      applications.add(entry.withFavorite(favorites.contains(entry.id())));
    }
    applications.sort(
        Comparator.comparing(ApplicationEntry::favorite)
            .reversed()
            .thenComparing(ApplicationEntry::name, String.CASE_INSENSITIVE_ORDER));
    return List.copyOf(applications);
  }

  public synchronized void setFavorite(String id, boolean favorite) {
    requireId(id);
    if (favorite) {
      favorites.add(id);
    } else {
      favorites.remove(id);
    }
  }

  public java.util.Optional<String> findPackage(String id) {
    requireId(id);
    return backend.findPackage(id);
  }

  @Override
  public ModuleId id() {
    return MODULE_ID;
  }

  @Override
  public String displayName() {
    return "Anwendungen";
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
    return Set.of(ActionId.APPLICATION_LAUNCH);
  }

  @Override
  public ActionResult execute(ActionRequest request) {
    if (!supports(request.actionId())) {
      return ActionResult.rejected("Unbekannte Anwendungsaktion.", "Unsupported action id");
    }
    String id = requireOnly(request.parameters());
    ApplicationOperationResult result = backend.launch(id);
    if (result.success()) {
      return ActionResult.success(result.message());
    }
    return result.available()
        ? ActionResult.failed(result.message(), "Application launch failed")
        : ActionResult.unavailable(result.message());
  }

  private static String requireOnly(Map<String, String> parameters) {
    if (parameters.size() != 1 || !parameters.containsKey("applicationId")) {
      throw new IllegalArgumentException("Invalid application parameters");
    }
    return requireId(parameters.get("applicationId"));
  }

  private static String requireId(String id) {
    if (id == null || !ID.matcher(id).matches()) {
      throw new IllegalArgumentException("Invalid application id");
    }
    return id;
  }
}
