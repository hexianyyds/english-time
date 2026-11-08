package com.feiyan.english.data

import android.content.Context
import com.feiyan.english.core.model.IcibaCatalog
import com.feiyan.english.core.model.IcibaGroup
import com.feiyan.english.core.model.IcibaLesson
import com.feiyan.english.core.model.IcibaWord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * 爱词吧（word.iciba.com）：
 *  - 首页分类/词书目录已抓取并内置为 assets/iciba/catalog.json；
 *  - 课表与每课单词由服务端 HTML 渲染，没有 JSON 接口，运行时按需解析。
 *
 * 站点按分钟限流约 20 请求，因此课程与单词页做进程内缓存。
 */
object IcibaRepository {

    private const val BASE = "https://word.iciba.com"
    private const val CATALOG_ASSET = "iciba/catalog.json"

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .callTimeout(40, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    @Volatile
    private var catalogCache: IcibaCatalog? = null

    private val lessonCache = ConcurrentHashMap<String, List<IcibaLesson>>()
    private val wordCache = ConcurrentHashMap<String, List<IcibaWord>>()

    suspend fun catalog(context: Context): IcibaCatalog {
        catalogCache?.let { return it }
        return withContext(Dispatchers.IO) {
            val text = context.assets.open(CATALOG_ASSET)
                .bufferedReader(Charsets.UTF_8)
                .use { it.readText() }
            json.decodeFromString(IcibaCatalog.serializer(), text).also { catalogCache = it }
        }
    }

    suspend fun group(context: Context, classId: String): IcibaGroup? =
        catalog(context).groups.firstOrNull { it.classId == classId }

    suspend fun lessons(
        classId: String,
        force: Boolean = false,
    ): List<IcibaLesson> {
        if (!force) lessonCache[classId]?.let { return it }
        return withContext(Dispatchers.IO) {
            val html = get("/?action=courses&classid=$classId")
            val document = Jsoup.parse(html, BASE)
            val lessons = document.select("li.c_panel[course_id]")
                .mapNotNull { item ->
                    val courseId = item.attr("course_id").toIntOrNull() ?: return@mapNotNull null
                    val title = item.selectFirst("h4")?.text()?.trim().orEmpty()
                    val wordCount = Regex("""(\d+)\s*个单词""")
                        .find(item.selectFirst("p")?.text().orEmpty())
                        ?.groupValues
                        ?.getOrNull(1)
                        ?.toIntOrNull()
                        ?: 0
                    if (courseId <= 0 || wordCount <= 0 || title.isBlank()) {
                        null
                    } else {
                        IcibaLesson(courseId = courseId, title = title, wordCount = wordCount)
                    }
                }
                .distinctBy { it.courseId }
                .sortedBy { it.courseId }
            lessonCache[classId] = lessons
            lessons
        }
    }

    suspend fun words(
        classId: String,
        courseId: Int,
        force: Boolean = false,
    ): List<IcibaWord> {
        val key = "$classId:$courseId"
        if (!force) wordCache[key]?.let { return it }
        return withContext(Dispatchers.IO) {
            val html = get("/?action=words&class=$classId&course=$courseId")
            val document = Jsoup.parse(html, BASE)
            val words = document.select("ul.word_main_list > li")
                .mapNotNull { item ->
                    val word = item.selectFirst(".word_main_list_w span[title]")
                        ?.text()
                        ?.trim()
                        .orEmpty()
                    if (word.isBlank()) {
                        null
                    } else {
                        IcibaWord(
                            word = word,
                            phonetic = item.selectFirst(".word_main_list_y strong")
                                ?.text()
                                ?.trim()
                                ?.trim('[', ']')
                                .orEmpty(),
                            meaning = item.selectFirst(".word_main_list_s span[title]")
                                ?.text()
                                ?.trim()
                                .orEmpty(),
                        )
                    }
                }
                .distinctBy { it.word.lowercase() }
            wordCache[key] = words
            words
        }
    }

    private fun get(path: String): String {
        val request = Request.Builder()
            .url(BASE + path)
            .header("Accept", "text/html,application/xhtml+xml")
            .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
            .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) EnglishTime/1.0")
            .header("Referer", "$BASE/")
            .get()
            .build()
        client.newCall(request).execute().use { response ->
            if (response.code == 429) {
                throw IllegalStateException("爱词吧请求过于频繁，请等待约一分钟后再试")
            }
            if (!response.isSuccessful) {
                throw IllegalStateException("爱词吧返回 HTTP ${response.code}")
            }
            return response.body.string()
        }
    }
}
