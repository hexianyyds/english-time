package com.feiyan.english.core.net

import android.util.Base64
import com.feiyan.english.core.model.YyBookDetail
import com.feiyan.english.core.model.YyBookState
import com.feiyan.english.core.model.YyChapterData
import com.feiyan.english.core.model.YyChapterState
import com.feiyan.english.core.model.YyHomeData
import com.feiyan.english.core.model.YyHomeState
import com.feiyan.english.core.model.YyParagraph
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.Request

/**
 * 英语小说网抓取。
 *
 * 站点是 Vue SPA，但服务端会把数据预渲染进
 * `window._yf_state = JSON.parse(atob(...))`，因此直接解析这段 base64 状态即可：
 *  - GET /                        -> main / hot / tuijian / more 书单
 *  - GET /book/{id}               -> 书籍信息 + 章节目录
 *  - GET /book/{id}/chapter/{n}   -> 双语正文
 */
object YyApi {

    private const val BASE = "https://www.yingyuxiaoshuo.com"

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        explicitNulls = false
    }

    private val stateRegex = Regex(
        """window\._yf_state=JSON\.parse\(decodeURIComponent\(escape\(window\.atob\('([^']+)'\)\)\)\)""",
    )

    suspend fun home(): YyHomeData {
        val html = fetchHtml("/")
        return decodeState(html, YyHomeState.serializer()).d?.data ?: YyHomeData()
    }

    suspend fun book(id: String): YyBookDetail {
        val html = fetchHtml("/book/$id")
        return decodeState(html, YyBookState.serializer()).d?.data?.book
            ?: throw ApiException(500, "书籍数据解析失败")
    }

    suspend fun chapter(bookId: String, index: Int): YyChapterData {
        val html = fetchHtml("/book/$bookId/chapter/$index")
        return decodeState(html, YyChapterState.serializer()).d?.data
            ?: throw ApiException(500, "章节数据解析失败")
    }

    /**
     * 把正文 HTML 拆成「英文 + 中文」段落对。
     * 站点结构是 <div class="c-en">英文</div><div class="c-cn" data-c="中文">…</div>，
     * 因此中文要回填到它前面最近的那段英文。
     */
    fun parseParagraphs(html: String): List<YyParagraph> {
        val regex = Regex("""<div class="c-(en|cn)"(?: data-c="([^"]*)")?>([\s\S]*?)</div>""")
        val result = mutableListOf<YyParagraph>()
        for (match in regex.findAll(html)) {
            val type = match.groupValues[1]
            val dataC = match.groupValues[2]
            val inner = match.groupValues[3]
            if (type == "en") {
                val english = cleanText(inner)
                if (english.isNotBlank()) result += YyParagraph(english, null)
            } else {
                val chinese = cleanText(unescape(dataC))
                if (chinese.isNotBlank() && result.isNotEmpty()) {
                    val last = result.last()
                    if (last.chinese.isNullOrBlank()) {
                        result[result.lastIndex] = last.copy(chinese = chinese)
                    }
                }
            }
        }
        return result
    }

    fun coverUrl(id: String): String = "$BASE/resource/cover/$id"

    private suspend fun fetchHtml(path: String): String = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(BASE + path)
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/126")
            .header("Accept", "text/html,application/xhtml+xml")
            .get()
            .build()
        FeiyanApi.httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw ApiException(response.code, "英语小说网返回 ${response.code}")
            response.body.string()
        }
    }

    private fun <T> decodeState(html: String, deserializer: kotlinx.serialization.DeserializationStrategy<T>): T {
        val encoded = stateRegex.find(html)?.groupValues?.get(1)
            ?: throw ApiException(500, "页面状态解析失败")
        val decoded = String(Base64.decode(encoded, Base64.DEFAULT), Charsets.UTF_8)
        return json.decodeFromString(deserializer, decoded)
    }

    private fun cleanText(text: String): String = unescape(text.replace(Regex("<[^>]+>"), " ")).trim()

    private fun unescape(text: String): String = text
        .replace("&nbsp;", " ")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
}
