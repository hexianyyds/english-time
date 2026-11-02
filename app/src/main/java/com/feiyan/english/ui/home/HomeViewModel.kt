package com.feiyan.english.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.feiyan.english.core.model.EmEpisode
import com.feiyan.english.core.model.HomeSection
import com.feiyan.english.core.model.XyPodcast
import com.feiyan.english.data.EmRepository
import com.feiyan.english.data.PodcastRepository
import com.feiyan.english.data.XyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Error(val message: String) : HomeUiState
    data class Ready(
        val sections: List<HomeSection>,
        val emEpisodes: List<EmEpisode> = emptyList(),
        val xyPodcast: XyPodcast? = null,
    ) : HomeUiState
}

class HomeViewModel : ViewModel() {

    private val _state = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load(force: Boolean = false) {
        viewModelScope.launch {
            if (_state.value !is HomeUiState.Ready) _state.value = HomeUiState.Loading
            runCatching { PodcastRepository.home(force) }
                .onSuccess { sections ->
                    // 另外两个内容源是附加的，失败不影响飞岩主内容
                    val em = runCatching { EmRepository.podcasts(force) }.getOrDefault(emptyList())
                    val xy = runCatching { XyRepository.podcast(force) }.getOrNull()
                    _state.value = HomeUiState.Ready(sections, em, xy)
                }
                .onFailure { _state.value = HomeUiState.Error(it.message ?: "网络似乎不太稳定") }
        }
    }
}
