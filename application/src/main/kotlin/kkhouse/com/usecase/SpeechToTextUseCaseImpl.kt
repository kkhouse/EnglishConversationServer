package kkhouse.com.usecase

import kkhouse.com.repository.SpeechToTextRepository
import kkhouse.com.speech.*
import kkhouse.com.utils.AppError
import kkhouse.com.utils.Resource
import mu.KLogging
import java.security.InvalidParameterException
import java.util.*

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
            .map { resultText -> UploadResult(userId = uploadData.userId, appChatRoom = uploadData.getRoomId().value, speech = resultText) }
            .effect { // NOTE エラーでも成功でも消してしまう。再Tryはもう一度やってもらう
                speechToTextRepository.deleteFlacFile(flacData)
//                speechToTextRepository.deleteFlacFileToGCP(flacData)
            }
    }

    override suspend fun handlePostAiSpeech(aiChatInquired: AiChatInquired): Resource<ChatData> {
        lateinit var cashRoomIds : CacheRoomIds
        val userId = aiChatInquired.userId
        return when(userId.isEmpty() || aiChatInquired.speech.isEmpty()) {
            true -> Resource.Failure(AppError.UnKnownError("invalid userId or user message"))
            else -> speechToTextRepository.findChatRoomsForUser(userId)
                .flatMapAsync { roomIds ->
                    cashRoomIds = CacheRoomIds(roomIds)
                    speechToTextRepository.findChatHistory(userId, roomIds, aiChatInquired.getRoomId())
                }.flatMapAsync { currentChatData ->
                    // 応答要求後に、ユーザの返答を保存する
                    speechToTextRepository.postConversation(currentChatData.getNewConversation(aiChatInquired.speech))
                        .tapAsync { _ ->
                            speechToTextRepository.writeConversation(
                                userId,
                                cashRoomIds.getRoomId(aiChatInquired.getRoomId()),
                                Conversation(Role.User.value, aiChatInquired.speech)
                            )
                        }
                }.flatMapAsync { aiResponded ->
                    speechToTextRepository.writeConversation(userId, cashRoomIds.getRoomId(aiChatInquired.getRoomId()), aiResponded)
                }.flatMapAsync {
                    speechToTextRepository.findChatHistory(userId, cashRoomIds.roomIds, aiChatInquired.getRoomId())
                }
        }
    }

    private suspend fun getFirstUserChatData(): Resource<ChatData> {
        lateinit var cashRoomIds : CacheRoomIds
        val userID = UUID.randomUUID().toString()
        return speechToTextRepository.createUserAndChatRoom(userID)
            .flatMapAsync { appRoomIds ->
                cashRoomIds = CacheRoomIds(appRoomIds)
                speechToTextRepository.postConversation(null)
            }
            .flatMapAsync { aiResponded ->
                speechToTextRepository.writeConversation(
                    userId = userID,
                    chatRoomId = cashRoomIds.getRoomId(ClientChatRoomId.EnglishConversation),
                    conversation = aiResponded
                )
            }
            .flatMapAsync {
                speechToTextRepository.findChatHistory(
                    userId = userID,
                    chatRoomIds = cashRoomIds.roomIds,
                    target = ClientChatRoomId.EnglishConversation
                )
            }
    }

    private suspend fun getCurrentChatData(userID: String, target: ClientChatRoomId): Resource<ChatData> {
        return speechToTextRepository.findChatRoomsForUser(userID)
            .flatMapAsync { speechToTextRepository.findChatHistory(userID, it, target) }
    }
}