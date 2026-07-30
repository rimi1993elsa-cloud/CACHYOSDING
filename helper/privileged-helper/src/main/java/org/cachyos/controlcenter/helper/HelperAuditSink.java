package org.cachyos.controlcenter.helper;

@FunctionalInterface
interface HelperAuditSink {
  void record(String sender, HelperAction action, String outcome);
}
