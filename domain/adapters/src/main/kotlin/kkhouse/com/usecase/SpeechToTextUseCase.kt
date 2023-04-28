package kkhouse.com.usecase

import kkhouse.com.speech.ByteFlacData
import kkhouse.com.speech.ChatData
import kkhouse.com.utils.Resource
import kkhouse.com.speech.SpeechToTextResult


interface SpeechToTextUseCase {
    suspend fun handleSpeechToTextClientRequest(flacData: Resource<ByteFlacData>): Resource<SpeechToTextResult>

    suspend fun handleFileUploaded(flacByteArray: ByteArray): Resource<SpeechToTextResult>

    suspend fun handleInitialize(userId: String?): Resource<ChatData>
}