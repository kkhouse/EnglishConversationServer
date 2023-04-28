package kkhouse.com.persistent

import kkhouse.com.speech.ChatRoomId
import kkhousecom.ChatLogShemeQueries
import kkhousecom.QueryMessagesAndRolesForUserInChatRoom
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class ChatDataBaseImpl(
    private val queries: ChatLogShemeQueries,
    private val dispatcher: CoroutineDispatcher
): ChatDataBase {
    override suspend fun queryChatRoomsForUser(userId: String): Result<List<ChatRoomId>> {
        return withContext(dispatcher) {
            runCatching {
                queries.queryChatRoomsForUser(userId).executeAsList().map { it.id.toInt() }
            }
        }
    }

    override suspend fun queryMessagesAndRolesForUserInChatRoom(
        userId: String,
        chatRoomId: Int
    ): Result<List<QueryMessagesAndRolesForUserInChatRoom>> {
        return withContext(dispatcher) {
            runCatching {
                queries.queryMessagesAndRolesForUserInChatRoom(userId, chatRoomId.toLong()).executeAsList()
            }
        }
    }

    override suspend fun createUser(userId: String): Result<Unit> {
        return withContext(dispatcher) {
            runCatching {
                queries.createUser(userId)
            }
        }
    }

    override suspend fun createChatRoomForUser(userId: String): Result<Unit> {
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

    override suspend fun deleteUserAndRelatedData(userId: String): Result<Unit> {
        return withContext(dispatcher) {
            runCatching {
                queries.deleteUserAndRelatedData(userId)
            }
        }
    }
}