interface LocalFileManager {
    fun saveFile(byteArray: ByteArray, filename: String): Result<Unit>

    fun deleteFile(fileName: String): Result<Unit>

    fun analyzeFileData(filename: String): Result<FlacData>
}