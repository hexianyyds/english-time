package com.feiyan.english.data

import android.content.Context
import com.feiyan.english.core.model.WfArticle
import com.feiyan.english.core.model.WfArticleFile
import com.feiyan.english.core.model.WfBook
import com.feiyan.english.core.model.WfWord
import com.feiyan.english.core.model.WfWordPage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

/**
 * 单词森林（wordforest.cn）数据源：
 *  - 词书和单词详情走站点公开 API；
 *  - 词源/短文为服务端页面，已抓取为 assets/wordforest/articles.json。
 */
object WordForestRepository {

    const val WEB_BASE = "https://wordforest.cn"
    private const val ARTICLE_ASSET = "wordforest/articles.json"

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        explicitNulls = false
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .callTimeout(40, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    @Volatile
    private var booksCache: List<WfBook>? = null

    private val pageCache = mutableMapOf<String, WfWordPage>()

    @Volatile
    private var articlesCache: List<WfArticle>? = null

    suspend fun books(force: Boolean = false): List<WfBook> {
        booksCache?.takeUnless { force }?.let { return it }
        return withContext(Dispatchers.IO) {
            val body = get("/api/book/en/zh/books")
            val books = json.decodeFromString(ListSerializer(WfBook.serializer()), body)
            booksCache = books
            books
        }
    }

    suspend fun book(id: String): WfBook? = books().firstOrNull { it.id == id }

    suspend fun wordPage(
        bookId: String,
        day: Int,
        size: Int = 100,
    ): WfWordPage = withContext(Dispatchers.IO) {
        val key = "$bookId:$day:$size"
        pageCache[key]?.let { return@withContext it }
        val body = get(
            "/api/book/en/zh/word/details?id=$bookId&day=$day&size=$size" +
                "&random=false&customWordData=true",
        )
        val page = json.decodeFromString(WfWordPage.serializer(), body)
        pageCache[key] = page
        page
    }

    suspend fun dictionary(word: String): WfWord? = withContext(Dispatchers.IO) {
        val encoded = URLEncoder.encode(word.trim(), Charsets.UTF_8.name()).replace("+", "%20")
        val request = Request.Builder()
            .url("$WEB_BASE/api/dict/en/zh/$encoded")
            .header("Accept", "application/json")
            .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) EnglishTime/1.0")
            .header("Referer", "$WEB_BASE/dict")
            .get()
            .build()
        client.newCall(request).execute().use { response ->
            if (response.code == 404) return@withContext null
            if (!response.isSuccessful) {
                throw IllegalStateException("单词森林返回 HTTP ${response.code}")
            }
            json.decodeFromString(WfWord.serializer(), response.body.string())
        }
    }

    suspend fun articles(context: Context): List<WfArticle> {
        articlesCache?.let { return it }
        return withContext(Dispatchers.IO) {
            val text = context.assets.open(ARTICLE_ASSET)
                .bufferedReader(Charsets.UTF_8)
                .use { it.readText() }
            val file = json.decodeFromString(WfArticleFile.serializer(), text)
            articlesCache = file.articles
            file.articles
        }
    }

    suspend fun article(context: Context, id: String): WfArticle? =
        articles(context).firstOrNull { it.id == id }

    fun coverUrl(book: WfBook): String? {
        val cover = book.properties.cover
        if (cover.isBlank()) return null
        return if (cover.startsWith("http")) cover else WEB_BASE + cover
    }

    private fun get(path: String): String {
        val request = Request.Builder()
            .url(WEB_BASE + path)
            .header("Accept", "application/json")
            .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) EnglishTime/1.0")
            .header("Referer", "$WEB_BASE/books")
            .get()
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IllegalStateException("单词森林返回 HTTP ${response.code}")
            }
            return response.body.string()
        }
    }
}
