package com.feiyan.english.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.feiyan.english.core.net.FeiyanApi
import com.feiyan.english.data.LibraryStore
import com.feiyan.english.ui.components.Eyebrow
import com.feiyan.english.ui.components.LocalBottomOverlayPadding
import com.feiyan.english.ui.components.ThemeToggleButton
import com.feiyan.english.ui.icons.FeiyanIcons
import com.feiyan.english.ui.theme.Breeze
import com.feiyan.english.ui.theme.Hairline
import com.feiyan.english.ui.theme.Ink
import com.feiyan.english.ui.theme.InkFaint
import com.feiyan.english.ui.theme.InkMuted
import com.feiyan.english.ui.theme.Kaki
import com.feiyan.english.ui.theme.PaperCard

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
    val signedIn = rememberSignedIn()

    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp),
    ) {
        Spacer(Modifier.height(22.dp))
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Eyebrow("LIBRARY", modifier = Modifier.weight(1f))
            ThemeToggleButton()
        }
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
        Spacer(Modifier.height(26.dp))

        // ---- 身份卡 ----
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(PaperCard)
                .border(1.dp, Hairline, RoundedCornerShape(20.dp))
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Breeze.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "E",
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 24.sp),
                    color = Breeze,
                )
            }
            Spacer(Modifier.width(15.dp))
            Column {
                Text(
                    text = "English Learner",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
                    color = Ink,
                )
                Spacer(Modifier.height(5.dp))
                Eyebrow(
                    if (signedIn) "SIGNED IN · 飞岩已登录" else "GUEST MODE · 游客模式",
                    color = InkMuted,
                )
            }
        }

        Spacer(Modifier.height(13.dp))

        // ---- 数据统计 ----
        Row(horizontalArrangement = Arrangement.spacedBy(11.dp)) {
            StatCard(favorites.size.toString(), "收藏", Modifier.weight(1f))
            StatCard(history.size.toString(), "记录", Modifier.weight(1f))
            StatCard("11", "内容源", Modifier.weight(1f))
        }

        Spacer(Modifier.height(13.dp))

        // ---- 菜单 ----
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(PaperCard)
                .border(1.dp, Hairline, RoundedCornerShape(18.dp)),
        ) {
            MenuRow(FeiyanIcons.Heart, "我的收藏", onClick = onFavoritesClick)
            MenuRow(FeiyanIcons.Clock, "播放历史", onClick = onHistoryClick)
            MenuRow(FeiyanIcons.Gear, "设置", onClick = onSettingsClick)
            MenuRow(FeiyanIcons.Info, "关于", onClick = onAboutClick)
            MenuRow(FeiyanIcons.AlertTriangle, "免责声明", onClick = onDisclaimerClick, last = true)
        }

        Spacer(Modifier.height(32.dp))
        Eyebrow("ENGLISH TIME · 2026", color = InkFaint)
        Spacer(Modifier.height(24.dp + LocalBottomOverlayPadding.current))
    }
}

@Composable
private fun rememberSignedIn(): Boolean = FeiyanApi.isLoggedIn

@Composable
private fun StatCard(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(PaperCard)
            .border(1.dp, Hairline, RoundedCornerShape(16.dp))
            .padding(vertical = 14.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                fontSize = 23.sp,
            ),
            color = Kaki,
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = InkMuted,
        )
    }
}

@Composable
private fun MenuRow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit = {},
    last: Boolean = false,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 14.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(Breeze.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Breeze,
                modifier = Modifier.size(17.dp),
            )
        }
        Spacer(Modifier.width(13.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
            color = Ink,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = FeiyanIcons.ChevronRight,
            contentDescription = null,
            tint = InkFaint,
            modifier = Modifier.size(14.dp),
        )
    }
    if (!last) {
        Box(
            Modifier
                .fillMaxWidth()
                .padding(start = 63.dp)
                .height(1.dp)
                .background(Hairline.copy(alpha = 0.6f)),
        )
    }
}
