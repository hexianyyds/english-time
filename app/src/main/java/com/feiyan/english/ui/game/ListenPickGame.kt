package com.feiyan.english.ui.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.feiyan.english.data.GameDefinition
import com.feiyan.english.data.GameQuestion
import com.feiyan.english.data.GameRepository
import com.feiyan.english.data.GameAudioPlayer
import com.feiyan.english.ui.components.ErrorView
import com.feiyan.english.ui.components.LoadingView
import com.feiyan.english.ui.theme.InkFaint
import com.feiyan.english.ui.theme.Kaki
import kotlinx.coroutines.delay

/**
 * 听音选图：只听发音，从四张图里选出正确的那张。
 */
@Composable
fun ListenPickGame(
    game: GameDefinition,
    session: GameSession,
    onExit: () -> Unit,
) {
    val context = LocalContext.current
    var questions by remember { mutableStateOf<List<GameQuestion>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var reload by remember { mutableIntStateOf(0) }
    var index by remember(game.id) { mutableIntStateOf(0) }
    var chosen by remember(game.id) { mutableStateOf<String?>(null) }
    var verdict by remember(game.id) { mutableStateOf<Boolean?>(null) }
    var replays by remember(game.id) { mutableIntStateOf(0) }
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

    // 每题自动播一次
    LaunchedEffect(index, q?.answer?.word) {
        val answer = q?.answer ?: return@LaunchedEffect
        replays = 0
        GameAudioPlayer.speak(answer)
    }

    LaunchedEffect(chosen, index) {
        if (chosen == null || q == null) return@LaunchedEffect
        delay(if (verdict == true) 1100L else 1700L)
        index += 1
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

            list == null -> LoadingView(Modifier.fillMaxSize(), hint = "正在准备词卡…")

            q == null -> Box(Modifier.fillMaxSize())

            else -> Column(Modifier.fillMaxSize()) {
                GameHud(
                    score = session.score,
                    combo = session.combo,
                    progress = index,
                    total = list.size,
                    onClose = { confirmExit = true },
                )
                Spacer(Modifier.height(8.dp))
                GamePrompt("听发音，选出对应的图。")

                // 喇叭居中于空白区，用留白做视觉呼吸
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        SpeakButton(
                            onClick = {
                                GameAudioPlayer.speak(q.answer)
                                if (chosen == null) replays += 1
                            },
                            size = 78,
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = if (replays == 0) "点喇叭可以再听一遍" else "已重听 $replays 次，本题得分略降",
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.4.sp, fontSize = 10.5.sp),
                            color = if (replays == 0) InkFaint else Kaki,
                        )
                    }
                }

                Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
                    q.options.chunked(2).forEach { row ->
                        Row(
                            Modifier.fillMaxWidth().height(118.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            row.forEach { option ->
                                ImageOptionTile(
                                    word = option,
                                    state = when {
                                        chosen == null -> TileState.Idle
                                        option.word == q.answer.word -> TileState.Correct
                                        option.word == chosen -> TileState.Wrong
                                        else -> TileState.Dimmed
                                    },
                                    onClick = {
                                        if (chosen != null) return@ImageOptionTile
                                        val ok = option.word == q.answer.word
                                        chosen = option.word
                                        verdict = ok
                                        val points = (100 - replays * 15).coerceAtLeast(40)
                                        session.submit(ok, missedWord = q.answer.word, points = points)
                                        if (!ok) GameAudioPlayer.speak(q.answer)
                                    },
                                    modifier = Modifier.weight(1f).fillMaxSize(),
                                )
                            }
                            if (row.size == 1) Spacer(Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(10.dp))
                    }

                    if (verdict != null) {
                        Spacer(Modifier.height(2.dp))
                        FeedbackBar(
                            message = if (verdict == true) {
                                "对了！${q.answer.word} · ${q.answer.zh}"
                            } else {
                                "正确答案：${q.answer.word} · ${q.answer.zh}"
                            },
                            positive = verdict == true,
                        )
                    }
                    Spacer(Modifier.height(18.dp))
                }
            }
        }

        if (confirmExit) {
            ExitConfirmDialog(onConfirm = onExit, onDismiss = { confirmExit = false })
        }
    }
}
