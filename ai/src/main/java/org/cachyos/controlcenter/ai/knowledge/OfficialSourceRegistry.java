package org.cachyos.controlcenter.ai.knowledge;

import java.net.URI;
import java.time.Duration;
import java.util.List;

/** Fixed registry; user input can never select an arbitrary retrieval URL. */
public final class OfficialSourceRegistry {
  private OfficialSourceRegistry() {}

  public static List<KnowledgeSource> sources() {
    Duration maximumAge = Duration.ofDays(7);
    return List.of(
        new KnowledgeSource(
            "cachyos-post-install",
            "CachyOS: Post Install",
            URI.create("https://wiki.cachyos.org/configuration/post_install_setup/"),
            maximumAge),
        new KnowledgeSource(
            "cachyos-kernel-manager",
            "CachyOS: Kernel Manager",
            URI.create("https://wiki.cachyos.org/features/kernel_manager/"),
            maximumAge),
        new KnowledgeSource(
            "cachyos-boot-managers",
            "CachyOS: Boot Managers",
            URI.create("https://wiki.cachyos.org/installation/boot_managers/"),
            maximumAge),
        new KnowledgeSource(
            "arch-pacman",
            "ArchWiki: pacman",
            URI.create("https://wiki.archlinux.org/title/Pacman"),
            maximumAge),
        new KnowledgeSource(
            "arch-systemd",
            "ArchWiki: systemd",
            URI.create("https://wiki.archlinux.org/title/Systemd"),
            maximumAge));
  }
}
