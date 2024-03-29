@file:OptIn(BetaOpenAI::class)

package kkhouse.com.handler

import arrow.core.Either
import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.chat.*
import com.aallam.openai.api.model.ModelId
import com.google.cloud.speech.v1.RecognizeResponse
import kkhouse.com.exceptions.EmptyTextException
import kkhouse.com.exceptions.MultiChunkException
import kkhouse.com.exceptions.MultiResultException
import kkhouse.com.exceptions.UnexpectedCompletion
import kkhouse.com.repository.TranscriptText
import kkhouse.com.speech.Conversation
import kkhouse.com.speech.Role
import kkhouse.com.utils.Const

interface RequestResponseHandler {
    // MEMO :Leftの型がExceptionなのでResultでよかったかも
    fun handleSpeechToTextResponse(response: RecognizeResponse): Either<Exception, TranscriptText>

    fun createChatRequest(conversation: List<Conversation>?): ChatCompletionRequest
    // MEMO :Leftの型がExceptionなのでResultでよかったかも
    fun handleChatResponse(chatCompletion: ChatCompletion): Either<Exception, Conversation>
}

class RequestResponseHandlerImpl(
): RequestResponseHandler {

    companion object {
        private const val MODEL_NAME = "gpt-3.5-turbo"
//        private const val PROMPT_BEGINNER_KEY = "Beginner"
//        private const val PROMPT_ADVANCED_KEY = "Advanced"
    }

    override fun handleSpeechToTextResponse(response: RecognizeResponse): Either<Exception, TranscriptText> {
        /*
        例外としているケースは発生しない様子のため、エラーにする
         */
        return when {
            response.resultsList.size != 1 -> Either.Left(MultiResultException(response.toString()))
            response.resultsList[0].alternativesCount != 1 -> Either.Left(MultiChunkException(response.toString()))
            response.resultsList[0].alternativesList[0].transcript.isEmpty() -> Either.Left(EmptyTextException(response.toString()))
            else -> Either.Right(response.resultsList[0].alternativesList[0].transcript)
        }
    }

    @OptIn(BetaOpenAI::class)
    override fun createChatRequest(conversation: List<Conversation>?): ChatCompletionRequest {
        return ChatCompletionRequestBuilder().apply {
            model = ModelId(MODEL_NAME)
            messages = when(conversation) {
                null -> getInitialPrompt()
                /*
                TODO 英会話の場合は最後の6つを記憶しておく
                    検索の場合は最新のユーザの応答で良い
                    このロジックを分けるためにChatRoomIdをここに登らせる必要がある
                 */
//                // NOTE: トークン節約のため最後の6つの会話を送る
//                else -> listOf(systemPrompt()) + conversation.takeLast(6).map(::toChatMessageRequest)
                else -> listOf(systemPrompt()) + conversation.takeLast(1).map(::toChatMessageRequest)
            }
        }.build()
    }

    @OptIn(BetaOpenAI::class)
    override fun handleChatResponse(chatCompletion: ChatCompletion): Either<Exception,Conversation> {
        // NOTE: どこにも記載がないが、複数くることがなさそうなのでサイズ1 を前提とする
        return chatCompletion.choices.let { chatList ->
            when(chatList.size == 1 && chatList[0].message == null) {
                true -> Either.Left(UnexpectedCompletion())
                else -> Either.Right(
                    Conversation(
                        role = Role.Assistant.value,
                        message = chatList[0].message!!.content
                    )
                )
            }
        }
    }

    @OptIn(BetaOpenAI::class)
    private fun getInitialPrompt() : List<ChatMessage> {
        return listOf(
            systemPrompt(),
            ChatMessage(role = ChatRole.User, content = "Please say \"Give speaking Japanese a try!\" if you have understood the prompt."),
        )
    }

    @OptIn(BetaOpenAI::class)
    private fun systemPrompt(): ChatMessage {
        // TODO プロパティから値が取れない・・
//        val filePath = this::class.java.classLoader.getResource("prompt.properties")?.path
//        val file = File(filePath)
//        val properties = Properties().apply {
//            this.load(Thread.currentThread().contextClassLoader.getResourceAsStream("sample.properties"))
//        }
        return ChatMessage(role = ChatRole.System, content = Const.Prompt.Casual)
    }

    @OptIn(BetaOpenAI::class)
    private fun toChatMessageRequest(conversation: Conversation): ChatMessage {
        return ChatMessage(
            role = when(conversation.getRole()) {
                Role.Assistant -> ChatRole.Assistant
                Role.User -> ChatRole.User
            },
            content = conversation.message
        )
    }
}