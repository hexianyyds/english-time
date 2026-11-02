package com.feiyan.english.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FeiyanColorScheme = lightColorScheme(
    primary = Kaki,
    onPrimary = Color.White,
    primaryContainer = KakiSoft,
    onPrimaryContainer = KakiDeep,
    secondary = Ink,
    onSecondary = Paper,
    secondaryContainer = PaperCard,
    onSecondaryContainer = Ink,
    tertiary = InkMuted,
    onTertiary = Paper,
    background = Paper,
    onBackground = Ink,
    surface = PaperCard,
    onSurface = Ink,
    surfaceVariant = KakiSoft,
    onSurfaceVariant = InkMuted,
    surfaceContainer = PaperCard,
    surfaceContainerHigh = PaperCard,
    outline = Hairline,
    outlineVariant = HairlineSoft,
    error = Color(0xFFB3261E),
    onError = Color.White,
)

@Composable
fun FeiyanTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = FeiyanColorScheme,
        typography = FeiyanTypography,
        content = content,
    )
}
