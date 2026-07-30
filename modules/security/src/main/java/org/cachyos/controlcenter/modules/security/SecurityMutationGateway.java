package org.cachyos.controlcenter.modules.security;

public interface SecurityMutationGateway {
  boolean available();

  SecurityOperationResult setFirewallEnabled(boolean enabled);
}
