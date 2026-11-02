package com.feiyan.english.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** EnglishMeasure（englishmeasure.com）单集播客。 */
@Serializable
data class EmEpisode(
    @SerialName("_id") val id: String = "",
    val slug: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val audioUrl: String = "",
    val scriptUrl: String? = null,
    val publishedAt: String? = null,
    @SerialName("isPremiumEpisode") val premium: Boolean = false,
)

@Serializable
data class EmEpisodeDetail(
    @SerialName("_id") val id: String = "",
    val slug: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val audioUrl: String = "",
    val scriptUrl: String? = null,
    val publishedAt: String? = null,
    @SerialName("isPremiumEpisode") val premium: Boolean = false,
    val scriptJson: List<EmScriptLine> = emptyList(),
) {
    fun toEpisode(): EmEpisode = EmEpisode(
        id = id,
        slug = slug,
        title = title,
        description = description,
        category = category,
        audioUrl = audioUrl,
        scriptUrl = scriptUrl,
        publishedAt = publishedAt,
        premium = premium,
    )
}

@Serializable
data class EmScriptLine(
    val speaker: String = "",
    val text: String = "",
    val joke: Boolean = false,
)

/** 分类中英对照（接口只给 slug）。 */
object EmCategories {
    private val map = linkedMapOf(
        "real-english" to ("真实英语" to "Real English"),
        "history-makers" to ("历史人物" to "History Makers"),
        "global-exploration" to ("环球探索" to "Global Exploration"),
        "beliefs-mythology" to ("信仰与神话" to "Beliefs & Mythology"),
        "dystopia" to ("反乌托邦" to "Dystopia"),
        "dangerous-places" to ("险境" to "Dangerous Places"),
        "time-traveler" to ("时间旅人" to "Time Traveler"),
        "animal-kingdom" to ("动物王国" to "Animal Kingdom"),
    )

    val all: List<String> get() = map.keys.toList()

    fun label(slug: String): String = map[slug]?.first ?: slug

    fun english(slug: String): String = map[slug]?.second ?: slug
}
