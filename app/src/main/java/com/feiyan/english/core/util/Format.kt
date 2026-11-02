package com.feiyan.english.core.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val CDN = "https://cdn.feiyanpod.com"

private val monthDay = DateTimeFormatter.ofPattern("M月d日", Locale.CHINA)
private val yearMonthDay = DateTimeFormatter.ofPattern("yyyy年M月d日", Locale.CHINA)

/** 封面地址：接口返回相对路径，需拼 CDN 与图片处理样式。 */
fun coverUrl(path: String?, style: String = "middle"): String? {
    if (path.isNullOrBlank()) return null
    if (path.startsWith("http")) return path
    return "$CDN$path?x-oss-process=style/$style"
}

/** 秒 -> mm:ss / h:mm:ss */
fun formatDuration(seconds: Long): String {
    if (seconds <= 0) return "00:00"
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) String.format(Locale.US, "%d:%02d:%02d", h, m, s)
    else String.format(Locale.US, "%02d:%02d", m, s)
}

/** 毫秒 -> mm:ss */
fun formatPosition(millis: Long): String = formatDuration(millis / 1000)

fun formatEpisodeDate(millis: Long): String {
    if (millis <= 0) return ""
    val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
    val now = java.time.LocalDate.now()
    return if (date.year == now.year) date.format(monthDay) else date.format(yearMonthDay)
}

/** ISO-8601（2026-07-02T08:48:18.622Z）-> 2026.07.02 */
fun formatIsoDate(iso: String?): String {
    if (iso.isNullOrBlank()) return ""
    return runCatching {
        val date = Instant.parse(iso).atZone(ZoneId.systemDefault()).toLocalDate()
        String.format(Locale.US, "%04d.%02d.%02d", date.year, date.monthValue, date.dayOfMonth)
    }.getOrDefault("")
}

fun formatCount(count: Long): String = when {
    count >= 10_000 -> String.format(Locale.US, "%.1f 万", count / 10_000f)
    count >= 1_000 -> String.format(Locale.US, "%.1f 千", count / 1_000f)
    else -> count.toString()
}
