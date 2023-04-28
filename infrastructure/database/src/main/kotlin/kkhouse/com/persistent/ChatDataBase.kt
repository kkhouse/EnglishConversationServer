package kkhouse.com.persistent

import kkhouse.com.speech.ChatRoomId
import kkhousecom.QueryMessagesAndRolesForUserInChatRoom

interface ChatDataBase {

    /*
    検索
     */

    // ユーザに紐づくチャットルームを取得する
    suspend fun queryChatRoomsForUser(userId: String): Result<List<ChatRoomId>>


    // 特定ユーザの１チャットルームないのチャット履歴を全て取得する
    suspend fun queryMessagesAndRolesForUserInChatRoom(
        userId: String,
        chatRoomId: Int
    ): Result<List<QueryMessagesAndRolesForUserInChatRoom>>



    /*
    挿入
     */
    // 新規ユーザを作成する
    suspend fun createUser(userId: String): Result<Unit>

    // 新規チャットルームを作成する
    suspend fun createChatRoomForUser(userId: String): Result<Unit>

    // チャットルームに1つのメッセージを追加する
    suspend fun insertChatLogForUserInChatRoom(
        chatRoomId: Int,
        role: Int,
        message: String,
        createdAt: Long
    ): Result<Unit>

    /*
    削除
     */
    // ユーザとそれに紐づくチャットルーム、履歴を削除する
    suspend fun deleteUserAndRelatedData(userId: String): Result<Unit>


}