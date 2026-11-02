package com.feiyan.english.data

import com.feiyan.english.core.model.YyBookDetail
import com.feiyan.english.core.model.YyChapterData
import com.feiyan.english.core.model.YyHomeData
import com.feiyan.english.core.net.YyApi

/**
 * 英语小说网数据入口。
 */
object YyRepository {

    @Volatile
    private var homeCache: YyHomeData? = null

    private val bookCache = mutableMapOf<String, YyBookDetail>()

    suspend fun home(force: Boolean = false): YyHomeData {
        val cached = homeCache
        if (!force && cached != null) return cached
        return YyApi.home().also { homeCache = it }
    }

    suspend fun book(id: String, force: Boolean = false): YyBookDetail {
        if (!force) bookCache[id]?.let { return it }
        return YyApi.book(id).also { bookCache[id] = it }
    }

    suspend fun chapter(bookId: String, index: Int): YyChapterData = YyApi.chapter(bookId, index)
}
