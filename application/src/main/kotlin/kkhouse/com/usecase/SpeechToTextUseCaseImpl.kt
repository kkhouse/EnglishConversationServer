package kkhouse.com.usecase

import kkhouse.com.ByteFlacData
import kkhouse.com.FlacData
import kkhouse.com.Resource
import kkhouse.com.SpeechToTextResult
import kkhouse.com.exceptions.EmptyTextException
import kkhouse.com.exceptions.MultiChunkException
import kkhouse.com.exceptions.MultiResultException
import kkhouse.com.AppError
import kkhouse.com.TextToSpeechError
import kkhouse.com.repository.SpeechToTextRepository
import kkhouse.com.usecase.SpeechToTextUseCase
import java.util.*

class SpeechToTextUseCaseImpl(
    private val speechToTextRepository: SpeechToTextRepository,
) : SpeechToTextUseCase {
    //TODO 消す
    override suspend fun handleSpeechToTextClientRequest(flacData: Resource<ByteFlacData>): Resource<SpeechToTextResult> {
        return flacData.flatMap { data ->
            speechToTextRepository.oldRecognizeSpeech(data.data).fold(
                onSuccess = { resultText ->
                    when(resultText.isNotEmpty()) {
                        true -> Resource.Success(SpeechToTextResult(text = resultText))
                        else -> Resource.Failure(TextToSpeechError.InvalidResultText)
                    }
                },
                onFailure = { throwable ->
                    Resource.Failure(
                        when(throwable) {
                            is MultiResultException ->  {
                                println("MultiResultException: ${throwable.unexpectedDataLog}")
                                TextToSpeechError.InvalidResultText
                            }
                            is MultiChunkException ->  {
                                println("MultiChunkException: ${throwable.unexpectedDataLog}")
                                TextToSpeechError.InvalidChunk
                            }
                            is EmptyTextException -> {
                                println("EmptyTextException: ${throwable.unexpectedDataLog}")
                                TextToSpeechError.InvalidResultText
                            }
                            else -> {
                                println("Other Exception: ${throwable.message}")
                                AppError.UnKnownError(throwable.message ?: "unknown error")
                            }
                        }
                    )
                }
            )
        }
    }

    override suspend fun handleFileUploaded(flacByteArray: ByteArray): Resource<SpeechToTextResult> {
        val fileName = UUID.randomUUID().toString() + ".flac" // NOTE : File名はUUID
        val flacData = FlacData(fileName = fileName)
        return speechToTextRepository.writeFlacFile(byteArray = flacByteArray, fileName = flacData)
            .flatMap(speechToTextRepository::uploadFlacFileToGCP)
            .flatMap(speechToTextRepository::recognizeSpeech)
            .flatMap { resultText ->
                when(resultText.isNotEmpty()) {
                    true -> Resource.Success(SpeechToTextResult(text = resultText))
                    else -> Resource.Failure(TextToSpeechError.InvalidResultText)
                }
            }
            .effect { // NOTE エラーでも成功でも消してしまう。再Tryはもう一度やってもらう
                speechToTextRepository.deleteFlacFile(flacData)
                speechToTextRepository.deleteFlacFileToGCP(flacData)
            }
    }
}