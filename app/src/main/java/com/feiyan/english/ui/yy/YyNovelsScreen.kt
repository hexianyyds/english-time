package com.feiyan.english.ui.yy

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.feiyan.english.core.model.YyBook
import com.feiyan.english.ui.by.ByBooksList
import com.feiyan.english.ui.components.ErrorView
import com.feiyan.english.ui.components.Eyebrow
import com.feiyan.english.ui.components.LoadingView
import com.feiyan.english.ui.components.LocalBottomOverlayPadding
import com.feiyan.english.ui.components.SectionHeader
import com.feiyan.english.ui.components.SourceSwitch
import com.feiyan.english.ui.theme.Ink
import com.feiyan.english.ui.theme.InkFaint
import com.feiyan.english.ui.theme.InkMuted

@Composable
fun YyNovelsScreen(
    onBookClick: (String) -> Unit,
    onByBookClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var source by rememberSaveable { mutableIntStateOf(0) }

    Column(modifier.fillMaxSize()) {
        Column(Modifier.padding(horizontal = 24.dp).padding(top = 26.dp)) {
            Eyebrow("FICTION · 双语小说")
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Stories,\nline by line.",
                style = MaterialTheme.typography.displaySmall.copy(fontSize = 32.sp, lineHeight = 40.sp),
                color = Ink,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "中英对照，逐段阅读。",
                style = MaterialTheme.typography.bodySmall,
                color = InkMuted,
            )
            Spacer(Modifier.height(16.dp))
        }

        SourceSwitch(
            labels = listOf("英语小说网", "BookYell"),
            selected = source,
            onSelect = { source = it },
        )
        Spacer(Modifier.height(4.dp))

        if (source == 1) {
            ByBooksList(onBookClick = onByBookClick, modifier = Modifier.weight(1f))
        } else {
            YyBooksContent(onBookClick = onBookClick, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun YyBooksContent(onBookClick: (String) -> Unit, modifier: Modifier = Modifier) {
    val viewModel: YyNovelsViewModel = viewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    when {
        state.loading -> LoadingView(modifier)
        state.error != null -> ErrorView(state.error!!, onRetry = { viewModel.load(true) })
        else -> LazyColumn(
            modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = LocalBottomOverlayPadding.current + 24.dp),
        ) {
            val sections = listOf(
                Triple("双语小说", "FEATURED", state.books.main),
                Triple("热门", "POPULAR", state.books.hot),
                Triple("推荐", "PICKS", state.books.tuijian),
                Triple("更多", "MORE", state.books.more),
            )
            sections.forEach { (title, english, books) ->
                if (books.isNotEmpty()) {
                    item(key = "h-$title") {
                        SectionHeader(title = title, english = english, trailing = "${books.size} 本")
                    }
                    item(key = "r-$title") {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            items(items = books, key = { it.id }) { book ->
                                BookCard(book = book, onClick = { onBookClick(book.id) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BookCard(book: YyBook, onClick: () -> Unit) {
    Column(Modifier.width(108.dp).clickable(onClick = onClick)) {
        BookCover(id = book.id, width = 108.dp, height = 146.dp)
        Spacer(Modifier.height(9.dp))
        Text(
            text = book.cnName,
            style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp),
            color = Ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = book.enName,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = InkFaint,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Eyebrow(book.nandu ?: "—", color = InkFaint)
            Spacer(Modifier.width(6.dp))
            Eyebrow(book.category ?: "", color = InkFaint)
        }
    }
}
