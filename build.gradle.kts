import com.diffplug.gradle.spotless.SpotlessExtension

plugins {
    base
    id("com.diffplug.spotless") version "8.9.0" apply false
}

group = "org.cachyos.controlcenter"
version = "0.1.0-SNAPSHOT"

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

