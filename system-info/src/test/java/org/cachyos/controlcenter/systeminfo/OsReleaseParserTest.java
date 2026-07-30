package org.cachyos.controlcenter.systeminfo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Map;
import org.junit.jupiter.api.Test;

class OsReleaseParserTest {
  @Test
  void parsesQuotedValuesAndIgnoresMalformedLines() {
    Map<String, String> values =
        OsReleaseParser.parse(
            """
            # comment
            NAME="CachyOS"
            ID=cachyos
            PRETTY_NAME="CachyOS Linux"
            invalid-key=value
            no-separator
            """);

    assertEquals("CachyOS", values.get("NAME"));
    assertEquals("cachyos", values.get("ID"));
    assertEquals("CachyOS Linux", values.get("PRETTY_NAME"));
    assertFalse(values.containsKey("invalid-key"));
  }
}
