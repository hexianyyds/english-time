package com.feiyan.english.ui.xy

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.feiyan.english.core.model.XyEpisode
import com.feiyan.english.core.util.formatIsoDate
import com.feiyan.english.core.util.formatPosition
import com.feiyan.english.player.PlaybackUiState
import com.feiyan.english.player.PlayerController
import com.feiyan.english.ui.components.Cover
import com.feiyan.english.ui.components.ErrorView
import com.feiyan.english.ui.components.Eyebrow
import com.feiyan.english.ui.components.Hairline
import com.feiyan.english.ui.components.LoadingView
import com.feiyan.english.ui.components.LocalBottomOverlayPadding
import com.feiyan.english.ui.components.PlayPauseButton
import com.feiyan.english.ui.icons.FeiyanIcons
import com.feiyan.english.ui.theme.Hairline as HairlineColor
import com.feiyan.english.ui.theme.Ink
import com.feiyan.english.ui.theme.InkFaint
import com.feiyan.english.ui.theme.InkMuted
import com.feiyan.english.ui.theme.Kaki

@Composable
fun XyEpisodeScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: XyEpisodeViewModel = viewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val player by PlayerController.state.collectAsStateWithLifecycle()

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
            Eyebrow("EPISODE · 小宇宙")
        }

        when {
            state.loading -> LoadingView()
            state.episode == null -> ErrorView(state.error ?: "载入失败", onRetry = viewModel::load)
            else -> Content(
                episode = state.episode!!,
                podcastTitle = state.podcastTitle,
                player = player,
                onPlay = viewModel::play,
            )
        }
    }
}

@Composable
private fun Content(
    episode: XyEpisode,
    podcastTitle: String,
    player: PlaybackUiState,
    onPlay: (Long) -> Unit,
) {
    val mediaId = XyEpisodeViewModel.MEDIA_PREFIX + episode.eid
    val isCurrent = player.mediaId == mediaId
    val durationMs = (if (isCurrent) player.durationMs else 0L)
        .takeIf { it > 0L } ?: (episode.duration * 1000L).coerceAtLeast(1L)
    val positionMs = if (isCurrent) player.positionMs else 0L

    var dragging by remember { mutableStateOf<Float?>(null) }
    var expanded by rememberSaveable { mutableStateOf(false) }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = LocalBottomOverlayPadding.current + 32.dp),
    ) {
        item(key = "head") {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(top = 12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Cover(episode.image?.best, size = 84.dp, corner = 4.dp)
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = episode.title,
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 19.sp),
                        color = Ink,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(8.dp))
                    Eyebrow(
                        listOfNotNull(
                            podcastTitle.ifBlank { null },
                            formatIsoDate(episode.pubDate).ifBlank { null },
                        ).joinToString(" · "),
                    )
                }
            }
        }

        item(key = "player") {
            Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(top = 20.dp)) {
                Slider(
                    value = dragging ?: (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f),
                    onValueChange = { dragging = it },
                    onValueChangeFinished = {
                        dragging?.let { fraction ->
                            if (isCurrent) PlayerController.seekTo((fraction * durationMs).toLong())
                        }
                        dragging = null
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = Kaki,
                        activeTrackColor = Kaki,
                        inactiveTrackColor = HairlineColor,
                    ),
                )
                Row {
                    Text(
                        text = formatPosition(if (dragging != null) (dragging!! * durationMs).toLong() else positionMs),
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.4.sp),
                        color = InkMuted,
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = formatPosition(durationMs),
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.4.sp),
                        color = InkFaint,
                    )
                }
                Spacer(Modifier.height(18.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    SeekChip("-15″") { PlayerController.seekBy(-15_000) }
                    PlayPauseButton(
                        playing = isCurrent && player.isPlaying,
                        onClick = { if (isCurrent) PlayerController.toggle() else onPlay(0L) },
                        size = 58.dp,
                        filled = true,
                    )
                    SeekChip("+30″") { PlayerController.seekBy(30_000) }
                }
                Spacer(Modifier.height(16.dp))
                Hairline()
            }
        }

        val notes = episode.shownotes?.trim().orEmpty()
        if (notes.isNotBlank()) {
            item(key = "notes") {
                Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(top = 18.dp)) {
                    Eyebrow("SHOWNOTES · 节目简介")
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = notes,
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 21.sp),
                        color = InkMuted,
                        maxLines = if (expanded) 200 else 6,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = if (expanded) "收起" else "展开全文",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp),
                        color = Kaki,
                        modifier = Modifier.clickable { expanded = !expanded },
                    )
                }
            }
        }

        item(key = "source") {
            Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(top = 26.dp)) {
                Hairline()
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "内容来源 xiaoyuzhoufm.com · 仅展示免费单集",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.3.sp),
                    color = InkFaint,
                )
            }
        }
    }
}

@Composable
private fun SeekChip(label: String, onClick: () -> Unit) {
    Box(
        Modifier.size(44.dp).clip(CircleShape).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.2.sp),
            color = InkMuted,
        )
    }
}


