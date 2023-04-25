package di

import org.koin.dsl.module
import usecase.SpeechToTextUseCase
import usecase.SpeechToTextUseCaseImpl

val applicationModule = module {
    single<SpeechToTextUseCase> { SpeechToTextUseCaseImpl(get()) }
}