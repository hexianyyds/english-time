package com.feiyan.english.ui.ipa

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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.feiyan.english.core.model.IpaPhoneme
import com.feiyan.english.data.IpaRepository
import com.feiyan.english.ui.components.Eyebrow
import com.feiyan.english.ui.components.Hairline
import com.feiyan.english.ui.components.LocalBottomOverlayPadding
import com.feiyan.english.ui.icons.FeiyanIcons
import com.feiyan.english.ui.theme.Ink
import com.feiyan.english.ui.theme.InkFaint
import com.feiyan.english.ui.theme.InkMuted
import com.feiyan.english.ui.theme.Kaki
import com.feiyan.english.ui.theme.KakiSoft
import com.feiyan.english.ui.theme.Paper
import com.feiyan.english.ui.theme.PaperCard
import androidx.compose.ui.text.TextStyle

private enum class IpaTab(val label: String) {
    LIBRARY("音标库"),
    LISTEN("听音"),
    COMPARE("对比"),
}

@Composable
fun IpaScreen(
    onBack: () -> Unit,
    onPhonemeClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var phonemes by remember { mutableStateOf<List<IpaPhoneme>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var tabIndex by rememberSaveable { mutableIntStateOf(0) }
    val tab = IpaTab.entries[tabIndex.coerceIn(IpaTab.entries.indices)]

    LaunchedEffect(context) {
        loading = true
        runCatching { IpaRepository.all(context) }
            .onSuccess { phonemes = it }
            .onFailure { error = it.message ?: "音标数据加载失败" }
        loading = false
    }

    Column(modifier.fillMaxSize().background(Paper)) {
        IpaTopBar(onBack = onBack)
        Hairline()

        Column(Modifier.padding(horizontal = 24.dp)) {
            Spacer(Modifier.height(15.dp))
            Eyebrow("WXAIWAY · 国际音标")
            Spacer(Modifier.height(6.dp))
            Text(
                text = "48 个国际音标",
                style = MaterialTheme.typography.displaySmall.copy(fontSize = 29.sp),
                color = Ink,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "系统认识音标、听音辨别，并对比容易混淆的发音。",
                style = MaterialTheme.typography.bodySmall,
                color = InkMuted,
            )
            Spacer(Modifier.height(14.dp))
        }

        IpaModeBar(
            selected = tab,
            onSelect = { tabIndex = it.ordinal },
        )
        Spacer(Modifier.height(6.dp))

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
                        text = "正在载入音标…",
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
            }
            tab == IpaTab.LIBRARY -> IpaLibraryTab(
                phonemes = phonemes,
                onPhonemeClick = onPhonemeClick,
            )
            tab == IpaTab.LISTEN -> IpaListenTab(phonemes = phonemes)
            else -> IpaCompareTab(phonemes = phonemes)
        }
    }
}

@Composable
fun IpaDetailScreen(
    phonemeId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val audio = remember { IpaAudioController() }
    var phonemes by remember { mutableStateOf<List<IpaPhoneme>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(context) {
        runCatching { IpaRepository.all(context) }
            .onSuccess { phonemes = it }
            .onFailure { error = it.message ?: "音标加载失败" }
    }
    DisposableEffect(Unit) {
        onDispose { audio.release() }
    }

    val phoneme = phonemes.firstOrNull { it.id == phonemeId }
    val isCurrent = audio.currentId == phoneme?.id

    Column(modifier.fillMaxSize().background(Paper)) {
        IpaTopBar(onBack = onBack, title = "音标详情")
        Hairline()

        when {
            error != null -> Text(
                text = error.orEmpty(),
                style = MaterialTheme.typography.bodySmall,
                color = InkMuted,
                modifier = Modifier.padding(24.dp),
            )
            phoneme == null && phonemes.isEmpty() -> Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    color = Kaki,
                    strokeWidth = 1.4.dp,
                    modifier = Modifier.size(18.dp),
                )
            }
            phoneme == null -> Text(
                text = "未找到该音标",
                style = MaterialTheme.typography.bodySmall,
                color = InkMuted,
                modifier = Modifier.padding(24.dp),
            )
            else -> LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 24.dp,
                    end = 24.dp,
                    top = 20.dp,
                    bottom = LocalBottomOverlayPadding.current + 34.dp,
                ),
            ) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(width = 92.dp, height = 92.dp)
                                .clip(RoundedCornerShape(22.dp))
                                .background(KakiSoft),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "/${phoneme.symbol}/",
                                style = MaterialTheme.typography.displaySmall.copy(fontSize = 31.sp),
                                color = Kaki,
                                textAlign = TextAlign.Center,
                            )
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Eyebrow(phoneme.categoryLabel)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = phoneme.description,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 14.sp,
                                    lineHeight = 21.sp,
                                ),
                                color = Ink,
                            )
                            Spacer(Modifier.height(9.dp))
                            Text(
                                text = when {
                                    audio.loading && isCurrent -> "正在加载…"
                                    audio.playing && isCurrent -> "停止播放"
                                    else -> "播放标准发音"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                ),
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Kaki)
                                    .clickable {
                                        if (isCurrent && (audio.loading || audio.playing)) {
                                            audio.stop()
                                        } else {
                                            audio.play(scope, context, phoneme)
                                        }
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                            )
                        }
                    }
                    if (audio.error != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = audio.error.orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                    Spacer(Modifier.height(20.dp))
                }

                item {
                    IpaInfoCard(title = "口型要领", body = phoneme.mouthShape)
                    Spacer(Modifier.height(10.dp))
                    IpaInfoCard(title = "学习技巧", body = phoneme.tips)
                    Spacer(Modifier.height(10.dp))
                }

                item {
                    Eyebrow("EXAMPLES · 单词示例")
                    Spacer(Modifier.height(9.dp))
                }
                items(phoneme.examples) { word ->
                    IpaWordRow(word = word)
                    Spacer(Modifier.height(7.dp))
                }
                item {
                    if (phoneme.extraWords.isNotEmpty()) {
                        Spacer(Modifier.height(10.dp))
                        Eyebrow("MORE WORDS · 更多例词")
                        Spacer(Modifier.height(8.dp))
                    }
                }
                items(phoneme.extraWords) { word ->
                    IpaWordRow(word = word, compact = true)
                }
            }
        }
    }
}

