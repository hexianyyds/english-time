package com.feiyan.english.ui.wordforest

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.feiyan.english.core.model.WfWord
import com.feiyan.english.data.WordForestRepository
import com.feiyan.english.ui.components.Eyebrow
import com.feiyan.english.ui.components.Hairline
import com.feiyan.english.ui.components.LocalBottomOverlayPadding
import com.feiyan.english.ui.theme.Ink
import com.feiyan.english.ui.theme.InkFaint
import com.feiyan.english.ui.theme.InkMuted
import com.feiyan.english.ui.theme.Kaki
import com.feiyan.english.ui.theme.KakiSoft
import com.feiyan.english.ui.theme.PaperCard

@Composable
fun WordDetailScreen(
    word: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var detail by remember(word) { mutableStateOf<WfWord?>(null) }
    var error by remember(word) { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }
    var reloadToken by remember { mutableIntStateOf(0) }

    LaunchedEffect(word, reloadToken) {
        loading = true
        error = null
        runCatching { WordForestRepository.dictionary(word) }
            .onSuccess {
                detail = it
                if (it == null) error = "词典中没有找到“$word”"
            }
            .onFailure { error = it.message ?: "词典查询失败" }
        loading = false
    }

    Column(modifier.fillMaxSize()) {
        WordForestTopBar(eyebrow = "WORDFOREST · 词典", onBack = onBack)
        Hairline()

        when {
            loading -> WordForestLoading("正在查询 $word…")
            error != null -> WordForestError(error.orEmpty()) { reloadToken++ }
            detail != null -> WordDetailContent(
                word = detail!!,
                contentPadding = PaddingValues(
                    start = 24.dp,
                    end = 24.dp,
                    top = 20.dp,
                    bottom = LocalBottomOverlayPadding.current + 36.dp,
                ),
            )
        }
    }
}

@Composable
private fun WordDetailContent(
    word: WfWord,
    contentPadding: PaddingValues,
) {
    val phonetics = listOfNotNull(
        word.usPhonetic?.takeIf { it.isNotBlank() }?.let { "美 / $it /" },
        word.ukPhonetic?.takeIf { it.isNotBlank() }?.let { "英 / $it /" },
    ).joinToString("    ")
    val translations = word.translations?.meanings.orEmpty()
    val explanations = word.explanations?.meanings.orEmpty()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding,
    ) {
        item {
            Eyebrow("DICTIONARY")
            Spacer(Modifier.height(7.dp))
            Text(
                text = word.word,
                style = MaterialTheme.typography.displaySmall.copy(fontSize = 38.sp),
                color = Ink,
            )
            if (phonetics.isNotBlank()) {
                Spacer(Modifier.height(7.dp))
                Text(
                    text = phonetics,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = InkMuted,
                )
            }
            Spacer(Modifier.height(20.dp))
        }

        if (!word.memory.isNullOrBlank()) {
            item {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(KakiSoft.copy(alpha = 0.5f))
                        .border(0.8.dp, Kaki.copy(alpha = 0.22f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 15.dp, vertical = 13.dp),
                ) {
                    Eyebrow("MEMORY · 记忆", color = Kaki)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = word.memory.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                        color = Ink,
                    )
                }
                Spacer(Modifier.height(22.dp))
            }
        }

        item {
            WordSectionTitle("释义", "MEANINGS")
            if (translations.isEmpty()) {
                Text(
                    text = "暂无中文释义",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkFaint,
                    modifier = Modifier.padding(top = 10.dp),
                )
            } else {
                translations.forEach { (part, meanings) ->
                    MeaningGroup(part = part, meanings = meanings)
                }
            }
            Spacer(Modifier.height(18.dp))
        }

        val sentences = word.sentences.orEmpty().filter { it.content.isNotBlank() }
        if (sentences.isNotEmpty()) {
            item {
                WordSectionTitle("例句", "EXAMPLES")
                sentences.forEach { sentence ->
                    SentenceRow(
                        english = sentence.content,
                        chinese = sentence.translation,
                    )
                }
                Spacer(Modifier.height(18.dp))
            }
        }

        val phrases = word.phrases.orEmpty().filter { it.content.isNotBlank() }
        if (phrases.isNotEmpty()) {
            item {
                WordSectionTitle("短语", "PHRASES")
                phrases.forEach { phrase ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 7.dp)) {
                        Text(
                            text = phrase.content,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                            ),
                            color = Ink,
                            modifier = Modifier.weight(1f),
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = phrase.translation,
                            style = MaterialTheme.typography.bodySmall,
                            color = InkMuted,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                Spacer(Modifier.height(18.dp))
            }
        }

        val families = word.families.orEmpty().filter { it.isNotBlank() }
        if (families.isNotEmpty()) {
            item {
                WordSectionTitle("词族", "WORD FAMILY")
                families.forEach { family ->
                    Text(
                        text = "· $family",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 13.sp,
                            lineHeight = 23.sp,
                        ),
                        color = InkMuted,
                        modifier = Modifier.padding(vertical = 2.dp),
                    )
                }
                Spacer(Modifier.height(18.dp))
            }
        }

        if (explanations.isNotEmpty()) {
            item {
                WordSectionTitle("英文释义", "DEFINITIONS")
                explanations.forEach { (part, meanings) ->
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 5.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Text(
                            text = posLabel(part),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = Kaki,
                            modifier = Modifier.width(36.dp).padding(top = 4.dp),
                        )
                        Column(Modifier.weight(1f)) {
                            meanings.forEach { meaning ->
                                Text(
                                    text = meaning,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = MaterialTheme.typography.bodySmall.fontFamily,
                                        fontStyle = FontStyle.Italic,
                                        fontSize = 12.5.sp,
                                        lineHeight = 20.sp,
                                    ),
                                    color = InkFaint,
                                    modifier = Modifier.padding(vertical = 2.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MeaningGroup(part: String, meanings: List<String>) {
    Row(
        Modifier.fillMaxWidth().padding(top = 10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = posLabel(part),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = Kaki,
            modifier = Modifier.width(38.dp).padding(top = 3.dp),
        )
        Column(Modifier.weight(1f)) {
            meanings.filter { it.isNotBlank() }.forEach { meaning ->
                Text(
                    text = meaning,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 15.sp,
                        lineHeight = 24.sp,
                    ),
                    color = Ink,
                    modifier = Modifier.padding(vertical = 2.dp),
                )
            }
        }
    }
}

@Composable
private fun SentenceRow(english: String, chinese: String) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(PaperCard)
            .padding(horizontal = 13.dp, vertical = 11.dp),
    ) {
        Text(
            text = english,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                lineHeight = 22.sp,
            ),
            color = Ink,
        )
        if (chinese.isNotBlank()) {
            Spacer(Modifier.height(5.dp))
            Text(
                text = chinese,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 19.sp),
                color = InkMuted,
            )
        }
    }
}

@Composable
private fun WordSectionTitle(title: String, english: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
            color = Ink,
        )
        Spacer(Modifier.width(7.dp))
        Eyebrow(english)
    }
}
