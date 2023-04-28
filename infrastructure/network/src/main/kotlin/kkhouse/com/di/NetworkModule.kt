package kkhouse.com.di

import com.aallam.openai.api.logging.LogLevel
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import kkhouse.com.SpeechToText
import kkhouse.com.SpeechToTextImpl
import kkhouse.com.SpeechToTextImpl.Companion.projectId
import com.google.cloud.speech.v1.SpeechClient
import com.google.cloud.storage.StorageOptions
import kkhouse.com.handler.RequestResponseHandler
import kkhouse.com.handler.RequestResponseHandlerImpl
import org.koin.dsl.module

val networkModule = module {
    single { SpeechClient.create() }
    single {
       StorageOptions.newBuilder().setProjectId(projectId).build().service
    }
    single {
        OpenAI(
            config = OpenAIConfig(
                token = System.getenv("OPENAI_API_KEY"),
                logLevel =LogLevel.All,
            )
        )
    }
    single<RequestResponseHandler>{ RequestResponseHandlerImpl() }
   single<SpeechToText> {
       SpeechToTextImpl(get(), get(), get(), get())
   }
}