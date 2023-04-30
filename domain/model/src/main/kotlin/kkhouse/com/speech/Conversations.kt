package kkhouse.com.speech

import kotlinx.serialization.Serializable

typealias ChatRoomId = Int

/**
 * initialize
 */
// クライアントからのリクエスト
@Serializable
data class InitializedConversation(
    val userId: String?,
    private val appChatRoom: Int?
) {
    fun getRoomId(): ClientChatRoomId {
        return ClientChatRoomId.values().find {
            it.value == appChatRoom
        } ?: throw IllegalStateException("Unexpected appChatRoom value")
    }
}
// クライアントへのレスポンス
@Serializable
data class ChatData(
    val userId: String? = null,
    private val appChatRoom: Int? = null,
    val conversation: List<Conversation>? = null,
    val errorCode: Int? = null
) {
    fun getRoomId(): ClientChatRoomId {
        return ClientChatRoomId.values().find {
            it.value == appChatRoom
        } ?: throw IllegalStateException("Unexpected appChatRoom value")
    }

    fun getNewConversation(newUserContent: String): List<Conversation>? {
        return conversation?.plus(Conversation(Role.User.value, newUserContent))
    }
}
@Serializable
data class Conversation(
    private val role: Int,
    val message: String
) {
    fun getRole(): Role {
        return Role.values().find {
            it.value == this.role
        } ?: throw IllegalStateException("Unexpected role value")
    }
}

/**
 * upload
 */
// クライアントからのリクエスト
@Serializable
data class UploadData(
    val userId: String,
    private val appChatRoom: Int,
    val userFlacData: ByteArray,
) {
    fun getRoomId(): ClientChatRoomId {
        return ClientChatRoomId.values().find {
            it.value == appChatRoom
        } ?: throw IllegalStateException("Unexpected appChatRoom value")
    }
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UploadData

        if (userId != other.userId) return false
        if (appChatRoom != other.appChatRoom) return false
        return userFlacData.contentEquals(other.userFlacData)
    }

    override fun hashCode(): Int {
        var result = userId.hashCode()
        result = 31 * result + appChatRoom
        result = 31 * result + userFlacData.contentHashCode()
        return result
    }
}
// レスポンス
@Serializable
data class UploadResult(
    val userId: String? = null,
    private val appChatRoom: Int? = null,
    val speech: String? = null,
    val errorCode: Int? = null
)

/**
 * ChatCompletionへ
 */
// クライアントから
@Serializable
data class AiChatInquired(
    val userId: String,
    private val appChatRoom: Int,
    val speech: String
) {
    fun getRoomId(): ClientChatRoomId {
        return ClientChatRoomId.values().find {
            it.value == appChatRoom
        } ?: throw IllegalStateException("Unexpected appChatRoom value")
    }
}
// クライアントへ
// ChatDataと同様。

enum class ClientChatRoomId(val value: Int) {
    EnglishConversation(0), SearchExpression(1), Unknown(100)
}
enum class Role(val value: Int) {
    Assistant(0), User(1);
}

data class CacheRoomIds(
    val roomIds: List<ChatRoomId>
) {
    fun getRoomId(target : ClientChatRoomId): ChatRoomId {
        return when(target) {
            ClientChatRoomId.EnglishConversation -> roomIds[0]
            ClientChatRoomId.SearchExpression -> roomIds[1]
            ClientChatRoomId.Unknown -> throw IllegalStateException("Unexpected appChatRoom value")
        }
    }
}