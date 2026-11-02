package com.feiyan.english

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.request.crossfade
import com.feiyan.english.data.LibraryStore
import com.feiyan.english.data.TokenStore
import com.feiyan.english.player.PlayerController

class FeiyanApplication : Application(), SingletonImageLoader.Factory {

    override fun onCreate() {
        super.onCreate()
        TokenStore.init(this)
        LibraryStore.init(this)
        PlayerController.connect(this)
    }

    /**
     * Coil 3 会自动发现 classpath 上的 coil-network-okhttp，
     * 这里只调整全局的淡入与缓存策略。
     */
    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader.Builder(context)
            .crossfade(200)
            .build()
}
