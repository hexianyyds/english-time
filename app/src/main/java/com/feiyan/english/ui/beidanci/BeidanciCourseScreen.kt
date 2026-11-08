package com.feiyan.english.ui.beidanci

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.feiyan.english.core.model.BdCourse
import com.feiyan.english.core.model.BdResume
import com.feiyan.english.core.model.BdUnit
import com.feiyan.english.data.BdProgressStore
import com.feiyan.english.data.BeidanciRepository
import com.feiyan.english.ui.components.Eyebrow
import com.feiyan.english.ui.components.Hairline
import com.feiyan.english.ui.components.LocalBottomOverlayPadding
import com.feiyan.english.ui.icons.FeiyanIcons
import com.feiyan.english.ui.theme.Ink
import com.feiyan.english.ui.theme.InkFaint
import com.feiyan.english.ui.theme.InkMuted
import com.feiyan.english.ui.theme.Kaki
import com.feiyan.english.ui.theme.KakiSoft
import com.feiyan.english.ui.theme.PaperCard

@Composable
fun BeidanciCourseScreen(
    courseKey: String,
    onBack: () -> Unit,
    onStudyClick: (String, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var course by remember(courseKey) { mutableStateOf<BdCourse?>(null) }
    var units by remember(courseKey) { mutableStateOf<List<BdUnit>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var retryToken by remember { mutableIntStateOf(0) }

    LaunchedEffect(courseKey, retryToken) {
        loading = true
        error = null
        runCatching {
            val value = BeidanciRepository.course(courseKey)
                ?: throw IllegalStateException("未找到这本词书")
            value to BeidanciRepository.units(value.id)
        }.onSuccess { (value, list) ->
            course = value
            units = list.filter { !it.membershipLocked }
            BdProgressStore.save(
                context,
                BdResume(
                    courseKey = value.key,
                    courseTitle = value.title,
                    unitId = 0,
                    unitTitle = "",
                ),
            )
        }.onFailure { error = it.message ?: "无法加载词书" }
        loading = false
    }

    Column(modifier.fillMaxSize()) {
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
            Eyebrow("BEIDANCI365 · 词书", modifier = Modifier.weight(1f))
        }
        Hairline()

        when {
            loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        color = Kaki,
                        strokeWidth = 1.4.dp,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "正在加载单元…",
                        style = MaterialTheme.typography.bodySmall,
                        color = InkMuted,
                    )
                }
            }
            error != null -> Column(Modifier.padding(24.dp)) {
                Text(
                    text = error.orEmpty(),
                    style = MaterialTheme.typography.titleSmall,
                    color = Ink,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "重新加载",
                    style = MaterialTheme.typography.labelSmall,
                    color = Kaki,
                    modifier = Modifier.clickable { retryToken++ },
                )
            }
            else -> LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = LocalBottomOverlayPadding.current + 30.dp),
            ) {
                item {
                    Column(Modifier.padding(horizontal = 24.dp)) {
                        Spacer(Modifier.height(20.dp))
                        Eyebrow("WORD BOOK · ${units.size} 个免费单元")
                        Spacer(Modifier.height(7.dp))
                        Text(
                            text = course?.title.orEmpty().ifBlank { "词书" },
                            style = MaterialTheme.typography.displaySmall.copy(fontSize = 30.sp),
                            color = Ink,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "${course?.totalWords ?: units.sumOf { it.totalWords }} 个单词 · 选择单元开始学习",
                            style = MaterialTheme.typography.bodySmall,
                            color = InkMuted,
                        )
                        Spacer(Modifier.height(18.dp))
                    }
                }

                itemsIndexed(units, key = { _, unit -> unit.id }) { index, unit ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onStudyClick(courseKey, unit.id) }
                            .padding(horizontal = 24.dp, vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = (index + 1).toString().padStart(2, '0'),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = InkFaint,
                            modifier = Modifier.width(30.dp),
                        )
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = unit.title.ifBlank { "Unit ${index + 1}" },
                                style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp),
                                color = Ink,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Spacer(Modifier.height(3.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${unit.totalWords} 个单词",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                                    color = Kaki,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(KakiSoft.copy(alpha = 0.5f))
                                        .padding(horizontal = 7.dp, vertical = 3.dp),
                                )
                            }
                        }
                        Icon(
                            imageVector = FeiyanIcons.ChevronRight,
                            contentDescription = null,
                            tint = InkFaint,
                            modifier = Modifier.size(13.dp),
                        )
                    }
                    Hairline(Modifier.padding(horizontal = 24.dp))
                }

                if (units.isEmpty()) {
                    item {
                        Text(
                            text = "这本词书暂无可直接学习的免费单元。",
                            style = MaterialTheme.typography.bodySmall,
                            color = InkFaint,
                            modifier = Modifier.padding(24.dp),
                        )
                    }
                }
            }
        }
    }
}
