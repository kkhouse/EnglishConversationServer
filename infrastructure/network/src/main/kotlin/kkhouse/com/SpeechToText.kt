package kkhouse.com

import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.chat.ChatCompletion
import kkhouse.com.repository.TranscriptText
import kkhouse.com.speech.Conversation
import kkhouse.com.speech.FlacData

interface SpeechToText {
    fun oldPostSpeechToText(content: String): Result<TranscriptText> // TODO 削除

    fun uploadFlatFileToStorage(flacData: FlacData): Result<FlacData>

    fun deleteFlatFileToStorage(flacData: FlacData): Result<Unit>

    fun postSpeechToText(flacData: FlacData): Result<TranscriptText>

    @OptIn(BetaOpenAI::class)
    suspend fun postCompletion(newMessage: String, conversation: List<Conversation>?): Result<ChatCompletion>
}