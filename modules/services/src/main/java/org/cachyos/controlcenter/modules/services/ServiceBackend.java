package org.cachyos.controlcenter.modules.services;

import java.util.List;

public interface ServiceBackend {
  ServiceState inspect();

  List<String> logs(ServiceScope scope, String unitName);
}
