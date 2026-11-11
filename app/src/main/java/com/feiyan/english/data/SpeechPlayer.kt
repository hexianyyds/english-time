package com.feiyan.english.data

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

/**
 * 游戏发音：用系统 TTS，离线、零延迟，不依赖网络朗读服务。
 * 引擎不可用或被静音时静默降级，游戏流程不受影响。
 */
object SpeechPlayer {

    private var engine: TextToSpeech? = null

    @Volatile
    private var ready = false

    @Volatile
    private var muted = false

    @Volatile
    private var pendingText: String? = null

    fun init(context: Context) {
        if (engine != null) return
        val app = context.applicationContext
        engine = TextToSpeech(app) { status ->
            if (status == TextToSpeech.SUCCESS) {
                engine?.let { tts ->
                    runCatching {
                        tts.language = Locale.US
                        tts.setSpeechRate(0.88f)
                        tts.setPitch(1.05f)
                    }
                    ready = true
                    pendingText?.let { text ->
                        pendingText = null
                        speak(text)
                    }
                }
            }
        }
    }

    val isMuted: Boolean get() = muted

    fun setMuted(value: Boolean) {
        muted = value
        if (value) engine?.stop()
    }

    fun stop() {
        engine?.stop()
    }

    fun speak(text: String) {
        if (text.isBlank() || muted) return
        val tts = engine
        if (tts == null || !ready) {
            pendingText = text
            return
        }
        runCatching {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "feiyan-game-${text.hashCode()}")
        }
    }

    fun release() {
        engine?.stop()
        engine?.shutdown()
        engine = null
        ready = false
    }
}
