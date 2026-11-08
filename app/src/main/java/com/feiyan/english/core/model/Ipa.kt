package com.feiyan.english.core.model

import kotlinx.serialization.Serializable

@Serializable
data class IpaPhoneme(
    val id: String = "",
    val symbol: String = "",
    val category: String = "",
    val categoryLabel: String = "",
    val description: String = "",
    val mouthShape: String = "",
    val tips: String = "",
    val examples: List<IpaWord> = emptyList(),
    val extraWords: List<IpaWord> = emptyList(),
    val color: String = "",
    val audio: String = "",
)

@Serializable
data class IpaWord(
    val word: String = "",
    val phonetic: String = "",
    val translation: String = "",
)
