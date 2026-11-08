package com.feiyan.english.ui.update

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.feiyan.english.ui.components.Eyebrow
import com.feiyan.english.ui.components.Hairline
import com.feiyan.english.ui.theme.Hairline as HairlineColor
import com.feiyan.english.ui.theme.Ink
import com.feiyan.english.ui.theme.InkFaint
import com.feiyan.english.ui.theme.InkMuted
import com.feiyan.english.ui.theme.Kaki
import com.feiyan.english.ui.theme.KakiSoft
import com.feiyan.english.ui.theme.PaperCard

/** 版本更新弹窗：发现新版 / 已是最新 / 下载进度。 */
@Composable
fun UpdateDialog(
    state: UpdateUiState,
    onUpdate: () -> Unit,
    onDismiss: () -> Unit,
) {
    if (!state.dialogVisible) return

    val info = state.info
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0x66000000))
            .clickable(enabled = !state.downloading, onClick = onDismiss),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier
                .padding(horizontal = 32.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(PaperCard)
                .border(0.8.dp, HairlineColor, RoundedCornerShape(16.dp))
                .clickable(enabled = false) {}
                .padding(22.dp),
        ) {
            Eyebrow(if (info != null) "UPDATE · 发现新版本" else "UPDATE · 版本检查")
            Spacer(Modifier.height(12.dp))
            Text(
                text = if (info != null) "v${info.version}" else state.status ?: "已是最新版本",
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp),
                color = Ink,
            )
            if (info != null) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "当前版本 v${state.currentVersion}",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkFaint,
                )
                if (info.notes.isNotBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = info.notes,
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                        color = InkMuted,
                        maxLines = 5,
                    )
                }
            }
            Spacer(Modifier.height(18.dp))
            Hairline()
            Spacer(Modifier.height(16.dp))

            if (state.downloading) {
                Text(
                    text = "正在下载 ${(state.progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.6.sp),
                    color = Kaki,
                )
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { state.progress },
                    color = Kaki,
                    trackColor = KakiSoft,
                    modifier = Modifier.fillMaxWidth().height(3.dp),
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (info != null) "立即更新" else "知道了",
                        style = MaterialTheme.typography.bodyMedium.copy(letterSpacing = 1.sp),
                        color = Kaki,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(KakiSoft)
                            .clickable(onClick = if (info != null) onUpdate else onDismiss)
                            .padding(horizontal = 18.dp, vertical = 12.dp),
                    )
                    Spacer(Modifier.width(12.dp))
                    if (info != null) {
                        Text(
                            text = "稍后",
                            style = MaterialTheme.typography.bodyMedium.copy(letterSpacing = 1.sp),
                            color = InkMuted,
                            modifier = Modifier
                                .clickable(onClick = onDismiss)
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                        )
                    }
                }
            }
            if (state.status != null && info != null && !state.downloading) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = state.status,
                    style = MaterialTheme.typography.labelSmall,
                    color = InkFaint,
                )
            }
        }
    }
}
