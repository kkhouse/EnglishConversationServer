@file:OptIn(BetaOpenAI::class)

package kkhouse.com.handler

import arrow.core.Either
import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.chat.*
import com.aallam.openai.api.model.ModelId
import com.google.cloud.speech.v1.RecognizeRequest
import com.google.cloud.speech.v1.RecognizeResponse
import kkhouse.com.exceptions.EmptyTextException
import kkhouse.com.exceptions.MultiChunkException
import kkhouse.com.exceptions.MultiResultException
import kkhouse.com.exceptions.UnexpectedCompletion
import kkhouse.com.repository.TranscriptText
import kkhouse.com.speech.Conversation
import kkhouse.com.speech.Role
import java.util.Properties

interface RequestResponseHandler {
    fun handleSpeechToTextResponse(response: RecognizeResponse): Either<Exception, TranscriptText>

    fun createChatRequest(conversation: List<Conversation>?): ChatCompletionRequest

    fun handleChatResponse(chatCompletion: ChatCompletion): Either<Exception, Conversation>
}

class RequestResponseHandlerImpl(
    private val promptProp: Properties,
): RequestResponseHandler {

    companion object {
        private const val MODEL_NAME = "gpt-3.5-turbo"
        private const val PROMPT_BEGINNER_KEY = "Beginner"
        private const val PROMPT_ADVANCED_KEY = "Advanced"
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
                // NOTE: トークン節約のため最後の6つの会話を送る
                else -> listOf(systemPrompt()) + conversation.takeLast(6).map(::toChatMessageRequest)
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
            ChatMessage(role = ChatRole.User, content = "Please say \"Let's talk\" if you have understood the prompt."),
        )
    }

    @OptIn(BetaOpenAI::class)
    private fun systemPrompt(): ChatMessage {
        return ChatMessage(role = ChatRole.System, content = promptProp.getProperty(PROMPT_BEGINNER_KEY))
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