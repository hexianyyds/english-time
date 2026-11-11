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
import com.feiyan.english.ui.theme.Hairline as HairlineColor
import com.feiyan.english.ui.theme.Ink
import com.feiyan.english.ui.theme.InkFaint
import com.feiyan.english.ui.theme.InkMuted
import com.feiyan.english.ui.theme.Kaki
import com.feiyan.english.ui.theme.KakiSoft
import com.feiyan.english.ui.theme.PaperCard
import com.feiyan.english.ui.theme.PaperDeep
import kotlinx.coroutines.delay

/**
 * 拼写挑战：看图拼单词，字母池里混了干扰字母。
 * 第一次拼错会清空重来，第二次拼错直接揭晓并进入下一题。
 */
@Composable
fun SpellGame(
    game: GameDefinition,
    session: GameSession,
    onExit: () -> Unit,
) {
    val context = LocalContext.current
    var words by remember { mutableStateOf<List<GameWord>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var reload by remember { mutableIntStateOf(0) }
    var index by remember(game.id) { mutableIntStateOf(0) }
    var picked by remember(game.id) { mutableStateOf(emptyList<Int>()) }
    var verdict by remember(game.id) { mutableStateOf<Boolean?>(null) }
    var attempts by remember(game.id) { mutableIntStateOf(0) }
    var confirmExit by remember { mutableStateOf(false) }

    LaunchedEffect(game.id, reload) {
        words = null
        error = null
        runCatching { GameRepository.drawSpellWords(context, game.rounds) }
            .onSuccess { words = it }
            .onFailure { error = it.message ?: "题库加载失败" }
    }

    val list = words
    val current = list?.getOrNull(index)
    val target = current?.word?.lowercase().orEmpty()

    val pool = remember(index, target) {
        if (target.isBlank()) emptyList()
        else GameRepository.letterPool(target, extra = if (target.length >= 8) 3 else 2)
    }

    LaunchedEffect(picked, index) {
        if (target.isBlank() || picked.size < target.length) return@LaunchedEffect
        val guess = picked.mapNotNull { pool.getOrNull(it) }.joinToString("").lowercase()
        if (guess == target) {
            verdict = true
            session.submit(true, points = 100)
            current?.let { GameAudioPlayer.speak(it) }
            delay(1300)
            index += 1
            picked = emptyList()
            verdict = null
            attempts = 0
        } else if (attempts == 0) {
            attempts = 1
            delay(600)
            picked = emptyList()
        } else {
            verdict = false
            session.submit(false, missedWord = target)
            delay(1800)
            index += 1
            picked = emptyList()
            verdict = null
            attempts = 0
        }
    }

    Box(Modifier.fillMaxSize()) {
        when {
            error != null -> ErrorView(
                message = error.orEmpty(),
                modifier = Modifier.fillMaxSize(),
                onRetry = { reload += 1 },
            )

            list == null -> LoadingView(Modifier.fillMaxSize(), hint = "正在准备题目…")

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
                GamePrompt("看图拼出这个单词。")
                Spacer(Modifier.height(10.dp))

                Box(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    WordStage(
                        word = current,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .height(210.dp),
                        fontSize = 68,
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "${current.zh} · ${current.pos}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                    color = InkMuted,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(12.dp))

                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    repeat(target.length) { slot ->
                        val letter = picked.getOrNull(slot)?.let { pool.getOrNull(it) }
                        SlotCell(letter = letter, filled = letter != null, modifier = Modifier.weight(1f))
                    }
                }

                Spacer(Modifier.height(16.dp))

                Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
                    when (verdict) {
                        true -> FeedbackBar(
                            message = "拼对了！${current.word} · ${current.zh}",
                            positive = true,
                        )

                        false -> FeedbackBar(
                            message = "正确拼写是 ${current.word}",
                            positive = false,
                        )

                        null -> if (attempts == 1) FeedbackBar(
                            message = "再试一次，注意干扰字母。",
                            positive = false,
                        )
                    }
                    Spacer(Modifier.height(12.dp))

                    pool.chunked(6).forEachIndexed { rowIndex, row ->
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            row.forEachIndexed { columnIndex, letter ->
                                val poolIndex = rowIndex * 6 + columnIndex
                                val used = poolIndex in picked
                                LetterKey(
                                    letter = letter,
                                    used = used,
                                    onClick = {
                                        if (used || verdict != null) return@LetterKey
                                        if (picked.size >= target.length) return@LetterKey
                                        picked = picked + poolIndex
                                    },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            repeat(6 - row.size) { Spacer(Modifier.weight(1f)) }
                        }
                        Spacer(Modifier.height(8.dp))
                    }

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = "退格",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                            ),
                            color = if (picked.isEmpty()) InkFaint else Kaki,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (picked.isEmpty()) PaperDeep.copy(alpha = 0.4f) else KakiSoft.copy(alpha = 0.5f))
                                .clickable(enabled = picked.isNotEmpty() && verdict == null) {
                                    picked = picked.dropLast(1)
                                }
                                .padding(horizontal = 20.dp, vertical = 9.dp),
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

@Composable
private fun SlotCell(letter: Char?, filled: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier
            .height(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (filled) PaperCard else PaperDeep.copy(alpha = 0.45f))
            .border(
                width = 0.8.dp,
                color = if (filled) Kaki.copy(alpha = 0.5f) else HairlineColor,
                shape = RoundedCornerShape(8.dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = letter?.toString() ?: "",
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
            ),
            color = Ink,
        )
    }
}

@Composable
private fun LetterKey(
    letter: Char,
    used: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .height(46.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(if (used) PaperDeep.copy(alpha = 0.35f) else PaperCard)
            .border(
                width = 0.8.dp,
                color = if (used) HairlineColor.copy(alpha = 0.5f) else HairlineColor,
                shape = RoundedCornerShape(11.dp),
            )
            .clickable(enabled = !used, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = letter.toString(),
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
            ),
            color = if (used) InkFaint.copy(alpha = 0.5f) else Ink,
        )
    }
}
