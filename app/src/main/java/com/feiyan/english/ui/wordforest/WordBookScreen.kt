package com.feiyan.english.ui.wordforest

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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.feiyan.english.core.model.WfBook
import com.feiyan.english.core.model.WfWord
import com.feiyan.english.data.WordForestRepository
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
import kotlinx.coroutines.launch

private data class WordBookState(
    val book: WfBook? = null,
    val words: List<WfWord> = emptyList(),
    val total: Int = 0,
    val days: Int = 0,
    val nextDay: Int = 0,
    val loading: Boolean = true,
    val loadingMore: Boolean = false,
    val error: String? = null,
    val loadMoreError: String? = null,
)

@Composable
fun WordBookScreen(
    bookId: String,
    onBack: () -> Unit,
    onWordClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    var state by remember { mutableStateOf(WordBookState()) }
    var reloadToken by remember { mutableIntStateOf(0) }

    LaunchedEffect(bookId, reloadToken) {
        state = WordBookState()
        runCatching {
            val book = WordForestRepository.book(bookId)
            val page = WordForestRepository.wordPage(bookId, day = 0)
            book to page
        }.onSuccess { (book, page) ->
            state = WordBookState(
                book = book,
                words = page.words.distinctBy { it.id },
                total = page.total,
                days = page.properties.days,
                nextDay = 1,
                loading = false,
            )
        }.onFailure {
            state = WordBookState(
                loading = false,
                error = it.message ?: "无法加载这本词书",
            )
        }
    }

    fun loadMore() {
        val current = state
        if (current.loadingMore || current.error != null || current.nextDay >= current.days) return
        state = current.copy(loadingMore = true, loadMoreError = null)
        scope.launch {
            runCatching { WordForestRepository.wordPage(bookId, current.nextDay) }
                .onSuccess { page ->
                    state = state.copy(
                        words = (state.words + page.words).distinctBy { it.id },
                        total = page.total,
                        days = page.properties.days,
                        nextDay = current.nextDay + 1,
                        loadingMore = false,
                    )
                }
                .onFailure {
                    state = state.copy(
                        loadingMore = false,
                        loadMoreError = it.message ?: "加载下一组失败",
                    )
                }
        }
    }

    Column(modifier.fillMaxSize()) {
        WordForestTopBar(eyebrow = "WORDFOREST · 词书", onBack = onBack)
        Hairline()

        when {
            state.loading -> WordForestLoading("正在加载词书…")
            state.error != null -> WordForestError(state.error.orEmpty()) {
                reloadToken++
            }
            else -> LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = LocalBottomOverlayPadding.current + 30.dp),
            ) {
                item {
                    BookHeader(state = state)
                }

                item {
                    Row(
                        Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Eyebrow("WORDS · 单词")
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = "已加载 ${state.words.size} / ${state.total}",
                            style = MaterialTheme.typography.labelSmall,
                            color = InkFaint,
                        )
                    }
                }

                itemsIndexed(state.words, key = { _, word -> word.id }) { index, word ->
                    WordRow(
                        index = index + 1,
                        word = word,
                        onClick = { onWordClick(word.word) },
                    )
                    Hairline(Modifier.padding(horizontal = 24.dp))
                }

                item {
                    Column(
                        Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        if (state.loadMoreError != null) {
                            Text(
                                text = state.loadMoreError.orEmpty(),
                                style = MaterialTheme.typography.bodySmall,
                                color = InkMuted,
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                        if (state.nextDay < state.days) {
                            Text(
                                text = if (state.loadingMore) "正在加载…" else "加载下一组",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 13.sp,
                                    letterSpacing = 0.6.sp,
                                ),
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Kaki)
                                    .clickable(enabled = !state.loadingMore) { loadMore() }
                                    .padding(horizontal = 24.dp, vertical = 12.dp),
                            )
                        } else {
                            Text(
                                text = "已加载全部单词",
                                style = MaterialTheme.typography.labelSmall,
                                color = InkFaint,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BookHeader(state: WordBookState) {
    Column(Modifier.padding(horizontal = 24.dp)) {
        Spacer(Modifier.height(20.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            val cover = state.book?.let { WordForestRepository.coverUrl(it) }
            Box(
                Modifier
                    .size(width = 76.dp, height = 102.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(KakiSoft),
            ) {
                if (cover != null) {
                    AsyncImage(
                        model = cover,
                        contentDescription = state.book.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = state.book?.name.orEmpty().ifBlank { "词书" },
                    style = MaterialTheme.typography.displaySmall.copy(fontSize = 29.sp),
                    color = Ink,
                )
                Spacer(Modifier.height(5.dp))
                Text(
                    text = state.book?.properties?.description.orEmpty().ifBlank { "Word Forest 词书" },
                    style = MaterialTheme.typography.bodySmall,
                    color = InkMuted,
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "${state.total} 词",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = Kaki,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(KakiSoft.copy(alpha = 0.55f))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                    Text(
                        text = "${state.days} 组",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = InkMuted,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(PaperCard)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }
        }
        Spacer(Modifier.height(18.dp))
    }
}

@Composable
private fun WordRow(
    index: Int,
    word: WfWord,
    onClick: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = index.toString().padStart(2, '0'),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            color = InkFaint,
            modifier = Modifier.width(28.dp),
        )
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = word.word,
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 19.sp),
                    color = Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val phonetic = word.usPhonetic.orEmpty().ifBlank { word.ukPhonetic.orEmpty() }
                if (phonetic.isNotBlank()) {
                    Spacer(Modifier.width(9.dp))
                    Text(
                        text = "/ $phonetic /",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp),
                        color = InkFaint,
                        maxLines = 1,
                    )
                }
            }
            val translation = word.translationPreview()
            if (translation.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = translation,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 18.sp),
                    color = InkMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        Icon(
            imageVector = FeiyanIcons.ChevronRight,
            contentDescription = null,
            tint = InkFaint,
            modifier = Modifier.size(13.dp),
        )
    }
}
