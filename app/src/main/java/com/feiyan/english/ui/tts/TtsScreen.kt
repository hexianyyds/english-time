package com.feiyan.english.ui.tts

import android.media.MediaPlayer
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.feiyan.english.data.EnttsNote
import com.feiyan.english.data.EnttsRepository
import com.feiyan.english.data.TtsSpeed
import com.feiyan.english.data.TtsVoice
import com.feiyan.english.ui.components.Eyebrow
import com.feiyan.english.ui.components.Hairline
import com.feiyan.english.ui.components.LocalBottomOverlayPadding
import com.feiyan.english.ui.icons.FeiyanIcons
import com.feiyan.english.ui.theme.Ink
import com.feiyan.english.ui.theme.InkFaint
import com.feiyan.english.ui.theme.InkMuted
import com.feiyan.english.ui.theme.Kaki
import com.feiyan.english.ui.theme.KakiSoft
import com.feiyan.english.ui.theme.Paper
import com.feiyan.english.ui.theme.PaperCard
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Composable
fun TtsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var text by rememberSaveable {
        mutableStateOf("How to use: Enter the text to be read aloud here, and click the Start Reading button below.")
    }
    var voiceId by rememberSaveable { mutableStateOf("en") }
    var speed by rememberSaveable { mutableIntStateOf(0) }
    var sentences by remember { mutableStateOf<List<String>>(emptyList()) }
    var notes by remember { mutableStateOf<Map<String, EnttsNote>>(emptyMap()) }
    var note by remember { mutableStateOf<EnttsNote?>(null) }
    var segments by remember { mutableStateOf<List<String>>(emptyList()) }
    var currentSegment by remember { mutableIntStateOf(-1) }
    var status by remember { mutableStateOf("输入文本后开始朗读") }
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    var job by remember { mutableStateOf<Job?>(null) }
    var playbackToken by remember { mutableIntStateOf(0) }

    val voice = EnttsRepository.voices.firstOrNull { it.id == voiceId }
        ?: EnttsRepository.voices.first()
    val speedOption = EnttsRepository.speeds.firstOrNull { it.value == speed }
        ?: EnttsRepository.speeds[3]

    LaunchedEffect(context) {
        runCatching { EnttsRepository.sentences(context) }
            .onSuccess { sentences = it }
        runCatching { EnttsRepository.notes(context) }
            .onSuccess { notes = it }
    }

    fun stopReading() {
        playbackToken++
        job?.cancel()
        job = null
        busy = false
        currentSegment = -1
        status = "已停止"
    }

    fun startReading(startAt: Int = 0) {
        val content = text.trim()
        if (content.isEmpty()) {
            error = "请先输入要朗读的文本"
            return
        }
        if (content.length > 30000) {
            error = "单次朗读最多 30000 个字符"
            return
        }
        playbackToken++
        val token = playbackToken
        job?.cancel()
        error = null
        segments = emptyList()
        currentSegment = -1
        busy = true
        status = "正在准备朗读…"

        job = scope.launch {
            try {
                val chunks = EnttsRepository.split(content, voice.id, speed)
                if (token != playbackToken) return@launch
                segments = chunks
                val key = EnttsRepository.key(voice.id, speed)
                if (token != playbackToken) return@launch

                for (index in startAt until chunks.size) {
                    if (!isActive || token != playbackToken) return@launch
                    currentSegment = index
                    status = "正在生成第 ${index + 1} / ${chunks.size} 段…"
                    val audio = EnttsRepository.audio(
                        context = context,
                        text = chunks[index],
                        voice = voice.id,
                        speed = speed,
                        key = key,
                    )
                    if (token != playbackToken) return@launch
                    status = "正在朗读 ${index + 1} / ${chunks.size}"
                    playMp3(audio)
                }
                if (token == playbackToken) {
                    status = "朗读完成"
                    currentSegment = -1
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (token == playbackToken) {
                    error = e.message ?: "朗读失败"
                    status = "朗读失败"
                    currentSegment = -1
                }
            } finally {
                if (token == playbackToken) busy = false
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            playbackToken++
            job?.cancel()
        }
    }

    Column(modifier.fillMaxSize().background(Paper)) {
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
            Eyebrow("ENTTS · 文本朗读", modifier = Modifier.weight(1f))
        }
        Hairline()

        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 24.dp,
                end = 24.dp,
                top = 18.dp,
                bottom = LocalBottomOverlayPadding.current + 36.dp,
            ),
        ) {
            item {
                Text(
                    text = "英语朗读",
                    style = MaterialTheme.typography.displaySmall.copy(fontSize = 30.sp),
                    color = Ink,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "22 种声音 · 7 档语速 · 长文本自动分段",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkMuted,
                )
                Spacer(Modifier.height(18.dp))
            }

            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Eyebrow("TEXT · 朗读文本")
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = "清空",
                        style = MaterialTheme.typography.labelSmall,
                        color = InkFaint,
                        modifier = Modifier
                            .clickable(enabled = !busy) {
                                text = ""
                                note = null
                                segments = emptyList()
                                error = null
                                status = "输入文本后开始朗读"
                            }
                            .padding(6.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "随机句子",
                        style = MaterialTheme.typography.labelSmall,
                        color = Kaki,
                        modifier = Modifier
                            .clickable(enabled = !busy && sentences.isNotEmpty()) {
                                val next = sentences.random()
                                text = next
                                note = notes[next]
                                segments = emptyList()
                                error = null
                                status = "已随机选择一句"
                            }
                            .padding(6.dp),
                    )
                }
                Spacer(Modifier.height(8.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 150.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(PaperCard)
                        .border(0.8.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                ) {
                    if (text.isEmpty()) {
                        Text(
                            text = "输入要朗读的英文、中文或其他语言…",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 15.sp,
                                lineHeight = 24.sp,
                            ),
                            color = InkFaint,
                        )
                    }
                    BasicTextField(
                        value = text,
                        onValueChange = {
                            if (!busy && it.length <= 30000) {
                                text = it
                                note = null
                            }
                        },
                        enabled = !busy,
                        textStyle = TextStyle(
                            color = Ink,
                            fontSize = 15.sp,
                            lineHeight = 24.sp,
                        ),
                        cursorBrush = SolidColor(Kaki),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 126.dp),
                    )
                }
                Row(Modifier.fillMaxWidth().padding(top = 5.dp)) {
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = "${text.length} / 30000",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = InkFaint,
                    )
                }
                Spacer(Modifier.height(18.dp))
            }

            note?.let { currentNote ->
                item {
                    EnttsNoteCard(currentNote)
                    Spacer(Modifier.height(18.dp))
                }
            }

            item {
                Eyebrow("VOICE · 声音")
                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    EnttsRepository.voices.forEach { option ->
                        TtsChip(
                            label = option.label,
                            selected = option.id == voice.id,
                            enabled = !busy,
                            onClick = { voiceId = option.id },
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                Eyebrow("SPEED · 语速")
                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    EnttsRepository.speeds.forEach { option ->
                        TtsSpeedChip(
                            option = option,
                            selected = option.value == speed,
                            enabled = !busy,
                            onClick = { speed = option.value },
                        )
                    }
                }
                Spacer(Modifier.height(18.dp))
            }

            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(9.dp),
                ) {
                    Text(
                        text = if (busy) "停止" else "开始朗读",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                        color = if (busy) Ink else MaterialTheme.colorScheme.onPrimary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (busy) PaperCard else Kaki)
                            .border(
                                0.8.dp,
                                if (busy) MaterialTheme.colorScheme.outlineVariant else Color.Transparent,
                                RoundedCornerShape(14.dp),
                            )
                            .clickable { if (busy) stopReading() else startReading(0) }
                            .padding(vertical = 14.dp),
                    )
                }
                Spacer(Modifier.height(12.dp))
            }

            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(13.dp))
                        .background(if (error != null) MaterialTheme.colorScheme.errorContainer else PaperCard)
                        .padding(horizontal = 13.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (busy) {
                        CircularProgressIndicator(
                            color = Kaki,
                            strokeWidth = 1.3.dp,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(Modifier.width(9.dp))
                    }
                    Text(
                        text = error ?: status,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.5.sp,
                            lineHeight = 18.sp,
                        ),
                        color = if (error != null) {
                            MaterialTheme.colorScheme.onErrorContainer
                        } else {
                            InkMuted
                        },
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (segments.isNotEmpty()) {
                    Spacer(Modifier.height(18.dp))
                    Eyebrow("SEGMENTS · 分段")
                    Spacer(Modifier.height(8.dp))
                }
            }

            itemsIndexed(segments) { index, segment ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(if (index == currentSegment) KakiSoft else PaperCard)
                        .clickable(enabled = !busy) { startReading(index) }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Text(
                        text = "${index + 1}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = if (index == currentSegment) Kaki else InkFaint,
                        modifier = Modifier.width(22.dp).padding(top = 2.dp),
                    )
                    Text(
                        text = segment,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            lineHeight = 19.sp,
                        ),
                        color = if (index == currentSegment) Ink else InkMuted,
                    )
                }
            }

            item {
                Spacer(Modifier.height(20.dp))
                Text(
                    text = "朗读服务由 entts.com 提供，音频生成需要联网。",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, lineHeight = 16.sp),
                    color = InkFaint,
                )
            }
        }
    }
}

