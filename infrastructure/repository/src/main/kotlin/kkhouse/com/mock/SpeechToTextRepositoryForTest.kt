package kkhouse.com.mock

import kkhouse.com.repository.SpeechToTextRepository
import kkhouse.com.repository.TranscriptText
import kkhouse.com.speech.*
import kkhouse.com.utils.Resource
import mu.KotlinLogging

private const val mockUserId: String = "userIduserIduserIduserIduserIduserIduserIduserId"
private val mockAppChatRoomIds = listOf(0)
private val mockFlacData = FlacData(
    fileName = "mockFlacFileName",
    sampleRate = 999999,
    chanelCount = 1,
    localFilePath = "/path/to/mock_flac_file",
    language = "en-US"
)
private val logger = KotlinLogging.logger {}


class SpeechToTextRepositoryForTest(
    private var count : Int = 0
): SpeechToTextRepository {
    override fun writeFlacFile(byteArray: ByteArray, fileName: FlacData): Resource<FlacData> {
        logger.debug {
            "Repository : writeFlacFile \n" +
                    "requested byteArray $byteArray" +
                    "requested FlacData $fileName"
        }
        return Resource.Success(mockFlacData)
    }

    override fun deleteFlacFile(flacData: FlacData): Resource<Unit> {
        logger.debug {
            "Repository : deleteFlacFile \n" +
                    "requested flacData $flacData"
        }
        return Resource.Success(Unit)
    }

    override fun uploadFlacFileToGCP(flacData: FlacData): Resource<FlacData> {
        logger.debug {
            "Repository : uploadFlacFileToGCP \n" +
                    "requested flacData $flacData"
        }
        return Resource.Success(mockFlacData)
    }

    override fun deleteFlacFileToGCP(flacData: FlacData): Resource<Unit> {
        logger.debug {
            "Repository : deleteFlacFileToGCP \n" +
            "requested flacData $flacData"
        }
        return Resource.Success(Unit)
    }

    override fun recognizeSpeech(flacData: FlacData): Resource<TranscriptText> {
        logger.debug {
            "Repository : recognizeSpeech \n" +
                    "requested flacData $flacData"
        }
        return Resource.Success("mock recognized text")
    }

    override suspend fun postConversation(conversation: List<Conversation>?): Resource<Conversation> {
        logger.debug {
            "Repository : postConversation \n" +
                    "requested conversation $conversation"
        }
        return Resource.Success(Conversation(role = Role.Assistant.value, message = "new Ai message"))
    }

    override suspend fun createUserAndChatRoom(userId: String): Resource<List<ChatRoomId>> {
        logger.debug {
            "Repository : createUserAndChatRoom \n" +
                    "requested userId $userId"
        }
        return Resource.Success(mockAppChatRoomIds)
    }

    override suspend fun createChatRoom(userId: String): Resource<Unit> {
        logger.debug {
            "Repository : createChatRoom \n" +
                    "requested userId $userId"
        }
        return Resource.Success(Unit)
    }

    override suspend fun writeConversation(
        userId: String,
        chatRoomId: Int,
        conversation: Conversation
    ): Resource<Unit> {
        logger.debug {
            "Repository : writeConversation \n" +
                "requested userId $userId \n" +
                    "requested userId $chatRoomId \n" +
                    "requested userId $conversation \n"
        }
        return Resource.Success(Unit)
    }

    override suspend fun findChatRoomsForUser(userId: String): Resource<List<ChatRoomId>> {
        logger.debug {
            "Repository : findChatRoomsForUser \n" +
                    "requested userId $userId"
        }
        return Resource.Success(mockAppChatRoomIds)
    }

    override suspend fun findChatHistory(
        userId: String,
        chatRoomIds: List<ChatRoomId>,
        target: ClientChatRoomId
    ): Resource<ChatData> {
        logger.debug {
            "Repository : findChatHistory \n" +
                    "requested userId $userId" +
                    "requested chatRoomIds $chatRoomIds" +
                    "requested ClientChatRoomId $target"
        }
        return Resource.Success(
            ChatData(
                userId = mockUserId,
                appChatRoom = mockAppChatRoomIds.first(),
                conversation = getMockConversation(),
                errorCode = null)
        )
    }
    private fun getMockConversation(): List<Conversation> {
        return when(count++) {
            0 -> listOf(
                Conversation(role = Role.Assistant.value, message = "Sample Text"),
                Conversation(role = Role.User.value, message = "sample uploaded user message"),
                Conversation(role = Role.Assistant.value, message = "Sample Ai Respond"),
            )
            1 -> listOf(
                Conversation(role = Role.Assistant.value, message = "Sample Text"),
                Conversation(role = Role.User.value, message = "sample uploaded user message"),
                Conversation(role = Role.Assistant.value, message = "Sample Ai Respond"),
                Conversation(role = Role.User.value, message = "sample uploaded user message"),
                Conversation(role = Role.Assistant.value, message = "Sample Ai Respond"),
            )
            2 -> listOf(
                Conversation(role = Role.Assistant.value, message = "Sample Text"),
                Conversation(role = Role.User.value, message = "sample uploaded user message"),
                Conversation(role = Role.Assistant.value, message = "Sample Ai Respond"),
                Conversation(role = Role.User.value, message = "sample uploaded user message"),
                Conversation(role = Role.Assistant.value, message = "Sample Ai Respond"),
                Conversation(role = Role.User.value, message = "sample uploaded user message"),
                Conversation(role = Role.Assistant.value, message = "Sample Ai Respond"),
            )
            3 -> listOf(
                Conversation(role = Role.Assistant.value, message = "Sample Text"),
                Conversation(role = Role.User.value, message = "sample uploaded user message"),
                Conversation(role = Role.Assistant.value, message = "Sample Ai Respond"),
                Conversation(role = Role.User.value, message = "sample uploaded user message"),
                Conversation(role = Role.Assistant.value, message = "Sample Ai Respond"),
                Conversation(role = Role.User.value, message = "sample uploaded user message"),
                Conversation(role = Role.Assistant.value, message = "Sample Ai Respond"),
                Conversation(role = Role.User.value, message = "sample uploaded user message"),
                Conversation(role = Role.Assistant.value, message = "Sample Ai Respond"),
            )
            else -> {
                count = 0
                listOf(
                    Conversation(role = Role.Assistant.value, message = "Sample Text"),
                    Conversation(role = Role.User.value, message = "sample uploaded user message"),
                    Conversation(role = Role.Assistant.value, message = "Sample Ai Respond"),
                    Conversation(role = Role.User.value, message = "sample uploaded user message"),
                    Conversation(role = Role.Assistant.value, message = "Sample Ai Respond"),
                    Conversation(role = Role.User.value, message = "sample uploaded user message"),
                    Conversation(role = Role.Assistant.value, message = "Sample Ai Respond"),
                    Conversation(role = Role.User.value, message = "sample uploaded user message"),
                    Conversation(role = Role.Assistant.value, message = "Sample Ai Respond"),
                    Conversation(role = Role.User.value, message = "sample uploaded user message"),Conversation(role = Role.Assistant.value, message = "Sample Ai Respond"),
                    Conversation(role = Role.User.value, message = "sample uploaded user message"),
                    Conversation(role = Role.Assistant.value, message = "Sample Ai Respond"),
                    Conversation(role = Role.User.value, message = "sample uploaded user message"),Conversation(role = Role.Assistant.value, message = "Sample Ai Respond"),
                    Conversation(role = Role.User.value, message = "sample uploaded user message"),
                    Conversation(role = Role.Assistant.value, message = "Sample Ai Respond"),
                    Conversation(role = Role.User.value, message = "sample uploaded user message"),Conversation(role = Role.Assistant.value, message = "Sample Ai Respond"),
                    Conversation(role = Role.User.value, message = "sample uploaded user message"),
                    Conversation(role = Role.Assistant.value, message = "Sample Ai Respond"),
                    Conversation(role = Role.User.value, message = "sample uploaded user message"),Conversation(role = Role.Assistant.value, message = "Sample Ai Respond"),
                    Conversation(role = Role.User.value, message = "sample uploaded user message"),
                    Conversation(role = Role.Assistant.value, message = "Sample Ai Respond"),
                    Conversation(role = Role.User.value, message = "sample uploaded user message"),
                )
            }
        }
    }
}
