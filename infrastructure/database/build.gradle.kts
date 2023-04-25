val koin_version: String = "3.4.0"
plugins {
    kotlin("jvm") version "1.8.20"
}

group = "com.kkhouse.englishconversationapp"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":domain:model"))
    implementation("io.insert-koin:koin-core:$koin_version")
}