@Composable
private fun EnttsNoteCard(note: EnttsNote) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(PaperCard)
            .border(1.dp, Kaki.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 13.dp),
    ) {
        Eyebrow("TRANSLATION · 翻译与讲解", color = Kaki)
        if (note.p.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = note.p,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 18.sp),
                color = InkFaint,
            )
        }
        if (note.t.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = note.t,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp, lineHeight = 24.sp),
                color = Ink,
            )
        }
        val words = note.w.filter { it.isNotEmpty() }
        if (words.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Hairline()
            Spacer(Modifier.height(9.dp))
            words.forEach { entry ->
                val term = entry.getOrElse(0) { "" }
                val phonetic = entry.getOrElse(1) { "" }
                val meaning = entry.getOrElse(2) { "" }
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = term,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold,
                            ),
                            color = Ink,
                        )
                        if (phonetic.isNotBlank()) {
                            Text(
                                text = "/ ${phonetic.trim('/', ' ')} /",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                                color = Kaki,
                            )
                        }
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = meaning,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp, lineHeight = 19.sp),
                        color = InkMuted,
                        modifier = Modifier.weight(1.35f),
                    )
                }
            }
        }
        if (note.g.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Hairline()
            Spacer(Modifier.height(9.dp))
            Text(
                text = "语法与用法 · ${note.g}",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp, lineHeight = 19.sp),
                color = InkMuted,
            )
        }
    }
}

