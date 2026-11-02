package com.feiyan.english.core.net

import com.feiyan.english.core.model.ChannelDetail
import com.feiyan.english.core.model.ChannelSearchResult
import com.feiyan.english.core.model.Episode
import com.feiyan.english.core.model.EpisodePage
import com.feiyan.english.core.model.HomeSection
import com.feiyan.english.core.model.Sentence
import com.feiyan.english.core.model.Transcript
import com.feiyan.english.core.model.UserInfo
import com.feiyan.english.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

/**
 * 飞岩播客开放接口封装（抓包自 www.feiyanpod.com）。
 *
 * 站点为 Nuxt SSR，客户端无 XHR，接口由页面 payload 反解得到：
 *  - GET /v2/index
 *  - GET /channel/{isbn}
 *  - GET /channel/episodes/{isbn}?page=&size=
 *  - GET /episodes/count/{isbn}
 *  - GET /episodes/unique-key/{key}
 *  - GET /episodes/play_auth/key/{key}   -> 纯文本音频地址
 *  - GET /episodes/transcript/preview/key/{key}
 *  - GET /channel/search?keyword=&page=&size=
 */
object FeiyanApi {

    private const val BASE = "https://api.feiyanpod.com/"

    /**
     * 登录态：编译期从 local.properties 注入做兜底，
     * 运行时可在「设置」里改写（TokenStore 持久化）。
     */
    @Volatile
    private var loginToken: String = BuildConfig.FEIYAN_TOKEN.trim()

    val isLoggedIn: Boolean get() = loginToken.isNotEmpty()

    fun currentToken(): String = loginToken

    fun setToken(token: String) {
        loginToken = token.trim()
    }

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        explicitNulls = false
    }

    val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .callTimeout(40, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .addInterceptor { chain ->
            val original = chain.request()
            val builder = original.newBuilder()
                .header("Accept", "application/json, text/plain, */*")
            // 各站点 Referer / UA 分开给，避免互相污染
            if (original.url.host.endsWith("bookyell.com")) {
                builder.header("User-Agent", "Mozilla/5.0 (Linux; Android 14) FeiyanEnglish/1.0")
                builder.header("Referer", "https://bookyell.com/")
                if (ByAuth.isLoggedIn) {
                    builder.header("Authorization", "Bearer ${ByAuth.current()}")
                }
            } else {
                builder.header("User-Agent", "FeiyanEnglish/1.0 (Android)")
                builder.header("Referer", "https://www.feiyanpod.com/")
                if (loginToken.isNotEmpty()) {
                    builder.header("Cookie", "token=$loginToken")
                }
            }
            chain.proceed(builder.build())
        }
        .build()

    suspend fun home(): List<HomeSection> = decode(
        ListSerializer(HomeSection.serializer()),
        get("v2/index"),
    )

    suspend fun channel(isbn: String): ChannelDetail = decode(
        ChannelDetail.serializer(),
        get("channel/$isbn"),
    )

    suspend fun episodes(isbn: String, page: Int, size: Int = 20): EpisodePage = decode(
        EpisodePage.serializer(),
        get("channel/episodes/$isbn?page=$page&size=$size"),
    )

    suspend fun episodeCount(isbn: String): Int = decode(
        Int.serializer(),
        get("episodes/count/$isbn"),
    )

    suspend fun episode(key: String): Episode = decode(
        Episode.serializer(),
        get("episodes/unique-key/$key"),
    )

    /** 返回可直接播放的签名音频地址（纯文本）。 */
    suspend fun playUrl(key: String): String = get("episodes/play_auth/key/$key").trim()

    suspend fun transcriptPreview(key: String): Transcript = decode(
        Transcript.serializer(),
        get("episodes/transcript/preview/key/$key"),
    )

    /** 登录后可用：完整字幕（按 lessonId 获取，游客调用返回 401）。 */
    suspend fun transcriptFull(lessonId: Long): List<Sentence> = decode(
        ListSerializer(Sentence.serializer()),
        get("episodes/transcript/$lessonId"),
    )

    /** 登录态校验：返回当前账号信息，未登录抛 401。 */
    suspend fun user(): UserInfo = decode(UserInfo.serializer(), get("user"))

    suspend fun search(keyword: String, page: Int = 1, size: Int = 20): ChannelSearchResult = decode(
        ChannelSearchResult.serializer(),
        get("channel/search?keyword=${URLEncoder.encode(keyword, "UTF-8")}&page=$page&size=$size"),
    )

    private suspend fun get(path: String): String = withContext(Dispatchers.IO) {
        val request = Request.Builder().url(BASE + path).get().build()
        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw ApiException(response.code, "接口返回 ${response.code}")
            }
            val body = response.body.string()
            if (body.isBlank()) throw ApiException(response.code, "接口返回空数据")
            body
        }
    }

    private fun <T> decode(deserializer: kotlinx.serialization.DeserializationStrategy<T>, body: String): T =
        json.decodeFromString(deserializer, body)
}

class ApiException(val code: Int, message: String) : Exception(message)
