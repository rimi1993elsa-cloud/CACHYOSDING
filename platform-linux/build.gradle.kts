dependencies {
    api(project(":core"))
    api(project(":system-info"))
    implementation(project(":ai"))
    implementation(project(":modules:network"))
    implementation(project(":modules:audio"))
    implementation(project(":modules:applications"))
    implementation(project(":modules:diagnostics"))
    implementation("com.fasterxml.jackson.core:jackson-databind:2.22.0")
}
