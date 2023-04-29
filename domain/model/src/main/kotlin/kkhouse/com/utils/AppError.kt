package kkhouse.com.utils

sealed class AppError {
    data class UnKnownError(val message: String? = "Unknown Error") : AppError()
}

sealed class TextToSpeechError(val code: Int): AppError() {
    object InvalidResultText: TextToSpeechError(2)
    object InvalidChunk: TextToSpeechError(1)
}

sealed class AiSpeechError(val code : Int): AppError() {
    object UnexpectedResultData: AiSpeechError(1)
}
