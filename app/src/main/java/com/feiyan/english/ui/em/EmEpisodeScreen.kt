package com.feiyan.english.ui.em

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
import com.feiyan.english.core.model.EmCategories
import com.feiyan.english.core.model.EmEpisodeDetail
import com.feiyan.english.core.model.EmScriptLine
import com.feiyan.english.core.util.formatIsoDate
import com.feiyan.english.core.util.formatPosition
import com.feiyan.english.player.PlaybackUiState
import com.feiyan.english.player.PlayerController
import com.feiyan.english.ui.components.ErrorView
import com.feiyan.english.ui.components.Eyebrow
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

@Composable
fun EmEpisodeScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: EmEpisodeViewModel = viewModel()
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
            Eyebrow("EPISODE · EnglishMeasure")
        }

        when {
            state.loading -> LoadingView()
            state.detail == null -> ErrorView(
                message = state.error ?: "载入失败",
                onRetry = viewModel::load,
            )
            else -> EmContent(
                detail = state.detail!!,
                player = player,
                onPlay = viewModel::play,
            )
        }
    }
}

@Composable
private fun EmContent(
    detail: EmEpisodeDetail,
    player: PlaybackUiState,
    onPlay: (Long) -> Unit,
) {
    val mediaId = EmEpisodeViewModel.MEDIA_PREFIX + detail.slug
    val isCurrent = player.mediaId == mediaId
    val durationMs = (if (isCurrent) player.durationMs else 0L).coerceAtLeast(1L)
    val positionMs = if (isCurrent) player.positionMs else 0L

    var dragging by remember { mutableStateOf<Float?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = LocalBottomOverlayPadding.current + 32.dp),
    ) {
        item(key = "head", contentType = "head") {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(top = 12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                EmCover(category = detail.category, title = detail.title, size = 88.dp)
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = detail.title,
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                        color = Ink,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(8.dp))
                    Eyebrow(
                        listOfNotNull(
                            EmCategories.label(detail.category),
                            formatIsoDate(detail.publishedAt).ifBlank { null },
                        ).joinToString(" · "),
                    )
                }
            }
        }

        item(key = "player", contentType = "player") {
            Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(top = 22.dp)) {
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
                        text = if (durationMs > 1000L) formatPosition(durationMs) else "--:--",
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
                Spacer(Modifier.height(14.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Text(
                        text = "语速 ${"%.2f".format(player.speed).trimEnd('0').trimEnd('.')}×",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp),
                        color = InkMuted,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable {
                                PlayerController.setSpeed(
                                    when {
                                        player.speed < 0.9f -> 1f
                                        player.speed < 1.1f -> 1.25f
                                        player.speed < 1.3f -> 1.5f
                                        else -> 0.75f
                                    },
                                )
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                }
                Spacer(Modifier.height(16.dp))
                Hairline()
            }
        }

        if (detail.description.isNotBlank()) {
            item(key = "about", contentType = "about") {
                Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(top = 18.dp)) {
                    Eyebrow("ABOUT · 简介")
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = detail.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = InkMuted,
                    )
                }
            }
        }

        item(key = "script-header", contentType = "section") {
            Column {
                SectionHeader(
                    title = "对话稿",
                    english = "SCRIPT",
                    trailing = "${detail.scriptJson.size} 段",
                )
                Text(
                    text = "对话稿按段落给出，不含时间轴",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.3.sp),
                    color = InkFaint,
                    modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 10.dp),
                )
            }
        }

        items(items = detail.scriptJson, key = { it.hashCode() }) { line ->
            ScriptLine(line)
        }

        item(key = "source", contentType = "footer") {
            Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(top = 24.dp)) {
                Hairline()
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "音频与文稿来源 englishmeasure.com · 版权归原作者所有",
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
        Modifier
            .size(44.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.2.sp),
            color = InkMuted,
        )
    }
}

@Composable
private fun ScriptLine(line: EmScriptLine) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp)) {
        if (line.speaker.isNotBlank()) {
            Eyebrow(line.speaker.uppercase(), color = Kaki)
            Spacer(Modifier.height(5.dp))
        }
        Text(
            text = cleanScript(line.text),
            style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp, lineHeight = 24.sp),
            color = Ink.copy(alpha = 0.86f),
        )
    }
    Hairline(Modifier.padding(horizontal = 24.dp))
}

private fun cleanScript(text: String): String =
    text.replace("**", "").removePrefix("Co-Host:").trim()
