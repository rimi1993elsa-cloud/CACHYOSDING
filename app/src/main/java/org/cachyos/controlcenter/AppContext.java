package org.cachyos.controlcenter;

import org.cachyos.controlcenter.core.action.ActionDispatcher;
import org.cachyos.controlcenter.core.audit.InMemoryAuditLog;
import org.cachyos.controlcenter.core.module.ModuleRegistry;
import org.cachyos.controlcenter.systeminfo.PlatformInfo;

/** Explicit container for application-wide services and trust boundaries. */
public record AppContext(
    PlatformInfo platformInfo,
    LifecycleManager lifecycleManager,
    ActionDispatcher actionDispatcher,
    InMemoryAuditLog auditLog,
    ModuleRegistry moduleRegistry) {}