@Composable
private fun IpaLibraryTab(
    phonemes: List<IpaPhoneme>,
    onPhonemeClick: (String) -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf<String?>(null) }
    val categories = remember(phonemes) {
        listOf("全部") + phonemes.map { it.categoryLabel }.distinct()
    }
    val filtered = phonemes.filter { item ->
        (category == null || item.categoryLabel == category) &&
            (query.isBlank() ||
                item.symbol.contains(query, ignoreCase = true) ||
                item.examples.any { it.word.contains(query, ignoreCase = true) })
    }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 24.dp,
            end = 24.dp,
            top = 10.dp,
            bottom = LocalBottomOverlayPadding.current + 28.dp,
        ),
    ) {
        item {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(PaperCard)
                    .border(0.8.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = FeiyanIcons.Search,
                    contentDescription = null,
                    tint = InkFaint,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(8.dp))
                BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    textStyle = TextStyle(color = Ink, fontSize = 13.sp),
                    cursorBrush = SolidColor(Kaki),
                    modifier = Modifier.weight(1f),
                    decorationBox = { inner ->
                        Box {
                            if (query.isEmpty()) {
                                Text(
                                    text = "搜索音标或例词",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = InkFaint,
                                )
                            }
                            inner()
                        }
                    },
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                categories.forEach { item ->
                    val active = (item == "全部" && category == null) || category == item
                    IpaChip(
                        label = item,
                        selected = active,
                        onClick = { category = if (item == "全部") null else item },
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        items(filtered.chunked(4)) { rowItems ->
            Row(
                Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rowItems.forEach { phoneme ->
                    PhonemeCard(
                        phoneme = phoneme,
                        modifier = Modifier.weight(1f),
                        onClick = { onPhonemeClick(phoneme.id) },
                    )
                }
                repeat(4 - rowItems.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }

        if (filtered.isEmpty()) {
            item {
                Text(
                    text = "没有匹配的音标",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkFaint,
                    modifier = Modifier.padding(vertical = 28.dp),
                )
            }
        }
    }
}

@Composable
private fun IpaListenTab(phonemes: List<IpaPhoneme>) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val audio = remember { IpaAudioController() }
    var round by remember { mutableIntStateOf(1) }
    var score by remember { mutableIntStateOf(0) }
    var current by remember { mutableStateOf<IpaPhoneme?>(null) }
    var options by remember { mutableStateOf<List<IpaPhoneme>>(emptyList()) }
    var selected by remember { mutableStateOf<IpaPhoneme?>(null) }
    var answered by remember { mutableStateOf(false) }
    var initialized by remember { mutableStateOf(false) }

    fun nextQuestion() {
        val answer = phonemes.random()
        current = answer
        val distractors = phonemes.filter { it.id != answer.id }.shuffled().take(3)
        options = (distractors + answer).shuffled()
        selected = null
        answered = false
    }

    LaunchedEffect(phonemes) {
        if (!initialized && phonemes.isNotEmpty()) {
            initialized = true
            nextQuestion()
        }
    }
    DisposableEffect(Unit) {
        onDispose { audio.release() }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Eyebrow("LISTEN · 听音辨别")
            Spacer(Modifier.weight(1f))
            Text(
                text = "第 $round 题 · 答对 $score",
                style = MaterialTheme.typography.labelSmall,
                color = InkFaint,
            )
        }
        Spacer(Modifier.height(16.dp))

        val question = current
        if (question == null) {
            CircularProgressIndicator(
                color = Kaki,
                strokeWidth = 1.4.dp,
                modifier = Modifier.size(18.dp),
            )
        } else {
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(PaperCard)
                    .border(0.8.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
                    .padding(vertical = 26.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "听发音，选择正确的音标",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkMuted,
                )
                Spacer(Modifier.height(18.dp))
                Box(
                    Modifier
                        .size(84.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(KakiSoft)
                        .clickable { audio.play(scope, context, question) },
                    contentAlignment = Alignment.Center,
                ) {
                    if (audio.loading && audio.currentId == question.id) {
                        CircularProgressIndicator(
                            color = Kaki,
                            strokeWidth = 1.5.dp,
                            modifier = Modifier.size(22.dp),
                        )
                    } else {
                        Icon(
                            imageVector = if (audio.playing && audio.currentId == question.id) {
                                FeiyanIcons.Pause
                            } else {
                                FeiyanIcons.Play
                            },
                            contentDescription = "播放音标",
                            tint = Kaki,
                            modifier = Modifier.size(30.dp),
                        )
                    }
                }
                Spacer(Modifier.height(9.dp))
                Text(
                    text = if (audio.playing && audio.currentId == question.id) "正在播放" else "点击播放",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                    color = InkFaint,
                )
            }
            if (audio.error != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = audio.error.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Spacer(Modifier.height(16.dp))
            options.forEach { option ->
                val isCorrect = option.id == question.id
                val isSelected = selected?.id == option.id
                val background = when {
                    !answered -> PaperCard
                    isCorrect -> KakiSoft
                    isSelected -> MaterialTheme.colorScheme.errorContainer
                    else -> PaperCard
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(background)
                        .border(
                            0.8.dp,
                            when {
                                !answered -> MaterialTheme.colorScheme.outlineVariant
                                isCorrect -> Kaki
                                isSelected -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.outlineVariant
                            },
                            RoundedCornerShape(13.dp),
                        )
                        .clickable(enabled = !answered) {
                            selected = option
                            answered = true
                            if (isCorrect) score++
                        }
                        .padding(horizontal = 15.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "/${option.symbol}/",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp),
                        color = Ink,
                        modifier = Modifier.width(76.dp),
                    )
                    Text(
                        text = option.categoryLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = InkMuted,
                    )
                }
            }

            if (answered) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = if (selected?.id == question.id) {
                        "回答正确 · ${question.examples.firstOrNull()?.word.orEmpty()}"
                    } else {
                        "正确答案是 /${question.symbol}/ · ${question.categoryLabel}"
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    color = if (selected?.id == question.id) Kaki else MaterialTheme.colorScheme.error,
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "下一题",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(13.dp))
                        .background(Kaki)
                        .clickable {
                            round++
                            audio.stop()
                            nextQuestion()
                        }
                        .padding(vertical = 13.dp),
                )
            }
        }
        Spacer(Modifier.height(LocalBottomOverlayPadding.current + 24.dp))
    }
}

@Composable
private fun IpaCompareTab(phonemes: List<IpaPhoneme>) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val audio = remember { IpaAudioController() }
    val pairs = remember(phonemes) {
        IpaRepository.comparisonPairs.mapNotNull { (a, b) ->
            val left = phonemes.firstOrNull { it.id == a }
            val right = phonemes.firstOrNull { it.id == b }
            if (left != null && right != null) left to right else null
        }
    }
    var pairIndex by remember { mutableIntStateOf(0) }
    DisposableEffect(Unit) {
        onDispose { audio.release() }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Eyebrow("COMPARE · 易混音对比")
            Spacer(Modifier.weight(1f))
            Text(
                text = "换一组",
                style = MaterialTheme.typography.labelSmall,
                color = Kaki,
                modifier = Modifier
                    .clickable {
                        audio.stop()
                        pairIndex = (pairIndex + 1) % pairs.size.coerceAtLeast(1)
                    }
                    .padding(6.dp),
            )
        }
        Spacer(Modifier.height(14.dp))
        val pair = pairs.getOrNull(pairIndex % pairs.size.coerceAtLeast(1))
        if (pair == null) {
            Text("音标对比数据不可用", color = InkMuted)
            return@Column
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            listOf(pair.first, pair.second).forEach { phoneme ->
                Column(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(17.dp))
                        .background(PaperCard)
                        .border(0.8.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(17.dp))
                        .padding(horizontal = 12.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "/${phoneme.symbol}/",
                        style = MaterialTheme.typography.displaySmall.copy(fontSize = 29.sp),
                        color = Kaki,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = phoneme.categoryLabel,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                        color = InkFaint,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = phoneme.mouthShape,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 17.sp),
                        color = InkMuted,
                        textAlign = TextAlign.Center,
                        minLines = 3,
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = if (audio.currentId == phoneme.id && audio.playing) "停止" else "播放",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Kaki)
                            .clickable {
                                if (audio.currentId == phoneme.id && audio.playing) {
                                    audio.stop()
                                } else {
                                    audio.play(scope, context, phoneme)
                                }
                            }
                            .padding(horizontal = 14.dp, vertical = 7.dp),
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(15.dp))
                .background(PaperCard)
                .padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            Eyebrow("区别提示")
            Spacer(Modifier.height(6.dp))
            Text(
                text = "${pair.first.symbol}：${pair.first.tips}",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 19.sp),
                color = InkMuted,
            )
            Spacer(Modifier.height(5.dp))
            Text(
                text = "${pair.second.symbol}：${pair.second.tips}",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 19.sp),
                color = InkMuted,
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            pair.first.examples.take(1).forEach { word ->
                CompareWordChip(word = word, modifier = Modifier.weight(1f))
            }
            pair.second.examples.take(1).forEach { word ->
                CompareWordChip(word = word, modifier = Modifier.weight(1f))
            }
        }
        Spacer(Modifier.height(LocalBottomOverlayPadding.current + 24.dp))
    }
}

