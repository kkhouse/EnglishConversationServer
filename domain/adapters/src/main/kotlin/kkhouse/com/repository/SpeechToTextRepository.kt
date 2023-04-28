package kkhouse.com.repository

import kkhouse.com.ChatData
import kkhouse.com.Conversation
import kkhouse.com.FlacData
import kkhouse.com.Resource

typealias TranscriptText = String
interface SpeechToTextRepository {
    fun oldRecognizeSpeech(flacBase64: String): Result<TranscriptText> // TODO 削除

    fun writeFlacFile(byteArray: ByteArray, fileName: FlacData): Resource<FlacData>

    fun deleteFlacFile(flacData: FlacData): Resource<Unit>

    fun uploadFlacFileToGCP(flacData: FlacData): Resource<FlacData>

    fun deleteFlacFileToGCP(flacData: FlacData): Resource<Unit>

    fun recognizeSpeech(flacData: FlacData): Resource<TranscriptText>

    suspend fun createUser(userId: Int): Resource<Unit>

    suspend fun writeConversation(
        userId: Int,
        chatRoomId: Int,
        conversation: Conversation
    ): Resource<ChatData>

    suspend fun findChatHistory(userId: Int, chatRoomId: Int): Resource<ChatData>
}