package com.feiyan.english.ui.eg

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.feiyan.english.core.model.EgArticle
import com.feiyan.english.ui.by.ByArticlesList
import com.feiyan.english.ui.components.ErrorView
import com.feiyan.english.ui.components.Eyebrow
import com.feiyan.english.ui.components.Hairline
import com.feiyan.english.ui.components.LoadingView
import com.feiyan.english.ui.components.LocalBottomOverlayPadding
import com.feiyan.english.ui.components.SourceSwitch
import com.feiyan.english.ui.theme.Ink
import com.feiyan.english.ui.theme.InkFaint
import com.feiyan.english.ui.theme.InkMuted
import com.feiyan.english.ui.theme.Kaki
import com.feiyan.english.ui.theme.KakiSoft

@Composable
fun EgListScreen(
    onArticleClick: (String) -> Unit,
    onByArticleClick: (String, Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var source by rememberSaveable { mutableIntStateOf(0) }

    Column(modifier.fillMaxSize()) {
        Column(Modifier.padding(horizontal = 24.dp).padding(top = 26.dp)) {
            Eyebrow("ARTICLES · 外刊精读")
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Reading",
                style = MaterialTheme.typography.displaySmall.copy(fontSize = 32.sp),
                color = Ink,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = if (source == 0) "Englio 精选外刊，按 CEFR 难度分级。" else "BookYell 每日外刊，原文精读。",
                style = MaterialTheme.typography.bodySmall,
                color = InkMuted,
            )
            Spacer(Modifier.height(16.dp))
        }

        SourceSwitch(
            labels = listOf("Englio", "BookYell"),
            selected = source,
            onSelect = { source = it },
        )
        Spacer(Modifier.height(4.dp))

        if (source == 1) {
            ByArticlesList(
                onArticleClick = { article -> onByArticleClick(article.bookId, article.id) },
                modifier = Modifier.weight(1f),
            )
        } else {
            EnglioContent(onArticleClick = onArticleClick, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun EnglioContent(onArticleClick: (String) -> Unit, modifier: Modifier = Modifier) {
    val viewModel: EgListViewModel = viewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(modifier.fillMaxSize()) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item(key = "all") {
                EgChip(
                    label = "全部",
                    selected = state.category == null,
                    onClick = { viewModel.selectCategory(null) },
                )
            }
            items(items = state.categories, key = { it }) { category ->
                EgChip(
                    label = category,
                    selected = state.category == category,
                    onClick = { viewModel.selectCategory(category) },
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Hairline()

        when {
            state.loading -> LoadingView()
            state.error != null -> ErrorView(state.error!!, onRetry = { viewModel.load(true) })
            else -> LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = LocalBottomOverlayPadding.current + 24.dp),
            ) {
                items(items = state.filtered, key = { it.id }) { article ->
                    ArticleRow(article = article, onClick = { onArticleClick(article.id) })
                }
            }
        }
    }
}

@Composable
private fun EgChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp),
        color = if (selected) Kaki else InkMuted,
        maxLines = 1,
        modifier = Modifier
            .clip(RoundedCornerShape(2.dp))
            .background(if (selected) KakiSoft else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    )
}

@Composable
private fun ArticleRow(article: EgArticle, onClick: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp),
    ) {
        Text(
            text = article.title,
            style = MaterialTheme.typography.titleSmall.copy(fontSize = 16.sp),
            color = Ink,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Eyebrow(article.level, color = Kaki)
            Spacer(Modifier.width(8.dp))
            Eyebrow(listOfNotNull(article.category, article.source).joinToString(" · "))
            Spacer(Modifier.width(8.dp))
            Eyebrow("${article.wordCount} 词", color = InkFaint)
        }
    }
    Hairline(Modifier.padding(horizontal = 24.dp))
}
