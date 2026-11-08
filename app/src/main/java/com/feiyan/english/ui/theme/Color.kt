package com.feiyan.english.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/* ============================================================
 * Midnight Studio 双主题色板
 *
 * 静态色值只在这里定义一次，供 Theme.kt 组装 M3 ColorScheme；
 * 界面代码继续使用下方与旧版同名的语义色（Paper / Ink / Kaki…），
 * 它们已改为「跟随主题」的 Composable 读取，无需改动任何调用处。
 * ============================================================ */

// ---- 夜间 · Midnight Studio（Dark OLED）----
val NightBg = Color(0xFF0F0F23)
val NightSurface = Color(0xFF15152B)
val NightCard = Color(0xFF1B1B30)
val NightCardHigh = Color(0xFF222240)
val NightCardHighest = Color(0xFF2A2A4A)
val NightVariant = Color(0xFF23233E)
val NightOnBg = Color(0xFFF8FAFC)
val NightOnBgMuted = Color(0xFFA6B0C3)
val NightOnBgFaint = Color(0xFF6B7590)
val NightLine = Color(0xFF20204C)
val NightAccent = Color(0xFFF97316)
val NightAccentContainer = Color(0xFF33201E)
val NightOnAccent = Color(0xFF160B02)
val NightIndigo = Color(0xFF818CF8)
val NightIndigoContainer = Color(0xFF1F2041)
val NightOnIndigo = Color(0xFFDDE1FF)
val NightError = Color(0xFFEF4444)
val NightOnError = Color(0xFF160B02)

// ---- 日间 · Morning Paper（暖纸白）----
val DayBg = Color(0xFFF6F4EE)
val DaySurface = Color(0xFFFDFCF8)
val DayCard = Color(0xFFFFFFFF)
val DayCardHigh = Color(0xFFF7F4EC)
val DayCardHighest = Color(0xFFF0EBE0)
val DayVariant = Color(0xFFF0EBE0)
val DayOnBg = Color(0xFF25231F)
val DayOnBgMuted = Color(0xFF5D5A50)
val DayOnBgFaint = Color(0xFF928E80)
val DayLine = Color(0xFFDBD8E8)
val DayAccent = Color(0xFFC2410C)
val DayAccentContainer = Color(0xFFF1E2D7)
val DayOnAccent = Color(0xFFFFFFFF)
val DayIndigo = Color(0xFF4F46E5)
val DayIndigoContainer = Color(0xFFE3E4FC)
val DayOnIndigo = Color(0xFF1A1B6C)
val DayError = Color(0xFFB3261E)
val DayOnError = Color(0xFFFFFFFF)

/* ---- 语义色：旧名新义，全 App 的界面代码无需改动 ---- */

/** 页面背景（夜：深夜蓝黑 / 日：暖纸白）。 */
val Paper: Color @Composable get() = MaterialTheme.colorScheme.background

/** 卡片表面。 */
val PaperCard: Color @Composable get() = MaterialTheme.colorScheme.surfaceContainer

/** 更深一档的表面（占位、色块）。 */
val PaperDeep: Color @Composable get() = MaterialTheme.colorScheme.surfaceContainerHigh

/** 封面占位底色。 */
val CoverPlaceholder: Color @Composable get() = MaterialTheme.colorScheme.surfaceContainerHighest

/** 主文字。 */
val Ink: Color @Composable get() = MaterialTheme.colorScheme.onBackground

/** 次级文字。 */
val InkMuted: Color @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant

/** 弱化文字 / 图标。 */
val InkFaint: Color @Composable get() = MaterialTheme.colorScheme.outline

/** 1px 分隔线。 */
val Hairline: Color @Composable get() = MaterialTheme.colorScheme.outlineVariant

/** 更浅的分隔线。 */
val HairlineSoft: Color @Composable get() = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)

/** 强调色（播放、选中、进度）。 */
val Kaki: Color @Composable get() = MaterialTheme.colorScheme.primary

/** 强调色的柔和底（封面占位、徽章）。 */
val KakiSoft: Color @Composable get() = MaterialTheme.colorScheme.primaryContainer

/** 强调色文字（放在 KakiSoft 上）。 */
val KakiDeep: Color @Composable get() = MaterialTheme.colorScheme.onPrimaryContainer

/** 交互靛蓝（链接、次级强调）。 */
val Breeze: Color @Composable get() = MaterialTheme.colorScheme.secondary
