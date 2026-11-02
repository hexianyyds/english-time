package com.feiyan.english.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** BookYell（bookyell.com）书目与文章。 */
@Serializable
data class ByAuthor(
    val name: String? = null,
    @SerialName("name_cn") val nameCn: String? = null,
)

@Serializable
data class ByBook(
    val id: String = "",
    val name: String = "",
    @SerialName("logo_url") val logoUrl: String? = null,
    val desc: String? = null,
    @SerialName("level_str") val levelStr: String? = null,
    @SerialName("word_count") val wordCount: Long = 0,
    val author: ByAuthor? = null,
)

@Serializable
data class ByBooksData(val books: List<ByBook> = emptyList())

@Serializable
data class ByBooksResponse(
    val code: Int = 0,
    val msg: String? = null,
    val data: ByBooksData? = null,
)

@Serializable
data class ByChapterRef(
    val id: Long = 0,
    val sort: Int = 0,
    val name: String = "",
    @SerialName("name_cn") val nameCn: String? = null,
)

@Serializable
data class ByBookDetailData(
    val book: ByBook? = null,
    val chapters: List<ByChapterRef> = emptyList(),
)

@Serializable
data class ByBookDetailResponse(
    val code: Int = 0,
    val data: ByBookDetailData? = null,
)

@Serializable
data class ByArticle(
    val id: Long = 0,
    @SerialName("book_id") val bookId: String = "",
    val name: String = "",
    @SerialName("name_cn") val nameCn: String? = null,
    @SerialName("word_count") val wordCount: Long = 0,
    val difficulty: Int = 0,
)

@Serializable
data class ByArticlesData(val articles: List<ByArticle> = emptyList())

@Serializable
data class ByArticlesResponse(
    val code: Int = 0,
    val data: ByArticlesData? = null,
)

@Serializable
data class ByWord(
    val text: String = "",
    val brief: String? = null,
)

@Serializable
data class BySentence(
    val no: Int = 0,
    val words: List<ByWord> = emptyList(),
)

@Serializable
data class ByParagraph(
    val no: Int = 0,
    /** 0 = 文本，1 = 图片 */
    val type: Int = 0,
    val path: String = "",
    val sentences: List<BySentence> = emptyList(),
) {
    val text: String
        get() = sentences
            .joinToString(separator = " ") { sentence ->
                sentence.words.joinToString(separator = "") { it.text }
            }
            .trim()
}

@Serializable
data class ByChapterInfo(
    val id: Long = 0,
    val name: String = "",
    @SerialName("nameCn") val nameCn: String? = null,
    val wordCount: Long = 0,
)

@Serializable
data class ByChapterData(
    val bookId: String = "",
    val bookName: String = "",
    val chapter: ByChapterInfo? = null,
    val paragraphs: List<ByParagraph> = emptyList(),
)

@Serializable
data class ByChapterResponse(
    val code: Int = 0,
    val data: ByChapterData? = null,
)

/** /api/web/auth/me：登录态与会员信息。 */
@Serializable
data class ByMe(
    @SerialName("is_logged_in") val isLoggedIn: Boolean = false,
    val nickname: String? = null,
    val email: String? = null,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("member_type_string") val memberTypeString: String? = null,
    @SerialName("member_expired_at") val memberExpiredAt: Long = 0,
)

@Serializable
data class ByMeResponse(
    val code: Int = 0,
    val msg: String? = null,
    val data: ByMe? = null,
)
