package com.feiyan.english.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.feiyan.english.data.ThemeStore
import com.feiyan.english.ui.icons.FeiyanIcons
import com.feiyan.english.ui.theme.Hairline
import com.feiyan.english.ui.theme.Ink
import com.feiyan.english.ui.theme.PaperCard

/** 一键切换日间 / 夜间模式，按钮显示将要切换到的模式。 */
@Composable
fun ThemeToggleButton(
    modifier: Modifier = Modifier,
    showLabel: Boolean = true,
) {
    val isDark by ThemeStore.isDark.collectAsStateWithLifecycle()
    val targetLabel = if (isDark) "日间" else "夜间"
    val description = "切换到${targetLabel}模式"

    Row(
        modifier
            .height(44.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(PaperCard)
            .border(1.dp, Hairline, RoundedCornerShape(22.dp))
            .clickable(role = Role.Button, onClickLabel = description) { ThemeStore.toggle() }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (isDark) FeiyanIcons.Sun else FeiyanIcons.Moon,
            contentDescription = description,
            tint = Ink,
            modifier = Modifier.size(16.dp),
        )
        if (showLabel) {
            Spacer(Modifier.width(6.dp))
            Text(
                text = targetLabel,
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 0.8.sp,
                    fontSize = 10.sp,
                ),
                color = Ink,
            )
        }
    }
}
