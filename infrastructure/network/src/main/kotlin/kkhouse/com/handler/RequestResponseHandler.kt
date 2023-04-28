@file:OptIn(BetaOpenAI::class)

package kkhouse.com.handler

import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.chat.*
import com.aallam.openai.api.model.ModelId
import kkhouse.com.speech.Conversation
import kkhouse.com.speech.Role

interface RequestResponseHandler {
    fun createChatRequest(newContent: String, conversation: List<Conversation>?): ChatCompletionRequest
}

class RequestResponseHandlerImpl(): RequestResponseHandler {

    companion object {
        private const val TAG = "ChatCompletionHandlerImpl"
        private const val MODEL_NAME = "gpt-3.5-turbo"
    }

    @OptIn(BetaOpenAI::class)
    private val systemRolePrompt = ChatMessage(
        role = ChatRole.System,
        content = ""
    )

    @OptIn(BetaOpenAI::class)
    override fun createChatRequest(newContent: String, conversation: List<Conversation>?): ChatCompletionRequest {
        return ChatCompletionRequestBuilder().apply {
            model = ModelId(MODEL_NAME)
            messages = when(conversation) {
                null -> getInitialPrompt()
                // NOTE: トークン節約のため最後の6つの会話を送る
                else -> listOf(systemRolePrompt) + conversation.takeLast(6).map(::toChatMessageRequest)
            }
        }.build()
    }

    /**
     * TODO
     */
    @OptIn(BetaOpenAI::class)
    private fun getInitialPrompt() : List<ChatMessage> {
        return listOf(
            systemRolePrompt,
            ChatMessage(role = ChatRole.User, content = ""),
        )
    }

    private fun toChatMessageRequest(conversation: Conversation): ChatMessage {
        return ChatMessage(
            role = when(conversation.role) {
                Role.Assistant -> ChatRole.Assistant
                Role.User -> ChatRole.User
            },
            content = conversation.message
        )
    }
}