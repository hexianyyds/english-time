package com.feiyan.english.ui.game

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.feiyan.english.core.model.GameWord
import com.feiyan.english.data.GameDefinition
import com.feiyan.english.ui.icons.FeiyanIcons
import com.feiyan.english.ui.theme.Breeze
import com.feiyan.english.ui.theme.Hairline
import com.feiyan.english.ui.theme.Ink
import com.feiyan.english.ui.theme.InkFaint
import com.feiyan.english.ui.theme.InkMuted
import com.feiyan.english.ui.theme.Kaki
import com.feiyan.english.ui.theme.KakiSoft
import com.feiyan.english.ui.theme.Paper
import com.feiyan.english.ui.theme.PaperCard
import com.feiyan.english.ui.theme.PaperDeep

/** 选项卡片视觉状态。 */
enum class TileState { Idle, Correct, Wrong, Dimmed }

@Composable
private fun TileState.borderColor(): Color = when (this) {
    TileState.Correct -> Kaki
    TileState.Wrong -> MaterialTheme.colorScheme.error
    else -> Hairline
}

@Composable
private fun TileState.container(): Color = when (this) {
    TileState.Correct -> KakiSoft.copy(alpha = 0.55f)
    TileState.Wrong -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
    TileState.Dimmed -> PaperDeep.copy(alpha = 0.45f)
    TileState.Idle -> PaperCard
}

/**
 * 顶部计分条：关闭 / 进度 / 得分 / 连击。
 */
@Composable
fun GameHud(
    score: Int,
    combo: Int,
    progress: Int,
    total: Int,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(19.dp))
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = FeiyanIcons.Close,
                    contentDescription = "退出本局",
                    tint = InkMuted,
                    modifier = Modifier.size(17.dp),
                )
            }
            Spacer(Modifier.width(10.dp))
            Box(
                Modifier
                    .weight(1f)
                    .height(7.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(PaperDeep),
            ) {
                val fraction = if (total <= 0) 0f else (progress.toFloat() / total).coerceIn(0f, 1f)
                Box(
                    Modifier
                        .fillMaxWidth(fraction)
                        .height(7.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Kaki),
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = score.toString(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = Ink,
                )
                Text(
                    text = if (combo >= 2) "🔥 x$combo" else "$progress / $total",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.4.sp),
                    color = if (combo >= 2) Kaki else InkFaint,
                )
            }
        }
    }
}

/** 大号图片舞台：有站点插画用插画，否则用 emoji。 */
@Composable
fun WordStage(
    word: GameWord,
    modifier: Modifier = Modifier,
    fontSize: Int = 64,
) {
    Box(
        modifier
            .clip(RoundedCornerShape(20.dp))
            .background(PaperCard)
            .border(0.8.dp, Hairline, RoundedCornerShape(20.dp)),
        contentAlignment = Alignment.Center,
    ) {
        WordVisual(
            word = word,
            modifier = Modifier.fillMaxSize(),
            emojiSize = fontSize,
            padding = if (word.hasImage) 12 else 0,
            preferHd = true,
        )
    }
}

/** 图片选项卡片。 */
@Composable
fun ImageOptionTile(
    word: GameWord,
    state: TileState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .clip(RoundedCornerShape(18.dp))
            .background(state.container())
            .border(
                width = if (state == TileState.Idle) 0.8.dp else 1.6.dp,
                color = state.borderColor(),
                shape = RoundedCornerShape(18.dp),
            )
            .clickable(enabled = state == TileState.Idle, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        WordVisual(word, Modifier.fillMaxSize(), emojiSize = 40, padding = 8)
    }
}

/** 单词选项卡片。 */
@Composable
fun WordTile(
    text: String,
    state: TileState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(state.container())
            .border(
                width = if (state == TileState.Idle) 0.8.dp else 1.6.dp,
                color = state.borderColor(),
                shape = RoundedCornerShape(14.dp),
            )
            .clickable(enabled = state == TileState.Idle, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
            ),
            color = when (state) {
                TileState.Dimmed -> InkFaint
                TileState.Wrong -> MaterialTheme.colorScheme.error
                else -> Ink
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

/** 主行动按钮。 */
@Composable
fun GameActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    filled: Boolean = true,
) {
    Box(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (filled) Kaki else KakiSoft.copy(alpha = 0.5f))
            .clickable(onClick = onClick)
            .padding(horizontal = 22.dp, vertical = 15.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.6.sp,
            ),
            color = if (filled) MaterialTheme.colorScheme.onPrimary else Breeze,
        )
    }
}

