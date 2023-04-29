package kkhouse.com.exceptions

/**
 * TextToSpeech
 */
data class MultiResultException(val unexpectedDataLog: String): Exception()
data class MultiChunkException(val unexpectedDataLog: String): Exception()
data class EmptyTextException(val unexpectedDataLog: String): Exception()

/**
 * ChatCompletion
 *
 */
data class UnexpectedCompletion(
    val unexpectedDataLog: String = "too much ChatChoice or empty message"
): Exception()