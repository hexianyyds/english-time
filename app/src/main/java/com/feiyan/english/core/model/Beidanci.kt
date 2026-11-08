package com.feiyan.english.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BdCourse(
    val id: Int = 0,
    val key: String = "",
    val title: String = "",
    @SerialName("total_words")
    val totalWords: Int = 0,
    val units: List<BdUnit> = emptyList(),
)

@Serializable
data class BdUnit(
    val id: Int = 0,
    @SerialName("total_words")
    val totalWords: Int = 0,
    val key: String = "",
    val title: String = "",
    @SerialName("order_index")
    val orderIndex: Int = 0,
    @SerialName("start_index")
    val startIndex: Int = 0,
    @SerialName("membership_locked")
    val membershipLocked: Boolean = false,
)

@Serializable
data class BdWord(
    val id: Int = 0,
    val word: String = "",
    val meaning: String = "",
    val phonetic: String = "",
    val pos: String = "",
    @SerialName("example_en")
    val exampleEn: String = "",
    @SerialName("example_cn")
    val exampleCn: String = "",
    val distractors: List<String> = emptyList(),
)

data class BdResume(
    val courseKey: String,
    val courseTitle: String,
    val unitId: Int,
    val unitTitle: String,
)
