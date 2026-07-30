package org.cachyos.controlcenter.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.cachyos.controlcenter.ai.api.AiProvider;
import org.cachyos.controlcenter.ai.api.AiRequest;
import org.cachyos.controlcenter.ai.api.AiStreamEvent;
import org.cachyos.controlcenter.ai.knowledge.KnowledgeCache;
import org.cachyos.controlcenter.ai.knowledge.KnowledgeService;
import org.cachyos.controlcenter.ai.provider.AiConfiguration;
import org.cachyos.controlcenter.core.action.ActionId;
import org.cachyos.controlcenter.core.action.ActionRequest;
import org.cachyos.controlcenter.core.action.ActionResult;
import org.cachyos.controlcenter.core.action.InputSource;
import org.cachyos.controlcenter.core.audit.InMemoryAuditLog;
import org.cachyos.controlcenter.input.intent.GermanIntentRouter;
import org.cachyos.controlcenter.input.voice.MicrophoneCatalog;
import org.cachyos.controlcenter.input.voice.MicrophoneDescriptor;
import org.cachyos.controlcenter.input.voice.SpeechModelManager;
import org.cachyos.controlcenter.input.voice.SpeechToTextEngine;
import org.cachyos.controlcenter.input.voice.TranscriptEvent;
import org.cachyos.controlcenter.modules.applications.ApplicationBackend;
import org.cachyos.controlcenter.modules.applications.ApplicationEntry;
import org.cachyos.controlcenter.modules.applications.ApplicationManagerModule;
import org.cachyos.controlcenter.modules.applications.ApplicationOperationResult;
import org.cachyos.controlcenter.modules.audio.AudioBackend;
import org.cachyos.controlcenter.modules.audio.AudioEvents;
import org.cachyos.controlcenter.modules.audio.AudioManagerModule;
import org.cachyos.controlcenter.modules.audio.AudioOperationResult;
import org.cachyos.controlcenter.modules.audio.AudioSnapshot;
import org.cachyos.controlcenter.modules.network.NetworkBackend;
import org.cachyos.controlcenter.modules.network.NetworkEvents;
import org.cachyos.controlcenter.modules.network.NetworkManagerModule;
import org.cachyos.controlcenter.modules.network.NetworkOperationResult;
import org.cachyos.controlcenter.modules.network.NetworkSnapshot;
import org.cachyos.controlcenter.systeminfo.DashboardDataSource;
import org.cachyos.controlcenter.systeminfo.DashboardMonitor;
import org.cachyos.controlcenter.systeminfo.OperatingSystemFamily;
import org.cachyos.controlcenter.systeminfo.PlatformInfo;
import org.cachyos.controlcenter.systeminfo.SupplementalStatus;
import org.cachyos.controlcenter.systeminfo.SystemSnapshotDetector;
import org.cachyos.controlcenter.ui.navigation.NavigationEntry;
import org.cachyos.controlcenter.ui.navigation.NavigationId;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

class ApplicationShellTest extends ApplicationTest {
  private final AtomicReference<ActionRequest> dispatched = new AtomicReference<>();
  private DashboardMonitor dashboardMonitor;
  private KnowledgeService knowledgeService;

