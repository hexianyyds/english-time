package com.feiyan.english.data

import com.feiyan.english.core.model.ChannelDetail
import com.feiyan.english.core.model.ChannelSearchResult
import com.feiyan.english.core.model.Episode
import com.feiyan.english.core.model.EpisodePage
import com.feiyan.english.core.model.HomeSection
import com.feiyan.english.core.model.Transcript
import com.feiyan.english.core.model.UserInfo
import com.feiyan.english.core.net.FeiyanApi

/**
 * 播客数据入口。首页做一次内存缓存，避免返回时重复请求。
 */
object PodcastRepository {

    @Volatile
    private var homeCache: List<HomeSection>? = null

    /** 会员专享分区 id，整段不展示。 */
    private const val SECTION_MEMBERS = -2

    suspend fun home(force: Boolean = false): List<HomeSection> {
        val cached = homeCache
        if (!force && cached != null) return cached
        return FeiyanApi.home().mapNotNull { section ->
            if (section.id == SECTION_MEMBERS) return@mapNotNull null
            val free = section.channels.filter { it.isMember == 0 }
            if (free.isEmpty()) null else section.copy(channels = free)
        }.also { homeCache = it }
    }

    suspend fun channel(isbn: String): ChannelDetail = FeiyanApi.channel(isbn)

    suspend fun episodes(isbn: String, page: Int, size: Int = 20): EpisodePage =
        FeiyanApi.episodes(isbn, page, size)

    suspend fun episodeCount(isbn: String): Int = FeiyanApi.episodeCount(isbn)

    suspend fun episode(key: String): Episode = FeiyanApi.episode(key)

    suspend fun playUrl(key: String): String = FeiyanApi.playUrl(key)

    /**
     * 登录优先取完整字幕（可能上百句），失败或游客时回落到 20 句试读。
     */
    suspend fun transcript(episode: Episode): Transcript {
        if (FeiyanApi.isLoggedIn && episode.id > 0L) {
            val full = runCatching { FeiyanApi.transcriptFull(episode.id) }.getOrNull()
            if (!full.isNullOrEmpty()) {
                return Transcript(count = full.size, sentences = full)
            }
        }
        return FeiyanApi.transcriptPreview(episode.uniqueKey)
    }

    suspend fun me(): UserInfo = FeiyanApi.user()

    suspend fun search(keyword: String): ChannelSearchResult {
        val result = FeiyanApi.search(keyword)
        return result.copy(channels = result.channels.filter { it.isMember == 0 })
    }
}
