package kkhouse.com.persistent

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime

object Users : IntIdTable("user") {
    val userId = varchar("user_id", 255).uniqueIndex()
}

object ChatRooms : IntIdTable("chat_room") {
    val userId = reference("user_id", Users.userId)
}

object ChatLogs : IntIdTable("chat_log") {
    val chatRoomId = reference("chat_room_id", ChatRooms.id)
    val role = integer("role")
    val message = text("message")
    val createdAt = datetime("created_at")
}