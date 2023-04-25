
import repository.SpeechToTextRepository
import repository.TranscriptText

class SpeechToTextRepositoryImpl(
    private val speechToText: SpeechToText,
    private val localFileManager: LocalFileManager
): SpeechToTextRepository {
    override fun oldRecognizeSpeech(flacBase64: String): Result<TranscriptText> {
        return speechToText.oldPostSpeechToText(content = flacBase64)
    }

    override fun writeFlacFile(byteArray: ByteArray, fileName: FlacData): Resource<FlacData> {
        return localFileManager.saveFile(byteArray, fileName.fileName)
            .map { localFileManager.analyzeFileData(fileName.fileName) }
            .fold(
                onSuccess = { it.toResource() },
                onFailure = { Resource.Failure(AppError.UnKnownError(it.message ?: "failed writing file"))}
            )
    }

    override fun deleteFlacFile(flacData: FlacData): Resource<Unit> {
        return localFileManager.deleteFile(fileName = flacData.fileName).toResource()
    }

    override fun uploadFlacFileToGCP(flacData: FlacData): Resource<FlacData> {
        return speechToText.uploadFlatFileToStorage(flacData).toResource()
    }

    override fun deleteFlacFileToGCP(flacData: FlacData): Resource<Unit> {
        return speechToText.deleteFlatFileToStorage(flacData).toResource()
    }

    override fun recognizeSpeech(flacData: FlacData): Resource<TranscriptText> {
        return speechToText.postSpeechToText(flacData).toResource()
    }

    /*
    TODO どこかそれっぽい所へ
     */
    private fun <T> Result<T>.toResource(
        mapAppError: (Throwable) -> AppError = { AppError.UnKnownError(it.message ?: "Error On Result to Resource Converter ")}
    ): Resource<T> {
        return this.fold(
            onSuccess = { Resource.Success(it) },
            onFailure = { Resource.Failure(mapAppError(it)) }
        )
    }
}