package com.feiyan.english.ui.beidanci

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.feiyan.english.core.model.BdCourse
import com.feiyan.english.data.BdProgressStore
import com.feiyan.english.data.BeidanciRepository
import com.feiyan.english.ui.components.Eyebrow
import com.feiyan.english.ui.components.LocalBottomOverlayPadding
import com.feiyan.english.ui.theme.Ink
import com.feiyan.english.ui.theme.InkFaint
import com.feiyan.english.ui.theme.InkMuted
import com.feiyan.english.ui.theme.Kaki
import com.feiyan.english.ui.theme.KakiSoft
import com.feiyan.english.ui.theme.PaperCard

@Composable
fun BeidanciScreen(
    onCourseClick: (String) -> Unit,
    onStudyClick: (String, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var courses by remember { mutableStateOf<List<BdCourse>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var retryToken by remember { mutableIntStateOf(0) }
    val resume by BdProgressStore.resume.collectAsStateWithLifecycle()

    LaunchedEffect(retryToken) {
        loading = true
        error = null
        runCatching { BeidanciRepository.courses(force = retryToken > 0) }
            .onSuccess { courses = it }
            .onFailure { error = it.message ?: "课程目录加载失败" }
        loading = false
    }

    val currentResume = resume
    val startLearning = {
        when {
            currentResume != null && currentResume.unitId > 0 -> {
                onStudyClick(currentResume.courseKey, currentResume.unitId)
            }
            currentResume != null -> onCourseClick(currentResume.courseKey)
            courses.isNotEmpty() -> onCourseClick(courses.first().key)
            else -> Unit
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = LocalBottomOverlayPadding.current + 30.dp),
    ) {
        item {
            Column(Modifier.padding(horizontal = 24.dp)) {
                Spacer(Modifier.height(16.dp))
                Eyebrow("BEIDANCI365 · 原生学习")
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "365 背单词",
                    style = MaterialTheme.typography.displaySmall.copy(fontSize = 30.sp),
                    color = Ink,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "词书、单元和单词学习全部原生呈现，无需登录。",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkMuted,
                )
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    BdStat(
                        (if (courses.isEmpty()) 33 else courses.size).toString(),
                        "本词书",
                        Modifier.weight(1f),
                    )
                    BdStat(
                        (if (courses.isEmpty()) 120067 else courses.sumOf { it.totalWords }).toString(),
                        "词条",
                        Modifier.weight(1f),
                    )
                    BdStat(
                        (if (courses.isEmpty()) 2425 else courses.sumOf { it.units.size }).toString(),
                        "单元",
                        Modifier.weight(1f),
                    )
                }
                Spacer(Modifier.height(16.dp))
            }
        }

        item {
            Column(
                Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(19.dp))
                    .background(Kaki)
                    .clickable(onClick = startLearning)
                    .padding(horizontal = 18.dp, vertical = 17.dp),
            ) {
                Eyebrow(
                    if (currentResume != null) "CONTINUE · 继续学习" else "START · 开始学习",
                    color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.72f),
                )
                Spacer(Modifier.height(7.dp))
                Text(
                    text = when {
                        currentResume == null -> "开始背单词"
                        currentResume.unitId > 0 -> "${currentResume.courseTitle} · ${currentResume.unitTitle}"
                        else -> currentResume.courseTitle
                    },
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = androidx.compose.ui.graphics.Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "学习记录仅保存在本机，可随时从上次位置继续。",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.78f),
                    ),
                )
            }
        }

        item {
            Column(Modifier.padding(horizontal = 24.dp)) {
                Spacer(Modifier.height(24.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Eyebrow("WORD BOOKS · 词书")
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = "33 本",
                        style = MaterialTheme.typography.labelSmall,
                        color = InkFaint,
                    )
                }
                Spacer(Modifier.height(12.dp))
            }
        }

        if (loading) {
            item {
                Text(
                    text = "正在读取课程目录…",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkFaint,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp),
                )
            }
        } else if (error != null) {
            item {
                Column(Modifier.padding(horizontal = 24.dp)) {
                    Text(
                        text = error.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = InkMuted,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "重新加载",
                        style = MaterialTheme.typography.labelSmall,
                        color = Kaki,
                        modifier = Modifier.clickable { retryToken++ },
                    )
                }
            }
        } else {
            item {
                Column(Modifier.padding(horizontal = 24.dp)) {
                    courses.chunked(2).forEach { rowCourses ->
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            rowCourses.forEach { course ->
                                CourseGridCard(
                                    course = course,
                                    onClick = { onCourseClick(course.key) },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            if (rowCourses.size == 1) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                    }
                }
            }
        }

        item {
            Text(
                text = "词书与单词数据来自 beidanci365.com 公开接口，不包含登录、会员和云端同步功能。",
                style = MaterialTheme.typography.labelSmall.copy(lineHeight = 17.sp),
                color = InkFaint,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
            )
        }
    }
}

@Composable
private fun BdStat(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(PaperCard)
            .border(0.8.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
            .padding(vertical = 11.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 19.sp),
            color = Kaki,
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = InkMuted,
        )
    }
}

@Composable
private fun CourseGridCard(
    course: BdCourse,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .height(105.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(PaperCard)
            .border(0.8.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(15.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 11.dp),
    ) {
        Text(
            text = course.title,
            style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.5.sp),
            color = Ink,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.weight(1f))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${course.totalWords} 词",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                color = Kaki,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(KakiSoft.copy(alpha = 0.55f))
                    .padding(horizontal = 6.dp, vertical = 3.dp),
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "${course.units.size} 单元",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = InkFaint,
                textAlign = TextAlign.End,
            )
        }
    }
}
