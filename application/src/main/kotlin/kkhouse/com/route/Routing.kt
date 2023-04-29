package kkhouse.com.route

import io.ktor.http.*
import io.ktor.http.HttpStatusCode.Companion.InternalServerError
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kkhouse.com.speech.ChatData
import kkhouse.com.speech.UploadResult
import kkhouse.com.usecase.SpeechToTextUseCase
import kkhouse.com.utils.AiSpeechError
import kkhouse.com.utils.AppError
import kkhouse.com.utils.TextToSpeechError
import kkhouse.com.utils.forEachAsync
import mu.KotlinLogging
import org.koin.java.KoinJavaComponent
import java.io.InputStream
import java.util.*

private val logger = KotlinLogging.logger {}

fun Application.configureRouting() {
    val useCase: SpeechToTextUseCase by KoinJavaComponent.inject(SpeechToTextUseCase::class.java)
    routing {
        post("/initilize") {
            useCase.handleInitialize(initData = call.receive())
                .forEachAsync(
                    onSuccess = { call.respond(status = HttpStatusCode.OK, message = it) },
                    onFailure = {
                        call.respond(status = InternalServerError, message = ChatData(errorCode = 0))
                        logger.error { "AppError : $it , If UnKnownError Message : ${(it as? AppError.UnKnownError)?.message} " }
                    }
                )
        }

        post("/upload") {
            useCase.handleFileUploaded(uploadData = call.receive())
                .forEachAsync(
                    onSuccess = { call.respond(HttpStatusCode.OK, it) },
                    onFailure = {
                        when (it) {
                            is TextToSpeechError.InvalidChunk -> call.respond(
                                status = InternalServerError,
                                UploadResult(errorCode = it.code)
                            )

                            is TextToSpeechError.InvalidResultText -> call.respond(
                                status = InternalServerError,
                                UploadResult(errorCode = it.code)
                            )

                            else -> call.respond(status = InternalServerError, message = ChatData(errorCode = 0))
                        }
                        logger.error { "AppError : $it , If UnKnownError Message : ${(it as? AppError.UnKnownError)?.message} " }
                    }
                )
        }

        post("/aiSpeech") {
            useCase.handlePostAiSpeech(aiChatInquired = call.receive())
                .forEachAsync(
                    onSuccess = { call.respond(HttpStatusCode.OK, it) },
                    onFailure = {
                        when (it) {
                            is AiSpeechError.UnexpectedResultData -> call.respond(
                                status = InternalServerError,
                                ChatData(errorCode = it.code)
                            )

                            else -> call.respond(status = InternalServerError, message = ChatData(errorCode = 0))
                        }
                        logger.error { "AppError : $it , If UnKnownError Message : ${(it as? AppError.UnKnownError)?.message} " }
                    }
                )
        }
    }
}
