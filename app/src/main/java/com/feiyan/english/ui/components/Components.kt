package com.feiyan.english.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.feiyan.english.data.LibraryItem
import com.feiyan.english.data.LibraryStore
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.HazeColorEffect
import dev.chrisbanes.haze.blur.hazeBlur
import com.feiyan.english.core.model.ChannelBrief
import com.feiyan.english.core.util.coverUrl
import com.feiyan.english.core.util.formatPosition
import com.feiyan.english.player.PlayerController
import com.feiyan.english.player.PlaybackUiState
import com.feiyan.english.ui.icons.FeiyanIcons
import com.feiyan.english.ui.theme.Hairline
import com.feiyan.english.ui.theme.Ink
import com.feiyan.english.ui.theme.InkFaint
import com.feiyan.english.ui.theme.InkMuted
import com.feiyan.english.ui.theme.Kaki
import com.feiyan.english.ui.theme.KakiSoft
import com.feiyan.english.ui.theme.Paper
import com.feiyan.english.ui.theme.PaperCard
import com.feiyan.english.ui.theme.PaperDeep

/** 1px 极浅分隔线 —— 整个界面的「呼吸」靠它和留白。 */
@Composable
fun Hairline(modifier: Modifier = Modifier, color: Color = Hairline) {
    Box(modifier.fillMaxWidth().height(0.8.dp).background(color))
}

@Composable
fun Cover(
    path: String?,
    size: Dp,
    modifier: Modifier = Modifier,
    corner: Dp = 4.dp,
) {
    Box(
        modifier
            .size(size)
            .clip(RoundedCornerShape(corner))
            .background(KakiSoft),
    ) {
        val url = coverUrl(path)
        if (url != null) {
            AsyncImage(
                model = url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

/** 无封面时用标题首字母做排版化封面（EnglishMeasure 没有图）。 */
@Composable
fun CoverOrMonogram(
    path: String?,
    title: String,
    size: Dp,
    modifier: Modifier = Modifier,
    corner: Dp = 4.dp,
) {
    if (!path.isNullOrBlank()) {
        Cover(path, size, modifier, corner)
        return
    }
    Box(
        modifier
            .size(size)
            .clip(RoundedCornerShape(corner))
            .background(PaperDeep),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = title.take(1).uppercase(),
            style = MaterialTheme.typography.titleMedium.copy(fontSize = (size.value * 0.42f).sp),
            color = Ink,
        )
    }
}

@Composable
fun Eyebrow(text: String, modifier: Modifier = Modifier, color: Color = InkFaint) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.8.sp),
        color = color,
        maxLines = 1,
    )
}

@Composable
fun SectionHeader(
    title: String,
    english: String,
    modifier: Modifier = Modifier,
    trailing: String? = null,
    onTrailingClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 28.dp, bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(letterSpacing = 1.sp),
            color = Ink,
        )
        Spacer(Modifier.width(8.dp))
        Eyebrow(english)
        Spacer(Modifier.weight(1f))
        if (trailing != null) {
            Text(
                text = trailing,
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                color = InkFaint,
                modifier = if (onTrailingClick != null) {
                    Modifier.clickable(onClick = onTrailingClick)
                } else {
                    Modifier
                },
            )
        }
    }
}

@Composable
fun PlayPauseButton(
    playing: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    filled: Boolean = false,
) {
    Box(
        modifier
            .size(size)
            .clip(CircleShape)
            .background(if (filled) Kaki else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = if (playing) FeiyanIcons.Pause else FeiyanIcons.Play,
            contentDescription = if (playing) "暂停" else "播放",
            tint = if (filled) MaterialTheme.colorScheme.onPrimary else Ink,
            modifier = Modifier.size(size * 0.42f),
        )
    }
}

@Composable
fun ChannelCard(
    channel: ChannelBrief,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.width(132.dp).clickable(onClick = onClick),
    ) {
        Cover(channel.cover, size = 132.dp, corner = 10.dp)
        Spacer(Modifier.height(10.dp))
        Text(
            text = channel.englishName.orEmpty(),
            style = MaterialTheme.typography.titleSmall,
            color = Ink,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = channel.author.orEmpty(),
            style = MaterialTheme.typography.bodySmall,
            color = InkFaint,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun ProgrammeRow(
    channel: ChannelBrief,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    meta: String? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Cover(channel.cover, size = 46.dp, corner = 10.dp)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = channel.englishName.orEmpty(),
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 16.sp),
                color = Ink,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = listOfNotNull(channel.author, meta).joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = InkFaint,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(12.dp))
        Box(
            Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color.Transparent),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = FeiyanIcons.Play,
                contentDescription = null,
                tint = InkMuted,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

/** 首页「正在播放」纸感卡片。 */
@Composable
fun NowPlayingCard(
    state: PlaybackUiState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!state.hasMedia) return
    val duration = if (state.durationMs > 0) state.durationMs else 0L
    val fraction = if (duration > 0) (state.positionMs.toFloat() / duration).coerceIn(0f, 1f) else 0f

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(PaperCard)
            .border(1.dp, Hairline, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
    ) {
        Eyebrow("NOW PLAYING · 正在播放", color = InkFaint)
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            CoverOrMonogram(state.cover, state.title, size = 56.dp, corner = 10.dp)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = state.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Ink,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = state.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = InkFaint,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        ProgressLine(fraction)
        Spacer(Modifier.height(6.dp))
        Row {
            Text(
                text = formatPosition(state.positionMs),
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.4.sp),
                color = InkMuted,
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = formatPosition(duration),
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.4.sp),
                color = InkFaint,
            )
        }
    }
}

