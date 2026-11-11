package com.feiyan.english.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.LocalDate

@Serializable
data class GameStat(
    val best: Int = 0,
    val stars: Int = 0,
    val plays: Int = 0,
    val correct: Int = 0,
    val wrong: Int = 0,
    val bestCombo: Int = 0,
)

@Serializable
data class GameProgress(
    val stats: Map<String, GameStat> = emptyMap(),
    /** 最近答错的词，最多保留 60 条，供「错词本」复习。 */
    val wrongWords: List<String> = emptyList(),
    val dayKey: String = "",
    val dayPlays: Int = 0,
    val totalPlays: Int = 0,
)

/**
 * 趣味闯关的本地进度：最高分、星级、连击、错词本。
 * 与 LibraryStore 同一套 SharedPreferences + StateFlow 风格。
 */
object GameProgressStore {

    private const val PREFS = "feiyan_game"
    private const val KEY_PROGRESS = "progress"
    private const val MAX_WRONG = 60

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private var prefs: android.content.SharedPreferences? = null

    private val _state = MutableStateFlow(GameProgress())
    val state: StateFlow<GameProgress> = _state.asStateFlow()

    fun init(context: Context) {
        if (prefs != null) return
        val store = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs = store
        val raw = store.getString(KEY_PROGRESS, null)
        _state.value = if (raw.isNullOrBlank()) {
            GameProgress()
        } else {
            runCatching { json.decodeFromString(GameProgress.serializer(), raw) }
                .getOrDefault(GameProgress())
        }
    }

    fun stat(gameId: String): GameStat = _state.value.stats[gameId] ?: GameStat()

    val totalStars: Int get() = _state.value.stats.values.sumOf { it.stars }

    val dayPlays: Int
        get() {
            val today = LocalDate.now().toString()
            val progress = _state.value
            return if (progress.dayKey == today) progress.dayPlays else 0
        }

    val wrongWords: List<String> get() = _state.value.wrongWords

    /** 一局结束后写入成绩；只记录更好的成绩，避免反复刷分把星级压下去。 */
    fun record(
        gameId: String,
        score: Int,
        stars: Int,
        correct: Int,
        wrong: Int,
        maxCombo: Int,
        missedWords: List<String>,
    ) {
        val current = _state.value
        val old = current.stats[gameId] ?: GameStat()
        val today = LocalDate.now().toString()
        val merged = old.copy(
            best = maxOf(old.best, score),
            stars = maxOf(old.stars, stars),
            plays = old.plays + 1,
            correct = old.correct + correct,
            wrong = old.wrong + wrong,
            bestCombo = maxOf(old.bestCombo, maxCombo),
        )
        val updatedWrong = if (missedWords.isEmpty()) {
            current.wrongWords
        } else {
            (missedWords + current.wrongWords)
                .distinct()
                .take(MAX_WRONG)
        }
        _state.value = current.copy(
            stats = current.stats + (gameId to merged),
            wrongWords = updatedWrong,
            dayKey = today,
            dayPlays = if (current.dayKey == today) current.dayPlays + 1 else 1,
            totalPlays = current.totalPlays + 1,
        )
        persist()
    }

    fun removeWrongWord(word: String) {
        val current = _state.value
        if (current.wrongWords.none { it == word }) return
        _state.value = current.copy(wrongWords = current.wrongWords.filterNot { it == word })
        persist()
    }

    fun reset() {
        _state.value = GameProgress()
        persist()
    }

    private fun persist() {
        prefs?.edit()
            ?.putString(KEY_PROGRESS, json.encodeToString(GameProgress.serializer(), _state.value))
            ?.apply()
    }
}
