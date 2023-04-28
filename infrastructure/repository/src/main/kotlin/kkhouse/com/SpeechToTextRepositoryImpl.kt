package kkhouse.com
import kkhouse.com.Role.Companion.getRowValue
import kkhouse.com.file.LocalFileManager
import kkhouse.com.mapping.mapConversation
import kkhouse.com.persistent.ChatDataBase
import kkhouse.com.repository.SpeechToTextRepository
import kkhouse.com.repository.TranscriptText
import kkhousecom.QueryMessagesAndRolesForUserInChatRoom

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
            .map { localFileManager.analyzeFileData(fileName.fileName) }
            .fold(
                onSuccess = { it.toResource() },
                onFailure = { Resource.Failure(AppError.UnKnownError(it.message ?: "failed writing file")) }
            )
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

    override suspend fun createUser(userId: Int): Resource<Unit> {
        return chatDatabase.createUser(userId.toLong()).toResource()
    }

    override suspend fun writeConversation(
        userId: Int,
        chatRoomId: Int,
        conversation: Conversation,
    ): Resource<ChatData> {
        return chatDatabase.insertChatLogForUserInChatRoom(
            chatRoomId = chatRoomId,
            role = conversation.role.getRowValue(),
            message = conversation.message,
            createdAt = 0 // TODO
        ).fold(
            onSuccess = {
                chatDatabase.queryMessagesAndRolesForUserInChatRoom(userId, chatRoomId).map {
                    ChatData(
                        userId = userId,
                        chatRoomId = chatRoomId,
                        conversation = it.map(QueryMessagesAndRolesForUserInChatRoom::mapConversation),
                        errorCode = null
                    )
                }.fold(
                    onSuccess = { Resource.of { it } },
                    onFailure = { Resource.Failure(AppError.UnKnownError(it.message ?:"")) } // TODO
                 )
            },
            onFailure = {
                Resource.Failure(AppError.UnKnownError(it.message ?: "")) // TODO
            }
        )
    }

    override suspend fun findChatHistory(userId: Int, chatRoomId: Int): Resource<ChatData> {
        return chatDatabase.queryMessagesAndRolesForUserInChatRoom(userId = userId, chatRoomId = chatRoomId)
            .map { list ->
                ChatData(
                    userId = userId,
                    chatRoomId = chatRoomId,
                    conversation = list.map(QueryMessagesAndRolesForUserInChatRoom::mapConversation)
                )
            }.toResource()
    }

    /*
    TODO どこかそれっぽい所へ
     */
    private fun <T> Result<T>.toResource(
        mapAppError: (Throwable) -> AppError = {
            AppError.UnKnownError(
                it.message ?: "Error On Result to Resource Converter "
            )
        }
    ): Resource<T> {
        return this.fold(
            onSuccess = { Resource.Success(it) },
            onFailure = { Resource.Failure(mapAppError(it)) }
        )
    }
}