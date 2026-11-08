package com.feiyan.english.ui.by

import androidx.compose.foundation.background
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.feiyan.english.core.model.ByArticle
import com.feiyan.english.core.model.ByBook
import com.feiyan.english.ui.components.ErrorView
import com.feiyan.english.ui.components.Eyebrow
import com.feiyan.english.ui.components.Hairline
import com.feiyan.english.ui.components.LoadingView
import com.feiyan.english.ui.components.LocalBottomOverlayPadding
import com.feiyan.english.ui.theme.Ink
import com.feiyan.english.ui.theme.InkFaint
import com.feiyan.english.ui.theme.InkMuted
import com.feiyan.english.ui.theme.Kaki
import com.feiyan.english.ui.theme.KakiSoft

/** BookYell 书目列表（小说 Tab 的第二个来源）。 */
@Composable
fun ByBooksList(
    onBookClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: ByNovelsViewModel = viewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    when {
        state.loading -> LoadingView()
        state.error != null -> ErrorView(state.error!!, onRetry = { viewModel.load(true) })
        else -> LazyColumn(
            modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = LocalBottomOverlayPadding.current + 24.dp),
        ) {
            items(items = state.books, key = { it.id }) { book ->
                BookRow(book, onClick = { onBookClick(book.id) })
                Hairline(Modifier.padding(start = 96.dp, end = 24.dp))
            }
            if (state.hasMore) {
                item(key = "more") {
                    LaunchedEffect(state.books.size) { viewModel.loadMore() }
                    LoadingRow()
                }
            }
        }
    }
}

@Composable
private fun BookRow(book: ByBook, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(58.dp, 78.dp)
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
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = book.name,
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp),
                color = Ink,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(5.dp))
            Eyebrow(
                listOfNotNull(
                    book.author?.nameCn?.ifBlank { null } ?: book.author?.name,
                    book.levelStr,
                    "${book.wordCount} 词",
                ).joinToString(" · "),
                color = InkFaint,
            )
        }
    }
}

/** BookYell 文章列表（文章 Tab 的第二个来源）。 */
@Composable
fun ByArticlesList(
    onArticleClick: (ByArticle) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: ByArticlesViewModel = viewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    when {
        state.loading -> LoadingView()
        state.error != null -> ErrorView(state.error!!, onRetry = { viewModel.load(true) })
        else -> LazyColumn(
            modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = LocalBottomOverlayPadding.current + 24.dp),
        ) {
            items(items = state.articles, key = { it.id }) { article ->
                ArticleRow(article) { onArticleClick(article) }
                Hairline(Modifier.padding(horizontal = 24.dp))
            }
            if (state.hasMore) {
                item(key = "more") {
                    LaunchedEffect(state.articles.size) { viewModel.loadMore() }
                    LoadingRow()
                }
            }
        }
    }
}

@Composable
private fun ArticleRow(article: ByArticle, onClick: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 15.dp),
    ) {
        Text(
            text = article.name,
            style = MaterialTheme.typography.titleSmall.copy(fontSize = 16.sp),
            color = Ink,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        if (!article.nameCn.isNullOrBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = article.nameCn!!,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = InkMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.height(7.dp))
        Eyebrow("${article.wordCount} 词", color = InkFaint)
    }
}

@Composable
private fun LoadingRow() {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 22.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircularProgressIndicator(
            color = Kaki,
            strokeWidth = 1.2.dp,
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.width(10.dp))
        Text("正在载入更多", style = MaterialTheme.typography.labelSmall, color = InkFaint)
    }
}
