package com.feiyan.english.ui.iciba

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.feiyan.english.core.model.IcibaCatalog
import com.feiyan.english.core.model.IcibaGroup
import com.feiyan.english.core.model.IcibaLesson
import com.feiyan.english.core.model.IcibaWord
import com.feiyan.english.data.IcibaRepository
import com.feiyan.english.ui.components.Eyebrow
import com.feiyan.english.ui.components.Hairline
import com.feiyan.english.ui.components.LocalBottomOverlayPadding
import com.feiyan.english.ui.icons.FeiyanIcons
import com.feiyan.english.ui.theme.Ink
import com.feiyan.english.ui.theme.InkFaint
import com.feiyan.english.ui.theme.InkMuted
import com.feiyan.english.ui.theme.Kaki
import com.feiyan.english.ui.theme.KakiSoft
import com.feiyan.english.ui.theme.PaperCard

@Composable
fun IcibaScreen(
    onGroupClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var catalog by remember { mutableStateOf<IcibaCatalog?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedCategory by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        runCatching { IcibaRepository.catalog(context) }
            .onSuccess { catalog = it }
            .onFailure { error = it.message ?: "无法读取爱词吧目录" }
    }

    val allGroups = catalog?.groups.orEmpty()
    val wordCount = allGroups.sumOf { it.wordCount }
    val visibleCategories = catalog?.categories
        ?.filter { it.groups.isNotEmpty() }
        ?.filter { selectedCategory == null || it.name == selectedCategory }
        .orEmpty()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = LocalBottomOverlayPadding.current + 30.dp),
    ) {
        item {
            Column(Modifier.padding(horizontal = 24.dp)) {
                Spacer(Modifier.height(16.dp))
                Eyebrow("ICIBA · 爱词吧")
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Word Books",
                    style = MaterialTheme.typography.displaySmall.copy(fontSize = 29.sp),
                    color = Ink,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "爱词吧词书目录已内置，课表与单词按需加载。",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkMuted,
                )
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    IcibaStat(
                        (catalog?.categories?.size ?: 8).toString(),
                        "类别",
                        Modifier.weight(1f),
                    )
                    IcibaStat(
                        (if (allGroups.isEmpty()) 69 else allGroups.size).toString(),
                        "词书",
                        Modifier.weight(1f),
                    )
                    IcibaStat(
                        (if (wordCount == 0) 166928 else wordCount).toString(),
                        "词条",
                        Modifier.weight(1f),
                    )
                }
                Spacer(Modifier.height(20.dp))
            }
        }

        if (error != null) {
            item {
                IcibaError(error.orEmpty(), Modifier.padding(horizontal = 24.dp))
            }
        } else if (catalog == null) {
            item { IcibaLoading("正在读取词书目录…") }
        } else {
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    IcibaFilterChip(
                        label = "全部",
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null },
                    )
                    catalog?.categories?.filter { it.groups.isNotEmpty() }?.forEach { category ->
                        IcibaFilterChip(
                            label = category.name,
                            selected = selectedCategory == category.name,
                            onClick = { selectedCategory = category.name },
                        )
                    }
                }
                Spacer(Modifier.height(18.dp))
            }

            visibleCategories.forEach { category ->
                item(key = "category-${category.name}") {
                    Row(
                        Modifier.padding(horizontal = 24.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                            color = Ink,
                        )
                        Spacer(Modifier.width(8.dp))
                        Eyebrow("${category.groups.size} 本", color = InkFaint)
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = "${category.groups.sumOf { it.wordCount }} 词",
                            style = MaterialTheme.typography.labelSmall,
                            color = InkFaint,
                        )
                    }
                }
                item(key = "card-${category.name}") {
                    Column(
                        Modifier
                            .padding(horizontal = 24.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(17.dp))
                            .background(PaperCard)
                            .border(
                                0.8.dp,
                                MaterialTheme.colorScheme.outlineVariant,
                                RoundedCornerShape(17.dp),
                            ),
                    ) {
                        category.groups.forEachIndexed { index, group ->
                            IcibaGroupRow(
                                group = group,
                                onClick = { onGroupClick(group.classId) },
                            )
                            if (index != category.groups.lastIndex) {
                                Hairline(Modifier.padding(start = 16.dp, end = 16.dp))
                            }
                        }
                    }
                }
            }

            item {
                Column(Modifier.padding(horizontal = 24.dp, vertical = 26.dp)) {
                    Text(
                        text = "数据源 · word.iciba.com",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.7.sp),
                        color = InkFaint,
                    )
                    Spacer(Modifier.height(5.dp))
                    Text(
                        text = "共 ${allGroups.size} 本词书、${allGroups.sumOf { it.lessonCount }} 课、${allGroups.sumOf { it.wordCount }} 词。",
                        style = MaterialTheme.typography.bodySmall,
                        color = InkFaint,
                    )
                }
            }
        }
    }
}

