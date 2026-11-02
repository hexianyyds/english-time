package com.feiyan.english.ui.em

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.feiyan.english.core.model.EmEpisode
import com.feiyan.english.data.EmRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EmListUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val episodes: List<EmEpisode> = emptyList(),
    val category: String? = null,
) {
    val filtered: List<EmEpisode>
        get() = category?.let { key -> episodes.filter { it.category == key } } ?: episodes
}

class EmListViewModel : ViewModel() {

    private val _state = MutableStateFlow(EmListUiState())
    val state: StateFlow<EmListUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load(force: Boolean = false) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            runCatching { EmRepository.podcasts(force) }
                .onSuccess { list ->
                    _state.update { it.copy(loading = false, episodes = list) }
                }
                .onFailure { error ->
                    _state.update { it.copy(loading = false, error = error.message ?: "载入失败") }
                }
        }
    }

    fun selectCategory(category: String?) {
        _state.update { it.copy(category = category) }
    }
}
