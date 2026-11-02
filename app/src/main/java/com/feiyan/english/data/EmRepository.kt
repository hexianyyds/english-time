package com.feiyan.english.data

import com.feiyan.english.core.model.EmEpisode
import com.feiyan.english.core.model.EmEpisodeDetail
import com.feiyan.english.core.net.EmApi

/**
 * EnglishMeasure 内容入口（列表很小，整体缓存）。
 */
object EmRepository {

    @Volatile
    private var cache: List<EmEpisode>? = null

    suspend fun podcasts(force: Boolean = false): List<EmEpisode> {
        val cached = cache
        if (!force && cached != null) return cached
        // 只保留免费单集，会员专享的不进列表
        return EmApi.podcasts().filter { !it.premium }.also { cache = it }
    }

    suspend fun podcast(slug: String): EmEpisodeDetail = EmApi.podcast(slug)
}
