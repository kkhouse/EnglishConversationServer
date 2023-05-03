package kkhouse.com

import combineResult
import kkhouse.com.exceptions.EmptyTextException
import kkhouse.com.exceptions.MultiChunkException
import kkhouse.com.exceptions.MultiResultException
import kkhouse.com.exceptions.UnexpectedCompletion
import kkhouse.com.file.LocalFileManager
import kkhouse.com.mapping.mapConversation
import kkhouse.com.persistent.ChatDataBase
import kkhouse.com.persistent.MessagesAndRolesForUserRoom
import kkhouse.com.repository.SpeechToTextRepository
import kkhouse.com.repository.TranscriptText
import kkhouse.com.speech.*
import kkhouse.com.utils.AiSpeechError
import kkhouse.com.utils.AppError
import kkhouse.com.utils.Resource
import kkhouse.com.utils.TextToSpeechError
import toResource
import zip3Result
import java.time.LocalDateTime

class SpeechToTextRepositoryImpl(
    private val speechToText: SpeechToText,
    private val localFileManager: LocalFileManager,
    private val chatDatabase: ChatDataBase
): SpeechToTextRepository {

    override fun writeFlacFile(byteArray: ByteArray, fileName: FlacData): Resource<FlacData> {
        return localFileManager.saveFile(byteArray, fileName.fileName)
            .combineResult(
                resultB = localFileManager.analyzeFileData(fileName.fileName),
                transformer = { _, analyzedFlacData -> analyzedFlacData }
            ).toResource()
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
        return speechToText.postSpeechToText(flacData)
            .toResource(
                mapAppError = { throwable ->
                    when(throwable) {
                        is MultiChunkException -> TextToSpeechError.InvalidChunk
                        is MultiResultException,
                        is EmptyTextException -> TextToSpeechError.InvalidResultText
                        else -> AppError.UnKnownError(throwable.message)
                    }
                }
            )
    }

    override suspend fun postConversation(conversation: List<Conversation>?): Resource<Conversation> {
        return speechToText.postCompletion(conversation)
            .toResource(
                mapAppError = { throwable ->
                    when(throwable) {
                        is UnexpectedCompletion -> AiSpeechError.UnexpectedResultData
                        else -> AppError.UnKnownError(throwable.message)
                    }
                }
            )
    }

    override suspend fun createUserAndChatRoom(userId: String): Resource<List<ChatRoomId>> {
        return chatDatabase.createUser(userId)
            .zip3Result(
                resultB = chatDatabase.createChatRoomForUser(userId),
                resultC = chatDatabase.queryChatRoomsForUser(userId),
                transformerWithAB = {_, _ -> },
                transformerWithC = { _, roomIdList -> roomIdList } // NOTE: 作成した最新のListのIndexがアプリのAppRoomChatの区分とひもづく
            ).toResource()
    }

    override suspend fun createChatRoom(userId: String): Resource<Unit> {
        return chatDatabase.createChatRoomForUser(userId).toResource()
    }

    override suspend fun writeConversation(
        userId: String,
        chatRoomId: Int,
        conversation: Conversation,
    ): Resource<Unit> {
        return chatDatabase.insertChatLogForUserInChatRoom(
            chatRoomId = chatRoomId,
            role = conversation.getRole().value,
            message = conversation.message,
            createdAt = LocalDateTime.now()
        ).toResource()
    }

    override suspend fun findChatRoomsForUser(userId: String): Resource<List<ChatRoomId>> {
        return chatDatabase.queryChatRoomsForUser(userId).toResource()
    }

    override suspend fun findChatHistory(
        userId: String,
        chatRoomIds: List<ChatRoomId>,
        target: ClientChatRoomId
    ): Resource<ChatData> {
        return chatDatabase.queryMessagesAndRolesForUserInChatRoom(
            userId = userId,
            chatRoomId = chatRoomIds[target.value]
        )
            .map { list ->
                ChatData(
                    userId = userId,
                    appChatRoom = target.value,
                    conversation = list.map(MessagesAndRolesForUserRoom::mapConversation)
                )
            }.toResource()
    }
}