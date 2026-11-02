package com.feiyan.english.core.net

import com.feiyan.english.core.model.XyNextData
import com.feiyan.english.core.model.XyPodcast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.Request

/**
 * 小宇宙公开数据抓取。
 *
 * web-api.xiaoyuzhoufm.com 的 /web/podcast 系列需要登录态（401），
 * 但播客页是 Next.js SSR，最新一页单集直接内嵌在 __NEXT_DATA__ 里，
 * 因此这里解析页面数据，只取 FREE 单集（付费与试听都不要）。
 */
object XyApi {

    private const val BASE = "https://www.xiaoyuzhoufm.com/podcast/"

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        explicitNulls = false
    }

    suspend fun podcast(pid: String): XyPodcast = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(BASE + pid)
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/126")
            .header("Accept", "text/html,application/xhtml+xml")
            .get()
            .build()
        val html = FeiyanApi.httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw ApiException(response.code, "小宇宙返回 ${response.code}")
            response.body.string()
        }
        val payload = extractNextData(html) ?: throw ApiException(500, "页面数据解析失败")
        json.decodeFromString(XyNextData.serializer(), payload).props?.pageProps?.podcast
            ?: throw ApiException(500, "页面数据为空")
    }

    /** 从 HTML 中截取 <script id="__NEXT_DATA__"> 的 JSON。 */
    private fun extractNextData(html: String): String? {
        val marker = "id=\"__NEXT_DATA__\""
        val start = html.indexOf(marker)
        if (start < 0) return null
        val open = html.indexOf('>', start)
        if (open < 0) return null
        val close = html.indexOf("</script>", open)
        if (close < 0) return null
        return html.substring(open + 1, close).trim()
    }
}
