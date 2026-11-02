package com.feiyan.english.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/**
 * 本地「播放历史 / 我的收藏」。
 * 只存最小信息 + 回跳路由，点击时按 route 直接导航。
 */
@Serializable
data class LibraryItem(
    val id: String,
    val title: String,
    val subtitle: String = "",
    val cover: String? = null,
    /** 回到该内容的导航路由，如 episode/xxx、by/bookId */
    val route: String,
    val positionMs: Long = 0,
    val durationMs: Long = 0,
    val updatedAt: Long = 0,
)

object LibraryStore {

    private const val PREFS = "feiyan_library"
    private const val KEY_HISTORY = "history"
    private const val KEY_FAVORITES = "favorites"
    private const val MAX_HISTORY = 100

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private var prefs: SharedPreferences? = null

    private val _history = MutableStateFlow<List<LibraryItem>>(emptyList())
    val history: StateFlow<List<LibraryItem>> = _history.asStateFlow()

    private val _favorites = MutableStateFlow<List<LibraryItem>>(emptyList())
    val favorites: StateFlow<List<LibraryItem>> = _favorites.asStateFlow()

    fun init(context: Context) {
        if (prefs != null) return
        val store = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs = store
        _history.value = read(store.getString(KEY_HISTORY, null))
        _favorites.value = read(store.getString(KEY_FAVORITES, null))
    }

    fun recordPlay(item: LibraryItem) {
        val now = System.currentTimeMillis()
        val existing = _history.value.firstOrNull { it.id == item.id }
        val merged = item.copy(
            updatedAt = now,
            durationMs = if (item.durationMs > 0) item.durationMs else existing?.durationMs ?: 0,
        )
        _history.value = (listOf(merged) + _history.value.filterNot { it.id == item.id })
            .take(MAX_HISTORY)
        persistHistory()
    }

    fun updateProgress(id: String, positionMs: Long, durationMs: Long) {
        val current = _history.value
        val index = current.indexOfFirst { it.id == id }
        if (index < 0) return
        val item = current[index].copy(
            positionMs = positionMs,
            durationMs = if (durationMs > 0) durationMs else current[index].durationMs,
            updatedAt = System.currentTimeMillis(),
        )
        _history.value = current.toMutableList().also { it[index] = item }
        persistHistory()
    }

    fun clearHistory() {
        _history.value = emptyList()
        persistHistory()
    }

    fun isFavorite(id: String): Boolean = _favorites.value.any { it.id == id }

    fun toggleFavorite(item: LibraryItem) {
        _favorites.value = if (isFavorite(item.id)) {
            _favorites.value.filterNot { it.id == item.id }
        } else {
            listOf(item.copy(updatedAt = System.currentTimeMillis())) + _favorites.value
        }
        persistFavorites()
    }

    fun removeFavorite(id: String) {
        _favorites.value = _favorites.value.filterNot { it.id == id }
        persistFavorites()
    }

    private fun persistHistory() {
        prefs?.edit()?.putString(KEY_HISTORY, write(_history.value))?.apply()
    }

    private fun persistFavorites() {
        prefs?.edit()?.putString(KEY_FAVORITES, write(_favorites.value))?.apply()
    }

    private fun read(raw: String?): List<LibraryItem> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching {
            json.decodeFromString(ListSerializer(LibraryItem.serializer()), raw)
        }.getOrDefault(emptyList())
    }

    private fun write(items: List<LibraryItem>): String =
        json.encodeToString(ListSerializer(LibraryItem.serializer()), items)
}
