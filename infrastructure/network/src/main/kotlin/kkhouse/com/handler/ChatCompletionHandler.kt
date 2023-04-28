@file:OptIn(BetaOpenAI::class)

package kkhouse.com.handler

import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.chat.*
import com.aallam.openai.api.model.ModelId
import kkhouse.com.speech.Conversation

interface ChatCompletionHandler {
//    fun handleResponse(data : ChatCompletion): ChatCompletion
    fun createRequest(newContent: String, conversation: List<Conversation>?): ChatCompletionRequest
}

class ChatCompletionHandlerImpl(): ChatCompletionHandler {

    companion object {
        private const val TAG = "ChatCompletionHandlerImpl"
    }

//    override fun handleResponse(data: ChatCompletion): ChatCompletion {
//        /**
//         * 記憶しておくチャット量は全部で６件まで
//         * 超えた場合は一番古いチャットを消しておく
//         * UI表示はUI側にて全て記憶しておく。
//         */
//        when(chatMessageCache.size >= 6) {
//            true -> {
//                chatMessageCache.removeAt(1)
//                chatMessageCache.add(
//                    ChatMessage(
//                        role = ChatRole.User,
//                        content = data.choices.lastOrNull()?.message?.content ?:""
//                    )
//                )
//            }
//            else -> {
//                chatMessageCache.add(
//                    ChatMessage(
//                        role = ChatRole.User,
//                        content = data.choices.lastOrNull()?.message?.content ?:""
//                    )
//                )
//            }
//        }
//        Log.d(TAG, "handleResponse: chatMessageCache via response : $chatMessageCache")
//        return data
//    }

    @OptIn(BetaOpenAI::class)
    override fun createRequest(newContent: String, conversation: List<Conversation>?): ChatCompletionRequest {
        return conversation?.let {
            // 更新
            ChatCompletionRequestBuilder().apply {
                model = ModelId("gpt-3.5-turbo")
                messages = conversation.let {
                    it.map { Conversation() }
                }
            }.build()
        } ?: {
            // 初回
            ChatCompletionRequestBuilder().apply {
                model = ModelId("gpt-3.5-turbo")
                messages = getInitialPrompt()
            }.build()
        }
    }

    /**
     * TODO
     */
    @OptIn(BetaOpenAI::class)
    private fun getInitialPrompt() : List<ChatMessage> {
        return listOf(
            ChatMessage(role = ChatRole.System, content = ""),
            ChatMessage(role = ChatRole.User, content = ""),
        )
    }
}