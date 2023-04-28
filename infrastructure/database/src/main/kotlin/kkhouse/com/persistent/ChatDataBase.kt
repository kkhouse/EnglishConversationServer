package kkhouse.com.persistent

import kkhousecom.QueryMessagesAndRolesForUserInChatRoom

typealias ChatRoomId = Int
interface ChatDataBase {

    /*
    検索
     */

    // ユーザに紐づくチャットルームを取得する
    suspend fun queryChatRoomsForUser(userId: Long): Result<List<ChatRoomId>>


    // 特定ユーザの１チャットルームないのチャット履歴を全て取得する
    suspend fun queryMessagesAndRolesForUserInChatRoom(
        userId: Int,
        chatRoomId: Int
    ): Result<List<QueryMessagesAndRolesForUserInChatRoom>>



    /*
    挿入
     */
    // 新規ユーザを作成する
    suspend fun createUser(userId: Long): Result<Unit>

    // 新規チャットルームを作成する
    suspend fun createChatRoomForUser(userId: Long): Result<Unit>

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
    suspend fun deleteUserAndRelatedData(userId: Long): Result<Unit>


}