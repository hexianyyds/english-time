package com.feiyan.english.ui.beidanci

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.feiyan.english.core.model.BdCourse
import com.feiyan.english.core.model.BdResume
import com.feiyan.english.core.model.BdUnit
import com.feiyan.english.core.model.BdWord
import com.feiyan.english.data.BdProgressStore
import com.feiyan.english.data.BeidanciRepository
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

private enum class BdStudyMode(val label: String) {
    LEARN("学习"),
    QUIZ("测试"),
    LIST("词表"),
}

@Composable
fun BeidanciStudyScreen(
    courseKey: String,
    unitId: Int,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var course by remember(courseKey) { mutableStateOf<BdCourse?>(null) }
    var unit by remember(courseKey, unitId) { mutableStateOf<BdUnit?>(null) }
    var words by remember(courseKey, unitId) { mutableStateOf<List<BdWord>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var retryToken by remember { mutableIntStateOf(0) }
    var modeIndex by rememberSaveable { mutableIntStateOf(0) }
    val mode = BdStudyMode.entries[modeIndex.coerceIn(BdStudyMode.entries.indices)]

    LaunchedEffect(courseKey, unitId, retryToken) {
        loading = true
        error = null
        runCatching {
            val value = BeidanciRepository.course(courseKey)
                ?: throw IllegalStateException("未找到这本词书")
            val unitList = BeidanciRepository.units(value.id)
            val valueUnit = unitList.firstOrNull { it.id == unitId }
                ?: throw IllegalStateException("未找到这个单元")
            val wordList = BeidanciRepository.words(
                courseId = value.id,
                unitId = valueUnit.id,
                limit = valueUnit.totalWords.coerceAtLeast(50),
            )
            Triple(value, valueUnit, wordList)
        }.onSuccess { (value, valueUnit, wordList) ->
            course = value
            unit = valueUnit
            words = wordList
            BdProgressStore.save(
                context,
                BdResume(
                    courseKey = value.key,
                    courseTitle = value.title,
                    unitId = valueUnit.id,
                    unitTitle = valueUnit.title,
                ),
            )
        }.onFailure { error = it.message ?: "无法加载本单元" }
        loading = false
    }

    Column(modifier.fillMaxSize()) {
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
            Eyebrow("BEIDANCI365 · 单元学习", modifier = Modifier.weight(1f))
        }
        Hairline()

        when {
            loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        color = Kaki,
                        strokeWidth = 1.4.dp,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "正在加载本单元…",
                        style = MaterialTheme.typography.bodySmall,
                        color = InkMuted,
                    )
                }
            }
            error != null -> Column(Modifier.padding(24.dp)) {
                Text(
                    text = error.orEmpty(),
                    style = MaterialTheme.typography.titleSmall,
                    color = Ink,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "重新加载",
                    style = MaterialTheme.typography.labelSmall,
                    color = Kaki,
                    modifier = Modifier.clickable { retryToken++ },
                )
            }
            else -> Column(Modifier.fillMaxSize()) {
                Column(Modifier.padding(horizontal = 24.dp)) {
                    Spacer(Modifier.height(16.dp))
                    Eyebrow(course?.title.orEmpty())
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = unit?.title.orEmpty().ifBlank { "Unit $unitId" },
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 23.sp),
                        color = Ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "${words.size} 个单词 · 无需登录",
                        style = MaterialTheme.typography.bodySmall,
                        color = InkMuted,
                    )
                    Spacer(Modifier.height(14.dp))
                }

                BdModeBar(
                    selected = mode,
                    onSelect = { modeIndex = it.ordinal },
                )
                Spacer(Modifier.height(6.dp))

                Box(Modifier.weight(1f).fillMaxWidth()) {
                    when (mode) {
                        BdStudyMode.LEARN -> LearnContent(words)
                        BdStudyMode.QUIZ -> QuizContent(words)
                        BdStudyMode.LIST -> WordListContent(words)
                    }
                }
            }
        }
    }
}

@Composable
private fun BdModeBar(selected: BdStudyMode, onSelect: (BdStudyMode) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(PaperCard)
            .border(0.8.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
            .padding(4.dp),
    ) {
        BdStudyMode.entries.forEach { mode ->
            val active = mode == selected
            Box(
                Modifier
                    .weight(1f)
                    .height(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (active) Kaki else Color.Transparent)
                    .clickable { onSelect(mode) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = mode.label,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                    ),
                    color = if (active) MaterialTheme.colorScheme.onPrimary else InkMuted,
                )
            }
        }
    }
}

