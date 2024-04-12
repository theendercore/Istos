plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.serialization)
    alias(libs.plugins.detekt)
}

group = "com.theendercore"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("dev.masecla:Modrinth4J:2.2.0")
    implementation("androidx.sqlite:sqlite-ktx:2.0.0")

    implementation(libs.arrow.core)
    implementation(libs.arrow.optics)
    implementation(libs.arrow.fx.coroutines)

    implementation(libs.semver)

    implementation(libs.log4j.api)
    implementation(libs.log4j.core)
    implementation(libs.log4j.slf4j)

    implementation(libs.kotlinx.serialization)
    implementation(libs.tomlkt)

    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}