@Composable
fun IcibaGroupScreen(
    classId: String,
    onBack: () -> Unit,
    onCourseClick: (String) -> Unit,
    onLessonClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var group by remember(classId) { mutableStateOf<IcibaGroup?>(null) }
    var lessons by remember(classId) { mutableStateOf<List<IcibaLesson>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var retryToken by remember { mutableIntStateOf(0) }

    LaunchedEffect(classId, retryToken) {
        loading = true
        error = null
        runCatching {
            val value = IcibaRepository.group(context, classId)
            val list = if (value?.subcourses.isNullOrEmpty()) {
                IcibaRepository.lessons(classId)
            } else {
                emptyList()
            }
            value to list
        }.onSuccess { (value, list) ->
            group = value
            lessons = list
        }.onFailure { error = it.message ?: "无法加载词书" }
        loading = false
    }

    val subcourses = group?.subcourses.orEmpty()

    Column(modifier.fillMaxSize()) {
        IcibaTopBar(eyebrow = "ICIBA · 词书", onBack = onBack)
        Hairline()

        when {
            loading -> IcibaLoading("正在读取词书…")
            error != null -> IcibaError(error.orEmpty()) { retryToken++ }
            else -> LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = LocalBottomOverlayPadding.current + 30.dp),
            ) {
                item {
                    Column(Modifier.padding(horizontal = 24.dp)) {
                        Spacer(Modifier.height(20.dp))
                        Eyebrow(
                            if (subcourses.isNotEmpty()) {
                                "WORD BOOK · ${subcourses.size} 个子词书"
                            } else {
                                "WORD BOOK · ${lessons.size} 课"
                            },
                        )
                        Spacer(Modifier.height(7.dp))
                        Text(
                            text = group?.name.orEmpty().ifBlank { "词书" },
                            style = MaterialTheme.typography.displaySmall.copy(fontSize = 30.sp),
                            color = Ink,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "${group?.wordCount ?: lessons.sumOf { it.wordCount }} 个单词 · 按课学习",
                            style = MaterialTheme.typography.bodySmall,
                            color = InkMuted,
                        )
                        Spacer(Modifier.height(18.dp))
                    }
                }

                if (subcourses.isNotEmpty()) {
                    items(subcourses, key = { it.classId }) { course ->
                        IcibaCourseRow(
                            name = course.name,
                            lessonCount = course.lessonCount,
                            wordCount = course.wordCount,
                            onClick = { onCourseClick(course.classId) },
                        )
                        Hairline(Modifier.padding(horizontal = 24.dp))
                    }
                } else {
                    itemsIndexed(lessons, key = { _, lesson -> lesson.courseId }) { index, lesson ->
                        IcibaLessonRow(
                            index = index,
                            lesson = lesson,
                            onClick = { onLessonClick(lesson.courseId) },
                        )
                        Hairline(Modifier.padding(horizontal = 24.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun IcibaCourseScreen(
    classId: String,
    onBack: () -> Unit,
    onLessonClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var title by remember(classId) { mutableStateOf("词书") }
    var groupName by remember(classId) { mutableStateOf("") }
    var totalWords by remember(classId) { mutableIntStateOf(0) }
    var lessons by remember(classId) { mutableStateOf<List<IcibaLesson>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var retryToken by remember { mutableIntStateOf(0) }

    LaunchedEffect(classId, retryToken) {
        loading = true
        error = null
        runCatching {
            val catalog = IcibaRepository.catalog(context)
            val pair = catalog.groups.firstNotNullOfOrNull { group ->
                group.subcourses.firstOrNull { it.classId == classId }?.let { course -> group to course }
            }
            if (pair != null) {
                title = pair.second.name
                groupName = pair.first.name
                totalWords = pair.second.wordCount
            }
            IcibaRepository.lessons(classId)
        }.onSuccess { lessons = it }
            .onFailure { error = it.message ?: "无法加载课表" }
        loading = false
    }

    Column(modifier.fillMaxSize()) {
        IcibaTopBar(eyebrow = "ICIBA · 课程", onBack = onBack)
        Hairline()

        when {
            loading -> IcibaLoading("正在加载课表…")
            error != null -> IcibaError(error.orEmpty()) { retryToken++ }
            else -> LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = LocalBottomOverlayPadding.current + 30.dp),
            ) {
                item {
                    Column(Modifier.padding(horizontal = 24.dp)) {
                        Spacer(Modifier.height(20.dp))
                        Eyebrow(
                            listOfNotNull(groupName.ifBlank { null }, "${lessons.size} 课")
                                .joinToString(" · "),
                        )
                        Spacer(Modifier.height(7.dp))
                        Text(
                            text = title,
                            style = MaterialTheme.typography.displaySmall.copy(fontSize = 30.sp),
                            color = Ink,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "${if (totalWords > 0) totalWords else lessons.sumOf { it.wordCount }} 个单词 · 按课学习",
                            style = MaterialTheme.typography.bodySmall,
                            color = InkMuted,
                        )
                        Spacer(Modifier.height(18.dp))
                    }
                }

                itemsIndexed(lessons, key = { _, lesson -> lesson.courseId }) { index, lesson ->
                    IcibaLessonRow(
                        index = index,
                        lesson = lesson,
                        onClick = { onLessonClick(lesson.courseId) },
                    )
                    Hairline(Modifier.padding(horizontal = 24.dp))
                }
            }
        }
    }
}

@Composable
fun IcibaLessonScreen(
    classId: String,
    courseId: Int,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var group by remember(classId) { mutableStateOf<IcibaGroup?>(null) }
    var words by remember(classId, courseId) { mutableStateOf<List<IcibaWord>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var retryToken by remember { mutableIntStateOf(0) }

    LaunchedEffect(classId, courseId, retryToken) {
        loading = true
        error = null
        runCatching {
            IcibaRepository.group(context, classId) to IcibaRepository.words(classId, courseId)
        }.onSuccess { (value, list) ->
            group = value
            words = list
        }.onFailure { error = it.message ?: "无法加载本课单词" }
        loading = false
    }

    Column(modifier.fillMaxSize()) {
        IcibaTopBar(eyebrow = "ICIBA · 单词", onBack = onBack)
        Hairline()

        when {
            loading -> IcibaLoading("正在加载第 $courseId 课…")
            error != null -> IcibaError(error.orEmpty()) { retryToken++ }
            else -> SelectionContainer {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = LocalBottomOverlayPadding.current + 30.dp),
                ) {
                    item {
                        Column(Modifier.padding(horizontal = 24.dp)) {
                            Spacer(Modifier.height(20.dp))
                            Eyebrow(group?.name.orEmpty().ifBlank { "WORDS" })
                            Spacer(Modifier.height(7.dp))
                            Text(
                                text = "第 $courseId 课",
                                style = MaterialTheme.typography.displaySmall.copy(fontSize = 30.sp),
                                color = Ink,
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "${words.size} 个单词 · 音标与释义",
                                style = MaterialTheme.typography.bodySmall,
                                color = InkMuted,
                            )
                            Spacer(Modifier.height(16.dp))
                        }
                    }

                    itemsIndexed(words, key = { index, word -> "${word.word}-$index" }) { index, word ->
                        IcibaWordRow(index = index + 1, word = word)
                        Hairline(Modifier.padding(horizontal = 24.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun IcibaTopBar(eyebrow: String, onBack: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 20.dp, top = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(44.dp).clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = FeiyanIcons.ArrowLeft,
                contentDescription = "返回",
                tint = Ink,
                modifier = Modifier.size(19.dp),
            )
        }
        Spacer(Modifier.width(4.dp))
        Eyebrow(eyebrow, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun IcibaStat(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(PaperCard)
            .border(0.8.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
            .padding(vertical = 11.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 19.sp),
            color = Kaki,
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = InkMuted,
        )
    }
}

@Composable
private fun IcibaFilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall.copy(
            fontSize = 10.sp,
            letterSpacing = 0.4.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        ),
        color = if (selected) MaterialTheme.colorScheme.onPrimary else InkMuted,
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) Kaki else PaperCard)
            .border(
                0.8.dp,
                if (selected) Color.Transparent else MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(18.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 9.dp),
    )
}

@Composable
private fun IcibaGroupRow(group: IcibaGroup, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(KakiSoft.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = group.name.take(1),
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                color = Kaki,
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = group.name,
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp),
                color = Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(4.dp))
            Eyebrow("${group.lessonCount} 课 · ${group.wordCount} 词", color = InkFaint)
        }
        Icon(
            imageVector = FeiyanIcons.ChevronRight,
            contentDescription = null,
            tint = InkFaint,
            modifier = Modifier.size(13.dp),
        )
    }
}

@Composable
private fun IcibaCourseRow(
    name: String,
    lessonCount: Int,
    wordCount: Int,
    onClick: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp),
                color = Ink,
            )
            Spacer(Modifier.height(3.dp))
            Eyebrow("$lessonCount 课 · $wordCount 词", color = InkFaint)
        }
        Icon(
            imageVector = FeiyanIcons.ChevronRight,
            contentDescription = null,
            tint = InkFaint,
            modifier = Modifier.size(13.dp),
        )
    }
}

@Composable
private fun IcibaLessonRow(index: Int, lesson: IcibaLesson, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = (index + 1).toString().padStart(2, '0'),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            color = InkFaint,
            modifier = Modifier.width(30.dp),
        )
        Column(Modifier.weight(1f)) {
            Text(
                text = lesson.title,
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp),
                color = Ink,
            )
            Spacer(Modifier.height(3.dp))
            Eyebrow("${lesson.wordCount} 个单词", color = InkFaint)
        }
        Icon(
            imageVector = FeiyanIcons.ChevronRight,
            contentDescription = null,
            tint = InkFaint,
            modifier = Modifier.size(13.dp),
        )
    }
}