@Composable
private fun LearnContent(words: List<BdWord>) {
    if (words.isEmpty()) {
        EmptyWords()
        return
    }
    var index by rememberSaveable { mutableIntStateOf(0) }
    var revealed by rememberSaveable { mutableStateOf(false) }
    val safeIndex = index.coerceIn(words.indices)
    val word = words[safeIndex]

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Eyebrow("FLASH CARD")
            Spacer(Modifier.weight(1f))
            Text(
                text = "${safeIndex + 1} / ${words.size}",
                style = MaterialTheme.typography.labelSmall,
                color = InkFaint,
            )
        }
        Spacer(Modifier.height(12.dp))

        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(PaperCard)
                    .border(0.8.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
                    .clickable { revealed = !revealed }
                    .padding(horizontal = 22.dp, vertical = 26.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = word.word,
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    color = Ink,
                    textAlign = TextAlign.Center,
                )
                if (word.phonetic.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "/ ${word.phonetic} /",
                        style = MaterialTheme.typography.bodySmall,
                        color = InkFaint,
                    )
                }
                Spacer(Modifier.height(22.dp))
                if (revealed) {
                    Text(
                        text = word.meaning,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 18.sp,
                            lineHeight = 27.sp,
                        ),
                        color = Ink,
                        textAlign = TextAlign.Center,
                    )
                    if (word.exampleEn.isNotBlank()) {
                        Spacer(Modifier.height(18.dp))
                        Text(
                            text = word.exampleEn,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.5.sp,
                                lineHeight = 20.sp,
                            ),
                            color = InkMuted,
                            textAlign = TextAlign.Center,
                        )
                        if (word.exampleCn.isNotBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = word.exampleCn,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = InkFaint,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                } else {
                    Text(
                        text = "点击卡片显示释义",
                        style = MaterialTheme.typography.bodySmall,
                        color = InkFaint,
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "上一个",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                color = if (safeIndex > 0) Ink else InkFaint,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(13.dp))
                    .background(PaperCard)
                    .clickable(enabled = safeIndex > 0) {
                        index = safeIndex - 1
                        revealed = false
                    }
                    .padding(vertical = 13.dp),
            )
            Text(
                text = when {
                    !revealed -> "显示释义"
                    safeIndex == words.lastIndex -> "已是最后一个"
                    else -> "下一个"
                },
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                ),
                color = if (revealed && safeIndex == words.lastIndex) {
                    InkMuted
                } else {
                    MaterialTheme.colorScheme.onPrimary
                },
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1.4f)
                    .clip(RoundedCornerShape(13.dp))
                    .background(if (revealed && safeIndex == words.lastIndex) PaperCard else Kaki)
                    .clickable {
                        if (!revealed) {
                            revealed = true
                        } else if (safeIndex < words.lastIndex) {
                            index = safeIndex + 1
                            revealed = false
                        }
                    }
                    .padding(vertical = 13.dp),
            )
        }
        Spacer(Modifier.height(LocalBottomOverlayPadding.current + 18.dp))
    }
}

