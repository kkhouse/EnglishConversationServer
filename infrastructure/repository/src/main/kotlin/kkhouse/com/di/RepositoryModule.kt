package kkhouse.com.di

import kkhouse.com.SpeechToTextRepositoryImpl
import org.koin.dsl.module
import kkhouse.com.repository.SpeechToTextRepository

val repositoryModule = module {
    single<SpeechToTextRepository> { SpeechToTextRepositoryImpl(get(), get(), get()) }
}