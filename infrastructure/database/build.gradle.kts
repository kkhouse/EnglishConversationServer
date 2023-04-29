val koin_version: String = "3.4.0"
plugins {
    kotlin("jvm") version "1.8.20"
    id("app.cash.sqldelight") version "2.0.0-alpha05"
}

group = "com.kkhouse.englishconversationapp"
version = "0.0.1"

repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation(project(":domain:model"))
    implementation("io.insert-koin:koin-core:$koin_version")
    implementation("net.jthink:jaudiotagger:3.0.1")
    implementation("app.cash.sqldelight:sqlite-driver:2.0.0-alpha05")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.0-RC")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.8.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.8.2")
    testImplementation("org.mockito.kotlin:mockito-kotlin:3.2.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")

    implementation("io.github.microutils:kotlin-logging:1.7.4")
    implementation("org.slf4j:slf4j-simple:1.7.26")
}

sqldelight {
    databases {
        create("ChatLogDataBase") {
            packageName.set("kkhouse.com")
        }
    }
}