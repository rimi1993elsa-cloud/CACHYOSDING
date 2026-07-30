plugins {
    id("org.openjfx.javafxplugin") version "0.1.0"
}

dependencies {
    api(project(":core"))
    api(project(":system-info"))
    api(project(":modules:network"))
    api(project(":modules:audio"))
    api(project(":modules:applications"))
    api(project(":modules:diagnostics"))
    api(project(":modules:packages"))
    api(project(":modules:security"))
    api(project(":modules:hardware"))
    api(project(":modules:storage"))
    api(project(":modules:snapshots"))
    api(project(":modules:services"))
    api(project(":modules:processes"))
    api(project(":input"))
    api(project(":ai"))
    testImplementation("org.testfx:testfx-junit5:4.0.18")
    testImplementation("org.hamcrest:hamcrest:3.0")
}

javafx {
    version = "21.0.12"
    modules = listOf("javafx.controls")
}
