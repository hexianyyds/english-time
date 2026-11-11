package com.feiyan.english.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.feiyan.english.data.GameCatalog
import com.feiyan.english.data.GameProgressStore
import com.feiyan.english.data.GameAudioPlayer
import com.feiyan.english.ui.theme.Ink
import com.feiyan.english.ui.theme.InkMuted
import com.feiyan.english.ui.theme.Paper

/**
 * 一局游戏的容器：负责创建计分会话、分发到具体玩法、结算并写入进度。
 */
@Composable
fun GamePlayScreen(
    gameId: String,
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val game = GameCatalog.find(gameId)
    if (game == null) {
        Column(
            modifier.fillMaxSize().background(Paper).padding(28.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("找不到这个玩法", style = MaterialTheme.typography.titleMedium, color = Ink)
            Text(
                text = gameId,
                style = MaterialTheme.typography.bodySmall,
                color = InkMuted,
            )
        }
        return
    }

    var round by remember(game.id) { mutableIntStateOf(0) }
    var showResult by remember(game.id, round) { mutableStateOf(false) }
    var recorded by remember(game.id, round) { mutableStateOf(false) }
    val session = remember(game.id, round) {
        GameSession(game.id, if (game.id == "fast") 20 else game.rounds)
    }

    LaunchedEffect(session.isFinished) {
        if (session.isFinished && !recorded) {
            recorded = true
            GameProgressStore.record(
                gameId = game.id,
                score = session.score,
                stars = session.stars,
                correct = session.correct,
                wrong = session.wrong,
                maxCombo = session.maxCombo,
                missedWords = session.missedWords.toList(),
            )
            showResult = true
        }
    }

    Box(modifier.fillMaxSize().background(Paper).navigationBarsPadding()) {
        // key(round)：重开一局时强制重建玩法内部状态（题目、计时、翻牌等）
        key(round) {
            when (game.id) {
                "reveal" -> RevealPickGame(game, session, onExit)
                "listen" -> ListenPickGame(game, session, onExit)
                "memory" -> MemoryMatchGame(game, session, onExit)
                "spell" -> SpellGame(game, session, onExit)
                "fast" -> FastQuizGame(game, session, onExit)
                "grammar" -> GrammarGame(game, session, onExit)
                else -> Unit
            }
        }

        if (showResult) {
            GameResultScreen(
                game = game,
                session = session,
                onReplay = { round += 1 },
                onExit = onExit,
                onSpeak = { GameAudioPlayer.speakText(it) },
            )
        }
    }
}
