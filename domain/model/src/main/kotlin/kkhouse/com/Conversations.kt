package kkhouse.com

import kotlinx.serialization.Serializable
@Serializable
data class Conversation(
    val chatID: Int,
    val role: Role,
    val message: String
)

enum class Role {
    Assistant, User;
    companion object {
        fun Int.createRole(): Role {
            return when(this) {
                0 -> Assistant
                1 -> User
                else -> throw IllegalStateException("Unexpected role value from database")
            }
        }

        fun Role.getRowValue(): Int {
            return when(this) {
                Assistant -> 0
                User -> 1
            }
        }
    }
}

@Serializable
data class ChatData(
    val userId: Int? = null,
    val chatRoomId: Int? = null,
    val conversation: List<Conversation>? = null,
    val errorCode: Int? = null
)