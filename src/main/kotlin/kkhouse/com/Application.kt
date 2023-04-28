package kkhouse.com

import io.ktor.server.application.*
import kkhouse.com.route.configureRouting
import kkhouse.com.route.configureSerialization

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {
    startKoin()
    configureSerialization()
    configureRouting()
}
