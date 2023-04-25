package usecase

import ByteFlacData
import Resource
import SpeechToTextResult


interface SpeechToTextUseCase {
    suspend fun handleSpeechToTextClientRequest(flacData: Resource<ByteFlacData>): Resource<SpeechToTextResult>

    suspend fun handleFileUploaded(flacByteArray: ByteArray): Resource<SpeechToTextResult>
}