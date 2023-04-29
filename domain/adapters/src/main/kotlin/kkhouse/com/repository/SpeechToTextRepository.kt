package kkhouse.com.repository

import kkhouse.com.speech.*
import kkhouse.com.utils.Resource

typealias TranscriptText = String

interface SpeechToTextRepository {
    fun writeFlacFile(byteArray: ByteArray, fileName: FlacData): Resource<FlacData>

    fun deleteFlacFile(flacData: FlacData): Resource<Unit>

    fun uploadFlacFileToGCP(flacData: FlacData): Resource<FlacData>

    fun deleteFlacFileToGCP(flacData: FlacData): Resource<Unit>

    fun recognizeSpeech(flacData: FlacData): Resource<TranscriptText>

    /**
     * 応答を1件
     */
    suspend fun postConversation(conversation: List<Conversation>?): Resource<Conversation>

    suspend fun createUserAndChatRoom(userId: String): Resource<ChatRoomId>

    suspend fun createChatRoom(userId: String): Resource<Unit>

    suspend fun writeConversation(
        userId: String,
        chatRoomId: Int,
        conversation: Conversation
    ): Resource<ChatData>

    suspend fun findChatRoomsForUser(userId: String): Resource<List<ChatRoomId>>

    suspend fun findChatHistory(userId: String, chatRoomId: Int): Resource<ChatData>

}