package di

import LocalFileManager
import LocalFileManagerImpl
import org.koin.dsl.module

val databaseModule = module {
    single<LocalFileManager> { LocalFileManagerImpl() }
}