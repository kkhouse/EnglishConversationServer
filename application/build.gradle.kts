val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val koin_version: String = "3.4.0"

plugins {
    kotlin("jvm") version "1.8.20"
    id("io.ktor.plugin") version "2.3.0"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.20"
}

group = "kkhouse.com"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    /*
    TODO VersionCatalog移行
     */
    implementation(project(":domain:model"))
    implementation(project(":domain:adapters"))
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-netty-jvm:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    testImplementation("io.ktor:ktor-server-tests-jvm:$ktor_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
    implementation("io.insert-koin:koin-core:$koin_version")

    implementation("io.github.microutils:kotlin-logging:1.7.4")
    implementation("org.slf4j:slf4j-simple:1.7.26")
}