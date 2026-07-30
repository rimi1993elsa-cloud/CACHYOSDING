package org.cachyos.controlcenter.input.voice;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TranscriptEventTest {
  @Test
  void normalizesNullTextWithoutChangingState() {
    TranscriptEvent event = new TranscriptEvent(TranscriptEvent.State.STOPPED, null);
    assertEquals("", event.text());
    assertEquals(TranscriptEvent.State.STOPPED, event.state());
  }
}
