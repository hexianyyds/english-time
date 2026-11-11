package com.feiyan.english.core.model

import kotlinx.serialization.Serializable

/**
 * 词库中的一个可玩词条。
 * 有 [image] 时优先显示插画（食物主题来自站点素材包），否则回落到 [emoji]。
 */
@Serializable
data class GameWord(
    val word: String = "",
    val zh: String = "",
    val emoji: String = "",
    val pos: String = "",
    val topic: String = "",
    val level: Int = 1,
    /** assets 相对路径，如 game/food/apple.webp（列表/选项用） */
    val image: String = "",
    /** 大图版本，用于舞台特写；缺省时回落到 [image] */
    val imageHd: String = "",
    /** 站点原声：整段 ogg 里属于这个词的区间（毫秒）。为空则用系统 TTS。 */
    val audioFile: String = "",
    val audioStart: Int = 0,
    val audioEnd: Int = 0,
) {
    val hasAudio: Boolean get() = audioFile.isNotBlank() && audioEnd > audioStart

    val hasImage: Boolean get() = image.isNotBlank()

    fun imagePath(preferHd: Boolean): String =
        if (preferHd) imageHd.ifBlank { image } else image
}

/** 主题分组，用于出题时挑选同主题干扰项。 */
@Serializable
data class GameTopic(
    val key: String = "",
    val name: String = "",
    val emoji: String = "",
    val level: Int = 1,
)

@Serializable
data class GameWordBank(
    val version: Int = 1,
    val topics: List<GameTopic> = emptyList(),
    val words: List<GameWord> = emptyList(),
)
