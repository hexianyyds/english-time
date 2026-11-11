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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.feiyan.english.core.model.GrammarItem
import com.feiyan.english.data.GameAudioPlayer
import com.feiyan.english.data.GameDefinition
import com.feiyan.english.data.GameRepository
import com.feiyan.english.ui.components.ErrorView
import com.feiyan.english.ui.components.LoadingView
import com.feiyan.english.ui.theme.Breeze
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
 * 语法选择：站点 superlatives / comparatives 题库。
 * 每题一个句子（或问句）+ 三个选项，选出语法正确的一项。
 */
@Composable
fun GrammarGame(
    game: GameDefinition,
    session: GameSession,
    onExit: () -> Unit,
) {
    val context = LocalContext.current
    var items by remember { mutableStateOf<List<GrammarItem>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var reload by remember { mutableIntStateOf(0) }
    var index by remember(game.id) { mutableIntStateOf(0) }
    var chosen by remember(game.id) { mutableStateOf<String?>(null) }
    var verdict by remember(game.id) { mutableStateOf<Boolean?>(null) }
    var confirmExit by remember { mutableStateOf(false) }

    LaunchedEffect(game.id, reload) {
        items = null
        error = null
        runCatching { GameRepository.drawGrammar(context, game.rounds) }
            .onSuccess {
                if (it.isEmpty()) error = "题库为空" else items = it
            }
            .onFailure { error = it.message ?: "题库加载失败" }
    }

    val list = items
    val current = list?.getOrNull(index)

    // 题库不足预期题数时按实际题数结算，避免卡在最后一题
    LaunchedEffect(list?.size) {
        val size = list?.size ?: return@LaunchedEffect
        if (size in 1 until session.total) session.updateTotal(size)
    }

    LaunchedEffect(chosen, index) {
        if (chosen == null) return@LaunchedEffect
        delay(if (verdict == true) 1500L else 2200L)
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

                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = if (current.type == "comparative") "COMPARATIVE · 比较级"
                        else "SUPERLATIVE · 最高级",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.5.sp,
                            letterSpacing = 1.2.sp,
                        ),
                        color = InkFaint,
                    )
                    Spacer(Modifier.weight(1f))
                    if (current.hint.isNotBlank()) {
                        Text(
                            text = "原级 ${current.hint}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = Kaki,
                            modifier = Modifier
                                .clip(RoundedCornerShape(7.dp))
                                .background(KakiSoft.copy(alpha = 0.45f))
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                        )
                    }
                }

                Spacer(Modifier.weight(1f))

                // 题干卡片：把答案嵌进句子中间的空位
                val blankColor = if (verdict == true) Breeze else Kaki
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(PaperCard)
                        .border(0.8.dp, HairlineColor, RoundedCornerShape(20.dp))
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                ) {
                    Text(
                        text = buildAnnotatedString {
                            if (current.hasBlank) {
                                append(current.prefix)
                                append(" ")
                                withStyle(
                                    SpanStyle(
                                        color = blankColor,
                                        fontWeight = FontWeight.SemiBold,
                                        textDecoration = TextDecoration.Underline,
                                    ),
                                ) {
                                    append(if (verdict != null) current.answer else "______")
                                }
                                if (current.suffix.isNotBlank()) {
                                    append(" ")
                                    append(current.suffix)
                                }
                            } else {
                                append(current.prompt)
                            }
                        },
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 19.sp,
                            lineHeight = 30.sp,
                        ),
                        color = Ink,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Spacer(Modifier.weight(1f))

                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    current.options.forEach { option ->
                        GrammarOption(
                            text = option,
                            state = when {
                                chosen == null -> TileState.Idle
                                option == current.answer -> TileState.Correct
                                option == chosen -> TileState.Wrong
                                else -> TileState.Dimmed
                            },
                            onClick = {
                                if (chosen != null) return@GrammarOption
                                val ok = option == current.answer
                                chosen = option
                                verdict = ok
                                session.submit(ok, missedWord = if (ok) null else current.prompt)
                                GameAudioPlayer.speakText(
                                    if (current.hasBlank) {
                                        "${current.prefix} ${current.answer} ${current.suffix}"
                                    } else {
                                        current.answer
                                    },
                                )
                            },
                        )
                    }

                    if (verdict != null) {
                        Spacer(Modifier.height(4.dp))
                        FeedbackBar(
                            message = if (verdict == true) "正确！" else "正确答案：${current.answer}",
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

@Composable
private fun GrammarOption(
    text: String,
    state: TileState,
    onClick: () -> Unit,
) {
    val container = when (state) {
        TileState.Correct -> KakiSoft.copy(alpha = 0.5f)
        TileState.Wrong -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.32f)
        TileState.Dimmed -> PaperDeep.copy(alpha = 0.4f)
        TileState.Idle -> PaperCard
    }
    val borderColor = when (state) {
        TileState.Correct -> Kaki
        TileState.Wrong -> MaterialTheme.colorScheme.error
        else -> HairlineColor
    }
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(container)
            .border(
                width = if (state == TileState.Idle) 0.8.dp else 1.6.dp,
                color = borderColor,
                shape = RoundedCornerShape(14.dp),
            )
            .clickable(enabled = state == TileState.Idle, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 15.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 15.sp,
                fontStyle = if (state == TileState.Correct) FontStyle.Italic else FontStyle.Normal,
            ),
            color = when (state) {
                TileState.Dimmed -> InkFaint
                TileState.Wrong -> MaterialTheme.colorScheme.error
                else -> InkMuted
            },
            textAlign = TextAlign.Center,
        )
    }
}
