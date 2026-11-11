package com.feiyan.english.ui.game

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.feiyan.english.data.GameDefinition
import com.feiyan.english.data.GameQuestion
import com.feiyan.english.data.GameRepository
import com.feiyan.english.data.GameAudioPlayer
import com.feiyan.english.ui.components.ErrorView
import com.feiyan.english.ui.components.LoadingView
import com.feiyan.english.ui.theme.Hairline as HairlineColor
import com.feiyan.english.ui.theme.InkFaint
import com.feiyan.english.ui.theme.Kaki
import com.feiyan.english.ui.theme.PaperDeep
import kotlinx.coroutines.delay

/**
 * 翻图猜词：图被四块纸片盖住，揭得越少分越高。
 */
@Composable
fun RevealPickGame(
    game: GameDefinition,
    session: GameSession,
    onExit: () -> Unit,
) {
    val context = LocalContext.current
    var questions by remember { mutableStateOf<List<GameQuestion>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var reload by remember { mutableIntStateOf(0) }
    var index by remember(game.id) { mutableIntStateOf(0) }
    var revealed by remember(game.id) { mutableStateOf(emptySet<Int>()) }
    var chosen by remember(game.id) { mutableStateOf<String?>(null) }
    var verdict by remember(game.id) { mutableStateOf<Boolean?>(null) }
    var confirmExit by remember { mutableStateOf(false) }

    LaunchedEffect(game.id, reload) {
        questions = null
        error = null
        runCatching { GameRepository.drawQuestions(context, game.rounds) }
            .onSuccess { questions = it }
            .onFailure { error = it.message ?: "题库加载失败" }
    }

    val list = questions
    val q = list?.getOrNull(index)

    LaunchedEffect(chosen, index) {
        if (chosen == null || q == null) return@LaunchedEffect
        GameAudioPlayer.speak(q.answer)
        delay(if (verdict == true) 1200L else 1700L)
        index += 1
        revealed = emptySet()
        chosen = null
        verdict = null
    }

    Box(Modifier.fillMaxSize()) {
        when {
            error != null -> ErrorView(
                message = error.orEmpty(),
                modifier = Modifier.fillMaxSize(),
                onRetry = { reload += 1 },
            )

            list == null -> LoadingView(
                modifier = Modifier.fillMaxSize(),
                hint = "正在准备词卡…",
            )

            q == null -> Box(Modifier.fillMaxSize())

            else -> {
                Column(Modifier.fillMaxSize()) {
                    GameHud(
                        score = session.score,
                        combo = session.combo,
                        progress = index,
                        total = list.size,
                        onClose = { confirmExit = true },
                    )

                    Spacer(Modifier.height(6.dp))
                    GamePrompt("这张图是什么？揭开得越少，得分越高。")

                    Spacer(Modifier.height(14.dp))
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .weight(1f),
                    ) {
                        WordStage(q.answer, Modifier.fillMaxSize(), fontSize = 110)
                        // 无缝 2x2 遮罩：整块铺底色，避免从缝隙里瞥见答案
                        Column(
                            Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(20.dp)),
                        ) {
                            Row(Modifier.weight(1f).fillMaxWidth()) {
                                MaskCell(0, revealed, chosen != null, Modifier.weight(1f).fillMaxSize()) {
                                    revealed = revealed + 0
                                }
                                MaskCell(1, revealed, chosen != null, Modifier.weight(1f).fillMaxSize()) {
                                    revealed = revealed + 1
                                }
                            }
                            Row(Modifier.weight(1f).fillMaxWidth()) {
                                MaskCell(2, revealed, chosen != null, Modifier.weight(1f).fillMaxSize()) {
                                    revealed = revealed + 2
                                }
                                MaskCell(3, revealed, chosen != null, Modifier.weight(1f).fillMaxSize()) {
                                    revealed = revealed + 3
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "已揭 ${revealed.size} / 4",
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.6.sp),
                            color = InkFaint,
                        )
                        Spacer(Modifier.weight(1f))
                        if (revealed.size < 4 && chosen == null) {
                            Text(
                                text = "再揭一块",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                ),
                                color = Kaki,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                                    .clickable {
                                        val next = (0..3).filterNot { it in revealed }
                                        if (next.isNotEmpty()) revealed = revealed + next.random()
                                    }
                                    .padding(horizontal = 12.dp, vertical = 7.dp),
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    Column(Modifier.padding(horizontal = 24.dp)) {
                        if (verdict != null) {
                            FeedbackBar(
                                message = if (verdict == true) {
                                    "对了！${q.answer.word} · ${q.answer.zh}"
                                } else {
                                    "答案是 ${q.answer.word} · ${q.answer.zh}"
                                },
                                positive = verdict == true,
                            )
                            Spacer(Modifier.height(12.dp))
                        }
                        q.options.chunked(2).forEach { row ->
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                row.forEach { option ->
                                    WordTile(
                                        text = option.word,
                                        state = when {
                                            chosen == null -> TileState.Idle
                                            option.word == q.answer.word -> TileState.Correct
                                            option.word == chosen -> TileState.Wrong
                                            else -> TileState.Dimmed
                                        },
                                        onClick = {
                                            if (chosen != null) return@WordTile
                                            val ok = option.word == q.answer.word
                                            chosen = option.word
                                            verdict = ok
                                            if (ok) {
                                                val points = (100 - revealed.size * 25).coerceAtLeast(25)
                                                session.submit(true, points = points)
                                            } else {
                                                session.submit(false, missedWord = q.answer.word)
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                                if (row.size == 1) Spacer(Modifier.weight(1f))
                            }
                            Spacer(Modifier.height(10.dp))
                        }
                        Spacer(Modifier.height(14.dp))
                    }
                }
            }
        }

        if (confirmExit) {
            ExitConfirmDialog(
                onConfirm = onExit,
                onDismiss = { confirmExit = false },
            )
        }
    }
}

@Composable
private fun MaskCell(
    cellIndex: Int,
    revealed: Set<Int>,
    locked: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val hidden = cellIndex !in revealed
    Box(
        modifier.then(
            if (hidden) {
                Modifier
                    // 整块不透明，四块拼接后完全挡住答案
                    .background(PaperDeep.copy(alpha = if (locked) 0.55f else 1f))
                    .border(0.8.dp, HairlineColor)
                    .clickable(enabled = !locked, onClick = onClick)
            } else {
                // 揭开后保留细边框，让四格结构始终可见
                Modifier.border(0.6.dp, HairlineColor.copy(alpha = 0.65f))
            },
        ),
        contentAlignment = Alignment.Center,
    ) {
        if (hidden) {
            Text(
                text = "?",
                fontSize = 28.sp,
                color = InkFaint,
            )
        }
    }
}