@Composable
private fun CompareWordChip(
    word: com.feiyan.english.core.model.IpaWord,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .background(PaperCard)
            .padding(horizontal = 12.dp, vertical = 9.dp),
    ) {
        Text(
            text = word.word,
            style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp),
            color = Ink,
        )
        Text(
            text = "${word.phonetic} · ${word.translation}",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, lineHeight = 14.sp),
            color = InkFaint,
        )
    }
}

@Composable
private fun PhonemeCard(
    phoneme: IpaPhoneme,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier
            .height(98.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(PaperCard)
            .border(0.8.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(13.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 7.dp, vertical = 9.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "/${phoneme.symbol}/",
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
            color = Kaki,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = phoneme.categoryLabel,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp),
            color = InkFaint,
            maxLines = 1,
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = phoneme.examples.firstOrNull()?.word.orEmpty(),
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.5.sp),
            color = InkMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun IpaModeBar(selected: IpaTab, onSelect: (IpaTab) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(PaperCard)
            .border(0.8.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
            .padding(4.dp),
    ) {
        IpaTab.entries.forEach { tab ->
            val active = tab == selected
            Box(
                Modifier
                    .weight(1f)
                    .height(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (active) Kaki else Color.Transparent)
                    .clickable { onSelect(tab) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = tab.label,
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
private fun IpaChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall.copy(
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        ),
        color = if (selected) MaterialTheme.colorScheme.onPrimary else InkMuted,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) Kaki else PaperCard)
            .border(
                0.8.dp,
                if (selected) Color.Transparent else MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(16.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 11.dp, vertical = 9.dp),
    )
}

@Composable
private fun IpaInfoCard(title: String, body: String) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(PaperCard)
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Eyebrow(title)
        Spacer(Modifier.height(5.dp))
        Text(
            text = body,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp, lineHeight = 20.sp),
            color = InkMuted,
        )
    }
}

@Composable
private fun IpaWordRow(
    word: com.feiyan.english.core.model.IpaWord,
    compact: Boolean = false,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(11.dp))
            .background(PaperCard)
            .padding(horizontal = 12.dp, vertical = if (compact) 8.dp else 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = word.word,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = if (compact) 12.5.sp else 14.sp,
                fontWeight = FontWeight.Medium,
            ),
            color = Ink,
            modifier = Modifier.width(90.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = word.phonetic,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
            color = Kaki,
            modifier = Modifier.width(78.dp),
            maxLines = 1,
        )
        Text(
            text = word.translation,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
            color = InkMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun IpaTopBar(
    onBack: () -> Unit,
    title: String = "国际音标",
) {
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
        Eyebrow("WXAIWAY · $title", modifier = Modifier.weight(1f))
    }
}
