package com.feiyan.english.core.net

import com.feiyan.english.core.model.EmEpisode
import com.feiyan.english.core.model.EmEpisodeDetail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import okhttp3.Request

/**
 * EnglishMeasure 开放接口（抓包自 englishmeasure.com）：
 *  - GET /api/podcasts            -> 全部单集（58 条，含分类 slug 与音频直链）
 *  - GET /api/podcasts/{slug}     -> 单集详情，附带 scriptJson 对话文稿
 */
object EmApi {

    private const val BASE = "https://englishmeasure.com"

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    suspend fun podcasts(): List<EmEpisode> = decode(
        ListSerializer(EmEpisode.serializer()),
        get("/api/podcasts"),
    )

    suspend fun podcast(slug: String): EmEpisodeDetail = decode(
        EmEpisodeDetail.serializer(),
        get("/api/podcasts/$slug"),
    )

    private suspend fun get(path: String): String = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(BASE + path)
            .header("Accept", "application/json")
            .header("User-Agent", "FeiyanEnglish/1.0 (Android)")
            .get()
            .build()
        FeiyanApi.httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw ApiException(response.code, "EnglishMeasure 返回 ${response.code}")
            response.body.string()
        }
    }

    private fun <T> decode(strategy: kotlinx.serialization.DeserializationStrategy<T>, body: String): T =
        json.decodeFromString(strategy, body)
}
