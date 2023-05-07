val ktor_version: String by project
val koin_version: String = "3.4.0"
val openai: String = "3.2.0"

plugins {
    kotlin("jvm") version "1.8.20"
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
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    implementation("com.google.cloud:google-cloud-speech:4.9.0")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktor_version")
    implementation("io.insert-koin:koin-core:$koin_version")
    implementation("io.arrow-kt:arrow-core:1.2.0-RC")
    implementation("io.arrow-kt:arrow-fx-coroutines:1.2.0-RC")
    implementation("commons-codec:commons-codec:1.15")
    implementation(platform("com.google.cloud:libraries-bom:26.13.0"))
    implementation("com.google.cloud:google-cloud-storage")
    implementation("com.aallam.openai:openai-client:$openai")

    implementation("io.github.microutils:kotlin-logging:1.7.4")
    implementation("org.slf4j:slf4j-simple:1.7.26")
}