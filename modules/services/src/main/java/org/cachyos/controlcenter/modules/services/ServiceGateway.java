package org.cachyos.controlcenter.modules.services;

public interface ServiceGateway {
  ServiceResult execute(ServiceScope scope, String unitName, ServiceOperation operation);
}
