package com.feiyan.english.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.feiyan.english.core.util.formatDuration
import com.feiyan.english.data.LibraryItem
import com.feiyan.english.data.LibraryStore
import com.feiyan.english.data.UpdateManager
import com.feiyan.english.ui.update.UpdateViewModel
import com.feiyan.english.ui.components.CoverOrMonogram
import com.feiyan.english.ui.components.Eyebrow
import com.feiyan.english.ui.components.Hairline
import com.feiyan.english.ui.components.LocalBottomOverlayPadding
import com.feiyan.english.ui.icons.FeiyanIcons
import com.feiyan.english.ui.theme.Hairline as HairlineColor2
import com.feiyan.english.ui.theme.Ink
import com.feiyan.english.ui.theme.InkFaint
import com.feiyan.english.ui.theme.InkMuted
import com.feiyan.english.ui.theme.Kaki
import com.feiyan.english.ui.theme.KakiSoft
import com.feiyan.english.ui.theme.PaperCard

@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    onOpen: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val items by LibraryStore.history.collectAsStateWithLifecycle()

    LibraryListScaffold(
        eyebrow = "HISTORY · 播放历史",
        title = "History",
        subtitle = "最近播放的内容，点开即从上次位置继续。",
        action = if (items.isEmpty()) null else "清空" to { LibraryStore.clearHistory() },
        onBack = onBack,
        modifier = modifier,
    ) {
        if (items.isEmpty()) {
            EmptyNote("还没有播放记录，去播客页听一集吧。")
        } else {
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = LocalBottomOverlayPadding.current + 24.dp),
            ) {
                items(items = items, key = { it.id }) { item ->
                    LibraryRow(
                        item = item,
                        progressText = if (item.durationMs > 0) {
                            "已听 ${formatDuration(item.positionMs / 1000)} / ${formatDuration(item.durationMs / 1000)}"
                        } else {
                            null
                        },
                        onClick = { onOpen(item.route) },
                    )
                    Hairline(Modifier.padding(start = 86.dp, end = 24.dp))
                }
            }
        }
    }
}

@Composable
fun FavoritesScreen(
    onBack: () -> Unit,
    onOpen: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val items by LibraryStore.favorites.collectAsStateWithLifecycle()

    LibraryListScaffold(
        eyebrow = "FAVORITES · 我的收藏",
        title = "Saved",
        subtitle = "收藏的节目、书籍与文章都在这里。",
        action = null,
        onBack = onBack,
        modifier = modifier,
    ) {
        if (items.isEmpty()) {
            EmptyNote("还没有收藏。内容页右上角的心形按钮即可收藏。")
        } else {
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = LocalBottomOverlayPadding.current + 24.dp),
            ) {
                items(items = items, key = { it.id }) { item ->
                    LibraryRow(
                        item = item,
                        progressText = null,
                        onClick = { onOpen(item.route) },
                        onRemove = { LibraryStore.removeFavorite(item.id) },
                    )
                    Hairline(Modifier.padding(start = 86.dp, end = 24.dp))
                }
            }
        }
    }
}

@Composable
fun DisclaimerScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LibraryListScaffold(
        eyebrow = "DISCLAIMER · 免责声明",
        title = "免责声明",
        subtitle = "内容来源、使用范围与责任声明",
        action = null,
        onBack = onBack,
        modifier = modifier,
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(bottom = LocalBottomOverlayPadding.current + 32.dp),
        ) {
            DisclaimerBlock(
                title = "一、内容来源",
                body = "本应用是个人学习用途的第三方客户端，自身不生产、不存储任何内容。应用内展示的文字、音频、图片、译文等，均来自公开可访问的第三方网站接口，包括但不限于飞岩播客（feiyanpod.com）、EnglishMeasure（englishmeasure.com）、小宇宙（xiaoyuzhoufm.com）、英语小说网（yingyuxiaoshuo.com）、BookYell（bookyell.com）与 Englio（englio.ai）。所有内容的著作权归原作者与各平台所有。",
            )
            DisclaimerBlock(
                title = "二、使用范围",
                body = "本应用仅供个人学习、研究与交流使用，不得用于任何商业用途，也不得二次分发、转售或批量抓取内容。若你喜欢某个节目、书籍或文章，请前往对应平台支持创作者。",
            )
            DisclaimerBlock(
                title = "三、接口稳定性",
                body = "上述接口均为非官方公开接口，可能随时变更、限流或关闭，届时相关功能可能失效。本应用与这些平台没有任何隶属或合作关系，也不代表其立场。",
            )
            DisclaimerBlock(
                title = "四、账号与隐私",
                body = "你在设置中填写的登录凭证（Cookie / JWT）仅保存在本机，用于向对应站点发起你自己的请求，不会上传到任何自建服务器，也不会被收集。凭证等同账号密码，请勿分享安装包或截图。",
            )
            DisclaimerBlock(
                title = "五、责任声明",
                body = "使用本应用产生的任何后果由使用者自行承担。若权利人认为内容侵权，请通过对应平台联系下架；本应用会在收到通知后停止使用相关接口。",
            )

            Spacer(Modifier.height(12.dp))
            Text(
                text = "ENGLISH TIME",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
                color = InkFaint,
            )
        }
    }
}

