package com.feiyan.english.core.model

import kotlinx.serialization.Serializable

/**
 * 语法题：站点 superlatives / comparatives 两个游戏的题库。
 *
 * comparative 的 prompt 里用 `___` 表示空位（如 "Horses are ___ than cows."），
 * superlative 的 prompt 是问句（如 "Who is the tallest?"），选项为完整句子。
 */
@Serializable
data class GrammarItem(
    val type: String = "",
    val prompt: String = "",
    /** 原级形容词提示，仅 comparative 有。 */
    val hint: String = "",
    val answer: String = "",
    val options: List<String> = emptyList(),
) {
    val hasBlank: Boolean get() = prompt.contains("___")

    /** 空位前 / 后的文本，用于把答案嵌进句子中间。 */
    val prefix: String get() = if (hasBlank) prompt.substringBefore("___").trim() else prompt
    val suffix: String get() = if (hasBlank) prompt.substringAfter("___").trim() else ""
}

@Serializable
data class GrammarPack(
    val source: String = "",
    val items: List<GrammarItem> = emptyList(),
)
