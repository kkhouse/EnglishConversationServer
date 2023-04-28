package kkhouse.com.mapping

import kkhouse.com.speech.Conversation
import kkhouse.com.speech.Role.Companion.createRole
import kkhousecom.QueryMessagesAndRolesForUserInChatRoom

fun QueryMessagesAndRolesForUserInChatRoom.mapConversation(): Conversation {
    return Conversation(
        chatID = this.id.toInt(),
        role = this.role.toInt().createRole(),
        message = this.message
    )
}