package com.feiyan.english.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** 夜间 · Midnight Studio：深夜蓝黑 + 暖橙强调 + 靛蓝交互。 */
private val MidnightColorScheme = darkColorScheme(
    primary = NightAccent,
    onPrimary = NightOnAccent,
    primaryContainer = NightAccentContainer,
    onPrimaryContainer = Color(0xFFFDBA74),
    secondary = NightIndigo,
    onSecondary = Color(0xFF10102A),
    secondaryContainer = NightIndigoContainer,
    onSecondaryContainer = NightOnIndigo,
    tertiary = Color(0xFF2DD4BF),
    onTertiary = Color(0xFF04211C),
    background = NightBg,
    onBackground = NightOnBg,
    surface = NightSurface,
    onSurface = NightOnBg,
    surfaceVariant = NightVariant,
    onSurfaceVariant = NightOnBgMuted,
    surfaceContainer = NightCard,
    surfaceContainerHigh = NightCardHigh,
    surfaceContainerHighest = NightCardHighest,
    surfaceContainerLow = Color(0xFF17172E),
    surfaceContainerLowest = Color(0xFF12122A),
    outline = NightOnBgFaint,
    outlineVariant = NightLine,
    error = NightError,
    onError = NightOnError,
    errorContainer = Color(0xFF3A1212),
    onErrorContainer = Color(0xFFFCA5A5),
    inverseSurface = DayBg,
    inverseOnSurface = DayOnBg,
    inversePrimary = DayAccent,
    scrim = Color(0xFF000000),
)

/** 日间 · Morning Paper：暖纸白 + 橙红强调 + 靛蓝交互。 */
private val MorningColorScheme = lightColorScheme(
    primary = DayAccent,
    onPrimary = DayOnAccent,
    primaryContainer = DayAccentContainer,
    onPrimaryContainer = Color(0xFF7C2D12),
    secondary = DayIndigo,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = DayIndigoContainer,
    onSecondaryContainer = DayOnIndigo,
    tertiary = Color(0xFF0F766E),
    onTertiary = Color(0xFFFFFFFF),
    background = DayBg,
    onBackground = DayOnBg,
    surface = DaySurface,
    onSurface = DayOnBg,
    surfaceVariant = DayVariant,
    onSurfaceVariant = DayOnBgMuted,
    surfaceContainer = DayCard,
    surfaceContainerHigh = DayCardHigh,
    surfaceContainerHighest = DayCardHighest,
    surfaceContainerLow = Color(0xFFFBF9F3),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    outline = DayOnBgFaint,
    outlineVariant = DayLine,
    error = DayError,
    onError = DayOnError,
    errorContainer = Color(0xFFF7DBD6),
    onErrorContainer = Color(0xFF5F1210),
    inverseSurface = NightBg,
    inverseOnSurface = NightOnBg,
    inversePrimary = NightAccent,
    scrim = Color(0xFF000000),
)

/**
 * 双主题入口：跟随系统深色模式自动切换。
 * 夜间 Midnight Studio / 日间 Morning Paper 共用一套语义色。
 */
@Composable
fun FeiyanTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) MidnightColorScheme else MorningColorScheme,
        typography = FeiyanTypography,
        content = content,
    )
}
