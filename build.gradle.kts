val ktor_version: String by project
val koin_version: String = "3.4.0"

plugins {
    kotlin("jvm") version "1.8.20"
    id("io.ktor.plugin") version "2.3.0"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.20"
}

group = "kkhouse.com"
version = "0.0.1"
application {
    mainClass.set("io.ktor.server.netty.EngineMain")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    /*
    TODO VersionCatalog移行
     */
    implementation(project(":application"))
    implementation(project(":domain:model"))
    implementation(project(":domain:adapters"))
    implementation(project(":infrastructure:repository"))
    implementation(project(":infrastructure:network"))
    implementation(project(":infrastructure:database"))
    implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-netty-jvm:$ktor_version")
    implementation("io.insert-koin:koin-core:$koin_version")
}