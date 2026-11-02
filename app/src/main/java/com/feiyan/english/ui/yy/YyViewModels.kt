package com.feiyan.english.ui.yy

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.feiyan.english.core.model.YyBookDetail
import com.feiyan.english.core.model.YyChapterData
import com.feiyan.english.core.model.YyHomeData
import com.feiyan.english.core.model.YyParagraph
import com.feiyan.english.core.net.YyApi
import com.feiyan.english.data.YyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class YyNovelsUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val books: YyHomeData = YyHomeData(),
)

class YyNovelsViewModel : ViewModel() {

    private val _state = MutableStateFlow(YyNovelsUiState())
    val state: StateFlow<YyNovelsUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load(force: Boolean = false) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            runCatching { YyRepository.home(force) }
                .onSuccess { books -> _state.update { it.copy(loading = false, books = books) } }
                .onFailure { error -> _state.update { it.copy(loading = false, error = error.message ?: "载入失败") } }
        }
    }
}

data class YyBookUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val book: YyBookDetail? = null,
) {
    val chapters: List<Pair<Int, String>>
        get() = book?.chapters.orEmpty()
            .mapNotNull { (key, title) -> key.toIntOrNull()?.let { it to title } }
            .sortedBy { it.first }
}

class YyBookViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val bookId: String = checkNotNull(savedStateHandle["bookId"])

    private val _state = MutableStateFlow(YyBookUiState())
    val state: StateFlow<YyBookUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            runCatching { YyRepository.book(bookId) }
                .onSuccess { book -> _state.update { it.copy(loading = false, book = book) } }
                .onFailure { error -> _state.update { it.copy(loading = false, error = error.message ?: "载入失败") } }
        }
    }
}

data class YyChapterUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val chapter: YyChapterData? = null,
    val paragraphs: List<YyParagraph> = emptyList(),
    val total: Int = 0,
)

class YyChapterViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val bookId: String = checkNotNull(savedStateHandle["bookId"])
    private val initialIndex: Int = savedStateHandle.get<String>("index")?.toIntOrNull() ?: 0

    private val _state = MutableStateFlow(YyChapterUiState())
    val state: StateFlow<YyChapterUiState> = _state.asStateFlow()

    init {
        load(initialIndex)
    }

    fun load(index: Int) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            runCatching {
                val chapter = YyRepository.chapter(bookId, index)
                val total = runCatching { YyRepository.book(bookId) }.getOrNull()?.chapters?.size ?: 0
                chapter to total
            }.onSuccess { (chapter, total) ->
                _state.update {
                    it.copy(
                        loading = false,
                        chapter = chapter,
                        paragraphs = YyApi.parseParagraphs(chapter.c),
                        total = total,
                    )
                }
            }.onFailure { error ->
                _state.update { it.copy(loading = false, error = error.message ?: "载入失败") }
            }
        }
    }
}
