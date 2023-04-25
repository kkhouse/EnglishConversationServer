package usecase

import ByteFlacData
import FlacData
import Resource
import SpeechToTextResult
import exceptions.EmptyTextException
import exceptions.MultiChunkException
import exceptions.MultiResultException
import repository.SpeechToTextRepository
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
        val fileName = UUID.randomUUID().toString() // NOTE : File名はUUID
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