package kkhouse.com.persistent

import kkhouse.com.speech.ChatRoomId
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import mu.KLogging
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inSubQuery
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

class ChatDataBaseImpl(
    private val dispatcher: CoroutineDispatcher,
    private val database: Database
): ChatDataBase {

    companion object : KLogging()

    override suspend fun queryChatRoomsForUser(userId: String): Result<List<ChatRoomId>> {
        return withContext(dispatcher) {
            runCatching {
                transaction(database) {
                    ChatRooms.select { ChatRooms.userId.eq(userId) }
                        .map { row -> row[ChatRooms.id].value }
                }
            }
        }
    }

    override suspend fun queryMessagesAndRolesForUserInChatRoom(
        userId: String,
        chatRoomId: Int
    ): Result<List<MessagesAndRolesForUserRoom>> {
        return withContext(dispatcher) {
            runCatching {
                transaction (database) {
                    (ChatLogs innerJoin ChatRooms).slice(ChatLogs.message, ChatLogs.role, ChatLogs.id)
                        .select { ChatRooms.userId.eq(userId) and ChatLogs.chatRoomId.eq(chatRoomId) }
                        .map { row ->
                            MessagesAndRolesForUserRoom(message = row[ChatLogs.message], role = row[ChatLogs.role])
                        }
                }
            }
        }
    }

    override suspend fun createUser(userId: String): Result<Unit> {
        return withContext(dispatcher) {
            runCatching {
                transaction (database) {
                    Users.insert {
                        it[Users.userId] = userId
                    }
                }
                Unit
            }
        }
    }

    override suspend fun createChatRoomForUser(userId: String): Result<Unit> {
        return withContext(dispatcher) {
            runCatching {
                transaction {
                    ChatRooms.insert {
                        it[ChatRooms.userId] = userId
                    }
                }
                Unit
            }
        }
    }

    override suspend fun insertChatLogForUserInChatRoom(
        chatRoomId: Int,
        role: Int,
        message: String,
        createdAt: LocalDateTime
    ): Result<Unit> {
        return withContext(dispatcher) {
            runCatching {
                transaction {
                    ChatLogs.insert {
                        it[ChatLogs.chatRoomId] = chatRoomId
                        it[ChatLogs.role] = role
                        it[ChatLogs.message] = message
                        it[ChatLogs.createdAt] = createdAt
                    }
                }
                Unit
            }
        }
    }

    override suspend fun deleteUserAndRelatedData(userId: String): Result<Unit> {
        return withContext(dispatcher) {
            runCatching {
                Users.select { Users.userId.eq(userId) }.singleOrNull()?.let { row ->
                    ChatLogs.deleteWhere { chatRoomId inSubQuery (ChatRooms.slice(ChatRooms.id).select { ChatRooms.userId.eq(userId) }) }
                    ChatRooms.deleteWhere { ChatRooms.userId.eq(userId) }
                    Users.deleteWhere { Users.id.eq(row[Users.id].value) }
                } ?: { throw IllegalArgumentException("delete target user is not existed") }
                Unit
            }
        }
    }
}