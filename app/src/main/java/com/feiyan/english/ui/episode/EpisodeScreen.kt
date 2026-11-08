package com.feiyan.english.ui.episode

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.feiyan.english.core.model.Episode
import com.feiyan.english.core.model.Sentence
import com.feiyan.english.core.model.Transcript
import com.feiyan.english.core.util.formatDuration
import com.feiyan.english.core.util.formatEpisodeDate
import com.feiyan.english.core.util.formatPosition
import com.feiyan.english.player.PlaybackUiState
import com.feiyan.english.player.PlayerController
import com.feiyan.english.ui.components.Cover
import com.feiyan.english.ui.components.ErrorView
import com.feiyan.english.ui.components.Eyebrow
import com.feiyan.english.ui.components.FavoriteButton
import com.feiyan.english.ui.components.Hairline
import com.feiyan.english.ui.components.LoadingView
import com.feiyan.english.ui.components.LocalBottomOverlayPadding
import com.feiyan.english.ui.components.PlayPauseButton
import com.feiyan.english.ui.components.SectionHeader
import com.feiyan.english.ui.components.TagBadge
import com.feiyan.english.ui.icons.FeiyanIcons
import com.feiyan.english.ui.theme.Hairline as HairlineColor
import com.feiyan.english.ui.theme.Ink
import com.feiyan.english.ui.theme.InkFaint
import com.feiyan.english.ui.theme.InkMuted
import com.feiyan.english.ui.theme.Kaki
import com.feiyan.english.ui.theme.KakiDeep
import com.feiyan.english.ui.theme.KakiSoft
import com.feiyan.english.ui.theme.PaperCard

@Composable
fun EpisodeScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: EpisodeViewModel = viewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val player by PlayerController.state.collectAsStateWithLifecycle()

    Column(modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(start = 8.dp, end = 20.dp, top = 6.dp, bottom = 2.dp),
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
            Eyebrow("EPISODE · 节目", modifier = Modifier.weight(1f))
            state.episode?.let { episode ->
                FavoriteButton(
                    item = com.feiyan.english.data.LibraryItem(
                        id = "feiyan:${episode.uniqueKey}",
                        title = episode.name,
                        subtitle = state.channelName,
                        cover = episode.cover,
                        route = "episode/${episode.uniqueKey}",
                    ),
                )
            }
        }

        when {
            state.loading -> LoadingView()
            state.episode == null -> ErrorView(
                message = state.error ?: "节目加载失败",
                onRetry = viewModel::load,
            )
            else -> EpisodeContent(
                episode = state.episode!!,
                channelName = state.channelName,
                transcript = state.transcript,
                preparing = state.preparing,
                player = player,
                onPlay = viewModel::play,
            )
        }
    }
}

