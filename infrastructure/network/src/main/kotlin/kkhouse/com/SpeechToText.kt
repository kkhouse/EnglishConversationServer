package kkhouse.com

import kkhouse.com.repository.TranscriptText

interface SpeechToText {
    fun oldPostSpeechToText(content: String): Result<TranscriptText> // TODO 削除

    fun uploadFlatFileToStorage(flacData: FlacData): Result<FlacData>

    fun deleteFlatFileToStorage(flacData: FlacData): Result<Unit>

    fun postSpeechToText(flacData: FlacData): Result<TranscriptText>
}