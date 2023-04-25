sealed class AppError {
    data class UnKnownError(val message: String) : AppError()
}

sealed class TextToSpeechError: AppError() {
    object InvalidResultText: TextToSpeechError()
    object InvalidChunk: TextToSpeechError()

}
