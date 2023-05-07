package kkhouse.com.di

import kkhouse.com.SpeechToTextRepositoryImpl
import kkhouse.com.mock.SpeechToTextRepositoryForTest
import kkhouse.com.repository.SpeechToTextRepository
import org.koin.dsl.module

val repositoryModule = module {
    single<SpeechToTextRepository> {
//        SpeechToTextRepositoryForTest()　// NOTE こっちにすると mock responseが返せる
        SpeechToTextRepositoryImpl(get(), get(), get())
    }
}