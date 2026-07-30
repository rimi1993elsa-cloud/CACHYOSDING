package org.cachyos.controlcenter.modules.security;

public record ListeningPort(String protocol, String localAddress, int port, String process) {}
