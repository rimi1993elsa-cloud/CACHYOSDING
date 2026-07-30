package org.cachyos.controlcenter.helper.api;

import java.util.Objects;

public record HelperReply(HelperErrorCode code, String message) {
  private static final String SEPARATOR = "|";

  public HelperReply {
    Objects.requireNonNull(code, "code");
    message = Objects.requireNonNullElse(message, "");
    if (message.indexOf('|') >= 0 || message.indexOf('\n') >= 0 || message.indexOf('\r') >= 0) {
      throw new IllegalArgumentException("Message contains a protocol separator");
    }
  }

  public boolean successful() {
    return code == HelperErrorCode.OK;
  }

  public String encode() {
    return code.name() + SEPARATOR + message;
  }

  public static HelperReply decode(String encoded) {
    Objects.requireNonNull(encoded, "encoded");
    String[] parts = encoded.split("\\|", 2);
    if (parts.length != 2) {
      throw new IllegalArgumentException("Malformed helper reply");
    }
    return new HelperReply(HelperErrorCode.valueOf(parts[0]), parts[1]);
  }
}
