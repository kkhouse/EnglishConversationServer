
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kkhouse.com.ChatLogDataBase
import kkhouse.com.persistent.ChatDataBaseImpl
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * TODO
 */
class ChatDataBaseImplTest {
    private lateinit var chatDatabase: ChatLogDataBase
    private lateinit var chatDataBaseImpl: ChatDataBaseImpl
    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setup() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).apply {
            ChatLogDataBase.Schema.create(this)
        }
        chatDatabase = ChatLogDataBase(driver)
        val chatLogQueries = chatDatabase.chatLogShemeQueries
        chatDataBaseImpl = ChatDataBaseImpl(chatLogQueries, testDispatcher)
    }

    @Test
    fun test_queryChatRoomsForUser() {
//        runBlockingTest {
//            val user = 1234L
//            chatDataBaseImpl.createUser(user)
//            chatDataBaseImpl.createChatRoomForUser(user)
//            val result = chatDataBaseImpl.queryChatRoomsForUser(user)
//
//            result.fold(
//                onSuccess = {
//                    assert(it.size == 1)
//                    assert(it[0] == 0)
//                },
//                onFailure = {
//                    throw IllegalStateException("test fail")
//                }
//            )
//        }
    }
}