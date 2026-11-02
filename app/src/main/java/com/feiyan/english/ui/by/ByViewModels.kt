package com.feiyan.english.ui.by

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.feiyan.english.core.model.ByArticle
import com.feiyan.english.core.model.ByBook
import com.feiyan.english.core.model.ByBookDetailData
import com.feiyan.english.core.model.ByChapterData
import com.feiyan.english.core.net.ByApi
import com.feiyan.english.core.net.ByAuth
import com.feiyan.english.core.net.FeiyanApi
import com.feiyan.english.data.ByRepository
import com.feiyan.english.player.PlayerController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Request

data class ByNovelsUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val books: List<ByBook> = emptyList(),
    val page: Int = 1,
    val loadingMore: Boolean = false,
    val hasMore: Boolean = true,
)

class ByNovelsViewModel : ViewModel() {

    private val _state = MutableStateFlow(ByNovelsUiState())
    val state: StateFlow<ByNovelsUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load(force: Boolean = false) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            runCatching { ByRepository.books(1, force) }
                .onSuccess { books ->
                    _state.update {
                        it.copy(loading = false, books = books, page = 1, hasMore = books.size >= PAGE_SIZE)
                    }
                }
                .onFailure { error -> _state.update { it.copy(loading = false, error = error.message ?: "载入失败") } }
        }
    }

    fun loadMore() {
        val current = _state.value
        if (current.loading || current.loadingMore || !current.hasMore) return
        val next = current.page + 1
        viewModelScope.launch {
            _state.update { it.copy(loadingMore = true) }
            runCatching { ByRepository.books(next) }
                .onSuccess { books ->
                    _state.update {
                        it.copy(
                            loadingMore = false,
                            books = it.books + books.filter { book -> it.books.none { b -> b.id == book.id } },
                            page = next,
                            hasMore = books.size >= PAGE_SIZE,
                        )
                    }
                }
                .onFailure { _state.update { it.copy(loadingMore = false, hasMore = false) } }
        }
    }

    private companion object {
        const val PAGE_SIZE = 20
    }
}

data class ByArticlesUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val articles: List<ByArticle> = emptyList(),
    val page: Int = 1,
    val loadingMore: Boolean = false,
    val hasMore: Boolean = true,
)

class ByArticlesViewModel : ViewModel() {

    private val _state = MutableStateFlow(ByArticlesUiState())
    val state: StateFlow<ByArticlesUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load(force: Boolean = false) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            runCatching { ByRepository.articles(1, force) }
                .onSuccess { articles ->
                    _state.update {
                        it.copy(loading = false, articles = articles, page = 1, hasMore = articles.size >= PAGE_SIZE)
                    }
                }
                .onFailure { error -> _state.update { it.copy(loading = false, error = error.message ?: "载入失败") } }
        }
    }

    fun loadMore() {
        val current = _state.value
        if (current.loading || current.loadingMore || !current.hasMore) return
        val next = current.page + 1
        viewModelScope.launch {
            _state.update { it.copy(loadingMore = true) }
            runCatching { ByRepository.articles(next) }
                .onSuccess { articles ->
                    _state.update {
                        it.copy(
                            loadingMore = false,
                            articles = it.articles + articles.filter { a -> it.articles.none { old -> old.id == a.id } },
                            page = next,
                            hasMore = articles.size >= PAGE_SIZE,
                        )
                    }
                }
                .onFailure { _state.update { it.copy(loadingMore = false, hasMore = false) } }
        }
    }

    private companion object {
        const val PAGE_SIZE = 20
    }
}

data class ByBookUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val detail: ByBookDetailData? = null,
)

class ByBookViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val bookId: String = checkNotNull(savedStateHandle["bookId"])

    private val _state = MutableStateFlow(ByBookUiState())
    val state: StateFlow<ByBookUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            runCatching { ByRepository.bookDetail(bookId) }
                .onSuccess { detail -> _state.update { it.copy(loading = false, detail = detail) } }
                .onFailure { error -> _state.update { it.copy(loading = false, error = error.message ?: "载入失败") } }
        }
    }
}

data class ByReaderUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val chapter: ByChapterData? = null,
)

class ByReaderViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val bookId: String = checkNotNull(savedStateHandle["bookId"])
    private val chapterId: Long =
        savedStateHandle.get<String>("chapterId")?.toLongOrNull() ?: 0L

    private val _state = MutableStateFlow(ByReaderUiState())
    val state: StateFlow<ByReaderUiState> = _state.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            runCatching { ByRepository.chapter(bookId, chapterId) }
                .onSuccess { chapter -> _state.update { it.copy(loading = false, chapter = chapter) } }
                .onFailure { error -> _state.update { it.copy(loading = false, error = error.message ?: "载入失败") } }
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    /** 章节朗读：需要 BookYell 登录；服务端用 JSON 返回额度/登录错误。 */
    fun speak(chapter: ByChapterData) {
        if (!ByAuth.isLoggedIn) {
            _message.value = "登录 BookYell 后可用朗读（我的 → 设置 → BookYell 登录）"
            return
        }
        val id = chapter.chapter?.id ?: run {
            _message.value = "该章节暂不支持朗读"
            return
        }
        viewModelScope.launch {
            val url = ByApi.audioUrl(chapter.bookId, id)
            val error = withContext(Dispatchers.IO) {
                runCatching { probeAudio(url) }.getOrDefault("朗读暂不可用")
            }
            if (error != null) {
                _message.value = error
                return@launch
            }
            PlayerController.play(
                key = "by:${chapter.bookId}:$id",
                title = chapter.chapter?.name.orEmpty(),
                subtitle = "BookYell · 朗读",
                cover = null,
                url = url,
                route = "by/${chapter.bookId}/$id",
            )
        }
    }

    /** 音频接口成功时返回音频流；失败时返回 JSON 错误文案。 */
    private fun probeAudio(url: String): String? {
        val request = Request.Builder()
            .url(url)
            .header("Range", "bytes=0-0")
            .get()
            .build()
        FeiyanApi.httpClient.newCall(request).execute().use { response ->
            val type = response.header("Content-Type").orEmpty()
            if (type.contains("json") || type.startsWith("text/")) {
                val body = response.body.string()
                return Regex("\"msg\"\\s*:\\s*\"([^\"]+)\"").find(body)?.groupValues?.get(1)
                    ?: "朗读暂不可用"
            }
            return null
        }
    }
}
