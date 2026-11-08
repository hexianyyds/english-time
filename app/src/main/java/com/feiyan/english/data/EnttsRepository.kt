package com.feiyan.english.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import okhttp3.FormBody
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

data class TtsVoice(
    val id: String,
    val label: String,
    val language: String,
)

data class TtsSpeed(
    val value: Int,
    val label: String,
)

@Serializable
data class EnttsNote(
    val p: String = "",
    val t: String = "",
    val w: List<List<String>> = emptyList(),
    val g: String = "",
)

object EnttsRepository {

    private const val BASE = "https://www.entts.com"
    private const val SENTENCE_ASSET = "entts/sentences-en.json"
    private const val NOTES_ASSET = "entts/notes-en.json"

    val voices = listOf(
        TtsVoice("en", "美式英语 · 男", "en-US"),
        TtsVoice("enf", "美式英语 · 女", "en-US"),
        TtsVoice("enuk", "英式英语 · 男", "en-GB"),
        TtsVoice("enukf", "英式英语 · 女", "en-GB"),
        TtsVoice("zh", "普通话 · 男", "zh-CN"),
        TtsVoice("zhf", "普通话 · 女", "zh-CN"),
        TtsVoice("zh-hk", "粤语 · 男", "zh-HK"),
        TtsVoice("zhf-hk", "粤语 · 女", "zh-HK"),
        TtsVoice("ja", "日语 · 男", "ja-JP"),
        TtsVoice("jaf", "日语 · 女", "ja-JP"),
        TtsVoice("ko", "韩语 · 男", "ko-KR"),
        TtsVoice("kof", "韩语 · 女", "ko-KR"),
        TtsVoice("de", "德语 · 男", "de-DE"),
        TtsVoice("def", "德语 · 女", "de-DE"),
        TtsVoice("ru", "俄语 · 男", "ru-RU"),
        TtsVoice("ruf", "俄语 · 女", "ru-RU"),
        TtsVoice("fr", "法语 · 男", "fr-FR"),
        TtsVoice("frf", "法语 · 女", "fr-FR"),
        TtsVoice("es", "西班牙语 · 男", "es-ES"),
        TtsVoice("esf", "西班牙语 · 女", "es-ES"),
        TtsVoice("pt", "葡萄牙语 · 男", "pt-PT"),
        TtsVoice("ptf", "葡萄牙语 · 女", "pt-PT"),
    )

    val speeds = listOf(
        TtsSpeed(-3, "很慢"),
        TtsSpeed(-2, "较慢"),
        TtsSpeed(-1, "稍慢"),
        TtsSpeed(0, "正常"),
        TtsSpeed(1, "稍快"),
        TtsSpeed(2, "较快"),
        TtsSpeed(3, "很快"),
    )

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .callTimeout(90, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    @Volatile
    private var sentencesCache: List<String>? = null

    @Volatile
    private var notesCache: Map<String, EnttsNote>? = null

    suspend fun sentences(context: Context): List<String> {
        sentencesCache?.let { return it }
        return withContext(Dispatchers.IO) {
            val text = context.assets.open(SENTENCE_ASSET)
                .bufferedReader(Charsets.UTF_8)
                .use { it.readText() }
            json.decodeFromString(ListSerializer(String.serializer()), text)
                .also { sentencesCache = it }
        }
    }

    suspend fun notes(context: Context): Map<String, EnttsNote> {
        notesCache?.let { return it }
        return withContext(Dispatchers.IO) {
            val text = context.assets.open(NOTES_ASSET)
                .bufferedReader(Charsets.UTF_8)
                .use { it.readText() }
            json.decodeFromString(
                MapSerializer(String.serializer(), EnttsNote.serializer()),
                text,
            ).also { notesCache = it }
        }
    }

    suspend fun split(
        text: String,
        voice: String,
        speed: Int,
    ): List<String> = withContext(Dispatchers.IO) {
        val body = FormBody.Builder()
            .add("format", "json")
            .add("content", text)
            .add("accent", voice)
            .add("speed", speed.toString())
            .build()
        val response = client.newCall(
            Request.Builder()
                .url("$BASE/api/post/split.php")
                .post(body)
                .header("Accept", "application/json")
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) EnglishTime/1.0")
                .header("Referer", "$BASE/")
                .build(),
        ).execute()
        response.use {
            if (!it.isSuccessful) throw IllegalStateException("文本分段失败 HTTP ${it.code}")
            val result = json.decodeFromString(EnttsSplitResponse.serializer(), it.body.string())
            if (!result.ok || result.segs.isEmpty()) {
                throw IllegalStateException("未能生成朗读分段")
            }
            result.segs
        }
    }

    suspend fun key(voice: String, speed: Int): String = withContext(Dispatchers.IO) {
        val body = FormBody.Builder()
            .add("accent", voice)
            .add("speed", speed.toString())
            .build()
        val response = client.newCall(
            Request.Builder()
                .url("$BASE/api/post/key.php")
                .post(body)
                .header("Accept", "application/json")
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) EnglishTime/1.0")
                .header("Referer", "$BASE/")
                .build(),
        ).execute()
        response.use {
            if (!it.isSuccessful) throw IllegalStateException("朗读签名获取失败 HTTP ${it.code}")
            val result = json.decodeFromString(EnttsKeyResponse.serializer(), it.body.string())
            if (!result.ok || result.key.isBlank()) {
                throw IllegalStateException("朗读服务暂时不可用")
            }
            result.key
        }
    }

    suspend fun audio(
        context: Context,
        text: String,
        voice: String,
        speed: Int,
        key: String,
    ): File = withContext(Dispatchers.IO) {
        val file = File(
            File(context.cacheDir, "entts").apply { mkdirs() },
            "${digest("$voice|$speed|$text")}.mp3",
        )
        if (file.exists() && file.length() > 0) return@withContext file

        val url = "$BASE/api/post/audio.php".toHttpUrl().newBuilder()
            .addQueryParameter("text", text)
            .addQueryParameter("key", key)
            .build()
        val response = client.newCall(
            Request.Builder()
                .url(url)
                .header("Accept", "audio/mpeg")
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) EnglishTime/1.0")
                .header("Referer", "$BASE/")
                .build(),
        ).execute()
        response.use {
            if (!it.isSuccessful) throw IllegalStateException("语音生成失败 HTTP ${it.code}")
            val contentType = it.header("Content-Type").orEmpty()
            if (!contentType.contains("audio", ignoreCase = true)) {
                throw IllegalStateException("语音生成失败，请稍后重试")
            }
            val temp = File(file.parentFile, "${file.name}.tmp")
            temp.outputStream().use { output ->
                it.body.byteStream().copyTo(output)
            }
            if (!temp.renameTo(file)) {
                temp.copyTo(file, overwrite = true)
                temp.delete()
            }
            file
        }
    }

    private fun digest(value: String): String {
        val bytes = MessageDigest.getInstance("MD5").digest(value.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it.toInt() and 0xff) }
    }

    @Serializable
    private data class EnttsSplitResponse(
        val ok: Boolean = false,
        val segs: List<String> = emptyList(),
    )

    @Serializable
    private data class EnttsKeyResponse(
        val ok: Boolean = false,
        val key: String = "",
        val ttl: Int = 0,
    )
}