@Composable
private fun IcibaWordRow(index: Int, word: IcibaWord) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 13.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = index.toString().padStart(2, '0'),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            color = InkFaint,
            modifier = Modifier.width(28.dp).padding(top = 5.dp),
        )
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = word.word,
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 19.sp),
                    color = Ink,
                )
                if (word.phonetic.isNotBlank()) {
                    Spacer(Modifier.width(9.dp))
                    Text(
                        text = "/ ${word.phonetic} /",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp),
                        color = InkFaint,
                    )
                }
            }
            if (word.meaning.isNotBlank()) {
                Spacer(Modifier.height(5.dp))
                Text(
                    text = word.meaning,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp, lineHeight = 19.sp),
                    color = InkMuted,
                )
            }
        }
    }
}

@Composable
private fun IcibaLoading(text: String) {
    Box(Modifier.fillMaxWidth().padding(vertical = 48.dp), contentAlignment = Alignment.Center) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(
                color = Kaki,
                strokeWidth = 1.4.dp,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = InkMuted,
            )
        }
    }
}

@Composable
private fun IcibaError(message: String, modifier: Modifier = Modifier, onRetry: (() -> Unit)? = null) {
    Column(modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 32.dp)) {
        Text(
            text = "加载失败",
            style = MaterialTheme.typography.titleSmall,
            color = Ink,
        )
        Spacer(Modifier.height(7.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
            color = InkMuted,
        )
        if (onRetry != null) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = "重新加载",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp),
                color = Kaki,
                modifier = Modifier.clickable(onClick = onRetry),
            )
        }
    }
}
