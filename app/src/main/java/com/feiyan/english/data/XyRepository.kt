package com.feiyan.english.data

import com.feiyan.english.core.model.XyEpisode
import com.feiyan.english.core.model.XyPodcast
import com.feiyan.english.core.net.XyApi

/**
 * 小宇宙「每日英语播客」数据入口：只保留免费单集。
 */
object XyRepository {

    /** 用户指定的播客：每日英语播客 */
    const val PODCAST_ID = "67d427c525802a186af2137a"

    @Volatile
    private var cache: XyPodcast? = null

    suspend fun podcast(force: Boolean = false): XyPodcast {
        val cached = cache
        if (!force && cached != null) return cached
        val podcast = XyApi.podcast(PODCAST_ID)
        val free = podcast.copy(
            episodes = podcast.episodes.filter { it.isFree && !it.audioUrl.isNullOrBlank() },
        )
        cache = free
        return free
    }

    suspend fun episode(eid: String): XyEpisode? =
        podcast().episodes.firstOrNull { it.eid == eid }
}
