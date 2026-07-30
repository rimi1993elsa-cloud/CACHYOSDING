plugins {
    id("org.openjfx.javafxplugin") version "0.1.0"
}

dependencies {
    api(project(":system-info"))
}

javafx {
    version = "21.0.12"
    modules = listOf("javafx.controls")
}
