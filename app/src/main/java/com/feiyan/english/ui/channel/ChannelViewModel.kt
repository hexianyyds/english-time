package com.feiyan.english.ui.channel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.feiyan.english.core.model.ChannelDetail
import com.feiyan.english.core.model.Episode
import com.feiyan.english.data.PodcastRepository
import com.feiyan.english.player.PlayerController
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChannelUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val channel: ChannelDetail? = null,
    val episodes: List<Episode> = emptyList(),
    val total: Int = 0,
    val page: Int = 0,
    val loadingMore: Boolean = false,
    val hasMore: Boolean = true,
    val preparing: Boolean = false,
)

class ChannelViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val isbn: String = checkNotNull(savedStateHandle["isbn"])

    private val _state = MutableStateFlow(ChannelUiState())
    val state: StateFlow<ChannelUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            runCatching {
                val detail = async { PodcastRepository.channel(isbn) }
                val count = async { PodcastRepository.episodeCount(isbn) }
                val page = async { PodcastRepository.episodes(isbn, page = 1, size = PAGE_SIZE) }
                Triple(detail.await(), count.await(), page.await())
            }.onSuccess { (detail, count, page) ->
                _state.update {
                    it.copy(
                        loading = false,
                        channel = detail,
                        total = count,
                        episodes = page.episodes,
                        page = 1,
                        hasMore = page.episodes.size >= PAGE_SIZE,
                        error = null,
                    )
                }
            }.onFailure { error ->
                _state.update { it.copy(loading = false, error = error.message ?: "节目加载失败") }
            }
        }
    }

    fun loadMore() {
        val current = _state.value
        if (current.loading || current.loadingMore || !current.hasMore) return
        val next = current.page + 1
        viewModelScope.launch {
            _state.update { it.copy(loadingMore = true) }
            runCatching { PodcastRepository.episodes(isbn, next, PAGE_SIZE) }
                .onSuccess { page ->
                    _state.update {
                        it.copy(
                            loadingMore = false,
                            episodes = it.episodes + page.episodes,
                            page = next,
                            hasMore = page.episodes.size >= PAGE_SIZE,
                        )
                    }
                }
                .onFailure {
                    _state.update { it.copy(loadingMore = false, hasMore = false) }
                }
        }
    }

    fun playLatest() {
        val latest = _state.value.episodes.firstOrNull() ?: return
        play(latest)
    }

    fun play(episode: Episode) {
        if (_state.value.preparing) return
        viewModelScope.launch {
            _state.update { it.copy(preparing = true) }
            val channel = _state.value.channel
            runCatching { PodcastRepository.playUrl(episode.uniqueKey) }
                .onSuccess { url ->
                    PlayerController.play(
                        key = episode.uniqueKey,
                        title = episode.name,
                        subtitle = channel?.englishName.orEmpty(),
                        cover = episode.cover ?: channel?.cover,
                        url = url,
                        route = "episode/${episode.uniqueKey}",
                    )
                }
            _state.update { it.copy(preparing = false) }
        }
    }

    private companion object {
        const val PAGE_SIZE = 20
    }
}
