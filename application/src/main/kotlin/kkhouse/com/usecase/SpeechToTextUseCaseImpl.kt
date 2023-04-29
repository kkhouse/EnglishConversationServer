package kkhouse.com.usecase

import kkhouse.com.repository.SpeechToTextRepository
import kkhouse.com.speech.*
import kkhouse.com.utils.AppError
import kkhouse.com.utils.Resource
import mu.KLoggable
import mu.KLogger
import mu.KLogging
import java.security.InvalidParameterException
import java.util.*
import kotlin.math.log

class SpeechToTextUseCaseImpl(
    private val speechToTextRepository: SpeechToTextRepository,
) : SpeechToTextUseCase {

    companion object: KLogging()

    override suspend fun handleInitialize(initData: InitializedConversation): Resource<ChatData> {
        val userId = initData.userId
        return when(userId.isNullOrEmpty()) {
            true -> getFirstUserChatData()
            else -> getCurrentChatData(userId, initData.getRoomId())
        }
    }

    override suspend fun handleFileUploaded(uploadData: UploadData): Resource<UploadResult> {
        // NOTE : fileNameは一意であればなんでもいい
        val flacData = FlacData(fileName = UUID.randomUUID().toString() + ".flac" )
        return speechToTextRepository.writeFlacFile(byteArray = uploadData.userFlacData, fileName = flacData)
            .flatMap(speechToTextRepository::uploadFlacFileToGCP)
            .flatMap(speechToTextRepository::recognizeSpeech)
            .filter { it.isNotEmpty() }
            .map { resultText -> UploadResult(userId = uploadData.userId, appChatRoom = uploadData.appChatRoom, speech = resultText) }
            .effect { // NOTE エラーでも成功でも消してしまう。再Tryはもう一度やってもらう
                speechToTextRepository.deleteFlacFile(flacData)
                speechToTextRepository.deleteFlacFileToGCP(flacData)
            }
    }

    override suspend fun handlePostAiSpeech(aiChatInquired: AiChatInquired): Resource<ChatData> {
        var cashDbRowRoomId : ChatRoomId = 0
        val userId = aiChatInquired.userId
        return when(userId.isEmpty() || aiChatInquired.speech.isEmpty()) {
            true -> Resource.Failure(AppError.UnKnownError("invalid userId or user message"))
            else -> getDatabaseRowRoomId(userId, aiChatInquired.getRoomId()).flatMapAsync { dbRowRoomId ->
                cashDbRowRoomId = dbRowRoomId
                speechToTextRepository.findChatHistory(userId, dbRowRoomId)
            }.flatMapAsync { currentChatData ->
                speechToTextRepository.postConversation(currentChatData.conversation)
            }.flatMapAsync { aiResponded ->
                speechToTextRepository.writeConversation(userId, cashDbRowRoomId, aiResponded)
            }
        }
    }

    private suspend fun getFirstUserChatData(): Resource<ChatData> {
        var cashRoomId : ChatRoomId = 0
        val userID = UUID.randomUUID().toString()
        return speechToTextRepository.createUserAndChatRoom(userID)
            .flatMapAsync { chatRoomId ->
                cashRoomId = chatRoomId
                speechToTextRepository.postConversation(null)
            }
            .flatMapAsync { aiResponded ->
                speechToTextRepository.writeConversation(
                    userId = userID,
                    chatRoomId = cashRoomId,
                    conversation = aiResponded
                )
            }
    }

    private suspend fun getCurrentChatData(userID: String, room: ClientChatRoomId): Resource<ChatData> {
        return speechToTextRepository.findChatRoomsForUser(userID)
            .flatMapAsync { getDatabaseRowRoomId(userID, room) }
            .flatMapAsync { speechToTextRepository.findChatHistory(userID, it) }
    }

    private suspend fun getDatabaseRowRoomId(userId: String, room: ClientChatRoomId): Resource<ChatRoomId> {
        return speechToTextRepository.findChatRoomsForUser(userId)
            .map { chatRooms ->
                when(room) {
                    ClientChatRoomId.EnglishConversation -> chatRooms.first()
                    ClientChatRoomId.SearchExpression -> chatRooms[1]
                    ClientChatRoomId.Unknown -> throw InvalidParameterException("clientChatRoomId is invalid")
                }
            }
    }
}