package org.cachyos.controlcenter.ui.dashboard;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import org.cachyos.controlcenter.core.action.ActionDispatcher;
import org.cachyos.controlcenter.core.audit.ActionAuditEvent;
import org.cachyos.controlcenter.core.audit.InMemoryAuditLog;
import org.cachyos.controlcenter.persistence.SettingsService;
import org.cachyos.controlcenter.systeminfo.DashboardMetrics;
import org.cachyos.controlcenter.systeminfo.DashboardMonitor;
import org.cachyos.controlcenter.ui.components.QuickActionBar;
import org.cachyos.controlcenter.ui.notifications.NotificationCenter;

/** Live, low-frequency system dashboard backed only by local read-only data. */
public final class DashboardView extends VBox {
  private static final DateTimeFormatter TIME_FORMAT =
      DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());

  private final Label cpuValue = new Label();
  private final ProgressBar cpuProgress = new ProgressBar();
  private final Label memoryValue = new Label();
  private final ProgressBar memoryProgress = new ProgressBar();
  private final Label storageValue = new Label();
  private final ProgressBar storageProgress = new ProgressBar();
  private final Label networkValue = new Label();
  private final Label batteryValue = new Label();
  private final Label updateValue = new Label();
  private final Label serviceValue = new Label();
  private final Label refreshedAt = new Label();
  private final ListView<String> warnings = new ListView<>();
  private final ListView<String> recentActions = new ListView<>();
  private final InMemoryAuditLog auditLog;

  public DashboardView(
      DashboardMonitor monitor,
      InMemoryAuditLog auditLog,
      ActionDispatcher actionDispatcher,
      NotificationCenter notifications,
      SettingsService settings) {
    this.auditLog = auditLog;
    FlowPane cards = new FlowPane(14, 14);
    cards
        .getChildren()
        .addAll(
            metricCard("CPU", cpuValue, cpuProgress),
            metricCard("Arbeitsspeicher", memoryValue, memoryProgress),
            metricCard("Systemspeicher", storageValue, storageProgress),
            metricCard("Netzwerk", networkValue, null),
            metricCard("Akku", batteryValue, null),
            metricCard("Updates", updateValue, null),
            metricCard("Dienste", serviceValue, null));

    warnings.setId("dashboard-warnings");
    warnings.setPlaceholder(new Label("Keine aktuellen Warnungen"));
    warnings.setPrefHeight(110);
    recentActions.setId("dashboard-recent-actions");
    recentActions.setPlaceholder(new Label("Noch keine lokale Aktion ausgeführt"));
    recentActions.setPrefHeight(120);
    refreshedAt.getStyleClass().add("muted-label");

    setSpacing(18);
    getChildren()
        .addAll(
            cards,
            new Label("Schnellaktionen"),
            new QuickActionBar(
                actionDispatcher, notifications, Set.copyOf(settings.current().quickButtons())),
            refreshedAt,
            section("Warnungen", warnings),
            section("Letzte Aktionen", recentActions));
    update(monitor.latest());
    monitor.subscribe(metrics -> Platform.runLater(() -> update(metrics)));
  }

  private void update(DashboardMetrics metrics) {
    setPercentage(cpuValue, cpuProgress, metrics.cpuLoad());
    long usedMemory = Math.max(0, metrics.totalMemoryBytes() - metrics.freeMemoryBytes());
    setUsage(memoryValue, memoryProgress, usedMemory, metrics.totalMemoryBytes());
    long usedStorage = Math.max(0, metrics.totalStorageBytes() - metrics.freeStorageBytes());
    setUsage(storageValue, storageProgress, usedStorage, metrics.totalStorageBytes());
    networkValue.setText(metrics.online() ? "Verbunden" : "Offline");
    batteryValue.setText(
        metrics.battery().present() && metrics.battery().percentage() >= 0
            ? metrics.battery().percentage() + " %"
            : "Nicht verfügbar");
    updateValue.setText(
        metrics.availableUpdates().isPresent()
            ? Integer.toString(metrics.availableUpdates().getAsInt())
            : "Nicht ermittelbar");
    serviceValue.setText(
        metrics.failedServices().isPresent()
            ? metrics.failedServices().getAsInt() + " fehlgeschlagen"
            : "Nicht ermittelbar");
    warnings.getItems().setAll(metrics.warnings());
    recentActions.getItems().setAll(formatActions(auditLog.events()));
    refreshedAt.setText("Zuletzt aktualisiert: " + TIME_FORMAT.format(metrics.capturedAt()));
  }

  private static VBox metricCard(String title, Label value, ProgressBar progress) {
    Label heading = new Label(title);
    heading.getStyleClass().add("card-title");
    value.getStyleClass().add("card-value");
    VBox card =
        progress == null ? new VBox(10, heading, value) : new VBox(10, heading, value, progress);
    card.setPadding(new Insets(16));
    card.setPrefWidth(210);
    card.setMinHeight(105);
    card.getStyleClass().add("status-card");
    return card;
  }

  private static VBox section(String title, ListView<String> list) {
    Label heading = new Label(title);
    heading.getStyleClass().add("card-title");
    VBox section = new VBox(8, heading, list);
    section.getStyleClass().add("details-panel");
    return section;
  }

  private static void setPercentage(Label value, ProgressBar progress, double ratio) {
    if (ratio < 0) {
      value.setText("Nicht verfügbar");
      progress.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
      return;
    }
    value.setText(String.format(Locale.GERMAN, "%.0f %%", ratio * 100));
    progress.setProgress(ratio);
  }

  private static void setUsage(Label value, ProgressBar progress, long used, long total) {
    if (total <= 0) {
      value.setText("Nicht verfügbar");
      progress.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
      return;
    }
    double ratio = Math.min(1, (double) used / total);
    value.setText(formatBytes(used) + " / " + formatBytes(total));
    progress.setProgress(ratio);
  }

  private static List<String> formatActions(List<ActionAuditEvent> events) {
    return events.stream()
        .skip(Math.max(0, events.size() - 5L))
        .map(
            event ->
                TIME_FORMAT.format(event.timestamp())
                    + " · "
                    + event.actionId().value()
                    + " · "
                    + event.result())
        .toList()
        .reversed();
  }

  private static String formatBytes(long bytes) {
    double gibibytes = bytes / (1024.0 * 1024.0 * 1024.0);
    return String.format(Locale.GERMAN, "%.1f GiB", gibibytes);
  }
}
