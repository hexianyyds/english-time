package com.feiyan.english.data

import android.content.Context
import com.feiyan.english.core.model.IpaPhoneme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit

object IpaRepository {

    const val BASE = "https://wxaiway.com/ipa"
    private const val ASSET = "ipa/phonemes.json"

    val comparisonPairs = listOf(
        "iː" to "i",
        "uː" to "ʊ",
        "ɑː" to "ʌ",
        "ɔː" to "ɒ",
        "e" to "æ",
        "p" to "b",
        "t" to "d",
        "k" to "g",
        "f" to "v",
        "s" to "z",
        "ʃ" to "ʒ",
        "θ" to "ð",
        "tʃ" to "dʒ",
        "tr" to "dr",
        "ts" to "dz",
        "m" to "n",
        "n" to "ŋ",
        "l" to "r",
        "w" to "v",
        "eɪ" to "aɪ",
        "aʊ" to "əʊ",
        "ɪə" to "eə",
    )

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .callTimeout(60, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    @Volatile
    private var cache: List<IpaPhoneme>? = null

    suspend fun all(context: Context): List<IpaPhoneme> {
        cache?.let { return it }
        return withContext(Dispatchers.IO) {
            val text = context.assets.open(ASSET)
                .bufferedReader(Charsets.UTF_8)
                .use { it.readText() }
            json.decodeFromString(ListSerializer(IpaPhoneme.serializer()), text)
                .also { cache = it }
        }
    }

    fun audioUrl(phoneme: IpaPhoneme): String =
        if (phoneme.audio.startsWith("http")) phoneme.audio else BASE + phoneme.audio

    suspend fun audioFile(context: Context, phoneme: IpaPhoneme): File = withContext(Dispatchers.IO) {
        val name = phoneme.audio.substringAfterLast('/').ifBlank { "${phoneme.id}.mp3" }
        val file = File(File(context.cacheDir, "ipa").apply { mkdirs() }, name)
        if (file.exists() && file.length() > 0) return@withContext file

        val request = Request.Builder()
            .url(audioUrl(phoneme))
            .header("Accept", "audio/mpeg,audio/*")
            .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) EnglishTime/1.0")
            .header("Referer", "$BASE/")
            .get()
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IllegalStateException("音标音频加载失败 HTTP ${response.code}")
            val temp = File(file.parentFile, "${file.name}.tmp")
            temp.outputStream().use { output -> response.body.byteStream().copyTo(output) }
            if (!temp.renameTo(file)) {
                temp.copyTo(file, overwrite = true)
                temp.delete()
            }
            file
        }
    }
}
