package org.cachyos.controlcenter.modules.processes;

public record ProcessEntry(
    long pid,
    String command,
    String user,
    long cpuMillis,
    long residentBytes,
    int priority,
    boolean critical) {}