@Composable
private fun QuizContent(words: List<BdWord>) {
    if (words.isEmpty()) {
        EmptyWords()
        return
    }
    var questionIndex by rememberSaveable { mutableIntStateOf(0) }
    var selected by rememberSaveable { mutableStateOf<String?>(null) }
    var correctCount by rememberSaveable { mutableIntStateOf(0) }
    var finished by rememberSaveable { mutableStateOf(false) }
    val safeIndex = questionIndex.coerceIn(words.indices)
    val word = words[safeIndex]
    val options = remember(safeIndex, word.id) {
        val pool = (word.distractors + words.map { it.meaning })
            .filter { it.isNotBlank() && it != word.meaning }
            .distinct()
        (pool.shuffled().take(3) + word.meaning).distinct().shuffled()
    }

    if (finished) {
        Column(
            Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "本单元测试完成",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp),
                color = Ink,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "答对 $correctCount / ${words.size}",
                style = MaterialTheme.typography.displaySmall.copy(fontSize = 34.sp),
                color = Kaki,
            )
            Spacer(Modifier.height(22.dp))
            Text(
                text = "再测一次",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(Kaki)
                    .clickable {
                        questionIndex = 0
                        selected = null
                        correctCount = 0
                        finished = false
                    }
                    .padding(horizontal = 28.dp, vertical = 13.dp),
            )
        }
        return
    }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 24.dp,
            end = 24.dp,
            top = 12.dp,
            bottom = LocalBottomOverlayPadding.current + 26.dp,
        ),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Eyebrow("QUIZ")
                Spacer(Modifier.weight(1f))
                Text(
                    text = "${safeIndex + 1} / ${words.size} · 答对 $correctCount",
                    style = MaterialTheme.typography.labelSmall,
                    color = InkFaint,
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = word.word,
                style = MaterialTheme.typography.displaySmall.copy(
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Medium,
                ),
                color = Ink,
            )
            if (word.phonetic.isNotBlank()) {
                Spacer(Modifier.height(5.dp))
                Text(
                    text = "/ ${word.phonetic} /",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkFaint,
                )
            }
            Spacer(Modifier.height(18.dp))
            Text(
                text = "选择正确的中文释义",
                style = MaterialTheme.typography.bodySmall,
                color = InkMuted,
            )
            Spacer(Modifier.height(12.dp))
        }

        itemsIndexed(options) { _, option ->
            val isCorrect = option == word.meaning
            val isSelected = option == selected
            val background = when {
                selected == null -> PaperCard
                isCorrect -> KakiSoft
                isSelected -> MaterialTheme.colorScheme.errorContainer
                else -> PaperCard
            }
            val borderColor = when {
                selected == null -> MaterialTheme.colorScheme.outlineVariant
                isCorrect -> Kaki
                isSelected -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.outlineVariant
            }
            Text(
                text = option,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, lineHeight = 21.sp),
                color = when {
                    selected != null && isSelected && !isCorrect -> MaterialTheme.colorScheme.onErrorContainer
                    else -> Ink
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(background)
                    .border(0.8.dp, borderColor, RoundedCornerShape(13.dp))
                    .clickable(enabled = selected == null) {
                        selected = option
                        if (isCorrect) correctCount++
                    }
                    .padding(horizontal = 15.dp, vertical = 13.dp),
            )
        }

        if (selected != null) {
            item {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = if (selected == word.meaning) "回答正确" else "正确答案：${word.meaning}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    color = if (selected == word.meaning) Kaki else MaterialTheme.colorScheme.error,
                )
                if (word.exampleEn.isNotBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = word.exampleEn,
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                        color = InkMuted,
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    text = if (safeIndex == words.lastIndex) "查看结果" else "下一题",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(13.dp))
                        .background(Kaki)
                        .clickable {
                            if (safeIndex == words.lastIndex) {
                                finished = true
                            } else {
                                questionIndex = safeIndex + 1
                                selected = null
                            }
                        }
                        .padding(vertical = 13.dp),
                )
            }
        }
    }
}

@Composable
private fun WordListContent(words: List<BdWord>) {
    if (words.isEmpty()) {
        EmptyWords()
        return
    }
    SelectionContainer {
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 24.dp,
                end = 24.dp,
                top = 12.dp,
                bottom = LocalBottomOverlayPadding.current + 26.dp,
            ),
        ) {
            itemsIndexed(words, key = { _, word -> word.id }) { index, word ->
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(PaperCard)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = (index + 1).toString().padStart(2, '0'),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = InkFaint,
                            modifier = Modifier.width(26.dp),
                        )
                        Text(
                            text = word.word,
                            style = MaterialTheme.typography.titleLarge.copy(fontSize = 19.sp),
                            color = Ink,
                        )
                        if (word.phonetic.isNotBlank()) {
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "/ ${word.phonetic} /",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                color = InkFaint,
                            )
                        }
                    }
                    Spacer(Modifier.height(5.dp))
                    Text(
                        text = word.meaning,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp, lineHeight = 21.sp),
                        color = Ink,
                    )
                    if (word.exampleEn.isNotBlank()) {
                        Spacer(Modifier.height(7.dp))
                        Text(
                            text = word.exampleEn,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.5.sp,
                                lineHeight = 18.sp,
                            ),
                            color = InkMuted,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyWords() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "本单元暂无单词",
            style = MaterialTheme.typography.bodySmall,
            color = InkFaint,
        )
    }
}
