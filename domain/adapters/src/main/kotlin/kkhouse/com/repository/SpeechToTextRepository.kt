package kkhouse.com.repository

import kkhouse.com.*

typealias TranscriptText = String

interface SpeechToTextRepository {
    fun oldRecognizeSpeech(flacBase64: String): Result<TranscriptText> // TODO 削除

    fun writeFlacFile(byteArray: ByteArray, fileName: FlacData): Resource<FlacData>

    fun deleteFlacFile(flacData: FlacData): Resource<Unit>

    fun uploadFlacFileToGCP(flacData: FlacData): Resource<FlacData>

    fun deleteFlacFileToGCP(flacData: FlacData): Resource<Unit>

    fun recognizeSpeech(flacData: FlacData): Resource<TranscriptText>

    suspend fun createUserAndChatRoom(userId: Int): Resource<Unit>

    suspend fun createChatRoom(userId: Int): Resource<Unit>

    suspend fun writeConversation(
        userId: Int,
        chatRoomId: Int,
        conversation: Conversation
    ): Resource<ChatData>

    suspend fun findChatRoomsForUser(userId: Int): Resource<List<ChatRoomId>>

    suspend fun findChatHistory(userId: Int, chatRoomId: Int): Resource<ChatData>
}