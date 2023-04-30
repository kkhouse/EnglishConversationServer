package kkhouse.com.di

import kkhouse.com.file.LocalFileManager
import kkhouse.com.file.LocalFileManagerImpl
import kkhouse.com.persistent.ChatDataBase
import kkhouse.com.persistent.ChatDataBaseImpl
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.koin.dsl.module

val databaseModule = module {
    single<LocalFileManager> { LocalFileManagerImpl() }
    single<ChatDataBase> {
        ChatDataBaseImpl(
            Dispatchers.IO,
            Database.connect(
                url = "jdbc:mysql://localhost:3306/chat_log_database",
//                driver = "com.mysql.jdbc.Driver",
                user = System.getenv("DB_USER"),
                password = System.getenv("DB_PASS")
            )
        )
    }
}