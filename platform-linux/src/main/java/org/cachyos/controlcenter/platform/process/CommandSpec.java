package org.cachyos.controlcenter.platform.process;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Fixed executable plus a structured argument list. No shell string is accepted. */
public record CommandSpec(Path executable, List<String> arguments) {
  public CommandSpec {
    Objects.requireNonNull(executable, "executable");
    if (!executable.isAbsolute()) {
      throw new IllegalArgumentException("Executable must be an absolute path");
    }
    arguments = List.copyOf(Objects.requireNonNull(arguments, "arguments"));
    for (String argument : arguments) {
      if (argument == null || argument.indexOf('\0') >= 0) {
        throw new IllegalArgumentException("Invalid process argument");
      }
    }
  }

  public List<String> commandLine() {
    List<String> command = new ArrayList<>(arguments.size() + 1);
    command.add(executable.normalize().toString());
    command.addAll(arguments);
    return List.copyOf(command);
  }
}
