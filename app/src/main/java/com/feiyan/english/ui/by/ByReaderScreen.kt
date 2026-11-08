package com.feiyan.english.ui.by

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.feiyan.english.core.model.ByChapterData
import com.feiyan.english.core.model.ByParagraph
import com.feiyan.english.core.model.ByWord
import com.feiyan.english.ui.components.ErrorView
import com.feiyan.english.ui.components.Eyebrow
import com.feiyan.english.ui.components.Hairline
import com.feiyan.english.ui.components.LoadingView
import com.feiyan.english.ui.components.LocalBottomOverlayPadding
import com.feiyan.english.ui.icons.FeiyanIcons
import com.feiyan.english.ui.theme.Hairline as HairlineColor
import com.feiyan.english.ui.theme.Ink
import com.feiyan.english.ui.theme.InkFaint
import com.feiyan.english.ui.theme.InkMuted
import com.feiyan.english.ui.theme.Kaki
import com.feiyan.english.ui.theme.KakiSoft
import com.feiyan.english.ui.theme.PaperCard
import kotlinx.coroutines.delay

private const val WORD_TAG = "word"

@Composable
fun ByReaderScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: ByReaderViewModel = viewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    var tip by remember { mutableStateOf<Pair<String, String>?>(null) }

    Box(modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier.fillMaxWidth().padding(start = 8.dp, end = 16.dp, top = 6.dp),
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
                Eyebrow(
                    text = state.chapter?.bookName.orEmpty().ifBlank { "BOOKYELL" },
                    modifier = Modifier.weight(1f),
                )
                state.chapter?.let { chapter ->
                    Text(
                        text = "朗读",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                        color = Kaki,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(KakiSoft)
                            .clickable { viewModel.speak(chapter) }
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                    )
                }
            }

            when {
                state.loading -> LoadingView()
                state.chapter == null -> ErrorView(state.error ?: "载入失败", onRetry = viewModel::load)
                else -> Content(
                    chapter = state.chapter!!,
                    onWordTap = { word -> tip = word.text.trim() to (word.brief ?: "") },
                )
            }
        }

        // 单词释义浮层：点任意带译文的单词弹出
        tip?.let { (word, brief) ->
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(PaperCard)
                    .border(0.8.dp, HairlineColor, RoundedCornerShape(10.dp))
                    .clickable { tip = null }
                    .padding(16.dp),
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = word,
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                            color = Ink,
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = "关闭",
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp),
                            color = InkFaint,
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = brief,
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkMuted,
                    )
                }
            }
        }

        // 朗读提示（登录 / 额度）
        message?.let { text ->
            LaunchedEffect(text) {
                delay(3200)
                viewModel.clearMessage()
            }
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(PaperCard)
                    .border(0.8.dp, HairlineColor, RoundedCornerShape(10.dp))
                    .padding(14.dp),
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodySmall,
                    color = InkMuted,
                )
            }
        }
    }
}

@Composable
private fun Content(
    chapter: ByChapterData,
    onWordTap: (ByWord) -> Unit,
) {
    val paragraphs = chapter.paragraphs.filter { it.type == 1 || it.text.isNotBlank() }
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
                    text = chapter.chapter?.name.orEmpty(),
                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 22.sp),
                    color = Ink,
                )
                if (!chapter.chapter?.nameCn.isNullOrBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = chapter.chapter!!.nameCn.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = InkMuted,
                    )
                }
                Spacer(Modifier.height(10.dp))
                Eyebrow("${chapter.chapter?.wordCount ?: 0} 词 · 点词看中文", color = InkFaint)
                Spacer(Modifier.height(16.dp))
                Hairline()
            }
        }

        itemsIndexed(items = paragraphs, key = { index, _ -> index }) { _, paragraph ->
            Paragraph(paragraph = paragraph, onWordTap = onWordTap)
        }

        item(key = "footer") {
            Column(Modifier.padding(top = 32.dp)) {
                Hairline()
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "内容来源 bookyell.com · 仅用于学习",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.3.sp),
                    color = InkFaint,
                )
            }
        }
    }
}

@Composable
private fun Paragraph(paragraph: ByParagraph, onWordTap: (ByWord) -> Unit) {
    if (paragraph.type == 1 && paragraph.path.isNotBlank()) {
        Box(
            Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(KakiSoft.copy(alpha = 0.35f)),
        ) {
            AsyncImage(
                model = paragraph.path,
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxWidth().aspectRatio(1.6f),
            )
        }
        return
    }

    val (annotated, tippedWords) = remember(paragraph) { buildWordText(paragraph) }
    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }

    Text(
        text = annotated,
        style = MaterialTheme.typography.titleSmall.copy(
            fontFamily = FontFamily.Serif,
            fontSize = 17.sp,
            lineHeight = 30.sp,
        ),
        color = Ink,
        onTextLayout = { layout = it },
        modifier = Modifier
            .padding(top = 18.dp)
            .fillMaxWidth()
            .pointerInput(paragraph) {
                detectTapGestures { position ->
                    val result = layout ?: return@detectTapGestures
                    val offset = result.getOffsetForPosition(position)
                    val index = annotated
                        .getStringAnnotations(WORD_TAG, offset, offset)
                        .firstOrNull()
                        ?.item
                        ?.toIntOrNull()
                        ?: return@detectTapGestures
                    tippedWords.getOrNull(index)?.let(onWordTap)
                }
            },
    )
}

/** 把段落拆成带注解的富文本：只有带中文释义的词才可点。 */
private fun buildWordText(paragraph: ByParagraph): Pair<AnnotatedString, List<ByWord>> {
    val tipped = mutableListOf<ByWord>()
    val builder = buildAnnotatedString {
        paragraph.sentences.forEachIndexed { sentenceIndex, sentence ->
            if (sentenceIndex > 0) append(" ")
            sentence.words.forEach { word ->
                val brief = word.brief
                if (!brief.isNullOrBlank()) {
                    pushStringAnnotation(WORD_TAG, tipped.size.toString())
                    append(word.text)
                    pop()
                    tipped += word
                } else {
                    append(word.text)
                }
            }
        }
    }
    return builder to tipped
}
