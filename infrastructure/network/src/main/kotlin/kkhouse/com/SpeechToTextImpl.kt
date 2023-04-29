@file:OptIn(BetaOpenAI::class)

package kkhouse.com

import arrow.core.Either
import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.client.OpenAI
import com.google.cloud.speech.v1.RecognitionAudio
import com.google.cloud.speech.v1.RecognitionConfig
import com.google.cloud.speech.v1.SpeechClient
import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import kkhouse.com.handler.RequestResponseHandler
import kkhouse.com.repository.TranscriptText
import kkhouse.com.speech.Conversation
import kkhouse.com.speech.FlacData
import mu.KLogging
import java.nio.file.Paths


class SpeechToTextImpl(
    private val client: SpeechClient,
    private val storage: Storage,
    private val openAi: OpenAI,
    private val handler: RequestResponseHandler
): SpeechToText {

    companion object : KLogging() {
        // The ID of your GCS bucket
        const val bucketName = "english-conversation-backet"

        const val projectId = "english-conversation-app"
    }

    override fun uploadFlacFileOnGCP(flacData: FlacData): Result<FlacData> {
        return runCatching {
            val blobId = BlobId.of(bucketName, flacData.fileName)
            val blobInfo = BlobInfo.newBuilder(blobId).build()
            val precondition: Storage.BlobWriteOption = if (storage.get(bucketName, flacData.fileName) == null) {
                Storage.BlobWriteOption.doesNotExist()
            } else {
                Storage.BlobWriteOption.generationMatch(
                    storage.get(bucketName, flacData.fileName).generation
                )
            }
            storage.createFrom(blobInfo, Paths.get(flacData.localFilePath), precondition)

            logger.debug {
                "UploadFlacFile: ${flacData.localFilePath} uploaded to bucket $bucketName as ${flacData.fileName}"
            }

            flacData
        }
    }

    override fun deleteFlacFileOnGCP(flacData: FlacData): Result<Unit> {
        return runCatching {
            val blobId = BlobId.of(bucketName, flacData.fileName)
            storage.delete(blobId)
            logger.debug {
                "DeleteFlacFile:File $flacData.localFilePath deleted to bucket $bucketName as ${flacData.fileName}"
            }
        }
    }

    override fun postSpeechToText(flacData: FlacData): Result<TranscriptText> {
        val uri = "gs://$bucketName/${flacData.fileName}"
        val audio = RecognitionAudio.newBuilder()
            .setUri(uri)
            .build()
        val config = RecognitionConfig.newBuilder()
            .setEncoding(RecognitionConfig.AudioEncoding.FLAC)
            .setSampleRateHertz(flacData.sampleRate)
            .setLanguageCode(flacData.language)
            .setAudioChannelCount(flacData.chanelCount)
            .build()
        return runCatching {
            handler.handleSpeechToTextResponse(
                response = client.recognize(config, audio)
            ).toResultValue()
        }
    }

    @OptIn(BetaOpenAI::class)
    override suspend fun postCompletion(conversation: List<Conversation>?): Result<Conversation> {
        return runCatching {
            handler.handleChatResponse(
                chatCompletion = openAi.chatCompletion(handler.createChatRequest(conversation))
            ).toResultValue()
        }
    }

    private fun <A: Throwable, B> Either<A,B>.toResultValue(): B {
        return this.fold(
            ifRight = { it },
            ifLeft =  { throw it }
        )
    }
}