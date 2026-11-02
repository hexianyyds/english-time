package com.feiyan.english.data

import com.feiyan.english.core.model.EgArticle
import com.feiyan.english.core.model.EgArticleDetail
import com.feiyan.english.core.net.EgApi

/**
 * Englio 文章入口：列表整体缓存，正文按 id 缓存。
 */
object EgRepository {

    @Volatile
    private var listCache: List<EgArticle>? = null

    private val detailCache = mutableMapOf<String, EgArticleDetail>()

    suspend fun articles(force: Boolean = false): List<EgArticle> {
        val cached = listCache
        if (!force && cached != null) return cached
        return EgApi.articles().also { listCache = it }
    }

    suspend fun article(id: String): EgArticleDetail {
        detailCache[id]?.let { return it }
        return EgApi.article(id).also { detailCache[id] = it }
    }
}
