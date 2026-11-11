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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.feiyan.english.core.model.GameWord
import com.feiyan.english.data.GameDefinition
import com.feiyan.english.data.GameRepository
import com.feiyan.english.data.GameAudioPlayer
import com.feiyan.english.ui.components.ErrorView
import com.feiyan.english.ui.components.LoadingView
import com.feiyan.english.ui.theme.Breeze
import com.feiyan.english.ui.theme.Hairline as HairlineColor
import com.feiyan.english.ui.theme.Ink
import com.feiyan.english.ui.theme.InkFaint
import com.feiyan.english.ui.theme.InkMuted
import com.feiyan.english.ui.theme.Kaki
import com.feiyan.english.ui.theme.KakiSoft
import kotlinx.coroutines.delay

private data class FastRound(
    /** 图片侧的真实词条（决定显示哪张图）。 */
    val visual: GameWord,
    /** 屏幕上给出的单词，可能故意与图片不符。 */
    val shownWord: String,
    val matched: Boolean,
)

private const val TOTAL_SECONDS = 60

/**
 * 极速判断：60 秒内连续判断「图和单词是否相配」。
 */
@Composable
fun FastQuizGame(
    game: GameDefinition,
    session: GameSession,
    onExit: () -> Unit,
) {
    val context = LocalContext.current
    var rounds by remember { mutableStateOf<List<FastRound>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var reload by remember { mutableIntStateOf(0) }
    var index by remember(game.id) { mutableIntStateOf(0) }
    var remaining by remember(game.id) { mutableIntStateOf(TOTAL_SECONDS) }
    var flash by remember { mutableStateOf<Boolean?>(null) }
    var confirmExit by remember { mutableStateOf(false) }

    LaunchedEffect(game.id, reload) {
        rounds = null
        error = null
        runCatching {
            val questions = GameRepository.drawQuestions(context, 20, optionCount = 4)
            questions.mapIndexed { i, q ->
                if (i % 2 == 0) {
                    FastRound(q.answer, q.answer.word, matched = true)
                } else {
                    val other = q.options.firstOrNull { it.word != q.answer.word }
                    FastRound(
                        visual = q.answer,
                        shownWord = other?.word ?: q.answer.word,
                        matched = other == null,
                    )
                }
            }.shuffled()
        }
            .onSuccess { rounds = it }
            .onFailure { error = it.message ?: "题库加载失败" }
    }

    LaunchedEffect(game.id, rounds != null) {
        if (rounds == null) return@LaunchedEffect
        while (remaining > 0 && !session.isFinished) {
            delay(1000)
            remaining -= 1
        }
        if (remaining <= 0) session.finish()
    }

    LaunchedEffect(index) {
        if (flash != null) {
            delay(200)
            flash = null
        }
    }

    val list = rounds
    val current = list?.getOrNull(index)

    Box(Modifier.fillMaxSize()) {
        when {
            error != null -> ErrorView(
                message = error.orEmpty(),
                modifier = Modifier.fillMaxSize(),
                onRetry = { reload += 1 },
            )

            list == null -> LoadingView(Modifier.fillMaxSize(), hint = "正在出题…")

            current == null -> Box(Modifier.fillMaxSize())

            else -> Column(Modifier.fillMaxSize()) {
                GameHud(
                    score = session.score,
                    combo = session.combo,
                    progress = index,
                    total = list.size,
                    onClose = { confirmExit = true },
                )

                Spacer(Modifier.height(4.dp))
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "⏱ $remaining s",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.6.sp,
                        ),
                        color = if (remaining <= 10) MaterialTheme.colorScheme.error else InkMuted,
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = "✓ ${session.correct}   ✗ ${session.wrong}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                        color = InkFaint,
                    )
                }

                Spacer(Modifier.weight(1f))

                WordStage(
                    word = current.visual,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 64.dp)
                        .height(176.dp),
                    fontSize = 74,
                )

                Spacer(Modifier.height(16.dp))
                Text(
                    text = current.shownWord,
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontSize = 33.sp,
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = Ink,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "这个单词和图相配吗？",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                    color = InkFaint,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.weight(1f))

                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    FastButton(
                        label = "✓  相配",
                        positive = true,
                        onClick = {
                            if (session.isFinished) return@FastButton
                            val ok = current.matched
                            flash = ok
                            session.submit(ok, missedWord = if (ok) null else current.visual.word)
                            if (ok) GameAudioPlayer.speak(current.visual)
                            index += 1
                        },
                        modifier = Modifier.weight(1f),
                    )
                    FastButton(
                        label = "✗  不相配",
                        positive = false,
                        onClick = {
                            if (session.isFinished) return@FastButton
                            val ok = !current.matched
                            flash = ok
                            session.submit(ok, missedWord = if (ok) null else current.visual.word)
                            index += 1
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
                Spacer(Modifier.height(26.dp))
            }
        }

        // 瞬时对错反馈
        val marker = flash
        if (marker != null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = if (marker) "✓" else "✗",
                    fontSize = 108.sp,
                    color = (if (marker) Kaki else MaterialTheme.colorScheme.error).copy(alpha = 0.55f),
                )
            }
        }

        if (confirmExit) {
            ExitConfirmDialog(onConfirm = onExit, onDismiss = { confirmExit = false })
        }
    }
}

@Composable
private fun FastButton(
    label: String,
    positive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val container = if (positive) {
        KakiSoft.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
    }
    Box(
        modifier
            .height(72.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(container)
            .border(0.8.dp, HairlineColor, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall.copy(
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp,
            ),
            color = if (positive) Breeze else MaterialTheme.colorScheme.error,
        )
    }
}
