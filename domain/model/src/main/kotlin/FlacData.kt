import kotlinx.serialization.Serializable

data class FlacData(
    val fileName: String,
    val sampleRate: Int = 0,
    val chanelCount: Int = 1,
    val localFilePath: String =""
)


@Serializable
data class Audio(val sampleRate: Int, val channels: Int, val data: ByteArray)