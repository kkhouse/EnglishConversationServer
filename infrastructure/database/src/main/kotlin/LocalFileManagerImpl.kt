import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import javax.sound.sampled.AudioSystem


class LocalFileManagerImpl (
): LocalFileManager {

    companion object {
        const val SAVE_LOCATION = "/infrastructure/repository/src/main/resources/"
    }

    override fun saveFile(byteArray: ByteArray, filename: String): Result<Unit> {
        val filePath = Path(".").absolutePathString() + SAVE_LOCATION
        return runCatching {
            Files.write(Paths.get(filePath, filename), byteArray)
        }
    }

    override fun deleteFile(fileName: String): Result<Unit> {
        val filePath = Path(".").absolutePathString() + SAVE_LOCATION
        return runCatching {
            Files.delete(Paths.get(filePath, fileName))
        }
    }

    /*
    TODO 抽象的にかけないか？
     */
    override fun analyzeFileData(filename: String): Result<FlacData> {
        val filePath = Path(".").absolutePathString() + SAVE_LOCATION + "/$filename"
        return runCatching {
            val file = File(filePath)
            val audioFormat = AudioSystem.getAudioInputStream(file).format
            val data = FlacData(
                fileName = filename,
                sampleRate = audioFormat.sampleRate.toInt(),
                chanelCount = audioFormat.channels,
                localFilePath = filePath
            )
            println("Debug: analyzeFileData is $data")
            data
        }
    }
}