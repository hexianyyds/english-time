package com.feiyan.english.ui.eg

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.feiyan.english.core.model.EgArticle
import com.feiyan.english.core.model.EgArticleDetail
import com.feiyan.english.data.EgRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EgListUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val articles: List<EgArticle> = emptyList(),
    val category: String? = null,
) {
    val categories: List<String>
        get() = articles.map { it.category }.filter { it.isNotBlank() }.distinct()

    val filtered: List<EgArticle>
        get() = category?.let { key -> articles.filter { it.category == key } } ?: articles
}

class EgListViewModel : ViewModel() {

    private val _state = MutableStateFlow(EgListUiState())
    val state: StateFlow<EgListUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load(force: Boolean = false) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            runCatching { EgRepository.articles(force) }
                .onSuccess { list -> _state.update { it.copy(loading = false, articles = list) } }
                .onFailure { error -> _state.update { it.copy(loading = false, error = error.message ?: "载入失败") } }
        }
    }

    fun selectCategory(category: String?) {
        _state.update { it.copy(category = category) }
    }
}

data class EgArticleUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val article: EgArticleDetail? = null,
)

class EgArticleViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val id: String = checkNotNull(savedStateHandle["id"])

    private val _state = MutableStateFlow(EgArticleUiState())
    val state: StateFlow<EgArticleUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            runCatching { EgRepository.article(id) }
                .onSuccess { detail -> _state.update { it.copy(loading = false, article = detail) } }
                .onFailure { error -> _state.update { it.copy(loading = false, error = error.message ?: "载入失败") } }
        }
    }
}
