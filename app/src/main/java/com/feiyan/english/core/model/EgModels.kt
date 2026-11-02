package com.feiyan.english.core.model

import kotlinx.serialization.Serializable

/** Englio（englio.ai）外刊文章。 */
data class EgArticle(
    val id: String,
    val title: String,
    val category: String,
    val source: String,
    val date: String,
    val wordCount: Int,
    val level: String,
)

data class EgArticleDetail(
    val id: String,
    val title: String,
    val level: String,
    val summaryZh: String,
    val sentences: List<String>,
)

@Serializable
data class EgSentenceGroup(
    val id: Int = 0,
    val sentences: List<String> = emptyList(),
)
