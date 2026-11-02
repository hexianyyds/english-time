package com.feiyan.english.data

import com.feiyan.english.core.model.ByArticle
import com.feiyan.english.core.model.ByBook
import com.feiyan.english.core.model.ByBookDetailData
import com.feiyan.english.core.model.ByChapterData
import com.feiyan.english.core.net.ByApi

/**
 * BookYell 数据入口：书目 / 文章都按页缓存，书籍详情与章节短缓存。
 */
object ByRepository {

    private val booksCache = mutableMapOf<Int, List<ByBook>>()
    private val articlesCache = mutableMapOf<Int, List<ByArticle>>()
    private val detailCache = mutableMapOf<String, ByBookDetailData>()
    private val chapterCache = mutableMapOf<String, ByChapterData>()

    suspend fun books(page: Int, force: Boolean = false): List<ByBook> {
        if (!force) booksCache[page]?.let { return it }
        return ByApi.books(page).also { booksCache[page] = it }
    }

    suspend fun articles(page: Int, force: Boolean = false): List<ByArticle> {
        if (!force) articlesCache[page]?.let { return it }
        return ByApi.articles(page).also { articlesCache[page] = it }
    }

    suspend fun bookDetail(bookId: String): ByBookDetailData {
        detailCache[bookId]?.let { return it }
        return ByApi.bookDetail(bookId).also { detailCache[bookId] = it }
    }

    suspend fun chapter(bookId: String, chapterId: Long): ByChapterData {
        val key = "$bookId-$chapterId"
        chapterCache[key]?.let { return it }
        return ByApi.chapter(bookId, chapterId).also { chapterCache[key] = it }
    }
}
