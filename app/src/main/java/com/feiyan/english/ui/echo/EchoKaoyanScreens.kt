package com.feiyan.english.ui.echo

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.feiyan.english.data.EchoAnswerGroup
import com.feiyan.english.data.EchoCatalog
import com.feiyan.english.data.EchoDocumentInfo
import com.feiyan.english.data.EchoDocumentKind
import com.feiyan.english.data.EchoExam
import com.feiyan.english.data.EchoKaoyanRepository
import com.feiyan.english.data.EchoYear
import com.feiyan.english.ui.components.Eyebrow
import com.feiyan.english.ui.components.Hairline
import com.feiyan.english.ui.components.LocalBottomOverlayPadding
import com.feiyan.english.ui.components.ThemeToggleButton
import com.feiyan.english.ui.icons.FeiyanIcons
import com.feiyan.english.ui.theme.Ink
import com.feiyan.english.ui.theme.InkFaint
import com.feiyan.english.ui.theme.InkMuted
import com.feiyan.english.ui.theme.Kaki
import com.feiyan.english.ui.theme.KakiSoft
import com.feiyan.english.ui.theme.PaperCard

@Composable
fun EchoKaoyanHubScreen(
    onBack: () -> Unit,
    onYearClick: (EchoExam, Int) -> Unit,
    onDocumentClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    showTopBar: Boolean = true,
) {
    val context = LocalContext.current
    var catalog by remember { mutableStateOf<EchoCatalog?>(null) }
    var referenceCount by remember { mutableIntStateOf(0) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var selectedExamKey by rememberSaveable { mutableStateOf(EchoExam.ENGLISH_I.key) }
    val selectedExam = EchoExam.fromKey(selectedExamKey) ?: EchoExam.ENGLISH_I

    LaunchedEffect(context) {
        runCatching { EchoKaoyanRepository.loadCatalog(context) }
            .onSuccess { catalog = it }
            .onFailure { loadError = it.message ?: "无法读取内置资料" }
        runCatching { EchoKaoyanRepository.countReferenceFiles(context) }
            .onSuccess { referenceCount = it }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = LocalBottomOverlayPadding.current + 34.dp),
    ) {
        if (showTopBar) {
            item {
                EchoTopBar(
                    eyebrow = "ECHO · 考研英语",
                    onBack = onBack,
                    trailing = { ThemeToggleButton() },
                )
            }
        }

        item {
            Column(Modifier.padding(horizontal = 24.dp)) {
                Spacer(Modifier.height(18.dp))
                Text(
                    text = "Echo 考研英语",
                    style = MaterialTheme.typography.displaySmall.copy(fontSize = 32.sp),
                    color = Ink,
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "2010–2026 英语一 / 英语二真题、客观题答案与全套方法资料，全部离线内置。",
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                    color = InkMuted,
                )
                Spacer(Modifier.height(18.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    EchoStat((catalog?.paperCount ?: 34).toString(), "套真题", Modifier.weight(1f))
                    EchoStat((catalog?.sectionCount ?: 272).toString(), "篇文本", Modifier.weight(1f))
                    EchoStat(if (referenceCount > 0) referenceCount.toString() else "419", "份资料", Modifier.weight(1f))
                }
                Spacer(Modifier.height(24.dp))
            }
        }

        if (loadError != null) {
            item {
                EchoError(
                    message = loadError.orEmpty(),
                    modifier = Modifier.padding(horizontal = 24.dp),
                )
            }
        } else if (catalog == null) {
            item { EchoLoading("正在载入 34 年真题…") }
        } else {
            item {
                Column(Modifier.padding(horizontal = 24.dp)) {
                    Eyebrow("EXAM TRACK")
                    Spacer(Modifier.height(10.dp))
                    EchoExamSwitch(
                        selected = selectedExam,
                        onSelect = { selectedExamKey = it.key },
                    )
                    Spacer(Modifier.height(22.dp))
                    Eyebrow("YEARS · 选择年份")
                }
            }

            val years = catalog?.yearsFor(selectedExam).orEmpty()
            items(years, key = { "${it.exam.key}-${it.year}" }) { year ->
                YearCard(
                    year = year,
                    onClick = { onYearClick(year.exam, year.year) },
                )
            }

            item {
                Column(Modifier.padding(horizontal = 24.dp)) {
                    Spacer(Modifier.height(24.dp))
                    Eyebrow("METHODS · 方法与规范")
                    Spacer(Modifier.height(5.dp))
                    Text(
                        text = "阅读、完形、翻译、写作与模拟训练的完整备考标准。",
                        style = MaterialTheme.typography.bodySmall,
                        color = InkMuted,
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }

            item {
                val guides = EchoKaoyanRepository.guideDocuments
                Column(
                    Modifier
                        .padding(horizontal = 24.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(PaperCard)
                        .border(0.8.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp)),
                ) {
                    guides.forEachIndexed { index, document ->
                        GuideRow(
                            document = document,
                            onClick = { onDocumentClick(document.path) },
                        )
                        if (index != guides.lastIndex) {
                            Hairline(Modifier.padding(start = 62.dp, end = 16.dp))
                        }
                    }
                }
            }

            item {
                Column(Modifier.padding(horizontal = 24.dp, vertical = 26.dp)) {
                    Text(
                        text = "资料源 · Echo_考研英语SKILL",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.9.sp),
                        color = InkFaint,
                    )
                    Spacer(Modifier.height(5.dp))
                    Text(
                        text = "所有内容均已打包进应用，无需联网即可阅读。",
                        style = MaterialTheme.typography.bodySmall,
                        color = InkFaint,
                    )
                }
            }
        }
    }
}

@Composable
fun EchoYearScreen(
    examKey: String,
    yearValue: Int,
    onBack: () -> Unit,
    onDocumentClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var year by remember { mutableStateOf<EchoYear?>(null) }
    var loadError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(context, examKey, yearValue) {
        runCatching { EchoKaoyanRepository.loadCatalog(context) }
            .onSuccess { catalog -> year = catalog.find(examKey, yearValue) }
            .onFailure { loadError = it.message ?: "无法读取该年份资料" }
    }

    Column(modifier.fillMaxSize()) {
        EchoTopBar(eyebrow = "ECHO · 真题", onBack = onBack)
        Hairline()

        val currentYear = year
        if (loadError != null) {
            EchoError(loadError.orEmpty(), Modifier.padding(24.dp))
        } else if (currentYear == null) {
            EchoLoading("正在载入 $yearValue 年真题…")
        } else {
            val documents = EchoKaoyanRepository.documents(currentYear)
            val paper = documents.firstOrNull { it.kind == EchoDocumentKind.PAPER }
            val sections = documents.filter { it.kind == EchoDocumentKind.SECTION }
            val answers = documents.firstOrNull { it.kind == EchoDocumentKind.ANSWERS }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = LocalBottomOverlayPadding.current + 34.dp),
            ) {
                item {
                    Column(Modifier.padding(horizontal = 24.dp)) {
                        Spacer(Modifier.height(20.dp))
                        Eyebrow("${currentYear.exam.shortName} · ${currentYear.year}")
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "${currentYear.year} 真题",
                            style = MaterialTheme.typography.displaySmall.copy(fontSize = 32.sp),
                            color = Ink,
                        )
                        Spacer(Modifier.height(9.dp))
                        Text(
                            text = "${currentYear.exam.displayName} · 完整试卷、${currentYear.sections.size} 个题型与客观题答案。",
                            style = MaterialTheme.typography.bodySmall,
                            color = InkMuted,
                        )
                        Spacer(Modifier.height(20.dp))
                    }
                }

                if (paper != null) {
                    item {
                        PaperCardRow(
                            document = paper,
                            onClick = { onDocumentClick(paper.path) },
                        )
                    }
                }

                item {
                    Column(Modifier.padding(horizontal = 24.dp)) {
                        Spacer(Modifier.height(24.dp))
                        Eyebrow("SECTIONS · 按题型学习")
                        Spacer(Modifier.height(12.dp))
                    }
                }

                item {
                    Column(
                        Modifier
                            .padding(horizontal = 24.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(PaperCard)
                            .border(0.8.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp)),
                    ) {
                        sections.forEachIndexed { index, document ->
                            SectionRow(
                                index = index + 1,
                                document = document,
                                onClick = { onDocumentClick(document.path) },
                            )
                            if (index != sections.lastIndex) {
                                Hairline(Modifier.padding(start = 62.dp, end = 16.dp))
                            }
                        }
                    }
                }

                if (answers != null) {
                    item {
                        AnswersCard(
                            document = answers,
                            onClick = { onDocumentClick(answers.path) },
                        )
                    }
                }

                item {
                    Text(
                        text = "先独立完成，再核对答案；客观题答案不替代逐题证据链分析。",
                        style = MaterialTheme.typography.labelSmall.copy(lineHeight = 17.sp),
                        color = InkFaint,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun EchoDocumentScreen(
    path: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val document = remember(path) { EchoKaoyanRepository.describe(path) }
    val contextLabel = listOfNotNull(document.exam?.shortName, document.year).joinToString(" · ")
    val eyebrow = when (document.kind) {
        EchoDocumentKind.GUIDE -> "ECHO · 方法资料"
        EchoDocumentKind.ANSWERS -> "ECHO · 参考答案 · $contextLabel"
        else -> "ECHO · $contextLabel"
    }

    Column(modifier.fillMaxSize()) {
        EchoTopBar(eyebrow = eyebrow, onBack = onBack)
        Hairline()
        if (document.kind == EchoDocumentKind.ANSWERS) {
            EchoAnswersContent(document)
        } else {
            EchoTextContent(document)
        }
    }
}

@Composable
private fun EchoTextContent(document: EchoDocumentInfo) {
    val context = LocalContext.current
    var text by remember(document.path) { mutableStateOf<String?>(null) }
    var error by remember(document.path) { mutableStateOf<String?>(null) }

    LaunchedEffect(context, document.path) {
        runCatching {
            val raw = EchoKaoyanRepository.readDocument(context, document.path)
            if (document.path.endsWith(".json", ignoreCase = true)) {
                EchoKaoyanRepository.prettyJson(raw)
            } else {
                raw
            }
        }.onSuccess { text = it }
            .onFailure { error = it.message ?: "无法读取资料" }
    }

    when {
        error != null -> EchoError(error.orEmpty(), Modifier.padding(24.dp))
        text == null -> EchoLoading("正在打开资料…")
        else -> EchoMarkdownArticle(
            markdown = text.orEmpty(),
            contentPadding = PaddingValues(
                start = 24.dp,
                end = 24.dp,
                top = 18.dp,
                bottom = LocalBottomOverlayPadding.current + 40.dp,
            ),
        )
    }
}

@Composable
private fun EchoAnswersContent(document: EchoDocumentInfo) {
    val context = LocalContext.current
    var groups by remember(document.path) { mutableStateOf<List<EchoAnswerGroup>?>(null) }
    var error by remember(document.path) { mutableStateOf<String?>(null) }
    val bottomPadding = LocalBottomOverlayPadding.current

    LaunchedEffect(context, document.path) {
        runCatching { EchoKaoyanRepository.loadAnswers(context, document.path) }
            .onSuccess { groups = it }
            .onFailure { error = it.message ?: "无法读取答案" }
    }

    when {
        error != null -> EchoError(error.orEmpty(), Modifier.padding(24.dp))
        groups == null -> EchoLoading("正在载入参考答案…")
        else -> LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = bottomPadding + 38.dp),
        ) {
            item {
                Column(Modifier.padding(horizontal = 24.dp)) {
                    Spacer(Modifier.height(20.dp))
                    Eyebrow("ANSWER KEY")
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "参考答案",
                        style = MaterialTheme.typography.displaySmall.copy(fontSize = 31.sp),
                        color = Ink,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = document.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = InkMuted,
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "建议先独立作答，再核对答案。需要完整证据链时，请结合对应题型原文使用。",
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                        color = Kaki,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(KakiSoft.copy(alpha = 0.55f))
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                    )
                    Spacer(Modifier.height(10.dp))
                }
            }

            val allGroups = groups.orEmpty()
            items(allGroups, key = { it.section }) { group ->
                AnswerGroupCard(group)
            }

            if (allGroups.isEmpty()) {
                item {
                    Text(
                        text = "该年份暂未收录客观题答案。",
                        style = MaterialTheme.typography.bodySmall,
                        color = InkMuted,
                        modifier = Modifier.padding(24.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun EchoTopBar(
    eyebrow: String,
    onBack: () -> Unit,
    trailing: (@Composable () -> Unit)? = null,
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
        Eyebrow(eyebrow, modifier = Modifier.weight(1f))
        trailing?.invoke()
    }
}

@Composable
private fun EchoStat(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(15.dp))
            .background(PaperCard)
            .border(0.8.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(15.dp))
            .padding(vertical = 13.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp),
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
private fun EchoExamSwitch(
    selected: EchoExam,
    onSelect: (EchoExam) -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(15.dp))
            .background(PaperCard)
            .border(0.8.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(15.dp))
            .padding(4.dp),
    ) {
        EchoExam.entries.forEach { exam ->
            val active = exam == selected
            Box(
                Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(if (active) Kaki else androidx.compose.ui.graphics.Color.Transparent)
                    .clickable { onSelect(exam) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "${exam.displayName} · ${exam.shortName}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        letterSpacing = 0.6.sp,
                    ),
                    color = if (active) MaterialTheme.colorScheme.onPrimary else InkMuted,
                )
            }
        }
    }
}

@Composable
private fun YearCard(year: EchoYear, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(PaperCard)
            .border(0.8.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = year.year.toString(),
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp),
            color = Ink,
        )
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = "完整试卷 · ${year.sections.size} 个题型",
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp),
                color = Ink,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "完形 · 阅读 4 篇 · 新题型 · 翻译 · 写作",
                style = MaterialTheme.typography.bodySmall,
                color = InkFaint,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(8.dp))
        Icon(
            imageVector = FeiyanIcons.ChevronRight,
            contentDescription = null,
            tint = InkFaint,
            modifier = Modifier.size(14.dp),
        )
    }
}

@Composable
private fun GuideRow(document: EchoDocumentInfo, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(KakiSoft.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = FeiyanIcons.Article,
                contentDescription = null,
                tint = Kaki,
                modifier = Modifier.size(16.dp),
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = document.title,
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp),
                color = Ink,
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = document.subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = InkFaint,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
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
private fun PaperCardRow(document: EchoDocumentInfo, onClick: () -> Unit) {
    Row(
        Modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(KakiSoft.copy(alpha = 0.5f))
            .border(0.8.dp, Kaki.copy(alpha = 0.25f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(Kaki),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = FeiyanIcons.BookOpen,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = "完整试卷",
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                color = Ink,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = document.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = InkMuted,
            )
        }
        Icon(
            imageVector = FeiyanIcons.ChevronRight,
            contentDescription = null,
            tint = Ink,
            modifier = Modifier.size(15.dp),
        )
    }
}

@Composable
private fun SectionRow(
    index: Int,
    document: EchoDocumentInfo,
    onClick: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 15.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = index.toString().padStart(2, '0'),
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                letterSpacing = 0.8.sp,
            ),
            color = Kaki,
            modifier = Modifier.width(34.dp),
        )
        Column(Modifier.weight(1f)) {
            Text(
                text = document.title,
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp),
                color = Ink,
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = document.subtitle.substringAfterLast(" · "),
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = InkFaint,
            )
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
private fun AnswersCard(document: EchoDocumentInfo, onClick: () -> Unit) {
    Column(Modifier.padding(horizontal = 24.dp, vertical = 18.dp)) {
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(PaperCard)
                .border(0.8.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(KakiSoft),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "A",
                    style = MaterialTheme.typography.titleMedium,
                    color = Kaki,
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = "参考答案",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = Ink,
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = document.subtitle.substringAfterLast(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = InkFaint,
                )
            }
            Icon(
                imageVector = FeiyanIcons.ChevronRight,
                contentDescription = null,
                tint = InkFaint,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

@Composable
private fun AnswerGroupCard(group: EchoAnswerGroup) {
    Column(Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = group.title,
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp),
                color = Ink,
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "${group.answers.size} 题",
                style = MaterialTheme.typography.labelSmall,
                color = InkFaint,
            )
        }
        Spacer(Modifier.height(9.dp))
        group.answers.chunked(5).forEach { rowAnswers ->
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                rowAnswers.forEach { answer ->
                    Column(
                        Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(PaperCard)
                            .border(0.8.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                            .padding(vertical = 9.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = answer.number,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = InkFaint,
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = answer.letter,
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                            color = Kaki,
                        )
                    }
                }
                repeat((5 - rowAnswers.size).coerceAtLeast(0)) {
                    Spacer(Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(7.dp))
        }
    }
}

@Composable
private fun EchoLoading(text: String) {
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
private fun EchoError(message: String, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().padding(vertical = 36.dp)) {
        Text(
            text = "资料读取失败",
            style = MaterialTheme.typography.titleSmall,
            color = Ink,
        )
        Spacer(Modifier.height(7.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
            color = InkMuted,
        )
    }
}
