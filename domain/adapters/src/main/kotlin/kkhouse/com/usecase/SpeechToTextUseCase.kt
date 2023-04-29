package kkhouse.com.usecase

import kkhouse.com.speech.*
import kkhouse.com.utils.Resource


interface SpeechToTextUseCase {
    suspend fun handleInitialize(initData: InitializedConversation): Resource<ChatData>

    suspend fun handleFileUploaded(uploadData: UploadData): Resource<UploadResult>

    suspend fun handlePostAiSpeech(aiChatInquired:AiChatInquired): Resource<ChatData>
}