package org.cachyos.controlcenter.helper.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class HelperReplyTest {
  @Test
  void roundTripsConstrainedWireReply() {
    HelperReply reply = new HelperReply(HelperErrorCode.OK, "fertig");
    assertEquals(reply, HelperReply.decode(reply.encode()));
  }

  @Test
  void rejectsProtocolInjection() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new HelperReply(HelperErrorCode.OK, "ok|INTERNAL_ERROR"));
    assertThrows(IllegalArgumentException.class, () -> HelperReply.decode("not-a-reply"));
  }
}
