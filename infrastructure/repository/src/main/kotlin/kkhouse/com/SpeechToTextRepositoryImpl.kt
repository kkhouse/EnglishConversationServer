package kkhouse.com
import zip3Result
import combineResult
import kkhouse.com.speech.Role.Companion.getRowValue
import kkhouse.com.file.LocalFileManager
import kkhouse.com.mapping.mapConversation
import kkhouse.com.persistent.ChatDataBase
import kkhouse.com.repository.SpeechToTextRepository
import kkhouse.com.repository.TranscriptText
import kkhouse.com.speech.*
import kkhouse.com.utils.Resource
import kkhousecom.QueryMessagesAndRolesForUserInChatRoom
import toResource

class SpeechToTextRepositoryImpl(
    private val speechToText: SpeechToText,
    private val localFileManager: LocalFileManager,
    private val chatDatabase: ChatDataBase
): SpeechToTextRepository {

    override fun writeFlacFile(byteArray: ByteArray, fileName: FlacData): Resource<FlacData> {
        return localFileManager.saveFile(byteArray, fileName.fileName)
            .combineResult(
                resultB = localFileManager.analyzeFileData(fileName.fileName)
            ) { _, analyzedFlacData -> analyzedFlacData }.toResource()
    }

    override fun deleteFlacFile(flacData: FlacData): Resource<Unit> {
        return localFileManager.deleteFile(fileName = flacData.fileName).toResource()
    }

    override fun uploadFlacFileToGCP(flacData: FlacData): Resource<FlacData> {
        return speechToText.uploadFlacFileOnGCP(flacData).toResource()
    }

    override fun deleteFlacFileToGCP(flacData: FlacData): Resource<Unit> {
        return speechToText.deleteFlacFileOnGCP(flacData).toResource()
    }

    override fun recognizeSpeech(flacData: FlacData): Resource<TranscriptText> {
        return speechToText.postSpeechToText(flacData).toResource()
    }

    override suspend fun postConversation(conversation: List<Conversation>?): Resource<AiResponded> {
        return speechToText.postCompletion(conversation).toResource()
    }

    override suspend fun createUserAndChatRoom(userId: String): Resource<ChatRoomId> {
        return chatDatabase.createUser(userId)
            .zip3Result(
                resultB = chatDatabase.createChatRoomForUser(userId),
                resultC = chatDatabase.queryChatRoomsForUser(userId),
                transformerWithAB = {_, _ -> },
                transformerWithC = { _, roomIdList -> roomIdList.last() } // 作成したRoomIDを取得する
            ).toResource()
    }

    override suspend fun createChatRoom(userId: String): Resource<Unit> {
        return chatDatabase.createChatRoomForUser(userId).toResource()
    }

    override suspend fun writeConversation(
        userId: String,
        chatRoomId: Int,
        conversation: AiResponded,
    ): Resource<ChatData> {
        return chatDatabase.insertChatLogForUserInChatRoom(
            chatRoomId = chatRoomId,
            role = conversation.role.value,
            message = conversation.message,
            createdAt = 0 // TODO
        ).combineResult(
            resultB = chatDatabase.queryMessagesAndRolesForUserInChatRoom(userId, chatRoomId)
        ) { _ , conversationData ->
            ChatData(
                userId = userId,
                appChatRoom = chatRoomId,
                conversation = conversationData.map(QueryMessagesAndRolesForUserInChatRoom::mapConversation),
            )
        }.toResource()
    }

    override suspend fun findChatRoomsForUser(userId: String): Resource<List<ChatRoomId>> {
        return chatDatabase.queryChatRoomsForUser(userId).toResource()
    }

    override suspend fun findChatHistory(userId: String, chatRoomId: Int): Resource<ChatData> {
        return chatDatabase.queryMessagesAndRolesForUserInChatRoom(userId = userId, chatRoomId = chatRoomId)
            .map { list ->
                ChatData(
                    userId = userId,
                    appChatRoom = chatRoomId,
                    conversation = list.map(QueryMessagesAndRolesForUserInChatRoom::mapConversation)
                )
            }.toResource()
    }
}