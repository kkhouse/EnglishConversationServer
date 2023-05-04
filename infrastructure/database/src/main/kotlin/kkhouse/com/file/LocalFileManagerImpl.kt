package kkhouse.com.file

import kkhouse.com.speech.FlacData
import mu.KotlinLogging
import org.jaudiotagger.audio.AudioFileIO
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString

private val logger = KotlinLogging.logger {}

class LocalFileManagerImpl (
): LocalFileManager {

    companion object {
        const val SAVE_LOCATION = "/infrastructure/database/src/main/resources/"
    }

    override fun saveFile(byteArray: ByteArray, filename: String): Result<Unit> {
        val filePath = Path("").absolutePathString() + SAVE_LOCATION
        return runCatching {
            Files.write(Paths.get(filePath, filename), byteArray)
        }
    }

    override fun deleteFile(fileName: String): Result<Unit> {
        val filePath = Path("").absolutePathString() + SAVE_LOCATION
        return runCatching {
            Files.delete(Paths.get(filePath, fileName))
        }
    }

    override fun analyzeFileData(filename: String): Result<FlacData> {
        val filePath = Path("").absolutePathString() + SAVE_LOCATION + filename
        return runCatching {
            val file = File(filePath)
            val audioFile = AudioFileIO.read(file)
            val data = FlacData(
                fileName = filename,
                sampleRate = audioFile.audioHeader.sampleRateAsNumber,
                chanelCount = audioFile.audioHeader.channels.toInt(),
                localFilePath = filePath
            )
            logger.debug { "analyzeFileData is $data" }
            data
        }
    }
}