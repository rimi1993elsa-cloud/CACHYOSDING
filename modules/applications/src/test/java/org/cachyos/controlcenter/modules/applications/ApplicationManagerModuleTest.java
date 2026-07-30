package org.cachyos.controlcenter.modules.applications;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.cachyos.controlcenter.core.action.ActionId;
import org.cachyos.controlcenter.core.action.ActionRequest;
import org.cachyos.controlcenter.core.action.InputSource;
import org.junit.jupiter.api.Test;

class ApplicationManagerModuleTest {
  private final RecordingBackend backend = new RecordingBackend();
  private final ApplicationManagerModule module = new ApplicationManagerModule(backend);

  @Test
  void launchesOnlyValidatedCatalogId() {
    String id = "0123456789abcdef";
    module.execute(request(Map.of("applicationId", id)));
    assertEquals(id, backend.launchedId);
  }

  @Test
  void rejectsPathAndShellSyntaxAsId() {
    assertThrows(
        IllegalArgumentException.class,
        () -> module.execute(request(Map.of("applicationId", "../../bin/sh"))));
  }

  private static ActionRequest request(Map<String, String> parameters) {
    return new ActionRequest(
        ActionId.APPLICATION_LAUNCH, InputSource.BUTTON, parameters, Instant.now());
  }

  private static final class RecordingBackend implements ApplicationBackend {
    private String launchedId;

    @Override
    public List<ApplicationEntry> loadApplications() {
      return List.of();
    }

    @Override
    public ApplicationOperationResult launch(String applicationId) {
      launchedId = applicationId;
      return ApplicationOperationResult.success("ok");
    }

    @Override
    public Optional<String> findPackage(String applicationId) {
      return Optional.empty();
    }
  }
}
