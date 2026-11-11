package com.feiyan.english.ui.game

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * 一局游戏的计分核心：得分、连击、正确率、星级、错词。
 * 五种玩法共用同一套结算规则，保证大厅统计口径一致。
 */
@Stable
class GameSession(
    val gameId: String,
    initialTotal: Int,
) {

    /** 本局总题数；题库不足时可以下调。 */
    var total by mutableIntStateOf(initialTotal)
        private set

    fun updateTotal(value: Int) {
        if (value > 0) total = value
    }

    var score by mutableIntStateOf(0)
        private set

    var combo by mutableIntStateOf(0)
        private set

    var maxCombo by mutableIntStateOf(0)
        private set

    var correct by mutableIntStateOf(0)
        private set

    var wrong by mutableIntStateOf(0)
        private set

    /** 已处理的题数（记忆配对里表示已配成的对数）。 */
    var progress by mutableIntStateOf(0)
        private set

    /** 限时玩法可以由外部直接结束。 */
    var finished by mutableStateOf(false)
        private set

    var starsOverride by mutableStateOf<Int?>(null)

    val missedWords = mutableStateListOf<String>()

    val isFinished: Boolean
        get() = finished || (total > 0 && progress >= total)

    val accuracy: Float
        get() {
            val answered = correct + wrong
            return if (answered == 0) 0f else correct.toFloat() / answered
        }

    val stars: Int
        get() = starsOverride ?: when {
            correct + wrong == 0 -> 0
            accuracy >= 0.9f -> 3
            accuracy >= 0.7f -> 2
            accuracy >= 0.5f -> 1
            else -> 0
        }

    /**
     * 提交一次作答。[points] 为本答基础分，连击加成后写入总分。
     */
    fun submit(correctAnswer: Boolean, missedWord: String? = null, points: Int = 100) {
        if (correctAnswer) {
            combo += 1
            maxCombo = maxOf(maxCombo, combo)
            correct += 1
            score += points + (combo - 1).coerceAtMost(5) * 20
        } else {
            combo = 0
            wrong += 1
            if (!missedWord.isNullOrBlank() && missedWords.none { it == missedWord }) {
                missedWords += missedWord
            }
        }
        progress += 1
        if (total > 0 && progress >= total) finished = true
    }

    fun finish() {
        finished = true
    }

    fun roundsLeft(): Int = (total - progress).coerceAtLeast(0)
}