@Composable
private fun TtsChip(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall.copy(
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        ),
        color = if (selected) MaterialTheme.colorScheme.onPrimary else InkMuted,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) Kaki else PaperCard)
            .border(
                0.8.dp,
                if (selected) Color.Transparent else MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(16.dp),
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 11.dp, vertical = 9.dp),
    )
}

@Composable
private fun TtsSpeedChip(
    option: TtsSpeed,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Text(
        text = option.label,
        style = MaterialTheme.typography.labelSmall.copy(
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        ),
        color = if (selected) MaterialTheme.colorScheme.onPrimary else InkMuted,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) Kaki else PaperCard)
            .border(
                0.8.dp,
                if (selected) Color.Transparent else MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(16.dp),
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 9.dp),
    )
}

private suspend fun playMp3(file: File) {
    suspendCancellableCoroutine<Unit> { continuation ->
        val player = MediaPlayer()
        fun releasePlayer() {
            runCatching { player.release() }
        }

        try {
            player.setDataSource(file.absolutePath)
            player.setOnCompletionListener {
                releasePlayer()
                if (continuation.isActive) continuation.resume(Unit)
            }
            player.setOnErrorListener { _, _, _ ->
                releasePlayer()
                if (continuation.isActive) {
                    continuation.resumeWithException(IllegalStateException("音频播放失败"))
                }
                true
            }
            player.prepare()
            player.start()
            continuation.invokeOnCancellation {
                runCatching { player.stop() }
                releasePlayer()
            }
        } catch (e: Exception) {
            releasePlayer()
            if (continuation.isActive) continuation.resumeWithException(e)
        }
    }
}
