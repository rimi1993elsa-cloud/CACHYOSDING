plugins {
    id("org.openjfx.javafxplugin") version "0.1.0"
}

dependencies {
    api(project(":system-info"))
    testImplementation("org.testfx:testfx-junit5:4.0.18")
    testImplementation("org.hamcrest:hamcrest:3.0")
}

javafx {
    version = "21.0.12"
    modules = listOf("javafx.controls")
}
