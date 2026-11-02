package com.feiyan.english.ui.episode

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.feiyan.english.core.model.Episode
import com.feiyan.english.core.model.Transcript
import com.feiyan.english.data.PodcastRepository
import com.feiyan.english.player.PlayerController
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EpisodeUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val episode: Episode? = null,
    val channelName: String = "",
    val transcript: Transcript? = null,
    val preparing: Boolean = false,
)

class EpisodeViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val key: String = checkNotNull(savedStateHandle["key"])

    private val _state = MutableStateFlow(EpisodeUiState())
    val state: StateFlow<EpisodeUiState> = _state.asStateFlow()

    private var cachedUrl: String? = null

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            runCatching { PodcastRepository.episode(key) }
                .onSuccess { episode ->
                    _state.update { it.copy(loading = false, episode = episode) }
                    viewModelScope.launch {
                        val channel = async {
                            runCatching { PodcastRepository.channel(episode.isbn) }.getOrNull()
                        }
                        val transcript = async {
                            runCatching { PodcastRepository.transcript(episode) }.getOrNull()
                        }
                        _state.update {
                            it.copy(
                                channelName = channel.await()?.englishName.orEmpty(),
                                transcript = transcript.await(),
                            )
                        }
                    }
                }
                .onFailure { error ->
                    _state.update { it.copy(loading = false, error = error.message ?: "节目加载失败") }
                }
        }
    }

    fun play(startMs: Long = 0L) {
        val episode = _state.value.episode ?: return
        val isPreparing = _state.value.preparing
        if (isPreparing) return

        viewModelScope.launch {
            val url = cachedUrl ?: run {
                _state.update { it.copy(preparing = true) }
                val fetched = runCatching { PodcastRepository.playUrl(key) }.getOrNull()
                _state.update { it.copy(preparing = false) }
                fetched?.also { cachedUrl = it }
            }
            if (url == null) {
                _state.update { it.copy(error = "音频地址获取失败，请稍后重试") }
                return@launch
            }
            PlayerController.play(
                key = episode.uniqueKey,
                title = episode.name,
                subtitle = _state.value.channelName,
                cover = episode.cover,
                url = url,
                startPositionMs = startMs,
                route = "episode/$key",
            )
        }
    }
}
