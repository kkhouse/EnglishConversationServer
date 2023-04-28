package kkhouse.com.di

import kkhouse.com.SpeechToText
import kkhouse.com.SpeechToTextImpl
import kkhouse.com.SpeechToTextImpl.Companion.projectId
import com.google.cloud.speech.v1.SpeechClient
import com.google.cloud.storage.StorageOptions
import org.koin.dsl.module

val networkModule = module {
    single { SpeechClient.create() }
    single {
       StorageOptions.newBuilder().setProjectId(projectId).build().service
    }
   single<SpeechToText> { SpeechToTextImpl(get(), get()) }
}