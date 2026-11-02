package com.feiyan.english.ui.settings

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.feiyan.english.ui.components.Eyebrow
import com.feiyan.english.ui.components.Hairline
import com.feiyan.english.ui.icons.FeiyanIcons
import com.feiyan.english.ui.theme.Hairline as HairlineColor
import com.feiyan.english.ui.theme.Ink
import com.feiyan.english.ui.theme.InkFaint
import com.feiyan.english.ui.theme.InkMuted
import com.feiyan.english.ui.theme.Kaki
import com.feiyan.english.ui.theme.KakiSoft

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: SettingsViewModel = viewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(top = 6.dp),
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
            Eyebrow("SETTINGS · 设置")
        }

        Spacer(Modifier.height(24.dp))
        Text(
            text = "Settings",
            style = MaterialTheme.typography.displaySmall.copy(fontSize = 32.sp),
            color = Ink,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "登录态只保存在本机，用来解锁完整字幕与朗读能力。",
            style = MaterialTheme.typography.bodyMedium,
            color = InkMuted,
        )

        Spacer(Modifier.height(28.dp))
        CookieSection(
            title = "飞岩播客 Cookie",
            hint = "feiyanpod.com 登录后，开发者工具 → Application → Cookies，复制 token 的值。",
            placeholder = "粘贴 token 值",
            credential = state.feiyan,
            onChange = viewModel::onFeiyanChange,
            onSave = viewModel::saveFeiyan,
            onClear = viewModel::clearFeiyan,
            onVerify = viewModel::verifyFeiyan,
        )

        Spacer(Modifier.height(30.dp))

        CookieSection(
            title = "BookYell 登录（朗读）",
            hint = "bookyell.com 登录后从 Cookie / localStorage 复制 JWT（eyJ... 开头的长字符串）。登录后才能使用文本朗读。",
            placeholder = "粘贴 eyJ... 开头的 JWT",
            credential = state.bookyell,
            onChange = viewModel::onBookyellChange,
            onSave = viewModel::saveBookyell,
            onClear = viewModel::clearBookyell,
            onVerify = viewModel::verifyBookyell,
        )

        Spacer(Modifier.height(36.dp))
        Text(
            text = "Cookie / JWT 等同账号凭证，请勿分享安装包或截图。",
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.2.sp),
            color = InkFaint,
        )
        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun CookieSection(
    title: String,
    hint: String,
    placeholder: String,
    credential: CredentialUiState,
    onChange: (String) -> Unit,
    onSave: () -> Unit,
    onClear: () -> Unit,
    onVerify: () -> Unit,
) {
    Column(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 16.sp),
                color = Ink,
            )
            Spacer(Modifier.weight(1f))
            if (credential.busy) {
                CircularProgressIndicator(
                    color = Kaki,
                    strokeWidth = 1.2.dp,
                    modifier = Modifier.size(12.dp),
                )
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = credential.summary
                    ?: when {
                        credential.error != null -> "校验失败"
                        credential.saved.isBlank() -> "未登录"
                        else -> "未校验"
                    },
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.4.sp),
                color = when {
                    credential.summary != null -> Kaki
                    credential.error != null -> InkMuted
                    else -> InkFaint
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.height(12.dp))

        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(3.dp))
                .background(KakiSoft.copy(alpha = 0.28f))
                .border(0.8.dp, HairlineColor, RoundedCornerShape(3.dp))
                .padding(horizontal = 14.dp, vertical = 13.dp),
        ) {
            BasicTextField(
                value = credential.input,
                onValueChange = onChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall.copy(
                    color = Ink,
                    fontSize = 12.5.sp,
                    letterSpacing = 0.2.sp,
                ),
                cursorBrush = SolidColor(Kaki),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { inner ->
                    Box {
                        if (credential.input.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = MaterialTheme.typography.bodySmall,
                                color = InkFaint,
                            )
                        }
                        inner()
                    }
                },
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = hint,
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.2.sp),
            color = InkFaint,
        )
        Spacer(Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "保存并校验",
                style = MaterialTheme.typography.bodyMedium.copy(letterSpacing = 1.sp),
                color = Kaki,
                modifier = Modifier
                    .clip(RoundedCornerShape(2.dp))
                    .background(KakiSoft)
                    .clickable(enabled = !credential.busy, onClick = onSave)
                    .padding(horizontal = 18.dp, vertical = 12.dp),
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = "清除",
                style = MaterialTheme.typography.bodyMedium.copy(letterSpacing = 1.sp),
                color = if (credential.saved.isBlank()) InkFaint else InkMuted,
                modifier = Modifier
                    .clip(RoundedCornerShape(2.dp))
                    .clickable(enabled = credential.saved.isNotBlank(), onClick = onClear)
                    .padding(horizontal = 12.dp, vertical = 12.dp),
            )
        }

        if (credential.error != null) {
            Spacer(Modifier.height(10.dp))
            Text(
                text = "${credential.error} · 点此重新校验",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.2.sp),
                color = Kaki,
                modifier = Modifier.clickable(onClick = onVerify),
            )
        }

        if (credential.rows.isNotEmpty()) {
            Spacer(Modifier.height(18.dp))
            credential.rows.forEach { (label, value) ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(label, style = MaterialTheme.typography.bodySmall, color = InkFaint)
                    Spacer(Modifier.weight(1f))
                    Text(value, style = MaterialTheme.typography.bodySmall, color = Ink)
                }
                Hairline()
            }
        }
    }
}
