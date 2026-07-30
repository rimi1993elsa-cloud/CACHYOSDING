package org.cachyos.controlcenter.helper;

import java.util.logging.Logger;

final class StructuredAuditSink implements HelperAuditSink {
  private static final Logger LOGGER = Logger.getLogger(StructuredAuditSink.class.getName());

  @Override
  public void record(String sender, HelperAction action, String outcome) {
    String safeSender = HelperValidation.sender(sender) ? sender : "invalid";
    LOGGER.info(
        () ->
            "event=privileged-action sender="
                + safeSender
                + " action="
                + action.name()
                + " outcome="
                + outcome);
  }
}
