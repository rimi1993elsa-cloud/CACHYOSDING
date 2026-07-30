package org.cachyos.controlcenter.modules.applications;

import java.util.List;
import java.util.Optional;

/** XDG desktop catalog and launcher port. */
public interface ApplicationBackend {
  List<ApplicationEntry> loadApplications();

  ApplicationOperationResult launch(String applicationId);

  Optional<String> findPackage(String applicationId);
}
