package kkhouse.com.di

import org.koin.dsl.module
import kkhouse.com.usecase.SpeechToTextUseCase
import kkhouse.com.usecase.SpeechToTextUseCaseImpl

val applicationModule = module {
    single<SpeechToTextUseCase> { SpeechToTextUseCaseImpl(get()) }
}