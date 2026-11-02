package com.feiyan.english.ui.em

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.MeshGradientPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.feiyan.english.core.model.EmCategories
import com.feiyan.english.ui.theme.Hairline
import com.feiyan.english.ui.theme.Ink
import com.feiyan.english.ui.theme.Kaki
import com.feiyan.english.ui.theme.KakiSoft
import com.feiyan.english.ui.theme.Paper
import com.feiyan.english.ui.theme.PaperDeep

/**
 * EnglishMeasure 没有封面图，用「排版即封面」的纸质方块代替。
 */
@Composable
fun EmCover(
    category: String,
    title: String,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    val large = size >= 100.dp
    // Compose 1.12 的 MeshGradientPainter：纸质渐变，比纯色块更有层次
    val mesh = remember(category) { paperMeshPainter() }
    Box(
        modifier
            .size(size)
            .clip(RoundedCornerShape(3.dp))
            .border(0.8.dp, Hairline, RoundedCornerShape(3.dp)),
    ) {
        Image(
            painter = mesh,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize(),
        )
        Column(Modifier.fillMaxSize().padding(if (large) 12.dp else 10.dp)) {
            Text(
                text = EmCategories.label(category),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = if (large) 9.sp else 8.sp,
                    letterSpacing = 1.2.sp,
                ),
                color = Kaki,
                maxLines = 1,
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontSize = if (large) 13.sp else 11.sp,
                    lineHeight = if (large) 18.sp else 15.sp,
                ),
                color = Ink,
                maxLines = if (large) 3 else 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * 纸色网格渐变：PaperDeep → Paper → KakiSoft 的极弱过渡。
 */
private fun paperMeshPainter(): MeshGradientPainter =
    MeshGradientPainter(rows = 2, columns = 2) {
        setVertex(0, 0, Offset(0f, 0f), PaperDeep)
        setVertex(0, 1, Offset(1f, 0f), Paper)
        setVertex(1, 0, Offset(0f, 1f), Paper)
        setVertex(1, 1, Offset(1f, 1f), KakiSoft)
    }
