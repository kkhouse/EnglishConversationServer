package kkhouse.com.di

import kkhouse.com.SpeechToTextRepositoryImpl
import kkhouse.com.mock.SpeechToTextRepositoryForTest
import org.koin.dsl.module
import kkhouse.com.repository.SpeechToTextRepository

val repositoryModule = module {
    single<SpeechToTextRepository> {
        SpeechToTextRepositoryForTest()
//        SpeechToTextRepositoryImpl(get(), get(), get())
    }
}