package org.cachyos.controlcenter;

import org.cachyos.controlcenter.core.action.ActionDispatcher;
import org.cachyos.controlcenter.core.audit.InMemoryAuditLog;
import org.cachyos.controlcenter.core.module.ModuleRegistry;
import org.cachyos.controlcenter.systeminfo.DashboardMonitor;
import org.cachyos.controlcenter.systeminfo.PlatformInfo;
import org.cachyos.controlcenter.systeminfo.SystemSnapshot;

/** Explicit container for application-wide services and trust boundaries. */
public record AppContext(
    PlatformInfo platformInfo,
    SystemSnapshot systemSnapshot,
    DashboardMonitor dashboardMonitor,
    LifecycleManager lifecycleManager,
    ActionDispatcher actionDispatcher,
    InMemoryAuditLog auditLog,
    ModuleRegistry moduleRegistry) {}
