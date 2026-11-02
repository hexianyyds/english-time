package com.feiyan.english.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** 首页一组节目（热门节目 / 最近更新 / 会员专享 ...） */
@Serializable
data class HomeSection(
    val id: Int = 0,
    val name: String = "",
    val type: Int = 0,
    val channels: List<ChannelBrief> = emptyList(),
)

@Serializable
data class ChannelBrief(
    val id: Int = 0,
    val isbn: String = "",
    val cover: String? = null,
    @SerialName("english_name") val englishName: String? = null,
    val author: String? = null,
    val desc: String? = null,
    @SerialName("china_desc") val chinaDesc: String? = null,
    @SerialName("is_member") val isMember: Int = 0,
    @SerialName("is_course") val isCourse: Int = 0,
    @SerialName("cover_type") val coverType: Int = 0,
)

@Serializable
data class ChannelDetail(
    val id: Int = 0,
    val name: String = "",
    @SerialName("english_name") val englishName: String? = null,
    val desc: String? = null,
    val isbn: String = "",
    val cover: String? = null,
    val author: String? = null,
    @SerialName("category_name") val categoryName: String? = null,
    @SerialName("total_play_count") val totalPlayCount: Long = 0,
    @SerialName("is_member") val isMember: Int = 0,
    @SerialName("cover_type") val coverType: Int = 0,
    val background: String? = null,
)

@Serializable
data class EpisodePage(
    val episodes: List<Episode> = emptyList(),
    val latest: Episode? = null,
)

@Serializable
data class Episode(
    val id: Long = 0,
    val name: String = "",
    @SerialName("china_name") val chinaName: String? = null,
    @SerialName("unique_key") val uniqueKey: String = "",
    val desc: String? = null,
    val isbn: String = "",
    val duration: Long = 0,
    val date: Long = 0,
    val cover: String? = null,
    @SerialName("origin_img") val originImg: String? = null,
    @SerialName("is_preview") val isPreview: Boolean = false,
    @SerialName("preview_duration") val previewDuration: Long = 0,
    @SerialName("origin_mp3") val originMp3: String? = null,
) {
    val displayDate: Long get() = if (date > 0) date else 0L
}

@Serializable
data class Transcript(
    val count: Int = 0,
    val sentences: List<Sentence> = emptyList(),
)

@Serializable
data class Sentence(
    val id: Long = 0,
    val start: Long = 0,
    val end: Long = 0,
    val content: String = "",
    val translate: String? = null,
    @SerialName("is_ad") val isAd: Int = 0,
)

@Serializable
data class ChannelSearchResult(
    val channels: List<ChannelBrief> = emptyList(),
)

@Serializable
data class UserInfo(
    val id: Long = 0,
    val phone: String? = null,
    @SerialName("user_name") val userName: String? = null,
    val member: Boolean = false,
    @SerialName("member_expire_time") val memberExpireTime: Long = 0,
    @SerialName("unique_key") val uniqueKey: String? = null,
)
