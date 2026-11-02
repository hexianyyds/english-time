package com.feiyan.english.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.feiyan.english.data.LibraryStore
import com.feiyan.english.ui.components.Eyebrow
import com.feiyan.english.ui.components.Hairline
import com.feiyan.english.ui.components.LocalBottomOverlayPadding
import com.feiyan.english.ui.icons.FeiyanIcons
import com.feiyan.english.ui.theme.Ink
import com.feiyan.english.ui.theme.InkFaint
import com.feiyan.english.ui.theme.InkMuted
import com.feiyan.english.ui.theme.Kaki
import com.feiyan.english.ui.theme.KakiSoft

@Composable
fun LibraryScreen(
    onSettingsClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onAboutClick: () -> Unit,
    onDisclaimerClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val history by LibraryStore.history.collectAsStateWithLifecycle()
    val favorites by LibraryStore.favorites.collectAsStateWithLifecycle()

    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
    ) {
        Spacer(Modifier.height(28.dp))
        Eyebrow("LIBRARY")
        Spacer(Modifier.height(18.dp))
        Text(
            text = "Your shelf",
            style = MaterialTheme.typography.displaySmall.copy(fontSize = 32.sp),
            color = Ink,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "播放记录、收藏与下载都会留在这里。",
            style = MaterialTheme.typography.bodyMedium,
            color = InkMuted,
        )
        Spacer(Modifier.height(34.dp))

        LibraryRow(
            "播放历史",
            if (history.isEmpty()) "还没有播放记录" else "已记录 ${history.size} 条，点开继续",
            onClick = onHistoryClick,
        )
        LibraryRow(
            "我的收藏",
            if (favorites.isEmpty()) "收藏喜欢的节目、书籍与文章" else "已收藏 ${favorites.size} 条",
            onClick = onFavoritesClick,
        )
        LibraryRow("设置", "Cookie 登录、播放与字幕偏好", onClick = onSettingsClick)
        LibraryRow("关于", "版本与更新", onClick = onAboutClick)
        LibraryRow(
            "免责声明",
            "内容来源、使用范围与责任声明",
            onClick = onDisclaimerClick,
        )

        Spacer(Modifier.height(40.dp))
        Eyebrow("ENGLISH TIME · 2026", color = InkFaint)
        Spacer(Modifier.height(28.dp + LocalBottomOverlayPadding.current))
    }
}

@Composable
private fun LibraryRow(title: String, subtitle: String, onClick: () -> Unit = {}) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 16.sp),
                color = Ink,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = InkFaint,
            )
        }
        Box(Modifier.size(24.dp), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = FeiyanIcons.ChevronRight,
                contentDescription = null,
                tint = InkFaint,
                modifier = Modifier.size(14.dp),
            )
        }
    }
    Hairline()
}
