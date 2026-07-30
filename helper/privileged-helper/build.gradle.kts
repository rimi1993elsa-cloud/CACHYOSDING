plugins {
    application
}

dependencies {
    implementation(project(":helper:helper-api"))
    runtimeOnly("com.github.hypfvieh:dbus-java-transport-jnr-unixsocket:5.2.0")
}

application {
    mainClass = "org.cachyos.controlcenter.helper.HelperMain"
}
