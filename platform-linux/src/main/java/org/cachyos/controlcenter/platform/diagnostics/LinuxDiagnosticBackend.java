package org.cachyos.controlcenter.platform.diagnostics;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import org.cachyos.controlcenter.modules.diagnostics.DiagnosticBackend;
import org.cachyos.controlcenter.modules.diagnostics.DiagnosticCategory;
import org.cachyos.controlcenter.modules.diagnostics.DiagnosticObservation;
import org.cachyos.controlcenter.modules.diagnostics.DiagnosticStatus;
import org.cachyos.controlcenter.platform.status.FixedCommandReader;
import org.cachyos.controlcenter.systeminfo.Capability;
import org.cachyos.controlcenter.systeminfo.CapabilityRegistry;

/** Fixed read-only Linux probes with bounded output and no shell. */
public final class LinuxDiagnosticBackend implements DiagnosticBackend {
  private final CapabilityRegistry capabilities;
  private final Path kernelCommandLine;
  private final CommandReader reader;

  public LinuxDiagnosticBackend(CapabilityRegistry capabilities) {
    this(
        capabilities,
        Path.of("/proc/cmdline"),
        (executable, arguments) ->
            FixedCommandReader.read(executable, arguments, Duration.ofSeconds(8)));
  }

  LinuxDiagnosticBackend(
      CapabilityRegistry capabilities, Path kernelCommandLine, CommandReader reader) {
    this.capabilities = capabilities;
    this.kernelCommandLine = kernelCommandLine;
    this.reader = reader;
  }

  @Override
  public DiagnosticObservation inspect(DiagnosticCategory category) {
    return switch (category) {
      case NETWORK ->
          command(
              category,
              Capability.NMCLI,
              List.of("-t", "-f", "STATE,CONNECTIVITY", "general"),
              "NetworkManager antwortet.",
              false);
      case AUDIO ->
          command(category, Capability.PACTL, List.of("info"), "PipeWire-Pulse antwortet.", false);
      case SERVICES ->
          command(
              category,
              Capability.SYSTEMCTL,
              List.of("--failed", "--no-legend", "--plain"),
              "Keine fehlgeschlagenen Systemdienste gemeldet.",
              true);
      case BOOT -> boot();
      case GRAPHICS ->
          command(
              category, Capability.LSPCI, List.of("-nnk"), "PCI-Grafikdaten sind lesbar.", false);
      case PACKAGES ->
          command(
              category,
              Capability.PACMAN,
              List.of("-Dk"),
              "Pacman-Datenbankprüfung erfolgreich.",
              false);
    };
  }

  private DiagnosticObservation command(
      DiagnosticCategory category,
      Capability capability,
      List<String> arguments,
      String success,
      boolean linesAreWarnings) {
    var status = capabilities.status(capability);
    if (status == null || !status.available()) {
      return new DiagnosticObservation(
          category,
          DiagnosticStatus.UNAVAILABLE,
          capability.displayName() + " ist nicht verfügbar.",
          status == null ? "" : status.reason());
    }
    Optional<List<String>> output = reader.read(status.executable().orElseThrow(), arguments);
    if (output.isEmpty()) {
      return new DiagnosticObservation(
          category,
          DiagnosticStatus.ERROR,
          "Die lesende Diagnoseabfrage ist fehlgeschlagen.",
          capability.displayName());
    }
    List<String> lines = output.orElseThrow();
    DiagnosticStatus resultStatus =
        linesAreWarnings && !lines.isEmpty() ? DiagnosticStatus.WARNING : DiagnosticStatus.OK;
    String summary =
        resultStatus == DiagnosticStatus.WARNING
            ? lines.size() + " fehlgeschlagene Dienstzeile(n) gefunden."
            : success;
    return new DiagnosticObservation(category, resultStatus, summary, String.join("\n", lines));
  }

  private DiagnosticObservation boot() {
    if (!Files.isRegularFile(kernelCommandLine) || Files.isSymbolicLink(kernelCommandLine)) {
      return new DiagnosticObservation(
          DiagnosticCategory.BOOT,
          DiagnosticStatus.UNAVAILABLE,
          "Kernel-Bootparameter sind nicht verfügbar.",
          "");
    }
    try {
      String value = Files.readString(kernelCommandLine);
      return new DiagnosticObservation(
          DiagnosticCategory.BOOT,
          DiagnosticStatus.OK,
          "Kernel-Bootparameter wurden lesend erfasst.",
          value);
    } catch (IOException exception) {
      return new DiagnosticObservation(
          DiagnosticCategory.BOOT,
          DiagnosticStatus.ERROR,
          "Kernel-Bootparameter konnten nicht gelesen werden.",
          "");
    }
  }

  @FunctionalInterface
  interface CommandReader {
    Optional<List<String>> read(Path executable, List<String> fixedArguments);
  }
}
