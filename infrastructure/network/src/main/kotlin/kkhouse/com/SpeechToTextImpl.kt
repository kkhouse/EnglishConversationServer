@file:OptIn(BetaOpenAI::class)

package kkhouse.com

import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.chat.ChatCompletion
import com.aallam.openai.client.OpenAI
import com.google.cloud.speech.v1.RecognitionAudio
import com.google.cloud.speech.v1.RecognitionConfig
import com.google.cloud.speech.v1.SpeechClient
import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import com.google.protobuf.ByteString
import kkhouse.com.exceptions.EmptyTextException
import kkhouse.com.exceptions.MultiChunkException
import kkhouse.com.exceptions.MultiResultException
import kkhouse.com.handler.RequestResponseHandler
import kkhouse.com.repository.TranscriptText
import kkhouse.com.speech.Conversation
import kkhouse.com.speech.FlacData
import java.nio.file.Paths


class SpeechToTextImpl (
    private val client: SpeechClient,
    private val storage: Storage,
    private val openAi: OpenAI,
    private val handler: RequestResponseHandler
): SpeechToText {

    companion object {
        // The ID of your GCS bucket
        const val bucketName = "english-conversation-backet"

        const val projectId = "english-conversation-app"
    }
    override fun oldPostSpeechToText(content: String): Result<TranscriptText> {
        val byte = ByteString.copyFromUtf8(content)
        val audio = RecognitionAudio.newBuilder()
            .setContent(byte)
//            .setUri("gs://cloud-samples-tests/speech/brooklyn.flac")
            .build()
        val config = RecognitionConfig.newBuilder()
            .setEncoding(RecognitionConfig.AudioEncoding.FLAC)
            .setSampleRateHertz(24000)
            .setLanguageCode("en-US")
            .setAudioChannelCount(1)
            .build()
        return runCatching {
            val results = client.recognize(config, audio)
            when {
                results.resultsList.lastIndex != 0 -> throw MultiResultException(results.toString()) // TODO 2つ以上になるケースがドキュメントから追えてない
                results.resultsList[0].alternativesCount != 1 -> throw MultiChunkException(results.toString())
                results.resultsList[0].alternativesList[0].transcript.isEmpty() -> throw EmptyTextException(results.toString())
                else -> results.resultsList[0].alternativesList[0].transcript
            }
        }
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

            println("DEBUG: File $flacData.localFilePath uploaded to bucket $bucketName as ${flacData.fileName}")

            flacData
        }
    }

    override fun deleteFlacFileOnGCP(flacData: FlacData): Result<Unit> {
        return runCatching {
            val blobId = BlobId.of(bucketName, flacData.fileName)
            storage.delete(blobId)
            println("DEBUG: File $flacData.localFilePath deleted to bucket $bucketName as ${flacData.fileName}")
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
            .setLanguageCode("en-US")
            .setAudioChannelCount(flacData.chanelCount)
            .build()
        return runCatching {
            val results = client.recognize(config, audio)
            when {
                results.resultsList.lastIndex != 0 -> throw MultiResultException(results.toString()) // TODO 2つ以上になるケースがドキュメントから追えてない
                results.resultsList[0].alternativesCount != 1 -> throw MultiChunkException(results.toString())
                results.resultsList[0].alternativesList[0].transcript.isEmpty() -> throw EmptyTextException(results.toString())
                else -> results.resultsList[0].alternativesList[0].transcript
            }
        }
    }

    override suspend fun postCompletion(newMessage: String, conversation: List<Conversation>?): Result<ChatCompletion> {
        return runCatching {
            openAi.chatCompletion(handler.createChatRequest(newMessage, conversation))
        }
    }
}