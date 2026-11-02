package com.feiyan.english.ui.yy

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.feiyan.english.core.model.YyBookDetail
import com.feiyan.english.ui.components.ErrorView
import com.feiyan.english.ui.components.Eyebrow
import com.feiyan.english.ui.components.Hairline
import com.feiyan.english.ui.components.LoadingView
import com.feiyan.english.ui.components.LocalBottomOverlayPadding
import com.feiyan.english.ui.components.SectionHeader
import com.feiyan.english.ui.icons.FeiyanIcons
import com.feiyan.english.ui.theme.Ink
import com.feiyan.english.ui.theme.InkFaint
import com.feiyan.english.ui.theme.InkMuted
import com.feiyan.english.ui.theme.Kaki

@Composable
fun YyBookScreen(
    onBack: () -> Unit,
    onChapterClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: YyBookViewModel = viewModel()
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
            Eyebrow("NOVEL · 双语小说")
        }

        when {
            state.loading -> LoadingView()
            state.error != null -> ErrorView(state.error!!, onRetry = viewModel::load)
            state.book != null -> LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = LocalBottomOverlayPadding.current + 24.dp),
            ) {
                item(key = "head") { BookHeader(state.book!!) }
                item(key = "chapters") {
                    SectionHeader("章节目录", "CHAPTERS", trailing = "${state.chapters.size} 章")
                }
                items(items = state.chapters, key = { it.first }) { (index, title) ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onChapterClick(index) }
                            .padding(horizontal = 24.dp, vertical = 15.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = (index + 1).toString().padStart(2, '0'),
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.4.sp),
                            color = InkFaint,
                            modifier = Modifier.width(28.dp),
                        )
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp),
                            color = Ink,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        Icon(
                            imageVector = FeiyanIcons.ChevronRight,
                            contentDescription = null,
                            tint = InkFaint,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                    Hairline(Modifier.padding(start = 76.dp, end = 24.dp))
                }
            }
        }
    }
}

@Composable
private fun BookHeader(book: YyBookDetail) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    Column(Modifier.padding(horizontal = 24.dp).padding(top = 12.dp)) {
        Row(verticalAlignment = Alignment.Top) {
            BookCover(id = book.id, width = 98.dp, height = 132.dp)
            Spacer(Modifier.width(18.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = book.cnName,
                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 22.sp),
                    color = Ink,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = book.enName,
                    style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp),
                    color = InkMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = listOfNotNull(book.cnAuthor?.ifBlank { null }, book.nandu?.let { "难度 $it" })
                        .joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = InkFaint,
                )
            }
        }
        if (!book.intro.isNullOrBlank()) {
            Spacer(Modifier.height(18.dp))
            Text(
                text = book.intro!!.trim(),
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 21.sp),
                color = InkMuted,
                maxLines = if (expanded) 60 else 4,
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
