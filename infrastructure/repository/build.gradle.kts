val koin_version: String = "3.4.0"

plugins {
    kotlin("jvm") version "1.8.20"
}

group = "kkhouse.com"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":domain:model"))
    implementation(project(":domain:adapters"))
    implementation(project(":infrastructure:network"))
    implementation(project(":infrastructure:database"))
    implementation("io.insert-koin:koin-core:$koin_version")
}