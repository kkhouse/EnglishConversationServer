package kkhouse.com

import kkhouse.com.repository.TranscriptText
import kkhouse.com.speech.Conversation
import kkhouse.com.speech.FlacData

interface SpeechToText {
    fun uploadFlacFileOnGCP(flacData: FlacData): Result<FlacData>

    fun deleteFlacFileOnGCP(flacData: FlacData): Result<Unit>

    fun postSpeechToText(flacData: FlacData): Result<TranscriptText>

    suspend fun postCompletion(conversation: List<Conversation>?): Result<Conversation>
}