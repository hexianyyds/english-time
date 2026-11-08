package com.feiyan.english.ui.yy

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.feiyan.english.core.model.YyChapterData
import com.feiyan.english.core.model.YyParagraph
import com.feiyan.english.ui.components.ErrorView
import com.feiyan.english.ui.components.Eyebrow
import com.feiyan.english.ui.components.Hairline
import com.feiyan.english.ui.components.LoadingView
import com.feiyan.english.ui.components.LocalBottomOverlayPadding
import com.feiyan.english.ui.icons.FeiyanIcons
import com.feiyan.english.ui.theme.Ink
import com.feiyan.english.ui.theme.InkFaint
import com.feiyan.english.ui.theme.InkMuted
import com.feiyan.english.ui.theme.Kaki
import com.feiyan.english.ui.theme.KakiSoft

@Composable
fun YyChapterScreen(
    onBack: () -> Unit,
    onOpenChapter: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: YyChapterViewModel = viewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showChinese by rememberSaveable { mutableStateOf(true) }

    Column(modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(start = 8.dp, end = 20.dp, top = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(40.dp).clickable(onClick = onBack),
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
            Eyebrow(state.chapter?.n?.ifBlank { "READING" } ?: "READING")
            Spacer(Modifier.weight(1f))
            Text(
                text = if (showChinese) "中英" else "仅英文",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp),
                color = if (showChinese) Kaki else InkFaint,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (showChinese) KakiSoft else InkFaint.copy(alpha = 0.08f))
                    .clickable { showChinese = !showChinese }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            )
        }

        when {
            state.loading -> LoadingView()
            state.chapter == null -> ErrorView(state.error ?: "载入失败", onRetry = { viewModel.load(0) })
            else -> ChapterContent(
                chapter = state.chapter!!,
                paragraphs = state.paragraphs,
                total = state.total,
                showChinese = showChinese,
                onPrev = { index -> onOpenChapter(index) },
                onNext = { index -> onOpenChapter(index) },
            )
        }
    }
}

@Composable
private fun ChapterContent(
    chapter: YyChapterData,
    paragraphs: List<YyParagraph>,
    total: Int,
    showChinese: Boolean,
    onPrev: (Int) -> Unit,
    onNext: (Int) -> Unit,
) {
    val hasPrev = chapter.i > 0
    val hasNext = !chapter.next.isNullOrBlank()

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 24.dp,
            end = 24.dp,
            bottom = LocalBottomOverlayPadding.current + 36.dp,
        ),
    ) {
        item(key = "head") {
            Column {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = chapter.t,
                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 23.sp),
                    color = Ink,
                )
                Spacer(Modifier.height(8.dp))
                Eyebrow(
                    listOfNotNull(
                        chapter.en.ifBlank { null },
                        if (total > 0) "第 ${chapter.i + 1} / $total 章" else null,
                    ).joinToString(" · "),
                    color = InkFaint,
                )
                Spacer(Modifier.height(16.dp))
                Hairline()
            }
        }

        itemsIndexed(items = paragraphs, key = { index, _ -> index }) { _, paragraph ->
            Column(Modifier.padding(top = 18.dp)) {
                Text(
                    text = paragraph.english,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontFamily = FontFamily.Serif,
                        fontSize = 17.sp,
                        lineHeight = 29.sp,
                    ),
                    color = Ink,
                )
                if (showChinese && !paragraph.chinese.isNullOrBlank()) {
                    Spacer(Modifier.height(7.dp))
                    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                        Box(
                            Modifier
                                .width(2.dp)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(2.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)),
                        )
                        Text(
                            text = paragraph.chinese!!,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp, lineHeight = 20.sp),
                            color = InkMuted,
                            modifier = Modifier
                                .padding(start = 12.dp)
                                .fillMaxWidth(),
                        )
                    }
                }
            }
        }

        item(key = "nav") {
            Column(Modifier.padding(top = 32.dp)) {
                Hairline()
                Row(Modifier.fillMaxWidth().padding(vertical = 18.dp)) {
                    if (hasPrev) {
                        Text(
                            text = "上一章",
                            style = MaterialTheme.typography.bodyMedium.copy(letterSpacing = 0.8.sp),
                            color = Kaki,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(KakiSoft)
                                .clickable { onPrev(chapter.i - 1) }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    if (hasNext) {
                        Text(
                            text = "下一章",
                            style = MaterialTheme.typography.bodyMedium.copy(letterSpacing = 0.8.sp),
                            color = Kaki,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(KakiSoft)
                                .clickable { onNext(chapter.next!!.toInt()) }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                        )
                    }
                }
                Text(
                    text = "内容来源 yingyuxiaoshuo.com · 中英对照仅供学习",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.3.sp),
                    color = InkFaint,
                )
            }
        }
    }
}
