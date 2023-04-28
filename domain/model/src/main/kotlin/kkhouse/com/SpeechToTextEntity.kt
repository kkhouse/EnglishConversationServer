package kkhouse.com

import kotlinx.serialization.Serializable

@Serializable
data class ByteFlacData(
    val data: String,
    val type: String
)

/**
 * @param errorCode
 *  0 : 想定外
 *  1 :　チャンクが多い
 *  2 :  空文字レスポンス or 結果が複数
 */
@Serializable
data class SpeechToTextResult(
    val text: String,
    val errorCode: String? = null
) {
    companion object {
        fun createErrorCodeResult(appError: AppError): SpeechToTextResult {
            val errorCode = when(appError is TextToSpeechError) {
                true -> when(appError) {
                    TextToSpeechError.InvalidChunk -> "1"
                    TextToSpeechError.InvalidResultText -> "2"
                }
                else -> "0"
            }
            return SpeechToTextResult(text = "", errorCode = errorCode)
        }
    }
}