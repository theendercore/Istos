plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.serialization)
    alias(libs.plugins.detekt)
    id("app.cash.sqldelight") version "2.0.2"
}


sqldelight {
    databases {
        create("Database") {
            packageName.set("com.theendercore")
        }
    }
}


group = "com.theendercore"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("dev.masecla:Modrinth4J:2.2.0")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0-RC.2")
    implementation("org.xerial:sqlite-jdbc:3.45.2.0")

    implementation("app.cash.sqldelight:sqlite-driver:2.0.2")
//    implementation("app.cash.sqldelight:jdbc-driver:2.0.2")


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