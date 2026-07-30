package org.cachyos.controlcenter.modules.packages;

public interface PackageMutationGateway {
  boolean available();

  PackageOperationResult execute(PackageAction action, String packageName);
}
