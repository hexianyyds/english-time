package com.feiyan.english.core.net

import com.feiyan.english.core.model.EgArticle
import com.feiyan.english.core.model.EgArticleDetail
import com.feiyan.english.core.model.EgSentenceGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import okhttp3.Request

/**
 * Englio 外刊抓取。
 *
 * 站点是 Next.js App Router：列表在 /explore 的 SSR HTML 里，
 * 正文在 /article/{id} 的 RSC flight 数据（self.__next_f.push）里。
 * 阅读本身不需要登录；更高级的 AI 分级 / 闪卡接口（/api/flashcards）才需要。
 */
object EgApi {

    private const val BASE = "https://englio.ai"

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        explicitNulls = false
    }

    private val cardRegex = Regex("""<a target="_blank" href="/article/([0-9a-f\-]{36})">([\s\S]{0,3000}?)</a>""")
    private val flightRegex = Regex("""self\.__next_f\.push\(\[1,("(?:[^"\\]|\\.)*")\]\)""")

    suspend fun articles(): List<EgArticle> {
        val html = fetchHtml("/explore")
        val result = mutableListOf<EgArticle>()
        for (match in cardRegex.findAll(html)) {
            val id = match.groupValues[1]
            val body = match.groupValues[2]
            val title = extractTag(body, "h3")?.let(::stripTags)?.let(::unescapeHtml) ?: continue
            if (title.isBlank()) continue
            val spans = Regex("""<span[^>]*>([^<]{1,40})</span>""")
                .findAll(body).map { it.groupValues[1].trim() }.toList()
            val words = Regex("""(\d+)<!-- --> 词""").find(body)?.groupValues?.get(1)?.toIntOrNull() ?: 0
            val date = Regex("""(\d{4}/\d{1,2}/\d{1,2})""").find(body)?.groupValues?.get(1).orEmpty()
            val level = Regex("""\b(A1|A2|B1|B2|C1|C2)\b""").find(body)?.groupValues?.get(1).orEmpty()
            result += EgArticle(
                id = id,
                title = title,
                category = spans.getOrNull(0).orEmpty(),
                source = spans.getOrNull(1).orEmpty(),
                date = date,
                wordCount = words,
                level = level,
            )
        }
        return result.distinctBy { it.id }
    }

    suspend fun article(id: String): EgArticleDetail {
        val html = fetchHtml("/article/$id")
        val payload = buildString {
            flightRegex.findAll(html).forEach { match ->
                runCatching {
                    json.decodeFromString(String.serializer(), match.groupValues[1])
                }.getOrNull()?.let(::append)
            }
        }
        if (payload.isEmpty()) throw ApiException(500, "文章数据解析失败")

        val title = Regex(""""headline"\s*:\s*"((?:[^"\\]|\\.)*)"""")
            .find(payload)?.groupValues?.get(1)?.let(::decodeLiteral)
            ?: extractTag(html, "h1")?.let(::stripTags)?.let(::unescapeHtml).orEmpty()
        val level = Regex(""""cefr_level"\s*:\s*"([A-C][12])"""")
            .find(payload)?.groupValues?.get(1).orEmpty()
        val summary = Regex(""""summary_zh"\s*:\s*"((?:[^"\\]|\\.)*)"""")
            .find(payload)?.groupValues?.get(1)?.let(::decodeLiteral).orEmpty()

        val groups = extractJsonArray(payload, "\"sentence_groups\":")
            ?.let { array ->
                runCatching {
                    json.decodeFromString(ListSerializer(EgSentenceGroup.serializer()), array)
                }.getOrNull()
            }.orEmpty()

        val sentences = groups.flatMap { group -> group.sentences }
            .map { it.trim() }
            .filter { it.isNotBlank() }

        return EgArticleDetail(
            id = id,
            title = title,
            level = level,
            summaryZh = summary,
            sentences = sentences,
        )
    }

    /** 登录后可用：闪卡数量（用于校验 Cookie 是否有效）。 */
    suspend fun flashcardsCount(cookie: String): Int? {
        if (cookie.isBlank()) return null
        return withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url("$BASE/api/flashcards?count=true")
                .header("Accept", "application/json")
                .header("Cookie", cookie)
                .get()
                .build()
            FeiyanApi.httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    Regex("""\d+""").find(response.body.string())?.value?.toIntOrNull() ?: 0
                } else {
                    null
                }
            }
        }
    }

    private suspend fun fetchHtml(path: String): String = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(BASE + path)
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/126")
            .header("Accept", "text/html,application/xhtml+xml")
            .get()
            .build()
        FeiyanApi.httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw ApiException(response.code, "Englio 返回 ${response.code}")
            response.body.string()
        }
    }

    private fun extractTag(html: String, tag: String): String? {
        val match = Regex("<$tag[^>]*>([\\s\\S]*?)</$tag>").find(html) ?: return null
        return match.groupValues[1]
    }

    private fun stripTags(text: String): String = text.replace(Regex("<[^>]+>"), "").trim()

    private fun unescapeHtml(text: String): String = text
        .replace("&#x27;", "'")
        .replace("&#39;", "'")
        .replace("&quot;", "\"")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&nbsp;", " ")

    private fun decodeLiteral(raw: String): String = runCatching {
        json.decodeFromString(String.serializer(), "\"$raw\"")
    }.getOrDefault(raw)

    /** 从 payload 中截取第一个完整的 JSON 数组。 */
    private fun extractJsonArray(payload: String, key: String): String? {
        val keyIndex = payload.indexOf(key)
        if (keyIndex < 0) return null
        val start = payload.indexOf('[', keyIndex)
        if (start < 0) return null
        var depth = 0
        var inString = false
        var escaped = false
        for (i in start until payload.length) {
            val c = payload[i]
            when {
                escaped -> escaped = false
                c == '\\' && inString -> escaped = true
                c == '"' -> inString = !inString
                !inString && c == '[' -> depth++
                !inString && c == ']' -> {
                    depth--
                    if (depth == 0) return payload.substring(start, i + 1)
                }
            }
        }
        return null
    }
}
