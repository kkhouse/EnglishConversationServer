package kkhouse.com

import kkhouse.com.di.applicationModule
import kkhouse.com.di.databaseModule
import kkhouse.com.di.networkModule
import kkhouse.com.di.repositoryModule
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