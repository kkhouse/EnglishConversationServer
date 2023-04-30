package kkhouse.com.persistent

import kkhouse.com.speech.Role

data class MessagesAndRolesForUserRoom(
    val message: String,
    private val role: Int
) {
    fun getRole(): Role {
        return when(role) {
            Role.Assistant.value -> Role.Assistant
            Role.User.value -> Role.User
            else -> throw IllegalStateException("MessagesAndRolesForUserInChatRoom : invalid role value")
        }
    }
}