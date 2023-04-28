package kkhouse.com
import combineResult
import kkhouse.com.speech.Role.Companion.getRowValue
import kkhouse.com.file.LocalFileManager
import kkhouse.com.mapping.mapConversation
import kkhouse.com.persistent.ChatDataBase
import kkhouse.com.repository.SpeechToTextRepository
import kkhouse.com.repository.TranscriptText
import kkhouse.com.speech.ChatData
import kkhouse.com.speech.ChatRoomId
import kkhouse.com.speech.Conversation
import kkhouse.com.speech.FlacData
import kkhouse.com.utils.Resource
import kkhousecom.QueryMessagesAndRolesForUserInChatRoom
import toResource

class SpeechToTextRepositoryImpl(
    private val speechToText: SpeechToText,
    private val localFileManager: LocalFileManager,
    private val chatDatabase: ChatDataBase
): SpeechToTextRepository {
    override fun oldRecognizeSpeech(flacBase64: String): Result<TranscriptText> {
        return speechToText.oldPostSpeechToText(content = flacBase64)
    }

    override fun writeFlacFile(byteArray: ByteArray, fileName: FlacData): Resource<FlacData> {
        return localFileManager.saveFile(byteArray, fileName.fileName)
            .combineResult(
                resultB = localFileManager.analyzeFileData(fileName.fileName)
            ) { _, analyzedFlacData -> analyzedFlacData }.toResource()

//        return localFileManager.saveFile(byteArray, fileName.fileName)
//            .map { localFileManager.analyzeFileData(fileName.fileName) }
//            .fold(
//                onSuccess = { it.toResource() },
//                onFailure = { Resource.Failure(AppError.UnKnownError(it.message ?: "failed writing file")) }
//            )
    }

    override fun deleteFlacFile(flacData: FlacData): Resource<Unit> {
        return localFileManager.deleteFile(fileName = flacData.fileName).toResource()
    }

    override fun uploadFlacFileToGCP(flacData: FlacData): Resource<FlacData> {
        return speechToText.uploadFlatFileToStorage(flacData).toResource()
    }

    override fun deleteFlacFileToGCP(flacData: FlacData): Resource<Unit> {
        return speechToText.deleteFlatFileToStorage(flacData).toResource()
    }

    override fun recognizeSpeech(flacData: FlacData): Resource<TranscriptText> {
        return speechToText.postSpeechToText(flacData).toResource()
    }

    override suspend fun createUserAndChatRoom(userId: String): Resource<Unit> {
        return chatDatabase.createUser(userId).combineResult(
            resultB = chatDatabase.createChatRoomForUser(userId)
        ) {_, _ -> }.toResource()
    }

    override suspend fun createChatRoom(userId: String): Resource<Unit> {
        return chatDatabase.createChatRoomForUser(userId).toResource()
    }

    override suspend fun writeConversation(
        userId: String,
        chatRoomId: Int,
        conversation: Conversation,
    ): Resource<ChatData> {
        return chatDatabase.insertChatLogForUserInChatRoom(
            chatRoomId = chatRoomId,
            role = conversation.role.getRowValue(),
            message = conversation.message,
            createdAt = 0 // TODO
        ).combineResult(
            resultB = chatDatabase.queryMessagesAndRolesForUserInChatRoom(userId, chatRoomId)
        ) { _ , conversationData ->
            ChatData(
                userId = userId,
                chatRoomId = chatRoomId,
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
                    chatRoomId = chatRoomId,
                    conversation = list.map(QueryMessagesAndRolesForUserInChatRoom::mapConversation)
                )
            }.toResource()
    }
}