package org.cachyos.controlcenter.modules.packages;

import java.util.regex.Pattern;

public final class PackageNames {
  private static final Pattern VALID = Pattern.compile("[a-z0-9@._+\\-]{1,128}");

  private PackageNames() {}

  public static boolean valid(String value) {
    return value != null && VALID.matcher(value).matches();
  }

  public static boolean validQuery(String value) {
    return value != null
        && !value.isBlank()
        && value.length() <= 80
        && value.codePoints().allMatch(PackageNames::queryCharacter);
  }

  private static boolean queryCharacter(int value) {
    return Character.isLetterOrDigit(value)
        || value == '@'
        || value == '.'
        || value == '_'
        || value == '+'
        || value == '-';
  }
}
