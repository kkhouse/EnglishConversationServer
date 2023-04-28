package kkhouse.com.route

import kkhouse.com.speech.Audio
import kkhouse.com.speech.ByteFlacData
import kkhouse.com.utils.Resource
import kkhouse.com.speech.SpeechToTextResult
import kkhouse.com.utils.forEachAsync
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kkhouse.com.utils.AppError
import kkhouse.com.utils.TextToSpeechError
import org.koin.java.KoinJavaComponent
import kkhouse.com.usecase.SpeechToTextUseCase

fun Application.configureRouting() {
    val useCase: SpeechToTextUseCase by KoinJavaComponent.inject(SpeechToTextUseCase::class.java)
    routing {
        post("/speechtotext") { // TODO 削除
            useCase.handleSpeechToTextClientRequest(
                Resource.ofAsync { call.receive<ByteFlacData>() }
            ).forEachAsync(
                onSuccess = { call.respond(status = HttpStatusCode.OK, message = it) },
                onFailure = { call.respond(status = HttpStatusCode.NotFound, message = SpeechToTextResult.createErrorCodeResult(it)) }
            )
        }

        post("/upload") {
//            when(ContentType.Audio.Any.contentType == call.request.contentType().contentType) {
//                true -> useCase.handleFileUploaded(flacByteArray = call.receive<ByteArray>())
//                    .forEachAsync(
//                        onSuccess = { resp -> call.respond(HttpStatusCode.OK, resp) },
//                        onFailure = { error ->
//                            when(error) { // TODO
//                                is TextToSpeechError.InvalidChunk -> call.respond(HttpStatusCode.NotFound, "multiple chunk")
//                                is TextToSpeechError.InvalidResultText -> call.respond(HttpStatusCode.NotFound, "text empty")
//                                is AppError.UnKnownError -> call.respond(HttpStatusCode.NotFound, "unknownError")
//                            }
//
//                        }
//                    )
//                else -> call.respond(HttpStatusCode.UnsupportedMediaType, "UnSupportedMediaType")
//            }

            // TODO
            val audio = call.receive<Audio>()

            useCase.handleFileUploaded(flacByteArray = audio.data)
                .forEachAsync(
                    onSuccess = { resp -> call.respond(HttpStatusCode.OK, resp) },
                    onFailure = { error ->
                        when(error) {
                            is TextToSpeechError.InvalidChunk -> call.respond(HttpStatusCode.NotFound, "multiple chunk")
                            is TextToSpeechError.InvalidResultText -> call.respond(HttpStatusCode.NotFound, "text empty")
                            is AppError.UnKnownError -> call.respond(HttpStatusCode.NotFound, "unknownError")
                        }

                    }
                )
        }
    }
}
