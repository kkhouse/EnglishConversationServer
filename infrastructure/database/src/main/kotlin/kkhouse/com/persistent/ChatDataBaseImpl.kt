package kkhouse.com.persistent

import kkhouse.com.ChatRoomId
import kkhousecom.ChatLogShemeQueries
import kkhousecom.QueryMessagesAndRolesForUserInChatRoom
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class ChatDataBaseImpl(
    private val queries: ChatLogShemeQueries,
    private val dispatcher: CoroutineDispatcher
): ChatDataBase {
    override suspend fun queryChatRoomsForUser(userId: Long): Result<List<ChatRoomId>> {
        return withContext(dispatcher) {
            runCatching {
                queries.queryChatRoomsForUser(userId).executeAsList().map { it.id.toInt() }
            }
        }
    }

    override suspend fun queryMessagesAndRolesForUserInChatRoom(
        userId: Int,
        chatRoomId: Int
    ): Result<List<QueryMessagesAndRolesForUserInChatRoom>> {
        return withContext(dispatcher) {
            runCatching {
                queries.queryMessagesAndRolesForUserInChatRoom(userId.toLong(), chatRoomId.toLong()).executeAsList()
            }
        }
    }

    override suspend fun createUser(userId: Long): Result<Unit> {
        return withContext(dispatcher) {
            runCatching {
                queries.createUser(userId)
            }
        }
    }

    override suspend fun createChatRoomForUser(userId: Long): Result<Unit> {
        return withContext(dispatcher) {
            runCatching {
                queries.createChatRoomForUser(userId)
            }
        }
    }

    override suspend fun insertChatLogForUserInChatRoom(
        chatRoomId: Int,
        role: Int,
        message: String,
        createdAt: Long
    ): Result<Unit> {
        return withContext(dispatcher) {
            runCatching {
                queries.insertChatLogForUserInChatRoom(
                    chatRoomId.toLong(),
                    role.toLong(),
                    message,
                    createdAt
                )
            }
        }
    }

    override suspend fun deleteUserAndRelatedData(userId: Long): Result<Unit> {
        return withContext(dispatcher) {
            runCatching {
                queries.deleteUserAndRelatedData(userId)
            }
        }
    }
}