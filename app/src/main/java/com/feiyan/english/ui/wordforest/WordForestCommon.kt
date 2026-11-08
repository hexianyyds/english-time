package com.feiyan.english.ui.wordforest

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.feiyan.english.core.model.WfWord
import com.feiyan.english.ui.components.Eyebrow
import com.feiyan.english.ui.icons.FeiyanIcons
import com.feiyan.english.ui.theme.Ink
import com.feiyan.english.ui.theme.InkMuted
import com.feiyan.english.ui.theme.Kaki

@Composable
internal fun WordForestTopBar(
    eyebrow: String,
    onBack: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 20.dp, top = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(44.dp).clickable(onClick = onBack),
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
    }
}

@Composable
internal fun WordForestLoading(text: String) {
    Box(
        Modifier.fillMaxWidth().padding(vertical = 48.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(
                color = Kaki,
                strokeWidth = 1.4.dp,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = InkMuted,
            )
        }
    }
}

@Composable
internal fun WordForestError(
    message: String,
    onRetry: (() -> Unit)? = null,
) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 36.dp)) {
        Text(
            text = "加载失败",
            style = MaterialTheme.typography.titleSmall,
            color = Ink,
        )
        Spacer(Modifier.height(7.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
            color = InkMuted,
        )
        if (onRetry != null) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = "重新加载",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp),
                color = Kaki,
                modifier = Modifier.clickable(onClick = onRetry),
            )
        }
    }
}

internal fun WfWord.translationPreview(): String =
    translations?.meanings?.values
        ?.flatten()
        ?.filter { it.isNotBlank() }
        ?.take(2)
        ?.joinToString("；")
        .orEmpty()

internal fun WfWord.phonetic(): String =
    listOfNotNull(usPhonetic?.takeIf { it.isNotBlank() }, ukPhonetic?.takeIf { it.isNotBlank() })
        .joinToString("  /  ")

internal fun posLabel(key: String): String = when (key.uppercase()) {
    "N" -> "n."
    "V" -> "v."
    "ADJ" -> "adj."
    "ADV" -> "adv."
    "PREP" -> "prep."
    "CONJ" -> "conj."
    "PRON" -> "pron."
    "NUM" -> "num."
    "ART" -> "art."
    "INT" -> "int."
    else -> key.lowercase()
}
