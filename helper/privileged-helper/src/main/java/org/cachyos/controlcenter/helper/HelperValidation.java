package org.cachyos.controlcenter.helper;

import java.util.Set;
import java.util.regex.Pattern;

final class HelperValidation {
  private static final Pattern PACKAGE = Pattern.compile("[a-z0-9@._+\\-]{1,128}");
  private static final Pattern UNIT =
      Pattern.compile("[A-Za-z0-9:_.@\\\\\\-]{1,160}\\.(service|socket|timer|mount|target)");
  private static final Pattern SNAPSHOT_DESCRIPTION =
      Pattern.compile("[\\p{L}\\p{N} .,()_+\\-]{1,120}");
  private static final Set<String> SERVICE_OPERATIONS =
      Set.of("start", "stop", "restart", "enable", "disable");

  private HelperValidation() {}

  static boolean packageName(String value) {
    return value != null && PACKAGE.matcher(value).matches();
  }

  static boolean unitName(String value) {
    return value != null && UNIT.matcher(value).matches();
  }

  static boolean serviceOperation(String value) {
    return value != null && SERVICE_OPERATIONS.contains(value);
  }

  static boolean snapshotDescription(String value) {
    return value != null && SNAPSHOT_DESCRIPTION.matcher(value).matches();
  }

  static boolean snapshotId(int value) {
    return value > 0;
  }

  static boolean processId(long value) {
    return value > 2;
  }

  static boolean signal(int value) {
    return value == 9 || value == 15;
  }

  static boolean priority(int value) {
    return value >= -20 && value <= 19;
  }

  static boolean sender(String value) {
    return value != null && value.matches(":[0-9]+\\.[0-9]+");
  }
}
