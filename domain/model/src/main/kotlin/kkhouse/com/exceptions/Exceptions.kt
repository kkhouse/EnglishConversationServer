package kkhouse.com.exceptions

data class MultiResultException(val unexpectedDataLog: String): Exception()
data class MultiChunkException(val unexpectedDataLog: String): Exception()

data class EmptyTextException(val unexpectedDataLog: String): Exception()