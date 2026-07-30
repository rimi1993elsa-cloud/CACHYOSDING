pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
    }
}

rootProject.name = "cachyos-control-center"

include(
    "app",
    "core",
    "ui",
    "input",
    "ai",
    "system-info",
    "platform-linux",
    "persistence",
    "helper:helper-api",
    "helper:privileged-helper",
    "modules:system",
    "modules:security",
    "modules:network",
    "modules:packages",
    "modules:applications",
    "modules:hardware",
    "modules:storage",
    "modules:snapshots",
    "modules:audio",
    "modules:display",
    "modules:power",
    "modules:services",
    "modules:processes",
    "modules:diagnostics",
    "modules:users",
    "modules:boot",
    "modules:browser",
)
