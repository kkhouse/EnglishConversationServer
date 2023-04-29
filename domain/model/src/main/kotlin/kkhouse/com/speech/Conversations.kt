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
    val appChatRoom: Int?
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
    val appChatRoom: Int? = null,
    val conversation: List<Conversation>? = null,
    val errorCode: Int? = null
)
@Serializable
data class Conversation(
    val role: Int,
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
    val appChatRoom: Int,
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
    val appChatRoom: Int? = null,
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
    val appChatRoom: Int,
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