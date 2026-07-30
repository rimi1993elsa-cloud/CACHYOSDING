package org.cachyos.controlcenter.modules.processes;

public interface ProcessGateway {
  ProcessResult signal(long pid, int signal);

  ProcessResult priority(long pid, int priority);
}
