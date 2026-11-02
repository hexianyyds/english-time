package com.feiyan.english.core.net

import com.feiyan.english.core.model.ByArticle
import com.feiyan.english.core.model.ByArticlesResponse
import com.feiyan.english.core.model.ByBook
import com.feiyan.english.core.model.ByBookDetailData
import com.feiyan.english.core.model.ByBookDetailResponse
import com.feiyan.english.core.model.ByBooksResponse
import com.feiyan.english.core.model.ByChapterData
import com.feiyan.english.core.model.ByChapterResponse
import com.feiyan.english.core.model.ByMe
import com.feiyan.english.core.model.ByMeResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.Request

/**
 * BookYell 开放接口（抓包自 bookyell.com）。
 *  - GET /api/web/books?page=           书目（固定 20/页）
 *  - GET /api/web/book/detail?bookId=   书籍 + 章节目录
 *  - GET /api/web/articles?page=        文章（The Conversation 等）
 *  - GET /api/reader/chapter?bookId=&chapterId=  章节正文（段落 / 句子 / 单词释义）
 *
 * 阅读与词义都在公开接口里，不需要登录。
 */
object ByApi {

    private const val BASE = "https://bookyell.com"

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        explicitNulls = false
    }

    suspend fun books(page: Int): List<ByBook> = decode(
        ByBooksResponse.serializer(),
        get("/api/web/books?page=$page"),
    ).data?.books.orEmpty()

    suspend fun bookDetail(bookId: String): ByBookDetailData = decode(
        ByBookDetailResponse.serializer(),
        get("/api/web/book/detail?bookId=$bookId"),
    ).data ?: ByBookDetailData()

    suspend fun articles(page: Int): List<ByArticle> = decode(
        ByArticlesResponse.serializer(),
        get("/api/web/articles?page=$page"),
    ).data?.articles.orEmpty()

    suspend fun chapter(bookId: String, chapterId: Long): ByChapterData = decode(
        ByChapterResponse.serializer(),
        get("/api/reader/chapter?bookId=$bookId&chapterId=$chapterId"),
    ).data ?: throw ApiException(500, "章节数据为空")

    /** 登录态校验（Bearer JWT）。 */
    suspend fun me(): ByMe? = decode(
        ByMeResponse.serializer(),
        get("/api/web/auth/me"),
    ).data

    /**
     * 章节朗读音频：直接返回音频流，需要 Authorization。
     * 游客 / 额度用完时服务端返回 JSON 错误（40100 / 40101）。
     */
    fun audioUrl(bookId: String, chapterId: Long): String =
        "$BASE/api/reader/chapter/audio?bookId=$bookId&chapterId=$chapterId"

    private suspend fun get(path: String): String = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(BASE + path)
            .header("Accept", "application/json")
            .header("Origin", BASE)
            .header("Referer", "$BASE/")
            .header("User-Agent", "FeiyanEnglish/1.0 (Android)")
            .get()
            .build()
        FeiyanApi.httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw ApiException(response.code, "BookYell 返回 ${response.code}")
            response.body.string()
        }
    }

    private fun <T> decode(strategy: kotlinx.serialization.DeserializationStrategy<T>, body: String): T =
        json.decodeFromString(strategy, body)
}
