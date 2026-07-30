package org.cachyos.controlcenter.helper;

@FunctionalInterface
interface HelperAuthorizer {
  boolean authorize(String sender, HelperAction action);
}
