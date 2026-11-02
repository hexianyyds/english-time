package com.feiyan.english.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.feiyan.english.ui.theme.Ink
import com.feiyan.english.ui.theme.InkFaint
import com.feiyan.english.ui.theme.Kaki

/**
 * 内容源切换：细下划线 tab，保持「白」的编辑风格，不用胶囊按钮。
 */
@Composable
fun SourceSwitch(
    labels: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            labels.forEachIndexed { index, label ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onSelect(index) },
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium.copy(letterSpacing = 0.6.sp),
                        color = if (index == selected) Ink else InkFaint,
                        modifier = Modifier.padding(bottom = 7.dp),
                    )
                    Box(
                        Modifier
                            .width(20.dp)
                            .height(1.5.dp)
                            .background(if (index == selected) Kaki else Color.Transparent),
                    )
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Hairline()
    }
}
