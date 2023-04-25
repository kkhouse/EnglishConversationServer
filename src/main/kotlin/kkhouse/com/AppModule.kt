package kkhouse.com

import di.applicationModule
import di.databaseModule
import di.networkModule
import di.repositoryModule
import org.koin.core.context.startKoin
import org.koin.dsl.module

val appModule = module {

}

fun startKoin() {
    startKoin {
        modules(
            networkModule,
            databaseModule,
            repositoryModule,
            applicationModule,
            appModule
        )
    }
}