  @Override
  public void start(Stage stage) {
    PlatformInfo platformInfo =
        new PlatformInfo(
            OperatingSystemFamily.LINUX, "CachyOS", "rolling", "x86_64", "KDE", "wayland");
    var systemSnapshot = SystemSnapshotDetector.detect(platformInfo);
    dashboardMonitor =
        new DashboardMonitor(
            new DashboardDataSource(platformInfo, ignored -> SupplementalStatus.unavailable()),
            DashboardDataSource.initial(systemSnapshot),
            Duration.ofMinutes(1));
    knowledgeService =
        new KnowledgeService(
            java.util.List.of(),
            new KnowledgeCache(
                java.nio.file.Path.of(System.getProperty("java.io.tmpdir"))
                    .resolve("cachyos-control-center-ui-test")),
            source -> {
              throw new AssertionError("No source registered");
            });
    MainView view =
        new MainView(
            platformInfo,
            systemSnapshot,
            dashboardMonitor,
            new InMemoryAuditLog(),
            new NetworkManagerModule(new UnavailableNetworkBackend()),
            new NetworkEvents() {
              @Override
              public void subscribe(Runnable listener) {}

              @Override
              public void close() {}
            },
            new AudioManagerModule(new UnavailableAudioBackend()),
            new AudioEvents() {
              @Override
              public void subscribe(Runnable listener) {}

              @Override
              public void close() {}
            },
            new ApplicationManagerModule(new EmptyApplicationBackend()),
            new GermanIntentRouter(java.util.List::of),
            new MicrophoneCatalog(),
            new SpeechModelManager(
                java.nio.file.Path.of(System.getProperty("user.home")).toAbsolutePath()),
            new NoopSpeechEngine(),
            new UnavailableAiProvider(),
            AiConfiguration.defaults(),
            knowledgeService,
            request -> {
              dispatched.set(request);
              return CompletableFuture.completedFuture(ActionResult.success("Test"));
            });
    Scene scene = new Scene(view.root(), 1100, 720);
    view.install(scene);
    stage.setScene(scene);
    stage.show();
  }

  @Override
  public void stop() {
    dashboardMonitor.close();
    knowledgeService.close();
  }

  @Test
  void navigationChangesRegisteredContent() {
    clickOn("System");

    Label systemHeading = lookup(".page-title").queryAs(Label.class);
    assertEquals("System", systemHeading.getText());

    ListView<NavigationEntry> navigation = lookup("#primary-navigation").queryListView();
    interact(
        () ->
            navigation
                .getSelectionModel()
                .select(
                    navigation.getItems().stream()
                        .filter(entry -> entry.id() == NavigationId.SETTINGS)
                        .findFirst()
                        .orElseThrow()));

    Label settingsHeading = lookup(".page-title").queryAs(Label.class);
    assertEquals("Einstellungen", settingsHeading.getText());
  }

  @Test
  void opensImplementedNetworkManagerPage() {
    clickOn("Netzwerk");

    Label heading = lookup(".page-title").queryAs(Label.class);
    assertEquals("Netzwerk", heading.getText());
    lookup("#network-devices").queryListView();
  }

  @Test
  void opensImplementedAudioManagerPage() {
    clickOn("Audio");

    Label heading = lookup(".page-title").queryAs(Label.class);
    assertEquals("Audio", heading.getText());
    lookup("#audio-outputs").queryListView();
  }

  @Test
  void opensImplementedApplicationManagerPage() {
    clickOn("Programme");

    Label heading = lookup(".page-title").queryAs(Label.class);
    assertEquals("Programme", heading.getText());
    lookup("#application-list").queryListView();
  }

  @Test
  void opensPushToTalkPageWithoutModel() {
    clickOn("Sprache");

    Label heading = lookup(".page-title").queryAs(Label.class);
    assertEquals("Sprache", heading.getText());
    Button pushToTalk = lookup("#push-to-talk").queryButton();
    assertEquals(true, pushToTalk.isDisabled());
  }

  @Test
  void quickButtonDispatchesItsFixedActionId() {
    Button firefox = lookup("#action-open-firefox").queryButton();
    interact(firefox::fire);

    assertEquals(ActionId.OPEN_FIREFOX, dispatched.get().actionId());
    assertEquals(InputSource.BUTTON, dispatched.get().source());
    assertEquals(Map.of(), dispatched.get().parameters());
  }

  @Test
  void textCommandUsesTheTypedLocalActionPath() {
    TextField command = lookup("#command-field").queryAs(TextField.class);
    clickOn(command).write("Öffne Firefox");
    press(javafx.scene.input.KeyCode.ENTER).release(javafx.scene.input.KeyCode.ENTER);

    assertEquals(ActionId.OPEN_FIREFOX, dispatched.get().actionId());
    assertEquals(InputSource.TEXT, dispatched.get().source());
  }

  @Test
  void unknownTextDoesNotDispatch() {
    TextField command = lookup("#command-field").queryAs(TextField.class);
    clickOn(command).write("rm -rf /");
    press(javafx.scene.input.KeyCode.ENTER).release(javafx.scene.input.KeyCode.ENTER);

    assertEquals(null, dispatched.get());
  }

