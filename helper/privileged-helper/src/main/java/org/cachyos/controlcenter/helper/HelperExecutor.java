package org.cachyos.controlcenter.helper;

interface HelperExecutor {
  int installPackage(String packageName) throws Exception;

  int removePackage(String packageName) throws Exception;

  int setFirewallEnabled(boolean enabled) throws Exception;

  int controlSystemService(String unitName, String operation) throws Exception;

  int createSnapshot(String description) throws Exception;

  int deleteSnapshot(int snapshotId) throws Exception;

  int signalProcess(long processId, int signal) throws Exception;

  int setProcessPriority(long processId, int priority) throws Exception;
}
