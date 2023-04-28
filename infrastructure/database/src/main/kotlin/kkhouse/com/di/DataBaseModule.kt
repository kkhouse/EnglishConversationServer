package kkhouse.com.di

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kkhouse.com.ChatLogDataBase
import kkhouse.com.file.LocalFileManager
import kkhouse.com.file.LocalFileManagerImpl
import kkhouse.com.persistent.ChatDataBase
import kkhouse.com.persistent.ChatDataBaseImpl
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module

val databaseModule = module {
    single<LocalFileManager> { LocalFileManagerImpl() }
    single<ChatDataBase> {
        ChatDataBaseImpl(
            ChatLogDataBase(
                JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).apply {
                    ChatLogDataBase.Schema.create(this)
                }
            ).chatLogShemeQueries,
            Dispatchers.IO
        )
    }
}