  @Test
  void questionMovesToChatDraftWithoutDispatching() {
    TextField command = lookup("#command-field").queryAs(TextField.class);
    clickOn(command).write("Warum ist mein WLAN langsam?");
    press(javafx.scene.input.KeyCode.ENTER).release(javafx.scene.input.KeyCode.ENTER);

    Label heading = lookup(".page-title").queryAs(Label.class);
    assertEquals("KI-Assistent", heading.getText());
    javafx.scene.control.TextArea question =
        lookup("#chat-question").queryAs(javafx.scene.control.TextArea.class);
    assertEquals("Warum ist mein WLAN langsam?", question.getText());
    assertEquals(null, dispatched.get());
  }

  private static final class UnavailableNetworkBackend implements NetworkBackend {
    @Override
    public NetworkSnapshot readSnapshot() {
      return NetworkSnapshot.unavailable("Test");
    }

    @Override
    public NetworkOperationResult scanWifi() {
      return NetworkOperationResult.unavailable("Test");
    }

    @Override
    public NetworkOperationResult setWifiEnabled(boolean enabled) {
      return NetworkOperationResult.unavailable("Test");
    }

    @Override
    public NetworkOperationResult activateProfile(String profileUuid) {
      return NetworkOperationResult.unavailable("Test");
    }

    @Override
    public NetworkOperationResult disconnectDevice(String deviceName) {
      return NetworkOperationResult.unavailable("Test");
    }
  }

  private static final class UnavailableAudioBackend implements AudioBackend {
    @Override
    public AudioSnapshot readSnapshot() {
      return AudioSnapshot.unavailable("Test");
    }

    @Override
    public AudioOperationResult setOutputVolume(String deviceName, int percent) {
      return AudioOperationResult.unavailable("Test");
    }

    @Override
    public AudioOperationResult setInputVolume(String deviceName, int percent) {
      return AudioOperationResult.unavailable("Test");
    }

    @Override
    public AudioOperationResult setOutputMute(String deviceName, boolean muted) {
      return AudioOperationResult.unavailable("Test");
    }

    @Override
    public AudioOperationResult setInputMute(String deviceName, boolean muted) {
      return AudioOperationResult.unavailable("Test");
    }

    @Override
    public AudioOperationResult setDefaultOutput(String deviceName) {
      return AudioOperationResult.unavailable("Test");
    }

    @Override
    public AudioOperationResult setDefaultInput(String deviceName) {
      return AudioOperationResult.unavailable("Test");
    }

    @Override
    public AudioOperationResult playTestTone() {
      return AudioOperationResult.unavailable("Test");
    }
  }

  private static final class EmptyApplicationBackend implements ApplicationBackend {
    @Override
    public java.util.List<ApplicationEntry> loadApplications() {
      return java.util.List.of();
    }

    @Override
    public ApplicationOperationResult launch(String applicationId) {
      return ApplicationOperationResult.unavailable("Test");
    }

    @Override
    public java.util.Optional<String> findPackage(String applicationId) {
      return java.util.Optional.empty();
    }
  }

  private static final class NoopSpeechEngine implements SpeechToTextEngine {
    @Override
    public void start(
        java.nio.file.Path modelDirectory,
        MicrophoneDescriptor microphone,
        java.util.function.Consumer<TranscriptEvent> listener) {}

    @Override
    public void stop() {}

    @Override
    public boolean recording() {
      return false;
    }

    @Override
    public void close() {}
  }

  private static final class UnavailableAiProvider implements AiProvider {
    @Override
    public boolean available() {
      return false;
    }

    @Override
    public String availabilityMessage() {
      return "Test: kein API-Schlüssel";
    }

    @Override
    public java.util.concurrent.CompletionStage<Void> stream(
        AiRequest request, Consumer<AiStreamEvent> listener) {
      return CompletableFuture.failedFuture(new IllegalStateException("Unavailable"));
    }

    @Override
    public void cancel() {}

    @Override
    public void close() {}
  }
}