/** 关于：版本号 + 检查更新 + 自动更新开关。 */
@Composable
fun AboutAppScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: UpdateViewModel = viewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LibraryListScaffold(
        eyebrow = "ABOUT · 关于",
        title = "关于",
        subtitle = "英语时光 · 播客 / 小说 / 外刊",
        action = null,
        onBack = onBack,
        modifier = modifier,
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(bottom = LocalBottomOverlayPadding.current + 32.dp),
        ) {
            Column(Modifier.padding(horizontal = 24.dp).padding(top = 8.dp)) {
                AboutRow("当前版本", "v${state.currentVersion}")
                AboutRow(
                    "更新仓库",
                    if (UpdateManager.configured) UpdateManager.repo else "未配置",
                )

                Spacer(Modifier.height(20.dp))
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = "自动更新",
                            style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp),
                            color = Ink,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "开启后启动时自动检查、下载并安装新版本",
                            style = MaterialTheme.typography.bodySmall,
                            color = InkFaint,
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Switch(
                        checked = state.autoUpdate,
                        onCheckedChange = viewModel::toggleAutoUpdate,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = PaperCard,
                            checkedTrackColor = Kaki,
                            uncheckedTrackColor = HairlineColor2,
                            uncheckedBorderColor = Color.Transparent,
                        ),
                    )
                }

                Spacer(Modifier.height(22.dp))
                Text(
                    text = when {
                        state.downloading -> "正在下载 ${(state.progress * 100).toInt()}%"
                        state.checking -> "正在检查更新…"
                        else -> "检查更新"
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(letterSpacing = 1.sp),
                    color = Kaki,
                    modifier = Modifier
                        .clip(RoundedCornerShape(2.dp))
                        .background(KakiSoft)
                        .clickable(enabled = !state.checking && !state.downloading) {
                            viewModel.check(silent = false)
                        }
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                )

                if (state.downloading) {
                    Spacer(Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { state.progress },
                        color = Kaki,
                        trackColor = KakiSoft,
                        modifier = Modifier.fillMaxWidth().height(3.dp),
                    )
                }

                state.info?.let { info ->
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = "发现新版本 v${info.version}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Ink,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "立即下载安装",
                        style = MaterialTheme.typography.bodyMedium.copy(letterSpacing = 1.sp),
                        color = PaperCard,
                        modifier = Modifier
                            .clip(RoundedCornerShape(2.dp))
                            .background(Kaki)
                            .clickable { viewModel.startDownload() }
                            .padding(horizontal = 18.dp, vertical = 10.dp),
                    )
                }

                state.status?.let { status ->
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = status,
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.3.sp),
                        color = InkMuted,
                    )
                }
            }
        }
    }
}

@Composable
private fun AboutRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = InkFaint)
        Spacer(Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodySmall, color = Ink)
    }
    Hairline()
}

@Composable
private fun DisclaimerBlock(title: String, body: String) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(top = 20.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp),
            color = Ink,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = body,
            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 22.sp),
            color = InkMuted,
        )
    }
}

@Composable
private fun LibraryListScaffold(
    eyebrow: String,
    title: String,
    subtitle: String,
    action: Pair<String, () -> Unit>?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(start = 8.dp, end = 20.dp, top = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(40.dp).clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = FeiyanIcons.ArrowLeft,
                    contentDescription = "返回",
                    tint = Ink,
                    modifier = Modifier.size(19.dp),
                )
            }
            Spacer(Modifier.width(4.dp))
            Eyebrow(eyebrow, modifier = Modifier.weight(1f))
            action?.let { (label, onClick) ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp),
                    color = Kaki,
                    modifier = Modifier.clickable(onClick = onClick),
                )
            }
        }

        Column(Modifier.padding(horizontal = 24.dp).padding(top = 18.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.displaySmall.copy(fontSize = 30.sp),
                color = Ink,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = InkMuted,
            )
            Spacer(Modifier.height(14.dp))
        }
        Hairline()
        Spacer(Modifier.height(4.dp))
        content()
    }
}

@Composable
private fun LibraryRow(
    item: LibraryItem,
    progressText: String?,
    onClick: () -> Unit,
    onRemove: (() -> Unit)? = null,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CoverOrMonogram(item.cover, item.title, size = 48.dp, corner = 3.dp)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp),
                color = Ink,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(5.dp))
            Eyebrow(
                listOfNotNull(item.subtitle.ifBlank { null }, progressText).joinToString(" · "),
                color = InkFaint,
            )
        }
        if (onRemove != null) {
            Spacer(Modifier.width(10.dp))
            Text(
                text = "移除",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.6.sp),
                color = InkFaint,
                modifier = Modifier.clickable(onClick = onRemove).padding(6.dp),
            )
        }
    }
}

@Composable
private fun EmptyNote(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = InkFaint,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 40.dp),
    )
}
