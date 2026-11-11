package com.feiyan.english.data

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.feiyan.english.core.model.GameWord

/**
 * 游戏发音：优先播放站点原声。
 *
 * 站点把每个 set 的 8 个单词压在一个 ogg 里（音频 sprite），
 * 这里用 ExoPlayer 的 ClippingConfiguration 直接按毫秒区间裁剪播放，
 * 既保留原声，又不用在构建期切分音频。
 * 没有原声时回落到系统 TTS。
 */
object GameAudioPlayer {

    private var appContext: Context? = null
    private var player: ExoPlayer? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    /** 播一个词：原声优先，缺原声或解码失败时用 TTS。 */
    fun speak(word: GameWord) {
        if (SpeechPlayer.isMuted) return
        val context = appContext
        if (context == null || !word.hasAudio) {
            SpeechPlayer.speak(word.word)
            return
        }
        val exo = ensurePlayer(context)
        if (exo == null) {
            SpeechPlayer.speak(word.word)
            return
        }
        SpeechPlayer.stop()
        runCatching {
            val item = MediaItem.Builder()
                .setUri("asset:///${word.audioFile}")
                .setClippingConfiguration(
                    MediaItem.ClippingConfiguration.Builder()
                        .setStartPositionMs(word.audioStart.toLong())
                        .setEndPositionMs(word.audioEnd.toLong())
                        .build(),
                )
                .build()
            exo.setMediaItem(item)
            exo.prepare()
            exo.play()
        }.onFailure {
            SpeechPlayer.speak(word.word)
        }
    }

    /** 只有文本时（错词本、结算页）走 TTS。 */
    fun speakText(text: String) {
        if (text.isBlank()) return
        player?.stop()
        SpeechPlayer.speak(text)
    }

    private fun ensurePlayer(context: Context): ExoPlayer? = runCatching {
        player ?: ExoPlayer.Builder(context).build().apply {
            // 不当成音乐播放：不抢音频焦点，也不打断用户正在听的播客
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
                    .build(),
                false,
            )
            volume = 1f
            player = this
        }
    }.getOrNull()
}
