package org.cachyos.controlcenter;

import org.cachyos.controlcenter.ai.api.AiProvider;
import org.cachyos.controlcenter.ai.provider.AiConfiguration;
import org.cachyos.controlcenter.core.action.ActionDispatcher;
import org.cachyos.controlcenter.core.audit.InMemoryAuditLog;
import org.cachyos.controlcenter.core.module.ModuleRegistry;
import org.cachyos.controlcenter.input.intent.GermanIntentRouter;
import org.cachyos.controlcenter.input.voice.MicrophoneCatalog;
import org.cachyos.controlcenter.input.voice.SpeechModelManager;
import org.cachyos.controlcenter.input.voice.SpeechToTextEngine;
import org.cachyos.controlcenter.modules.applications.ApplicationManagerModule;
import org.cachyos.controlcenter.modules.audio.AudioEvents;
import org.cachyos.controlcenter.modules.audio.AudioManagerModule;
import org.cachyos.controlcenter.modules.network.NetworkEvents;
import org.cachyos.controlcenter.modules.network.NetworkManagerModule;
import org.cachyos.controlcenter.systeminfo.DashboardMonitor;
import org.cachyos.controlcenter.systeminfo.PlatformInfo;
import org.cachyos.controlcenter.systeminfo.SystemSnapshot;

/** Explicit container for application-wide services and trust boundaries. */
public record AppContext(
    PlatformInfo platformInfo,
    SystemSnapshot systemSnapshot,
    DashboardMonitor dashboardMonitor,
    NetworkManagerModule networkManager,
    NetworkEvents networkEvents,
    AudioManagerModule audioManager,
    AudioEvents audioEvents,
    ApplicationManagerModule applicationManager,
    GermanIntentRouter intentRouter,
    MicrophoneCatalog microphoneCatalog,
    SpeechModelManager speechModelManager,
    SpeechToTextEngine speechToTextEngine,
    AiProvider aiProvider,
    AiConfiguration aiConfiguration,
    LifecycleManager lifecycleManager,
    ActionDispatcher actionDispatcher,
    InMemoryAuditLog auditLog,
    ModuleRegistry moduleRegistry) {}
