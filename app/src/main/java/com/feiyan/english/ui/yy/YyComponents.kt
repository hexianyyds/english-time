package com.feiyan.english.ui.yy

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.feiyan.english.core.net.YyApi
import com.feiyan.english.ui.theme.KakiSoft

/** 书籍封面（3:4 竖版），接口只给 id，需要拼 /resource/cover。 */
@Composable
fun BookCover(
    id: String,
    width: Dp,
    height: Dp,
    modifier: Modifier = Modifier,
    corner: Dp = 3.dp,
) {
    Box(
        modifier
            .size(width, height)
            .clip(RoundedCornerShape(corner))
            .background(KakiSoft),
    ) {
        AsyncImage(
            model = YyApi.coverUrl(id),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(width, height),
        )
    }
}
