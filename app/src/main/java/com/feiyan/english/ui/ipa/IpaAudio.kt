package com.feiyan.english.ui.ipa

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.feiyan.english.core.model.IpaPhoneme
import com.feiyan.english.data.IpaRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class IpaAudioController {

    var loading by mutableStateOf(false)
        private set
    var playing by mutableStateOf(false)
        private set
    var currentId by mutableStateOf<String?>(null)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    private var player: MediaPlayer? = null
    private var job: Job? = null
    private var token = 0

    fun play(scope: CoroutineScope, context: Context, phoneme: IpaPhoneme) {
        job?.cancel()
        releasePlayer()
        val request = ++token
        error = null
        loading = true
        currentId = phoneme.id
        job = scope.launch {
            try {
                val file = IpaRepository.audioFile(context, phoneme)
                if (request != token) return@launch
                withContext(Dispatchers.Main) {
                    if (request != token) return@withContext
                    val mediaPlayer = MediaPlayer()
                    mediaPlayer.setDataSource(file.absolutePath)
                    mediaPlayer.setOnCompletionListener {
                        playing = false
                        currentId = null
                        runCatching { mediaPlayer.release() }
                        if (player === mediaPlayer) player = null
                    }
                    mediaPlayer.setOnErrorListener { _, _, _ ->
                        playing = false
                        currentId = null
                        error = "音频播放失败"
                        runCatching { mediaPlayer.release() }
                        if (player === mediaPlayer) player = null
                        true
                    }
                    mediaPlayer.prepare()
                    mediaPlayer.start()
                    player = mediaPlayer
                    playing = true
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (request == token) {
                    error = e.message ?: "音标音频加载失败"
                    playing = false
                    currentId = null
                }
            } finally {
                if (request == token) loading = false
            }
        }
    }

    fun stop() {
        token++
        job?.cancel()
        job = null
        releasePlayer()
        loading = false
        playing = false
        currentId = null
    }

    fun release() {
        stop()
    }

    private fun releasePlayer() {
        player?.let { mediaPlayer ->
            runCatching { mediaPlayer.stop() }
            runCatching { mediaPlayer.release() }
        }
        player = null
    }
}
