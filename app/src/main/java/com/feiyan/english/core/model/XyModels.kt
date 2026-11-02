package com.feiyan.english.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * 小宇宙（xiaoyuzhoufm.com）播客页 SSR 数据（Next.js __NEXT_DATA__）。
 * 公开接口只有页面数据可用，付费单集（payType != FREE）与试听（trial）都过滤掉。
 */
@Serializable
data class XyNextData(
    val props: XyProps? = null,
)

@Serializable
data class XyProps(
    @SerialName("pageProps") val pageProps: XyPageProps? = null,
)

@Serializable
data class XyPageProps(
    val podcast: XyPodcast? = null,
)

@Serializable
data class XyPodcast(
    val pid: String = "",
    val title: String = "",
    val author: String? = null,
    val description: String? = null,
    val image: XyImage? = null,
    val episodeCount: Int = 0,
    val subscriptionCount: Int = 0,
    val episodes: List<XyEpisode> = emptyList(),
)

@Serializable
data class XyImage(
    @SerialName("picUrl") val picUrl: String? = null,
    @SerialName("middlePicUrl") val middlePicUrl: String? = null,
    @SerialName("smallPicUrl") val smallPicUrl: String? = null,
    @SerialName("thumbnailUrl") val thumbnailUrl: String? = null,
) {
    val best: String? get() = middlePicUrl ?: picUrl ?: smallPicUrl ?: thumbnailUrl
}

@Serializable
data class XyEpisode(
    val eid: String = "",
    val pid: String = "",
    val title: String = "",
    val shownotes: String? = null,
    val duration: Long = 0,
    val enclosure: XyEnclosure? = null,
    val pubDate: String? = null,
    val payType: String? = null,
    val trial: JsonElement? = null,
    val image: XyImage? = null,
    val playCount: Long = 0,
) {
    val isFree: Boolean get() = payType == "FREE" && trial == null
    val audioUrl: String? get() = enclosure?.url
}

@Serializable
data class XyEnclosure(
    val url: String = "",
)