@Composable
private fun EpisodeContent(
    episode: Episode,
    channelName: String,
    transcript: Transcript?,
    preparing: Boolean,
    player: PlaybackUiState,
    onPlay: (Long) -> Unit,
) {
    val isCurrent = player.mediaId == episode.uniqueKey
    val durationMs = (if (isCurrent) player.durationMs else 0L)
        .takeIf { it > 0L } ?: (episode.duration * 1000L).coerceAtLeast(1L)
    val positionMs = if (isCurrent) player.positionMs else 0L
    val sentences = transcript?.sentences.orEmpty()
    val activeId = if (isCurrent) {
        sentences.firstOrNull { positionMs >= it.start && positionMs < it.end }?.id
    } else {
        null
    }

    var dragging by remember { mutableStateOf<Float?>(null) }
    var descExpanded by rememberSaveable { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp + LocalBottomOverlayPadding.current),
    ) {
        item(key = "head", contentType = "head") {
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(top = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    Modifier
                        .size(216.dp)
                        .shadow(28.dp, RoundedCornerShape(24.dp), spotColor = Color.Black.copy(alpha = 0.5f))
                        .clip(RoundedCornerShape(24.dp)),
                ) {
                    Cover(episode.cover, size = 216.dp, corner = 24.dp)
                }
                Spacer(Modifier.height(22.dp))
                Text(
                    text = episode.name,
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 19.sp, lineHeight = 27.sp),
                    color = Ink,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(9.dp))
                Eyebrow(
                    listOfNotNull(
                        channelName.ifBlank { null },
                        formatEpisodeDate(episode.displayDate).ifBlank { null },
                        formatDuration(episode.duration),
                    ).joinToString(" · ").uppercase(),
                    color = InkFaint,
                )
            }
        }

        item(key = "player", contentType = "player") {
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
                        text = formatDuration(durationMs / 1000),
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
                    Box(contentAlignment = Alignment.Center) {
                        if (preparing) {
                            CircularProgressIndicator(
                                color = Kaki,
                                strokeWidth = 1.4.dp,
                                modifier = Modifier.size(24.dp),
                            )
                        } else {
                            PlayPauseButton(
                                playing = isCurrent && player.isPlaying,
                                onClick = {
                                    if (isCurrent) PlayerController.toggle() else onPlay(0L)
                                },
                                size = 58.dp,
                                filled = true,
                            )
                        }
                    }
                    SeekChip("+30″") { PlayerController.seekBy(30_000) }
                }

                Spacer(Modifier.height(16.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "语速 ${formatSpeed(player.speed)}",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                        color = InkMuted,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .border(1.dp, HairlineColor, RoundedCornerShape(20.dp))
                            .clickable { PlayerController.setSpeed(nextSpeed(player.speed)) }
                            .padding(horizontal = 14.dp, vertical = 7.dp),
                    )
                }
                Spacer(Modifier.height(20.dp))
                Hairline()
            }
        }

        if (!episode.desc.isNullOrBlank()) {
            item(key = "about", contentType = "about") {
                Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(top = 20.dp)) {
                    Eyebrow("ABOUT · 节目简介")
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = episode.desc!!.trim(),
                        style = MaterialTheme.typography.bodySmall,
                        color = InkMuted,
                        maxLines = if (descExpanded) 60 else 5,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = if (descExpanded) "收起" else "展开全文",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp),
                        color = Kaki,
                        modifier = Modifier.clickable { descExpanded = !descExpanded },
                    )
                }
            }
        }

        item(key = "transcript-header", contentType = "section") {
            Column {
                SectionHeader(
                    title = "字幕",
                    english = "TRANSCRIPT",
                    trailing = transcript?.let { "${it.count} 句" },
                )
                if (transcript != null) {
                    val full = transcript.sentences.size >= transcript.count
                    Text(
                        text = if (full) {
                            "完整字幕 · 共 ${transcript.count} 句，点击任意句可跳转"
                        } else {
                            "游客可看前 ${transcript.sentences.size} 句，登录后解锁全部 ${transcript.count} 句"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.4.sp),
                        color = if (full) Kaki else InkFaint,
                        modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 8.dp),
                    )
                }
            }
        }

        if (sentences.isEmpty()) {
            item(key = "transcript-empty", contentType = "empty") {
                Text(
                    text = if (transcript == null) "本集暂无字幕" else "字幕载入中或暂不可用",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkFaint,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                )
            }
        } else {
            items(items = sentences, key = { it.id }) { sentence ->
                SentenceRow(
                    sentence = sentence,
                    active = sentence.id == activeId,
                    onClick = {
                        if (isCurrent) {
                            PlayerController.seekTo(sentence.start)
                        } else {
                            onPlay(sentence.start)
                        }
                    },
                )
            }
        }

        item(key = "source", contentType = "footer") {
            Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(top = 28.dp)) {
                Hairline()
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "音频与字幕版权归原节目所有 · 内容来自 feiyanpod.com",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.4.sp),
                    color = InkFaint,
                )
            }
        }
    }
}

@Composable
private fun SeekChip(label: String, onClick: () -> Unit) {
    Box(
        Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(PaperCard)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.6.sp),
            color = InkMuted,
        )
    }
}

@Composable
private fun SentenceRow(sentence: Sentence, active: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 3.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (active) KakiSoft else Color.Transparent)
            .clickable(onClick = onClick),
    ) {
        Column(Modifier.padding(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 12.dp)) {
            Text(
                text = sentence.content,
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.5.sp, lineHeight = 24.sp),
                color = if (active) KakiDeep else Ink.copy(alpha = 0.84f),
            )
            if (!sentence.translate.isNullOrBlank()) {
                Spacer(Modifier.height(5.dp))
                Text(
                    text = sentence.translate!!,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 19.sp),
                    color = if (active) InkMuted else InkFaint,
                )
            }
        }
    }
}

private fun formatSpeed(speed: Float): String = when {
    speed == 1f -> "1.0×"
    speed == speed.toInt().toFloat() -> "${speed.toInt()}.0×"
    else -> "${speed}×"
}

private fun nextSpeed(current: Float): Float = when {
    current < 0.9f -> 1f
    current < 1.1f -> 1.25f
    current < 1.3f -> 1.5f
    current < 1.6f -> 0.75f
    else -> 1f
}
