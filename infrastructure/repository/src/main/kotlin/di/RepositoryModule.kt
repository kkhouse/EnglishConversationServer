package di

import SpeechToTextRepositoryImpl
import org.koin.dsl.module
import repository.SpeechToTextRepository

val repositoryModule = module {
    single<SpeechToTextRepository> { SpeechToTextRepositoryImpl(get(), get()) }
}