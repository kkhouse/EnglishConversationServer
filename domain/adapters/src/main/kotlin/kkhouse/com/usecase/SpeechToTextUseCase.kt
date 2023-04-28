package kkhouse.com.usecase

import kkhouse.com.ByteFlacData
import kkhouse.com.Resource
import kkhouse.com.SpeechToTextResult


interface SpeechToTextUseCase {
    suspend fun handleSpeechToTextClientRequest(flacData: Resource<ByteFlacData>): Resource<SpeechToTextResult>

    suspend fun handleFileUploaded(flacByteArray: ByteArray): Resource<SpeechToTextResult>
}