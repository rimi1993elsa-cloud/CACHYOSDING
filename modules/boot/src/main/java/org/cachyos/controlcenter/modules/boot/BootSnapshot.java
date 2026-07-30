package org.cachyos.controlcenter.modules.boot;

import java.util.List;

public record BootSnapshot(
    boolean available,
    String activeKernel,
    List<KernelInfo> installedKernels,
    String bootManager,
    String kernelParameters,
    String bootDuration,
    List<SlowBootUnit> slowUnits,
    boolean kernelManagerAvailable,
    String message) {}
