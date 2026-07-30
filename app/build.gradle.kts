plugins {
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
}

dependencies {
    implementation(project(":core"))
    implementation(project(":ui"))
    implementation(project(":system-info"))
    implementation(project(":platform-linux"))
    implementation(project(":modules:network"))
    implementation(project(":modules:audio"))
    implementation(project(":modules:applications"))
    implementation(project(":modules:diagnostics"))
    implementation(project(":modules:packages"))
    implementation(project(":modules:security"))
    implementation(project(":modules:hardware"))
    implementation(project(":input"))
    implementation(project(":ai"))
    implementation("org.slf4j:slf4j-api:2.0.18")
    runtimeOnly("ch.qos.logback:logback-classic:1.5.38")
}

javafx {
    version = "21.0.12"
    modules = listOf("javafx.controls")
}

application {
    mainClass = "org.cachyos.controlcenter.Main"
}
