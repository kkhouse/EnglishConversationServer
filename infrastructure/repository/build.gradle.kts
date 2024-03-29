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
    /*
    TODO VersionCatalog移行
     */
    implementation(project(":domain:model"))
    implementation(project(":domain:adapters"))
    implementation(project(":infrastructure:network"))
    implementation(project(":infrastructure:database"))
    implementation("io.insert-koin:koin-core:$koin_version")

    implementation("io.github.microutils:kotlin-logging:1.7.4")
    implementation("org.slf4j:slf4j-simple:1.7.26")
}