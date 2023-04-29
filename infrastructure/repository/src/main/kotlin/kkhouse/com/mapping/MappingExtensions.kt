package kkhouse.com.mapping

import kkhouse.com.speech.Conversation
import kkhouse.com.speech.Role.Companion.createRole
import kkhousecom.QueryMessagesAndRolesForUserInChatRoom

fun QueryMessagesAndRolesForUserInChatRoom.mapConversation(): Conversation {
    return Conversation(
        role = this.role.toInt(),
        message = this.message
    )
}