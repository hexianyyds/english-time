package com.feiyan.english.ui.em

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.feiyan.english.core.model.EmEpisodeDetail
import com.feiyan.english.data.EmRepository
import com.feiyan.english.player.PlayerController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EmEpisodeUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val detail: EmEpisodeDetail? = null,
)

class EmEpisodeViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val slug: String = checkNotNull(savedStateHandle["slug"])

    private val _state = MutableStateFlow(EmEpisodeUiState())
    val state: StateFlow<EmEpisodeUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            runCatching { EmRepository.podcast(slug) }
                .onSuccess { detail -> _state.update { it.copy(loading = false, detail = detail) } }
                .onFailure { error ->
                    _state.update { it.copy(loading = false, error = error.message ?: "载入失败") }
                }
        }
    }

    fun play(startMs: Long = 0L) {
        val detail = _state.value.detail ?: return
        PlayerController.play(
            key = MEDIA_PREFIX + detail.slug,
            title = detail.title,
            subtitle = "EnglishMeasure · " + com.feiyan.english.core.model.EmCategories.label(detail.category),
            cover = null,
            url = detail.audioUrl,
            startPositionMs = startMs,
            route = "em/${detail.slug}",
        )
    }

    companion object {
        const val MEDIA_PREFIX = "em:"
    }
}
