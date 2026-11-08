package com.feiyan.english.ui.by

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.feiyan.english.core.model.ByBook
import com.feiyan.english.core.model.ByChapterRef
import com.feiyan.english.ui.components.ErrorView
import com.feiyan.english.ui.components.Eyebrow
import com.feiyan.english.ui.components.FavoriteButton
import com.feiyan.english.ui.components.Hairline
import com.feiyan.english.ui.components.LoadingView
import com.feiyan.english.ui.components.LocalBottomOverlayPadding
import com.feiyan.english.ui.components.SectionHeader
import com.feiyan.english.ui.icons.FeiyanIcons
import com.feiyan.english.ui.theme.Ink
import com.feiyan.english.ui.theme.InkFaint
import com.feiyan.english.ui.theme.InkMuted
import com.feiyan.english.ui.theme.Kaki
import com.feiyan.english.ui.theme.KakiSoft

@Composable
fun ByBookScreen(
    onBack: () -> Unit,
    onChapterClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: ByBookViewModel = viewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

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
            Eyebrow("BOOKYELL · 小说", modifier = Modifier.weight(1f))
            state.detail?.book?.let { book ->
                FavoriteButton(
                    item = com.feiyan.english.data.LibraryItem(
                        id = "by:${book.id}",
                        title = book.name,
                        subtitle = "BookYell · 小说",
                        cover = book.logoUrl,
                        route = "by/${book.id}",
                    ),
                )
            }
        }

        when {
            state.loading -> LoadingView()
            state.error != null -> ErrorView(state.error!!, onRetry = viewModel::load)
            state.detail?.book != null -> {
                val detail = state.detail!!
                LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = LocalBottomOverlayPadding.current + 24.dp),
                ) {
                    item(key = "head") { Header(detail.book!!, detail.chapters.size) }
                    item(key = "chapters") {
                        SectionHeader("章节目录", "CHAPTERS", trailing = "${detail.chapters.size} 章")
                    }
                    items(items = detail.chapters, key = { it.id }) { chapter ->
                        ChapterRow(chapter) { onChapterClick(chapter.id) }
                        Hairline(Modifier.padding(start = 76.dp, end = 24.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun Header(book: ByBook, chapterCount: Int) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    Column(Modifier.padding(horizontal = 24.dp).padding(top = 12.dp)) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                Modifier
                    .size(96.dp, 130.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(KakiSoft),
            ) {
                if (!book.logoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = book.logoUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
            Spacer(Modifier.width(18.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = book.name,
                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 21.sp),
                    color = Ink,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = listOfNotNull(
                        book.author?.nameCn?.ifBlank { null } ?: book.author?.name,
                        book.levelStr,
                    ).joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = InkMuted,
                )
                Spacer(Modifier.height(10.dp))
                Eyebrow("${chapterCount} 章 · ${book.wordCount} 词")
            }
        }
        if (!book.desc.isNullOrBlank()) {
            Spacer(Modifier.height(18.dp))
            Text(
                text = book.desc!!.trim(),
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 21.sp),
                color = InkMuted,
                maxLines = if (expanded) 40 else 4,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = if (expanded) "收起" else "展开简介",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp),
                color = Kaki,
                modifier = Modifier.clickable { expanded = !expanded },
            )
        }
    }
}

@Composable
private fun ChapterRow(chapter: ByChapterRef, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = chapter.sort.toString().padStart(2, '0'),
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.4.sp),
            color = InkFaint,
            modifier = Modifier.width(28.dp),
        )
        Column(Modifier.weight(1f)) {
            Text(
                text = chapter.name,
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp),
                color = Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (!chapter.nameCn.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = chapter.nameCn!!,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                    color = InkFaint,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Icon(
            imageVector = FeiyanIcons.ChevronRight,
            contentDescription = null,
            tint = InkFaint,
            modifier = Modifier.size(14.dp),
        )
    }
}
