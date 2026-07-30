package org.cachyos.controlcenter.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import org.cachyos.controlcenter.core.action.ActionId;
import org.cachyos.controlcenter.core.action.ActionRequest;
import org.cachyos.controlcenter.core.action.ActionResult;
import org.cachyos.controlcenter.core.action.InputSource;
import org.cachyos.controlcenter.core.audit.InMemoryAuditLog;
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
  void quickButtonDispatchesItsFixedActionId() {
    Button firefox = lookup("#action-open-firefox").queryButton();
    interact(firefox::fire);

    assertEquals(ActionId.OPEN_FIREFOX, dispatched.get().actionId());
    assertEquals(InputSource.BUTTON, dispatched.get().source());
    assertEquals(Map.of(), dispatched.get().parameters());
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
}
