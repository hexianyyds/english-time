package com.feiyan.english.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.feiyan.english.core.model.ChannelBrief
import com.feiyan.english.data.PodcastRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _results = MutableStateFlow<List<ChannelBrief>>(emptyList())
    val results: StateFlow<List<ChannelBrief>> = _results.asStateFlow()

    private val _searching = MutableStateFlow(false)
    val searching: StateFlow<Boolean> = _searching.asStateFlow()

    private var job: Job? = null

    fun onQueryChange(value: String) {
        _query.value = value
        job?.cancel()
        val keyword = value.trim()
        if (keyword.isEmpty()) {
            _results.value = emptyList()
            _searching.value = false
            return
        }
        job = viewModelScope.launch {
            delay(350)
            _searching.value = true
            runCatching { PodcastRepository.search(keyword) }
                .onSuccess { _results.value = it.channels }
                .onFailure { _results.value = emptyList() }
            _searching.value = false
        }
    }
}
