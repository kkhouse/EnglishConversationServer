package kkhouse.com.repository

import kkhouse.com.speech.ChatData
import kkhouse.com.speech.ChatRoomId
import kkhouse.com.speech.Conversation
import kkhouse.com.speech.FlacData
import kkhouse.com.utils.Resource

typealias TranscriptText = String

interface SpeechToTextRepository {
    fun oldRecognizeSpeech(flacBase64: String): Result<TranscriptText> // TODO 削除

    fun writeFlacFile(byteArray: ByteArray, fileName: FlacData): Resource<FlacData>

    fun deleteFlacFile(flacData: FlacData): Resource<Unit>

    fun uploadFlacFileToGCP(flacData: FlacData): Resource<FlacData>

    fun deleteFlacFileToGCP(flacData: FlacData): Resource<Unit>

    fun recognizeSpeech(flacData: FlacData): Resource<TranscriptText>

    suspend fun createUserAndChatRoom(userId: String): Resource<Unit>

    suspend fun createChatRoom(userId: String): Resource<Unit>

    suspend fun writeConversation(
        userId: String,
        chatRoomId: Int,
        conversation: Conversation
    ): Resource<ChatData>

    suspend fun findChatRoomsForUser(userId: String): Resource<List<ChatRoomId>>

    suspend fun findChatHistory(userId: String, chatRoomId: Int): Resource<ChatData>
}