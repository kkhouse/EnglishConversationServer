package repository

import FlacData
import Resource

typealias TranscriptText = String
interface SpeechToTextRepository {
    fun oldRecognizeSpeech(flacBase64: String): Result<TranscriptText> // TODO 削除

    fun writeFlacFile(byteArray: ByteArray, fileName: FlacData): Resource<FlacData>

    fun deleteFlacFile(flacData: FlacData): Resource<Unit>

    fun uploadFlacFileToGCP(flacData: FlacData): Resource<FlacData>

    fun deleteFlacFileToGCP(flacData: FlacData): Resource<Unit>

    fun recognizeSpeech(flacData: FlacData): Resource<TranscriptText>
}