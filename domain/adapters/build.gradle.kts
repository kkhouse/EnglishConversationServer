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
}