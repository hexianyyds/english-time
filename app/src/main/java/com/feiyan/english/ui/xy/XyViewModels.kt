package com.feiyan.english.ui.xy

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.feiyan.english.core.model.XyEpisode
import com.feiyan.english.core.model.XyPodcast
import com.feiyan.english.data.XyRepository
import com.feiyan.english.player.PlayerController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class XyPodcastUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val podcast: XyPodcast? = null,
)

class XyPodcastViewModel : ViewModel() {

    private val _state = MutableStateFlow(XyPodcastUiState())
    val state: StateFlow<XyPodcastUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load(force: Boolean = false) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            runCatching { XyRepository.podcast(force) }
                .onSuccess { podcast -> _state.update { it.copy(loading = false, podcast = podcast) } }
                .onFailure { error -> _state.update { it.copy(loading = false, error = error.message ?: "载入失败") } }
        }
    }
}

data class XyEpisodeUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val episode: XyEpisode? = null,
    val podcastTitle: String = "",
)

class XyEpisodeViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val eid: String = checkNotNull(savedStateHandle["eid"])

    private val _state = MutableStateFlow(XyEpisodeUiState())
    val state: StateFlow<XyEpisodeUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            runCatching { XyRepository.podcast() }
                .onSuccess { podcast ->
                    val episode = podcast.episodes.firstOrNull { it.eid == eid }
                    if (episode == null) {
                        _state.update { it.copy(loading = false, error = "单集不在免费列表中") }
                    } else {
                        _state.update {
                            it.copy(loading = false, episode = episode, podcastTitle = podcast.title)
                        }
                    }
                }
                .onFailure { error ->
                    _state.update { it.copy(loading = false, error = error.message ?: "载入失败") }
                }
        }
    }

    fun play(startMs: Long = 0L) {
        val episode = _state.value.episode ?: return
        val url = episode.audioUrl ?: return
        PlayerController.play(
            key = MEDIA_PREFIX + episode.eid,
            title = episode.title,
            subtitle = "小宇宙 · " + _state.value.podcastTitle,
            cover = episode.image?.best,
            url = url,
            startPositionMs = startMs,
            route = "xy/${episode.eid}",
        )
    }

    companion object {
        const val MEDIA_PREFIX = "xy:"
    }
}
