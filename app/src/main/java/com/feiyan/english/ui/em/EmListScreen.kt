package com.feiyan.english.ui.em

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.feiyan.english.core.model.EmCategories
import com.feiyan.english.core.model.EmEpisode
import com.feiyan.english.core.util.formatIsoDate
import com.feiyan.english.ui.components.ErrorView
import com.feiyan.english.ui.components.Eyebrow
import com.feiyan.english.ui.components.Hairline
import com.feiyan.english.ui.components.LoadingView
import com.feiyan.english.ui.components.LocalBottomOverlayPadding
import com.feiyan.english.ui.components.TagBadge
import com.feiyan.english.ui.icons.FeiyanIcons
import com.feiyan.english.ui.theme.Ink
import com.feiyan.english.ui.theme.InkFaint
import com.feiyan.english.ui.theme.InkMuted
import com.feiyan.english.ui.theme.Kaki
import com.feiyan.english.ui.theme.KakiSoft

@Composable
fun EmListScreen(
    onBack: () -> Unit,
    onEpisodeClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: EmListViewModel = viewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val availableCategories = EmCategories.all.filter { slug ->
        state.episodes.any { it.category == slug }
    }

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
            Eyebrow("ENGLISHMEASURE · 英语播客")
        }

        Column(Modifier.padding(horizontal = 24.dp).padding(top = 20.dp)) {
            Text(
                text = "Podcasts",
                style = MaterialTheme.typography.displaySmall.copy(fontSize = 30.sp),
                color = Ink,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "对话式英语播客，每集配完整对话稿。",
                style = MaterialTheme.typography.bodySmall,
                color = InkMuted,
            )
        }

        Spacer(Modifier.height(18.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item(key = "all") {
                CategoryChip(
                    label = "全部",
                    selected = state.category == null,
                    onClick = { viewModel.selectCategory(null) },
                )
            }
            items(items = availableCategories, key = { it }) { slug ->
                CategoryChip(
                    label = EmCategories.label(slug),
                    selected = state.category == slug,
                    onClick = { viewModel.selectCategory(slug) },
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Hairline()

        when {
            state.loading -> LoadingView()
            state.error != null -> ErrorView(state.error!!, onRetry = { viewModel.load(true) })
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = 4.dp,
                    bottom = LocalBottomOverlayPadding.current + 24.dp,
                ),
            ) {
                items(items = state.filtered, key = { it.slug }) { episode ->
                    EmEpisodeRow(episode = episode, onClick = { onEpisodeClick(episode.slug) })
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp),
        color = if (selected) Kaki else InkMuted,
        maxLines = 1,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) KakiSoft else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    )
}

@Composable
private fun EmEpisodeRow(episode: EmEpisode, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = episode.title,
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 16.sp),
                color = Ink,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Eyebrow(EmCategories.label(episode.category))
                val date = formatIsoDate(episode.publishedAt)
                if (date.isNotBlank()) {
                    Spacer(Modifier.width(8.dp))
                    Eyebrow(date, color = InkFaint)
                }
            }
        }
        Spacer(Modifier.width(12.dp))
        Icon(
            imageVector = FeiyanIcons.Play,
            contentDescription = null,
            tint = InkMuted,
            modifier = Modifier.size(12.dp),
        )
    }
    Hairline(Modifier.padding(horizontal = 24.dp))
}
