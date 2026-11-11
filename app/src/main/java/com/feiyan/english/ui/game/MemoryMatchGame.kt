package com.feiyan.english.ui.game

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import com.feiyan.english.ui.theme.PaperCard
import com.feiyan.english.ui.theme.PaperDeep
import kotlinx.coroutines.delay

private data class MemoryCard(
    val id: Int,
    val word: GameWord,
    /** true = 图片牌，false = 单词牌。 */
    val imageSide: Boolean,
)

/**
 * 记忆配对：单词牌与图片牌各六张，翻牌配对，步数越少星级越高。
 */
@Composable
fun MemoryMatchGame(
    game: GameDefinition,
    session: GameSession,
    onExit: () -> Unit,
) {
    val context = LocalContext.current
    var words by remember { mutableStateOf<List<GameWord>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var reload by remember { mutableIntStateOf(0) }
    var attempt by remember(game.id) { mutableIntStateOf(0) }

    val cards = remember(words, attempt) {
        (words ?: emptyList()).flatMapIndexed { index, word ->
            listOf(
                MemoryCard(index * 2, word, imageSide = false),
                MemoryCard(index * 2 + 1, word, imageSide = true),
            )
        }.shuffled()
    }
    val matched = remember(cards) { mutableStateListOf<Int>() }
    var flipped by remember(cards) { mutableStateOf(emptyList<Int>()) }
    var moves by remember(cards) { mutableIntStateOf(0) }
    var confirmExit by remember { mutableStateOf(false) }

    LaunchedEffect(game.id, reload) {
        words = null
        error = null
        runCatching { GameRepository.drawPairs(context, game.rounds) }
            .onSuccess { words = it }
            .onFailure { error = it.message ?: "题库加载失败" }
    }

    // 翻满两张就判定
    LaunchedEffect(flipped) {
        if (flipped.size != 2) return@LaunchedEffect
        val first = cards.getOrNull(flipped[0])
        val second = cards.getOrNull(flipped[1])
        if (first == null || second == null) {
            flipped = emptyList()
            return@LaunchedEffect
        }
        val sameWord = first.word.word == second.word.word
        delay(if (sameWord) 260L else 780L)
        moves += 1
        if (sameWord && first.imageSide != second.imageSide) {
            matched += flipped
            GameAudioPlayer.speak(first.word)
            val willFinish = session.progress + 1 >= session.total
            if (willFinish) session.starsOverride = starsForMoves(moves, session.total)
            session.submit(true, points = 100)
        }
        flipped = emptyList()
    }

    Box(Modifier.fillMaxSize()) {
        when {
            error != null -> ErrorView(
                message = error.orEmpty(),
                modifier = Modifier.fillMaxSize(),
                onRetry = { reload += 1 },
            )

            words == null -> LoadingView(Modifier.fillMaxSize(), hint = "正在发牌…")

            else -> Column(Modifier.fillMaxSize()) {
                GameHud(
                    score = session.score,
                    combo = session.combo,
                    progress = matched.size / 2,
                    total = session.total,
                    onClose = { confirmExit = true },
                )
                Spacer(Modifier.height(6.dp))
                GamePrompt("翻开两张牌，把单词和它的图片配成一对。")
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "已翻 $moves 步 · 最少 ${session.total} 步可通关",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp, letterSpacing = 0.4.sp),
                    color = InkFaint,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(14.dp))
                Column(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 18.dp),
                ) {
                    cards.chunked(3).forEach { row ->
                        Row(
                            Modifier.weight(1f).fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            row.forEach { card ->
                                val isMatched = card.id in matched
                                val isFlipped = card.id in flipped
                                MemoryTile(
                                    card = card,
                                    revealed = isFlipped || isMatched,
                                    matched = isMatched,
                                    onClick = {
                                        if (isMatched || isFlipped || flipped.size >= 2) return@MemoryTile
                                        flipped = flipped + card.id
                                    },
                                    modifier = Modifier.weight(1f).fillMaxSize(),
                                )
                            }
                            repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                    Spacer(Modifier.height(6.dp))
                }
            }
        }

        if (confirmExit) {
            ExitConfirmDialog(onConfirm = onExit, onDismiss = { confirmExit = false })
        }
    }
}

private fun starsForMoves(moves: Int, pairs: Int): Int = when {
    moves <= (pairs * 1.5f).toInt() -> 3
    moves <= pairs * 2 + 2 -> 2
    else -> 1
}

@Composable
private fun MemoryTile(
    card: MemoryCard,
    revealed: Boolean,
    matched: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val alpha by animateFloatAsState(
        targetValue = if (matched) 0.32f else 1f,
        animationSpec = tween(280),
        label = "memory-alpha",
    )
    Box(
        modifier
            .graphicsLayer { this.alpha = alpha }
            .clip(RoundedCornerShape(14.dp))
            .background(if (revealed) PaperCard else PaperDeep.copy(alpha = 0.7f))
            .border(
                width = if (matched) 1.4.dp else 0.8.dp,
                color = when {
                    matched -> Kaki
                    revealed -> HairlineColor
                    else -> HairlineColor
                },
                shape = RoundedCornerShape(14.dp),
            )
            .clickable(enabled = !revealed, onClick = onClick)
            .padding(6.dp),
        contentAlignment = Alignment.Center,
    ) {
        when {
            !revealed -> Text(
                text = "?",
                fontSize = 24.sp,
                color = InkMuted.copy(alpha = 0.5f),
            )

            card.imageSide -> WordVisual(card.word, Modifier.fillMaxSize(), emojiSize = 34, padding = 2)

            else -> Text(
                text = card.word.word,
                fontSize = if (card.word.word.length > 8) 10.5.sp else 12.5.sp,
                lineHeight = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Ink,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
