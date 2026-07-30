package org.cachyos.controlcenter.platform.services;

import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.cachyos.controlcenter.modules.services.ServiceBackend;
import org.cachyos.controlcenter.modules.services.ServiceManager;
import org.cachyos.controlcenter.modules.services.ServiceScope;
import org.cachyos.controlcenter.modules.services.ServiceState;
import org.cachyos.controlcenter.modules.services.ServiceUnit;
import org.cachyos.controlcenter.platform.status.FixedCommandReader;

public final class LinuxServiceBackend implements ServiceBackend {
  private static final Duration TIMEOUT = Duration.ofSeconds(15);
  private final boolean linux;

  public LinuxServiceBackend(boolean linux) {
    this.linux = linux;
  }

  @Override
  public ServiceState inspect() {
    if (!linux) {
      return new ServiceState(false, List.of(), "systemd ist auf dieser Plattform nicht verfügbar");
    }
    List<ServiceUnit> units = new ArrayList<>();
    units.addAll(read(ServiceScope.SYSTEM));
    units.addAll(read(ServiceScope.USER));
    return new ServiceState(true, units, "System- und Benutzerdienste sind getrennt");
  }

  @Override
  public List<String> logs(ServiceScope scope, String unitName) {
    if (!linux || !ServiceManager.valid(unitName)) {
      return List.of("Logs nicht verfügbar");
    }
    List<String> arguments = new ArrayList<>();
    arguments.add(scope == ServiceScope.USER ? "--user-unit" : "--unit");
    arguments.add(unitName);
    arguments.add("--lines=200");
    arguments.add("--no-pager");
    arguments.add("--output=short-iso");
    return FixedCommandReader.read(Path.of("/usr/bin/journalctl"), arguments, TIMEOUT)
        .orElse(List.of("Keine lesbaren Logs"));
  }

  static List<ServiceUnit> parse(List<String> lines, ServiceScope scope) {
    return lines.stream()
        .map(String::trim)
        .filter(line -> !line.isBlank())
        .map(line -> line.split("\\s+", 5))
        .filter(parts -> parts.length >= 4 && ServiceManager.valid(parts[0]))
        .map(
            parts ->
                new ServiceUnit(
                    parts[0],
                    scope,
                    parts[1],
                    parts[2],
                    parts[3],
                    parts.length > 4 ? parts[4] : ""))
        .toList();
  }

  private List<ServiceUnit> read(ServiceScope scope) {
    List<String> arguments = new ArrayList<>();
    if (scope == ServiceScope.USER) {
      arguments.add("--user");
    }
    arguments.addAll(
        List.of("list-units", "--all", "--type=service", "--no-legend", "--plain", "--no-pager"));
    return FixedCommandReader.read(Path.of("/usr/bin/systemctl"), arguments, TIMEOUT)
        .map(lines -> parse(lines, scope))
        .orElse(List.of());
  }
}
