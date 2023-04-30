package kkhouse.com.mapping

import kkhouse.com.persistent.MessagesAndRolesForUserRoom
import kkhouse.com.speech.Conversation
import kkhouse.com.speech.Role
import kkhousecom.QueryMessagesAndRolesForUserInChatRoom

fun MessagesAndRolesForUserRoom.mapConversation(): Conversation {
    return Conversation(
        role = this.getRole().value,
        message = this.message
    )
}