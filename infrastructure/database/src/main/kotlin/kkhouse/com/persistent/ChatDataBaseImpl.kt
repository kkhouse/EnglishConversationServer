package kkhouse.com.persistent

import kkhouse.com.speech.ChatRoomId
import kkhousecom.ChatLogShemeQueries
import kkhousecom.QueryMessagesAndRolesForUserInChatRoom
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mu.KLogging

class ChatDataBaseImpl(
    private val queries: ChatLogShemeQueries,
    private val dispatcher: CoroutineDispatcher
): ChatDataBase {

    companion object : KLogging()

    init { log() }

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

    private fun log() {
        GlobalScope.launch {
            flow {
                while (true) {
                    kotlinx.coroutines.delay(10000)
                    try {
                        emit(queries.queryAllData().executeAsList())
                    }catch (e: Exception) {
                        logger.error { "allData is null ? : ${e.message}" }
                    }
                }
            }.collect { list ->
                list.forEachIndexed { index, queryAllData ->
                    logger.info {
                        "All database value \n" +
                                "index : $index " +
                                "queryAll : ${queryAllData.toString()}"
                    }
                }
            }
        }
    }
}