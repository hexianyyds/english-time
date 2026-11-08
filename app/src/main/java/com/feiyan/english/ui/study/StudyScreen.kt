package com.feiyan.english.ui.study

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.feiyan.english.data.EchoExam
import com.feiyan.english.ui.components.Eyebrow
import com.feiyan.english.ui.beidanci.BeidanciScreen
import com.feiyan.english.ui.components.ThemeToggleButton
import com.feiyan.english.ui.echo.EchoKaoyanHubScreen
import com.feiyan.english.ui.eg.EgListScreen
import com.feiyan.english.ui.home.HomeScreen
import com.feiyan.english.ui.iciba.IcibaScreen
import com.feiyan.english.ui.icons.FeiyanIcons
import com.feiyan.english.ui.theme.Ink
import com.feiyan.english.ui.theme.InkMuted
import com.feiyan.english.ui.theme.Kaki
import com.feiyan.english.ui.theme.PaperCard
import com.feiyan.english.ui.wordforest.WordForestScreen
import com.feiyan.english.ui.yy.YyNovelsScreen

private enum class StudyMode(val label: String) {
    PODCAST("播客"),
    NOVELS("小说"),
    ARTICLES("文章"),
    ECHO("考研"),
    WORDFOREST("词林"),
    ICIBA("词霸"),
    BEIDANCI("365"),
}

@Composable
fun StudyScreen(
    onSearchClick: () -> Unit,
    onChannelClick: (String) -> Unit,
    onEpisodeClick: (String) -> Unit,
    onEmAllClick: () -> Unit,
    onEmEpisodeClick: (String) -> Unit,
    onXyAllClick: () -> Unit,
    onXyEpisodeClick: (String) -> Unit,
    onYyBookClick: (String) -> Unit,
    onByBookClick: (String) -> Unit,
    onEgArticleClick: (String) -> Unit,
    onByArticleClick: (String, Long) -> Unit,
    onEchoYearClick: (EchoExam, Int) -> Unit,
    onEchoDocumentClick: (String) -> Unit,
    onWfBookClick: (String) -> Unit,
    onWfWordClick: (String) -> Unit,
    onWfArticleClick: (String) -> Unit,
    onIcibaGroupClick: (String) -> Unit,
    onBdCourseClick: (String) -> Unit,
    onBdStudyClick: (String, Int) -> Unit,
    onTtsClick: () -> Unit,
    onIpaClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }
    val selectedMode = StudyMode.entries[selectedIndex.coerceIn(StudyMode.entries.indices)]
    val stateHolder = rememberSaveableStateHolder()

    Column(modifier.fillMaxSize()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 16.dp, top = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Eyebrow("STUDY · 学习")
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Study",
                    style = MaterialTheme.typography.displaySmall.copy(fontSize = 30.sp),
                    color = Ink,
                )
            }
            if (selectedMode == StudyMode.PODCAST) {
                Box(
                    Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .clickable(onClick = onSearchClick),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = FeiyanIcons.Search,
                        contentDescription = "搜索播客",
                        tint = Ink,
                        modifier = Modifier.size(19.dp),
                    )
                }
                Spacer(Modifier.width(2.dp))
            }
            Box(
                Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .clickable(onClick = onTtsClick),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = FeiyanIcons.Headphones,
                    contentDescription = "文本朗读",
                    tint = Ink,
                    modifier = Modifier.size(19.dp),
                )
            }
            Box(
                Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .clickable(onClickLabel = "打开国际音标") { onIpaClick() },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "ə",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp),
                    color = Kaki,
                )
            }
            Spacer(Modifier.width(2.dp))
            ThemeToggleButton(showLabel = false)
        }

        Spacer(Modifier.height(12.dp))
        StudyModeBar(
            selected = selectedMode,
            onSelect = { selectedIndex = it.ordinal },
        )
        Spacer(Modifier.height(4.dp))

        Box(Modifier.weight(1f)) {
            stateHolder.SaveableStateProvider(selectedMode.name) {
                when (selectedMode) {
                    StudyMode.PODCAST -> HomeScreen(
                        onChannelClick = onChannelClick,
                        onSearchClick = onSearchClick,
                        onEpisodeClick = onEpisodeClick,
                        onEmAllClick = onEmAllClick,
                        onEmEpisodeClick = onEmEpisodeClick,
                        onXyAllClick = onXyAllClick,
                        onXyEpisodeClick = onXyEpisodeClick,
                        showBrandHeader = false,
                    )

                    StudyMode.NOVELS -> YyNovelsScreen(
                        onBookClick = onYyBookClick,
                        onByBookClick = onByBookClick,
                    )

                    StudyMode.ARTICLES -> EgListScreen(
                        onArticleClick = onEgArticleClick,
                        onByArticleClick = onByArticleClick,
                    )

                    StudyMode.ECHO -> EchoKaoyanHubScreen(
                        onBack = {},
                        onYearClick = onEchoYearClick,
                        onDocumentClick = onEchoDocumentClick,
                        showTopBar = false,
                    )

                    StudyMode.WORDFOREST -> WordForestScreen(
                        onBookClick = onWfBookClick,
                        onWordClick = onWfWordClick,
                        onArticleClick = onWfArticleClick,
                    )

                    StudyMode.ICIBA -> IcibaScreen(onGroupClick = onIcibaGroupClick)

                    StudyMode.BEIDANCI -> BeidanciScreen(
                        onCourseClick = onBdCourseClick,
                        onStudyClick = onBdStudyClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun StudyModeBar(
    selected: StudyMode,
    onSelect: (StudyMode) -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(PaperCard)
            .border(0.8.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(15.dp))
            .padding(4.dp),
    ) {
        StudyMode.entries.forEach { mode ->
            val active = mode == selected
            Box(
                Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(if (active) Kaki else Color.Transparent)
                    .selectable(selected = active) { onSelect(mode) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = mode.label,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                    ),
                    color = if (active) MaterialTheme.colorScheme.onPrimary else InkMuted,
                )
            }
        }
    }
}
