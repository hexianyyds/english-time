package com.feiyan.english.core.model

import kotlinx.serialization.Serializable

@Serializable
data class IcibaCatalog(
    val generatedAt: String = "",
    val source: String = "",
    val categories: List<IcibaCategory> = emptyList(),
) {
    val groups: List<IcibaGroup> get() = categories.flatMap { it.groups }
}

@Serializable
data class IcibaCategory(
    val name: String = "",
    val groups: List<IcibaGroup> = emptyList(),
)

@Serializable
data class IcibaGroup(
    val name: String = "",
    val classId: String = "",
    val lessonCount: Int = 0,
    val wordCount: Int = 0,
    val meta: String = "",
    val subcourses: List<IcibaCourseRef> = emptyList(),
)

@Serializable
data class IcibaCourseRef(
    val name: String = "",
    val classId: String = "",
    val lessonCount: Int = 0,
    val wordCount: Int = 0,
)

data class IcibaLesson(
    val courseId: Int,
    val title: String,
    val wordCount: Int,
)

data class IcibaWord(
    val word: String,
    val phonetic: String,
    val meaning: String,
)
