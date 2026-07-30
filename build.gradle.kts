import com.diffplug.gradle.spotless.SpotlessExtension

plugins {
    base
    id("com.diffplug.spotless") version "8.9.0" apply false
}

group = "org.cachyos.controlcenter"
version = "1.2.0"

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "checkstyle")
    apply(plugin = "com.diffplug.spotless")

    group = rootProject.group
    version = rootProject.version

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
        withSourcesJar()
    }

    dependencies {
        "testImplementation"(platform("org.junit:junit-bom:5.14.4"))
        "testImplementation"("org.junit.jupiter:junit-jupiter")
        "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
    }

    extensions.configure<CheckstyleExtension> {
        toolVersion = "13.9.0"
        configFile = rootProject.file("config/checkstyle/checkstyle.xml")
        isShowViolations = true
    }

    extensions.configure<SpotlessExtension> {
        java {
            googleJavaFormat()
            formatAnnotations()
            removeUnusedImports()
            trimTrailingWhitespace()
            endWithNewline()
        }
        kotlinGradle {
            ktlint()
            trimTrailingWhitespace()
            endWithNewline()
        }
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release = 21
        options.compilerArgs.addAll(listOf("-Xlint:all", "-Werror"))
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        jvmArgs("-Dfile.encoding=UTF-8")
        testLogging {
            events("passed", "skipped", "failed")
        }
    }

    tasks.named("check") {
        dependsOn("spotlessCheck")
    }
}

tasks.named("build") {
    dependsOn(subprojects.map { it.tasks.named("build") })
}

tasks.register("quality") {
    group = "verification"
    description = "Runs formatting checks, static analysis, and tests for all modules."
    dependsOn(subprojects.map { it.tasks.named("check") })
}

val verifyPackaging = tasks.register("verifyPackaging") {
    group = "verification"
    description = "Validates Phase 24 desktop, AppStream, D-Bus, Polkit and PKGBUILD assets."
    doLast {
        val required = listOf(
            "packaging/PKGBUILD",
            "packaging/.SRCINFO",
            "packaging/desktop/org.cachyos.ControlCenter.desktop",
            "packaging/appstream/org.cachyos.ControlCenter.metainfo.xml",
            "packaging/icons/org.cachyos.ControlCenter.svg",
            "packaging/dbus/org.cachyos.ControlCenter.Helper1.service",
            "packaging/dbus/org.cachyos.ControlCenter.Helper1.conf",
            "packaging/polkit/org.cachyos.controlcenter.policy",
            "scripts/install-cachyos.sh",
            "scripts/verify-linux.sh",
            "scripts/verify-installed.sh",
            "Installieren.desktop",
            "install.sh",
            "README-INSTALLATION.txt",
        ).map(::file)
        check(required.all { it.isFile }) { "Packaging asset is missing" }
        val desktop = required[2].readText()
        check("Exec=cachyos-control-center" in desktop && "Terminal=false" in desktop)
        val service = required[5].readText()
        check("User=root" in service && "/usr/lib/cachyos-control-center-helper/" in service)
        val policy = required[7].readText()
        check("<allow_any>no</allow_any>" in policy && "allow_active>yes" !in policy)
        val installScript = file("packaging/cachyos-control-center.install").readText()
        check("rm -r" !in installScript && "XDG_CONFIG_HOME" in installScript)
        val packageBuild = required[0].readText()
        check("chmod 777" !in packageBuild && "package_cachyos-control-center-helper" in packageBuild)
        check(
            "vosk-model-small-de-0.15.zip" in packageBuild &&
                "b7e53c90b1f0a38456f4cd62b366ecd58803cd97cd42b06438e2c131713d5e43" in packageBuild
        ) { "Packaged Vosk model source or checksum is missing" }
        check("pkgver=1.2.0" in packageBuild) { "PKGBUILD version drifted" }
        check(required.slice(8..10).all { it.readText().startsWith("#!/usr/bin/env bash") }) {
            "Linux helper script is not executable shell source"
        }
        val oneClickDesktop = file("Installieren.desktop").readText()
        check(
            "Terminal=true" in oneClickDesktop &&
                "Exec=/usr/bin/bash" in oneClickDesktop &&
                "install.sh" in oneClickDesktop
        ) { "One-click desktop launcher is invalid" }
        val oneClickInstaller = file("install.sh").readText()
        check(
            oneClickInstaller.startsWith("#!/usr/bin/env bash") &&
                "scripts/install-cachyos.sh" in oneClickInstaller &&
                "PIPESTATUS[0]" in oneClickInstaller
        ) { "One-click installer does not preserve the underlying exit status" }
        check(
            file("packaging/dbus/org.cachyos.ControlCenter.Helper1.service").readText() ==
                file("helper/privileged-helper/src/main/resources/dbus-1/system-services/org.cachyos.ControlCenter.Helper1.service").readText()
        ) { "D-Bus service assets drifted" }
        check(
            file("packaging/dbus/org.cachyos.ControlCenter.Helper1.conf").readText() ==
                file("helper/privileged-helper/src/main/resources/dbus-1/system.d/org.cachyos.ControlCenter.Helper1.conf").readText()
        ) { "D-Bus policy assets drifted" }
    }
}

tasks.named("quality") {
    dependsOn(verifyPackaging)
}
