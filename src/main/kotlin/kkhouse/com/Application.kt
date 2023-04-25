package kkhouse.com

import di.applicationModule
import di.networkModule
import di.repositoryModule
import io.ktor.server.application.*
import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent
import route.configureRouting
import route.configureSerialization
import usecase.SpeechToTextUseCase

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {
    startKoin {
        modules(
            networkModule,
            repositoryModule,
            applicationModule,
            appModule
        )
    }
    configureSerialization()
    configureRouting()
}
