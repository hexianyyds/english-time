package com.feiyan.english.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.feiyan.english.core.model.GameWord
import com.feiyan.english.data.GameAudioPlayer
import com.feiyan.english.data.GameCatalog
import com.feiyan.english.data.GameDefinition
import com.feiyan.english.data.GameProgressStore
import com.feiyan.english.data.GameRepository
import com.feiyan.english.data.GameStat
import com.feiyan.english.ui.components.Eyebrow
import com.feiyan.english.ui.components.Hairline
import com.feiyan.english.ui.components.LocalBottomOverlayPadding
import com.feiyan.english.ui.icons.FeiyanIcons
import com.feiyan.english.ui.theme.Breeze
import com.feiyan.english.ui.theme.Hairline as HairlineColor
import com.feiyan.english.ui.theme.Ink
import com.feiyan.english.ui.theme.InkFaint
import com.feiyan.english.ui.theme.InkMuted
import com.feiyan.english.ui.theme.Kaki
import com.feiyan.english.ui.theme.KakiSoft
import com.feiyan.english.ui.theme.Paper
import com.feiyan.english.ui.theme.PaperCard

/**
 * 趣味学英语大厅：六种玩法 + 战绩统计 + 错词本。
 * 词库与素材完全离线，不依赖任何第三方站点。
 */
@Composable
fun GameHubScreen(
    onGameClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val progress by GameProgressStore.state.collectAsStateWithLifecycle()
    val totalStars = progress.stats.values.sumOf { it.stars }
    val totalCorrect = progress.stats.values.sumOf { it.correct }
    val totalWrong = progress.stats.values.sumOf { it.wrong }
    val accuracy = if (totalCorrect + totalWrong == 0) 0
    else (totalCorrect * 100 / (totalCorrect + totalWrong))

    // 错词本只有单词文本，这里查一次词库，好让点击时能放站点原声
    val context = LocalContext.current
    var wordLookup by remember { mutableStateOf<Map<String, GameWord>>(emptyMap()) }
    LaunchedEffect(Unit) {
        wordLookup = runCatching {
            GameRepository.bank(context).words.associateBy { it.word }
        }.getOrDefault(emptyMap())
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Paper),
        contentPadding = PaddingValues(bottom = LocalBottomOverlayPadding.current + 34.dp),
    ) {
        item {
            Column(Modifier.padding(horizontal = 24.dp)) {
                Spacer(Modifier.height(10.dp))
                Eyebrow("GAMES · 趣味闯关")
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Word Games",
                    style = MaterialTheme.typography.displaySmall.copy(fontSize = 27.sp),
                    color = Ink,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "看图、听音、拼写、限时判断——用玩的方式把单词记牢。全部内置词库，断网也能玩。",
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 21.sp),
                    color = InkMuted,
                )
                Spacer(Modifier.height(18.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    HubStat("⭐", totalStars.toString(), "总星数", Modifier.weight(1f))
                    HubStat("🎯", if (totalCorrect + totalWrong == 0) "—" else "$accuracy%", "正确率", Modifier.weight(1f))
                    HubStat("🎮", GameProgressStore.dayPlays.toString(), "今日局数", Modifier.weight(1f))
                }
                Spacer(Modifier.height(24.dp))
                Eyebrow("CHOOSE A GAME · 选一个开始")
                Spacer(Modifier.height(10.dp))
            }
        }

        items(GameCatalog.games, key = { it.id }) { game ->
            val stat = progress.stats[game.id] ?: GameStat()
            GameCard(
                game = game,
                best = stat.best,
                stars = stat.stars,
                plays = stat.plays,
                onClick = { onGameClick(game.id) },
            )
        }

        val wrongWords = progress.wrongWords
        if (wrongWords.isNotEmpty()) {
            item {
                Column(Modifier.padding(horizontal = 24.dp)) {
                    Spacer(Modifier.height(26.dp))
                    Hairline()
                    Spacer(Modifier.height(20.dp))
                    Eyebrow("MISTAKES · 错词本")
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "这些词在游戏里答错过，点一下听发音，多听几遍就记住了。",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = InkFaint,
                    )
                    Spacer(Modifier.height(12.dp))
                    wrongWords.take(12).chunked(3).forEach { row ->
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            row.forEach { word ->
                                Box(
                                    Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(KakiSoft.copy(alpha = 0.4f))
                                        .clickable {
                                            val entry = wordLookup[word]
                                            if (entry != null) GameAudioPlayer.speak(entry)
                                            else GameAudioPlayer.speakText(word)
                                        }
                                        .padding(vertical = 9.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = word,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
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
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = "清空游戏进度",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.4.sp),
                        color = InkFaint,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { GameProgressStore.reset() }
                            .padding(vertical = 6.dp),
                    )
                }
            }
        }

        item {
            Column(Modifier.padding(horizontal = 24.dp)) {
                Spacer(Modifier.height(28.dp))
                Text(
                    text = "内置 13 个主题 320+ 个词：食物主题使用站点原版插画与原声，其余用 emoji 与系统语音，全程离线。",
                    style = MaterialTheme.typography.labelSmall.copy(lineHeight = 17.sp),
                    color = InkFaint,
                )
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun GameCard(
    game: GameDefinition,
    best: Int,
    stars: Int,
    plays: Int,
    onClick: () -> Unit,
) {
    val accent = if (game.accentIndex == 0) Kaki else Breeze
    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(PaperCard)
            .border(0.8.dp, HairlineColor, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(accent.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = game.emoji, fontSize = 27.sp)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = game.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = Ink,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = game.english.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            letterSpacing = 1.1.sp,
                        ),
                        color = InkFaint,
                    )
                }
                Spacer(Modifier.height(3.dp))
                Text(
                    text = game.tagline,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                    color = InkMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(7.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "⭐".repeat(stars) + "☆".repeat(3 - stars.coerceIn(0, 3)),
                        fontSize = 11.sp,
                        color = if (stars > 0) accent else InkFaint,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (plays == 0) "还没玩过" else "最高 $best · 已玩 $plays 局",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                        color = InkFaint,
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = FeiyanIcons.ChevronRight,
                contentDescription = null,
                tint = InkFaint,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
private fun HubStat(
    emoji: String,
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(PaperCard)
            .border(0.8.dp, HairlineColor, RoundedCornerShape(14.dp))
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = emoji, fontSize = 15.sp)
        Spacer(Modifier.height(3.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
            ),
            color = Ink,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = InkFaint,
        )
    }
}
