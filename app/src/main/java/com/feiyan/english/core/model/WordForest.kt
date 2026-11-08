package com.feiyan.english.core.model

import kotlinx.serialization.Serializable

@Serializable
data class WfBook(
    val properties: WfBookProperties = WfBookProperties(),
    val id: String = "",
    val total: Int = 0,
    val name: String = "",
    val type: String = "",
)

@Serializable
data class WfBookProperties(
    val cover: String = "",
    val description: String = "",
)

@Serializable
data class WfWordPage(
    val properties: WfWordPageProperties = WfWordPageProperties(),
    val id: String = "",
    val total: Int = 0,
    val name: String = "",
    val type: String = "",
    val words: List<WfWord> = emptyList(),
)

@Serializable
data class WfWordPageProperties(
    val random: Boolean = false,
    val description: String = "",
    val days: Int = 0,
    val size: Int = 0,
    val day: Int = 0,
)

@Serializable
data class WfWord(
    val id: Long = 0,
    val word: String = "",
    val language: String = "",
    val usPhonetic: String? = null,
    val ukPhonetic: String? = null,
    val explanations: WfMeanings? = null,
    val translations: WfMeanings? = null,
    val phrases: List<WfPhrase>? = null,
    val sentences: List<WfSentence>? = null,
    val families: List<String>? = null,
    val memory: String? = null,
)

@Serializable
data class WfMeanings(
    val meanings: Map<String, List<String>> = emptyMap(),
)

@Serializable
data class WfPhrase(
    val content: String = "",
    val translation: String = "",
)

@Serializable
data class WfSentence(
    val content: String = "",
    val translation: String = "",
)

@Serializable
data class WfArticleFile(
    val generatedAt: String = "",
    val source: String = "",
    val count: Int = 0,
    val articles: List<WfArticle> = emptyList(),
)

@Serializable
data class WfArticle(
    val id: String = "",
    val title: String = "",
    val category: String = "",
    val date: String = "",
    val summary: String = "",
    val blocks: List<WfArticleBlock> = emptyList(),
)

@Serializable
data class WfArticleBlock(
    val type: String = "",
    val level: Int = 0,
    val text: String = "",
    val ordered: Boolean = false,
    val items: List<String> = emptyList(),
    val url: String = "",
    val alt: String = "",
    val links: List<WfArticleLink> = emptyList(),
)

@Serializable
data class WfArticleLink(
    val text: String = "",
    val url: String = "",
)