@Composable
fun ProgressLine(fraction: Float, modifier: Modifier = Modifier, height: Dp = 1.5.dp) {
    Box(
        modifier
            .fillMaxWidth()
            .height(height)
            .background(Hairline),
    ) {
        Box(
            Modifier
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .height(height)
                .background(Kaki),
        )
    }
}

/** 底部迷你播放条。 */
@Composable
fun MiniPlayer(
    state: PlaybackUiState,
    onClick: () -> Unit,
    onToggle: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!state.hasMedia) return
    val duration = if (state.durationMs > 0) state.durationMs else 0L
    val fraction = if (duration > 0) (state.positionMs.toFloat() / duration).coerceIn(0f, 1f) else 0f
    val cardShape = RoundedCornerShape(16.dp)

    Column(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .clip(cardShape)
            .background(PaperCard)
            .border(1.dp, Hairline, cardShape),
    ) {
        ProgressLine(fraction, height = 2.dp)
        Row(
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(start = 12.dp, end = 6.dp, top = 7.dp, bottom = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CoverOrMonogram(state.cover, state.title, size = 40.dp, corner = 10.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = state.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp),
                    color = Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = state.subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.2.sp),
                    color = InkFaint,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            PlayPauseButton(
                playing = state.isPlaying,
                onClick = onToggle,
                size = 40.dp,
            )
            Box(
                Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = FeiyanIcons.Close,
                    contentDescription = "关闭播放",
                    tint = InkFaint,
                    modifier = Modifier.size(13.dp),
                )
            }
        }
    }
}

/** 收藏按钮：状态直接读本地 LibraryStore，任意页面可复用。 */
@Composable
fun FavoriteButton(item: LibraryItem, modifier: Modifier = Modifier) {
    val favorites by LibraryStore.favorites.collectAsStateWithLifecycle()
    val active = favorites.any { it.id == item.id }
    Box(
        modifier
            .size(36.dp)
            .clip(CircleShape)
            .clickable { LibraryStore.toggleFavorite(item) },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = FeiyanIcons.Heart,
            contentDescription = if (active) "取消收藏" else "收藏",
            tint = if (active) Kaki else InkFaint,
            modifier = Modifier.size(17.dp),
        )
    }
}

/**
 * 首页内容底部需要留出的高度（胶囊导航 + 迷你播放条 + 系统导航栏），
 * 由 FeiyanApp 统一计算后下发，各页面加到自己的 contentPadding 上。
 */
val LocalBottomOverlayPadding = staticCompositionLocalOf { 0.dp }

/** Haze 2.x 的毛玻璃样式：模糊 + 表面色薄染 + 极轻噪点（颜色跟随主题）。 */
@Composable
private fun rememberGlassStyle(): HazeBlurStyle {
    // 在组合期读取主题色，再交给非组合 lambda 捕获，日夜两用
    val tint = Paper.copy(alpha = 0.58f)
    return HazeBlurStyle {
        blurRadius(28.dp)
        colorEffects(listOf(HazeColorEffect.tint(tint)))
        noiseFactor(0.03f)
    }
}

/**
 * 悬浮毛玻璃胶囊导航栏。
 */
@Composable
fun FeiyanBottomBar(
    currentRoute: String,
    onSelect: (String) -> Unit,
    hazeState: HazeState,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(22.dp)
    Row(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 8.dp)
            .height(64.dp)
            .clip(shape)
            .hazeBlur(
                input = HazeInput.Backdrop(hazeState),
                style = rememberGlassStyle(),
            )
            .border(1.dp, Hairline.copy(alpha = 0.75f), shape)
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BottomTab(
            label = "学习",
            icon = FeiyanIcons.BookOpen,
            selected = currentRoute == "study",
            modifier = Modifier.weight(1f),
        ) { onSelect("study") }
        BottomTab(
            label = "我的",
            icon = FeiyanIcons.Person,
            selected = currentRoute == "library",
            modifier = Modifier.weight(1f),
        ) { onSelect("library") }
    }
}

@Composable
private fun BottomTab(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) Kaki else InkFaint,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.6.sp),
            color = if (selected) Ink else InkFaint,
        )
        Spacer(Modifier.height(3.dp))
        Box(
            Modifier
                .width(12.dp)
                .height(1.5.dp)
                .background(if (selected) Kaki else Color.Transparent),
        )
    }
}

@Composable
fun LoadingView(modifier: Modifier = Modifier, hint: String = "正在载入") {
    Column(
        modifier.fillMaxSize().padding(top = 120.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator(
            color = Kaki,
            strokeWidth = 1.2.dp,
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.height(18.dp))
        Text(hint, style = MaterialTheme.typography.labelSmall, color = InkFaint)
    }
}

@Composable
fun ErrorView(message: String, modifier: Modifier = Modifier, onRetry: () -> Unit) {
    Column(
        modifier.fillMaxSize().padding(horizontal = 40.dp, vertical = 120.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Something went quiet.",
            style = MaterialTheme.typography.titleLarge,
            color = Ink,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = InkFaint,
        )
        Spacer(Modifier.height(22.dp))
        Text(
            text = "重 新 载 入",
            style = MaterialTheme.typography.labelMedium.copy(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Medium,
            ),
            color = Kaki,
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .clickable(onClick = onRetry)
                .padding(horizontal = 16.dp, vertical = 10.dp),
        )
    }
}

@Composable
fun TagBadge(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.6.sp),
        color = Kaki,
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(KakiSoft)
            .padding(horizontal = 5.dp, vertical = 2.dp),
    )
}

@Composable
fun PlayerBarSpacer() = Spacer(Modifier.height(8.dp))

/** 记住：播放动作统一走这里，避免各页面重复拼状态。 */
fun togglePlayback() = PlayerController.toggle()
