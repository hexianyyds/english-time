package com.feiyan.english.ui.xy

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.feiyan.english.core.model.XyEpisode
import com.feiyan.english.core.model.XyPodcast
import com.feiyan.english.core.util.formatDuration
import com.feiyan.english.core.util.formatIsoDate
import com.feiyan.english.ui.components.Cover
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

@Composable
fun XyPodcastScreen(
    onBack: () -> Unit,
    onEpisodeClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: XyPodcastViewModel = viewModel()
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
            Eyebrow("XIAOYUZHOU · 小宇宙")
        }

        when {
            state.loading -> LoadingView()
            state.error != null -> ErrorView(state.error!!, onRetry = { viewModel.load(true) })
            state.podcast != null -> {
                val podcast = state.podcast!!
                LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = LocalBottomOverlayPadding.current + 24.dp),
                ) {
                    item(key = "header") { PodcastHeader(podcast) }
                    item(key = "episodes-header") {
                        SectionHeader("免费单集", "FREE EPISODES", trailing = "${podcast.episodes.size} 集")
                    }
                    itemsIndexed(items = podcast.episodes, key = { _, ep -> ep.eid }) { index, episode ->
                        EpisodeRow(index + 1, episode) { onEpisodeClick(episode.eid) }
                        Hairline(Modifier.padding(start = 74.dp, end = 24.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun PodcastHeader(podcast: XyPodcast) {
    Column(Modifier.padding(horizontal = 24.dp).padding(top = 12.dp)) {
        Row(verticalAlignment = Alignment.Top) {
            Cover(podcast.image?.best, size = 100.dp, corner = 10.dp)
            Spacer(Modifier.width(18.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = podcast.title,
                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 22.sp),
                    color = Ink,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = podcast.author.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = InkMuted,
                )
                Spacer(Modifier.height(10.dp))
                Eyebrow("共 ${podcast.episodeCount} 集 · 已过滤付费与试听")
            }
        }
        if (!podcast.description.isNullOrBlank()) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = podcast.description!!.trim(),
                style = MaterialTheme.typography.bodySmall,
                color = InkMuted,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun EpisodeRow(index: Int, episode: XyEpisode, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 24.dp, end = 24.dp, top = 15.dp, bottom = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = index.toString().padStart(2, '0'),
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.4.sp),
            color = InkFaint,
            modifier = Modifier.width(26.dp),
        )
        Column(Modifier.weight(1f)) {
            Text(
                text = episode.title,
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp),
                color = Ink,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(6.dp))
            Eyebrow(
                listOfNotNull(
                    formatIsoDate(episode.pubDate).ifBlank { null },
                    formatDuration(episode.duration),
                ).joinToString(" · "),
                color = InkFaint,
            )
        }
        Spacer(Modifier.width(12.dp))
        Icon(
            imageVector = FeiyanIcons.Play,
            contentDescription = null,
            tint = InkMuted,
            modifier = Modifier.size(12.dp),
        )
    }
}