/** 星级：未点亮为空心。 */
@Composable
fun StarRow(stars: Int, modifier: Modifier = Modifier, size: Int = 30) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(3) { index ->
            val lit = index < stars
            var visible by remember { mutableStateOf(false) }
            LaunchedEffect(stars) {
                if (lit) {
                    kotlinx.coroutines.delay(index * 160L)
                    visible = true
                }
            }
            val scale = remember { Animatable(if (lit) 0.2f else 1f) }
            LaunchedEffect(visible) {
                if (visible) {
                    scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                }
            }
            Text(
                text = if (lit) "⭐" else "☆",
                fontSize = size.sp,
                color = if (lit) Kaki else InkFaint,
                modifier = Modifier.scale(scale.value),
            )
        }
    }
}

/** 结算页。 */
@Composable
fun GameResultScreen(
    game: GameDefinition,
    session: GameSession,
    onReplay: () -> Unit,
    onExit: () -> Unit,
    onSpeak: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val stars = session.stars
    val headline = when {
        stars >= 3 -> "完美通关"
        stars == 2 -> "表现不错"
        stars == 1 -> "完成本局"
        else -> "再来一次"
    }
    Column(
        modifier
            .fillMaxSize()
            .background(Paper)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(36.dp))
        Text(text = game.emoji, fontSize = 46.sp)
        Spacer(Modifier.height(12.dp))
        Text(
            text = headline,
            style = MaterialTheme.typography.displaySmall.copy(fontSize = 28.sp),
            color = Ink,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "${game.title} · ${game.english}",
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
            color = InkFaint,
        )
        Spacer(Modifier.height(20.dp))
        StarRow(stars = stars)
        Spacer(Modifier.height(24.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ResultCell("得分", session.score.toString(), Modifier.weight(1f), highlight = true)
            ResultCell("正确", session.correct.toString(), Modifier.weight(1f))
            ResultCell("最高连击", "x${session.maxCombo}", Modifier.weight(1f))
        }

        if (session.missedWords.isNotEmpty()) {
            Spacer(Modifier.height(26.dp))
            Column(Modifier.fillMaxWidth()) {
                Text(
                    text = "错词回顾 · 点一下听发音",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp),
                    color = InkMuted,
                )
                Spacer(Modifier.height(10.dp))
                session.missedWords.take(9).chunked(3).forEach { row ->
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        row.forEach { word ->
                            Box(
                                Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(KakiSoft.copy(alpha = 0.42f))
                                    .clickable { onSpeak(word) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = word,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                                    color = Ink,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                        repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }

        Spacer(Modifier.height(30.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GameActionButton("再来一局", onReplay, Modifier.weight(1f))
            GameActionButton("回到大厅", onExit, Modifier.weight(1f), filled = false)
        }
        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun ResultCell(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    highlight: Boolean = false,
) {
    Column(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (highlight) KakiSoft.copy(alpha = 0.45f) else PaperCard)
            .border(0.8.dp, Hairline, RoundedCornerShape(14.dp))
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
            ),
            color = if (highlight) Breeze else Ink,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = InkFaint,
        )
    }
}

/** 底部反馈条：答对 / 答错一句话提示。 */
@Composable
fun FeedbackBar(
    message: String,
    positive: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (positive) KakiSoft.copy(alpha = 0.5f)
                else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f),
            )
            .padding(horizontal = 14.dp, vertical = 11.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
            color = if (positive) Breeze else MaterialTheme.colorScheme.error,
            maxLines = 2,
            textAlign = TextAlign.Center,
        )
    }
}

/** 圆形喇叭按钮。 */
@Composable
fun SpeakButton(onClick: () -> Unit, modifier: Modifier = Modifier, size: Int = 44) {
    Box(
        modifier
            .size(size.dp)
            .clip(RoundedCornerShape((size / 2).dp))
            .background(KakiSoft.copy(alpha = 0.55f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = FeiyanIcons.Headphones,
            contentDescription = "播放发音",
            tint = Breeze,
            modifier = Modifier.size((size / 2.4f).dp),
        )
    }
}

/** 供各玩法复用的「题干」提示文本。 */
@Composable
fun GamePrompt(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier.fillMaxWidth(),
        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
        color = InkMuted,
        textAlign = TextAlign.Center,
    )
}

/** 退出确认：避免误触丢失本局进度。 */
@Composable
fun ExitConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.42f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier
                .padding(horizontal = 34.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(PaperCard)
                .clickable { }
                .padding(horizontal = 22.dp, vertical = 22.dp),
        ) {
            Text(
                text = "要退出这一局吗？",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
                color = Ink,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "本局得分与星级不会保存。",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                color = InkMuted,
            )
            Spacer(Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GameActionButton(
                    text = "继续玩",
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    filled = false,
                )
                GameActionButton(
                    text = "退出",
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}




