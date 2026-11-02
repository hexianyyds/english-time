package com.feiyan.english.player

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.feiyan.english.core.util.coverUrl
import com.feiyan.english.data.LibraryItem
import com.feiyan.english.data.LibraryStore
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class PlaybackUiState(
    val mediaId: String? = null,
    val title: String = "",
    val subtitle: String = "",
    val cover: String? = null,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val speed: Float = 1f,
) {
    val hasMedia: Boolean get() = mediaId != null
}

private data class PendingPlay(
    val key: String,
    val title: String,
    val subtitle: String,
    val cover: String?,
    val url: String,
)

/**
 * UI 与 MediaSessionService 之间的唯一桥梁（进程内单例）。
 */
object PlayerController {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _state = MutableStateFlow(PlaybackUiState())
    val state: StateFlow<PlaybackUiState> = _state.asStateFlow()

    private var future: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null
    private var ticker: Job? = null
    private var pending: PendingPlay? = null

    fun connect(context: Context) {
        if (future != null) return
        val app = context.applicationContext
        val token = SessionToken(app, ComponentName(app, PlaybackService::class.java))
        val created = MediaController.Builder(app, token).buildAsync()
        future = created
        created.addListener(
            {
                val resolved = runCatching { created.get() }.getOrNull()
                controller = resolved
                resolved?.addListener(object : Player.Listener {
                    override fun onEvents(player: Player, events: Player.Events) = sync()
                })
                sync()
                startTicker()
                pending?.let { request ->
                    pending = null
                    play(request.key, request.title, request.subtitle, request.cover, request.url)
                }
            },
            ContextCompat.getMainExecutor(app),
        )
    }

    fun play(
        key: String,
        title: String,
        subtitle: String,
        cover: String?,
        url: String,
        startPositionMs: Long = 0L,
        route: String? = null,
    ) {
        val c = controller
        if (c == null) {
            pending = PendingPlay(key, title, subtitle, cover, url)
            return
        }
        if (route != null) {
            LibraryStore.recordPlay(
                LibraryItem(
                    id = key,
                    title = title,
                    subtitle = subtitle,
                    cover = cover,
                    route = route,
                    positionMs = startPositionMs,
                ),
            )
        }
        val metadata = MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(subtitle)
            .setArtworkUri(coverUrl(cover)?.let(Uri::parse))
            .build()
        val item = MediaItem.Builder()
            .setUri(url)
            .setMediaId(key)
            .setMediaMetadata(metadata)
            .build()

        c.setMediaItem(item)
        c.prepare()
        if (startPositionMs > 0L) c.seekTo(startPositionMs)
        c.play()
        _state.update {
            it.copy(
                mediaId = key,
                title = title,
                subtitle = subtitle,
                cover = cover,
                isPlaying = true,
                positionMs = 0L,
                durationMs = 0L,
            )
        }
        startTicker()
    }

    fun toggle() {
        val c = controller ?: return
        if (c.isPlaying) {
            c.pause()
        } else {
            if (c.playbackState == Player.STATE_IDLE) c.prepare()
            c.play()
        }
        sync()
    }

    fun seekTo(positionMs: Long) {
        controller?.seekTo(positionMs.coerceAtLeast(0L))
        sync()
    }

    fun seekBy(deltaMs: Long) {
        val c = controller ?: return
        val target = (c.currentPosition + deltaMs).coerceIn(0L, c.duration.coerceAtLeast(0L))
        c.seekTo(target)
        sync()
    }

    fun setSpeed(speed: Float) {
        controller?.setPlaybackSpeed(speed)
        _state.update { it.copy(speed = speed) }
    }

    /** 关闭播放条：停止播放并清空当前媒体。 */
    fun stop() {
        persistProgress()
        controller?.run {
            stop()
            clearMediaItems()
        }
        _state.value = PlaybackUiState()
    }

    private fun persistProgress() {
        val snapshot = _state.value
        val id = snapshot.mediaId ?: return
        LibraryStore.updateProgress(id, snapshot.positionMs, snapshot.durationMs)
    }

    fun isCurrent(key: String?): Boolean = key != null && _state.value.mediaId == key

    fun release() {
        ticker?.cancel()
        ticker = null
        future?.let(MediaController::releaseFuture)
        future = null
        controller = null
    }

    private fun startTicker() {
        if (ticker != null) return
        ticker = scope.launch {
            var sincePersist = 0
            while (isActive) {
                syncPosition()
                delay(500)
                sincePersist += 500
                if (sincePersist >= 5000) {
                    sincePersist = 0
                    if (_state.value.isPlaying) persistProgress()
                }
            }
        }
    }

    private fun syncPosition() {
        val c = controller ?: return
        val position = c.currentPosition.coerceAtLeast(0L)
        val duration = c.duration.coerceAtLeast(0L)
        _state.update { current ->
            if (current.positionMs == position && current.durationMs == duration) current
            else current.copy(positionMs = position, durationMs = duration)
        }
    }

    private fun sync() {
        val c = controller ?: return
        _state.update {
            it.copy(
                isPlaying = c.isPlaying,
                isBuffering = c.playbackState == Player.STATE_BUFFERING,
                positionMs = c.currentPosition.coerceAtLeast(0L),
                durationMs = c.duration.coerceAtLeast(0L),
                speed = c.playbackParameters.speed,
            )
        }
    }
}
