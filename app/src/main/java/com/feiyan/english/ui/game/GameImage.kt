package com.feiyan.english.ui.game

import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.feiyan.english.core.model.GameWord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 词卡插画加载：从 assets 解码并做进程内缓存。
 * 一局同时最多显示十几张图，缓存 24 张足够，也不会占太多内存。
 */
private val imageCache = LruCache<String, ImageBitmap>(24)

@Composable
fun rememberWordImage(word: GameWord, preferHd: Boolean = false): ImageBitmap? {
    val path = word.imagePath(preferHd).takeIf { it.isNotBlank() } ?: return null
    val context = LocalContext.current
    var bitmap by remember(path) { mutableStateOf(imageCache.get(path)) }

    LaunchedEffect(path) {
        if (bitmap != null) return@LaunchedEffect
        val loaded = withContext(Dispatchers.IO) {
            runCatching {
                context.assets.open(path).use { stream ->
                    BitmapFactory.decodeStream(stream)?.asImageBitmap()
                }
            }.getOrNull()
        }
        if (loaded != null) {
            imageCache.put(path, loaded)
            bitmap = loaded
        }
    }
    return bitmap
}

/**
 * 统一渲染一个词的「图」：有插画用插画，否则回落到 emoji。
 */
@Composable
fun WordVisual(
    word: GameWord,
    modifier: Modifier = Modifier,
    emojiSize: Int = 64,
    padding: Int = 6,
    preferHd: Boolean = false,
) {
    val image = rememberWordImage(word, preferHd)
    Box(modifier, contentAlignment = Alignment.Center) {
        if (image != null) {
            Image(
                bitmap = image,
                contentDescription = word.word,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize().padding(padding.dp),
            )
        } else if (word.emoji.isNotBlank()) {
            Text(
                text = word.emoji,
                fontSize = emojiSize.sp,
                lineHeight = (emojiSize * 1.15f).sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}
