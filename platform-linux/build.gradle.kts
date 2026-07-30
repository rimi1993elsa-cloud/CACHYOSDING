dependencies {
    api(project(":core"))
    api(project(":system-info"))
    implementation(project(":modules:network"))
    implementation(project(":modules:audio"))
    implementation(project(":modules:applications"))
    implementation("com.fasterxml.jackson.core:jackson-databind:2.22.0")
}
