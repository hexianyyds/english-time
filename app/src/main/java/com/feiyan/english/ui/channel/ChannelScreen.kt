package com.feiyan.english.ui.channel

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.feiyan.english.core.model.ChannelDetail
import com.feiyan.english.core.model.Episode
import com.feiyan.english.core.util.formatDuration
import com.feiyan.english.core.util.formatEpisodeDate
import com.feiyan.english.player.PlaybackUiState
import com.feiyan.english.ui.components.Cover
import com.feiyan.english.ui.components.ErrorView
import com.feiyan.english.ui.components.Eyebrow
import com.feiyan.english.ui.components.Hairline
import com.feiyan.english.ui.components.LoadingView
import com.feiyan.english.ui.components.LocalBottomOverlayPadding
import com.feiyan.english.ui.components.SectionHeader
import com.feiyan.english.ui.components.TagBadge
import com.feiyan.english.ui.icons.FeiyanIcons
import com.feiyan.english.ui.theme.Ink
import com.feiyan.english.ui.theme.InkFaint
import com.feiyan.english.ui.theme.InkMuted
import com.feiyan.english.ui.theme.Kaki
import com.feiyan.english.ui.theme.KakiSoft
import com.feiyan.english.ui.theme.Paper

@Composable
fun ChannelScreen(
    onBack: () -> Unit,
    onEpisodeClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: ChannelViewModel = viewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val player by com.feiyan.english.player.PlayerController.state.collectAsStateWithLifecycle()

    Column(modifier.fillMaxSize()) {
        BackBar(onBack = onBack, title = state.channel?.englishName.orEmpty())
        when {
            state.loading -> LoadingView()
            state.error != null -> ErrorView(state.error!!, onRetry = viewModel::load)
            state.channel != null -> ChannelContent(
                channel = state.channel!!,
                episodes = state.episodes,
                total = state.total,
                hasMore = state.hasMore,
                loadingMore = state.loadingMore,
                preparing = state.preparing,
                player = player,
                onPlayLatest = viewModel::playLatest,
                onLoadMore = viewModel::loadMore,
                onEpisodeClick = onEpisodeClick,
            )
        }
    }
}

@Composable
private fun BackBar(onBack: () -> Unit, title: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 20.dp, top = 6.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(40.dp)
                .clickable(onClick = onBack),
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
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
            color = InkFaint,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ChannelContent(
    channel: ChannelDetail,
    episodes: List<Episode>,
    total: Int,
    hasMore: Boolean,
    loadingMore: Boolean,
    preparing: Boolean,
    player: PlaybackUiState,
    onPlayLatest: () -> Unit,
    onLoadMore: () -> Unit,
    onEpisodeClick: (String) -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp + LocalBottomOverlayPadding.current),
    ) {
        item(key = "header", contentType = "header") {
            Column(Modifier.padding(horizontal = 24.dp).padding(top = 10.dp)) {
                Row(verticalAlignment = Alignment.Top) {
                    Cover(channel.cover, size = 104.dp, corner = 14.dp)
                    Spacer(Modifier.width(18.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = channel.englishName.orEmpty(),
                            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 23.sp),
                            color = Ink,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = channel.author.orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                            color = InkMuted,
                        )
                        Spacer(Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (!channel.categoryName.isNullOrBlank()) {
                                Eyebrow(channel.categoryName!!)
                                Spacer(Modifier.width(8.dp))
                                Box(Modifier.width(1.dp).height(8.dp).background(InkFaint))
                                Spacer(Modifier.width(8.dp))
                            }
                            Eyebrow("共 $total 集")
                        }
                    }
                }

                if (!channel.desc.isNullOrBlank()) {
                    Spacer(Modifier.height(20.dp))
                    Text(
                        text = channel.desc!!.trim(),
                        style = MaterialTheme.typography.bodySmall,
                        color = InkMuted,
                        maxLines = if (expanded) 40 else 3,
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

                Spacer(Modifier.height(22.dp))
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(14.dp))
                        .clickable(enabled = !preparing && episodes.isNotEmpty(), onClick = onPlayLatest)
                        .background(Kaki),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    if (preparing) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 1.4.dp,
                            modifier = Modifier.size(16.dp),
                        )
                    } else {
                        Icon(
                            imageVector = FeiyanIcons.Play,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(12.dp),
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "播放最新一集",
                        style = MaterialTheme.typography.bodyMedium.copy(letterSpacing = 1.sp),
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
                Spacer(Modifier.height(8.dp))
            }
        }

        item(key = "episodes-header", contentType = "section") {
            SectionHeader(title = "全部节目", english = "EPISODES", trailing = "$total 集")
        }

        itemsIndexed(
            items = episodes,
            key = { _, episode -> episode.uniqueKey },
            contentType = { _, _ -> "episode" },
        ) { index, episode ->
            EpisodeRow(
                index = index + 1,
                episode = episode,
                playing = player.mediaId == episode.uniqueKey && player.isPlaying,
                onClick = { onEpisodeClick(episode.uniqueKey) },
            )
            Hairline(Modifier.padding(start = 74.dp, end = 24.dp))
        }

        if (hasMore) {
            item(key = "load-more", contentType = "footer") {
                LaunchedEffect(episodes.size) { onLoadMore() }
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 24.dp),
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
        } else if (episodes.isNotEmpty()) {
            item(key = "end", contentType = "footer") {
                Text(
                    text = "已 到 底 部",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                    color = InkFaint,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 28.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun EpisodeRow(
    index: Int,
    episode: Episode,
    playing: Boolean,
    onClick: () -> Unit,
) {
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
            color = if (playing) Kaki else InkFaint,
            modifier = Modifier.width(26.dp),
        )
        Column(Modifier.weight(1f)) {
            Text(
                text = episode.name,
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp),
                color = if (playing) Kaki else Ink,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Eyebrow(
                    listOfNotNull(
                        formatEpisodeDate(episode.displayDate).ifBlank { null },
                        formatDuration(episode.duration),
                    ).joinToString(" · "),
                    color = InkFaint,
                )
                if (episode.isPreview) {
                    Spacer(Modifier.width(8.dp))
                    TagBadge("试听")
                }
                if (playing) {
                    Spacer(Modifier.width(8.dp))
                    TagBadge("播放中")
                }
            }
        }
        Spacer(Modifier.width(12.dp))
        Box(
            Modifier.size(30.dp).background(Color.Transparent, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = FeiyanIcons.Play,
                contentDescription = null,
                tint = if (playing) Kaki else InkMuted,
                modifier = Modifier.size(12.dp),
            )
        }
    }
}
