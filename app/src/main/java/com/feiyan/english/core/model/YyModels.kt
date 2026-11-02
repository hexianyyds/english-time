package com.feiyan.english.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** 英语小说网（yingyuxiaoshuo.com）书目。 */
@Serializable
data class YyBook(
    @SerialName("_id") val id: String = "",
    val category: String? = null,
    val cnAuthor: String? = null,
    val enAuthor: String? = null,
    val cnName: String = "",
    val enName: String = "",
    val intro: String? = null,
    val nandu: String? = null,
)

@Serializable
data class YyHomeData(
    val main: List<YyBook> = emptyList(),
    val hot: List<YyBook> = emptyList(),
    val tuijian: List<YyBook> = emptyList(),
    val more: List<YyBook> = emptyList(),
)

@Serializable
data class YyHomeState(val d: YyHomeD? = null)

@Serializable
data class YyHomeD(val data: YyHomeData? = null)

@Serializable
data class YyBookDetail(
    @SerialName("_id") val id: String = "",
    val category: String? = null,
    val cnAuthor: String? = null,
    val enAuthor: String? = null,
    val cnName: String = "",
    val enName: String = "",
    val intro: String? = null,
    val nandu: String? = null,
    /** 章节：index -> 标题 */
    val chapters: Map<String, String> = emptyMap(),
)

@Serializable
data class YyBookData(val book: YyBookDetail? = null)

@Serializable
data class YyBookState(val d: YyBookD? = null)

@Serializable
data class YyBookD(val data: YyBookData? = null)

@Serializable
data class YyChapterData(
    val id: String = "",
    /** 正文 HTML：<div class="c-en">英文</div><div class="c-cn" data-c="中文">查看中文翻译</div> */
    val c: String = "",
    val t: String = "",
    val n: String = "",
    val en: String = "",
    val i: Int = 0,
    val next: String? = null,
)

@Serializable
data class YyChapterState(val d: YyChapterD? = null)

@Serializable
data class YyChapterD(val data: YyChapterData? = null)

/** 双语段落。 */
data class YyParagraph(val english: String, val chinese: String?)
