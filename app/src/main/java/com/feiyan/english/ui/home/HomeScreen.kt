package com.feiyan.english.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.layout.LazyLayoutCacheWindow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.feiyan.english.core.model.ChannelBrief
import com.feiyan.english.core.model.EmEpisode
import com.feiyan.english.core.model.HomeSection
import com.feiyan.english.core.model.XyPodcast
import com.feiyan.english.player.PlayerController
import com.feiyan.english.player.PlaybackUiState
import com.feiyan.english.ui.components.ChannelCard
import com.feiyan.english.ui.components.Cover
import com.feiyan.english.ui.components.ErrorView
import com.feiyan.english.ui.components.Eyebrow
import com.feiyan.english.ui.components.Hairline
import com.feiyan.english.ui.components.LoadingView
import com.feiyan.english.ui.components.LocalBottomOverlayPadding
import com.feiyan.english.ui.components.NowPlayingCard
import com.feiyan.english.ui.components.ProgrammeRow
import com.feiyan.english.ui.components.SectionHeader
import com.feiyan.english.ui.em.EmCover
import com.feiyan.english.ui.icons.FeiyanIcons
import com.feiyan.english.ui.theme.Ink
import com.feiyan.english.ui.theme.InkFaint
import com.feiyan.english.ui.theme.InkMuted
import com.feiyan.english.ui.theme.Kaki

@Composable
fun HomeScreen(
    onChannelClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    onEpisodeClick: (String) -> Unit,
    onEmAllClick: () -> Unit,
    onEmEpisodeClick: (String) -> Unit,
    onXyAllClick: () -> Unit,
    onXyEpisodeClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: HomeViewModel = viewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val player by PlayerController.state.collectAsStateWithLifecycle()

    Box(modifier.fillMaxSize()) {
        when (val current = state) {
            HomeUiState.Loading -> LoadingView()
            is HomeUiState.Error -> ErrorView(current.message) { viewModel.load(force = true) }
            is HomeUiState.Ready -> HomeContent(
                sections = current.sections,
                emEpisodes = current.emEpisodes,
                xyPodcast = current.xyPodcast,
                player = player,
                onChannelClick = onChannelClick,
                onSearchClick = onSearchClick,
                onEpisodeClick = onEpisodeClick,
                onEmAllClick = onEmAllClick,
                onEmEpisodeClick = onEmEpisodeClick,
                onXyAllClick = onXyAllClick,
                onXyEpisodeClick = onXyEpisodeClick,
                onTogglePlayback = PlayerController::toggle,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HomeContent(
    sections: List<HomeSection>,
    emEpisodes: List<EmEpisode>,
    xyPodcast: XyPodcast?,
    player: PlaybackUiState,
    onChannelClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    onEpisodeClick: (String) -> Unit,
    onEmAllClick: () -> Unit,
    onEmEpisodeClick: (String) -> Unit,
    onXyAllClick: () -> Unit,
    onXyEpisodeClick: (String) -> Unit,
    onTogglePlayback: () -> Unit,
) {
    // Compose 1.12：自定义 Lazy 预取窗口，减少首页长列表滚动时的空白帧
    val listState = rememberLazyListState(
        cacheWindow = LazyLayoutCacheWindow(ahead = 240.dp, behind = 120.dp),
    )
    val latest = sections.firstOrNull { it.id == SECTION_POPULAR }?.channels?.firstOrNull()
        ?: sections.firstOrNull { it.id == SECTION_LATEST }?.channels?.firstOrNull()
        ?: sections.firstOrNull()?.channels?.firstOrNull()

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 28.dp + LocalBottomOverlayPadding.current),
    ) {
        item(key = "brand", contentType = "brand") {
            BrandHeader(onSearchClick = onSearchClick)
        }
        item(key = "hero", contentType = "hero") {
            HeroBlock(latest = latest, onChannelClick = onChannelClick)
        }
        if (player.hasMedia) {
            item(key = "now-playing", contentType = "now-playing") {
                NowPlayingCard(
                    state = player,
                    onClick = { player.mediaId?.let(onEpisodeClick) },
                    modifier = Modifier.padding(top = 20.dp),
                )
            }
        }
        sections.forEach { section ->
            item(key = "section-${section.id}", contentType = "section") {
                SectionBlock(
                    section = section,
                    onChannelClick = onChannelClick,
                )
            }
        }
        if (emEpisodes.isNotEmpty()) {
            item(key = "em-header", contentType = "section") {
                SectionHeader(
                    title = "英语播客",
                    english = "ENGLISHMEASURE",
                    trailing = "全部",
                    onTrailingClick = onEmAllClick,
                )
            }
            item(key = "em-rail", contentType = "rail") {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(items = emEpisodes.take(12), key = { it.slug }) { episode ->
                        EmCover(
                            category = episode.category,
                            title = episode.title,
                            size = 132.dp,
                            modifier = Modifier
                                .animateItem()
                                .clickable { onEmEpisodeClick(episode.slug) },
                        )
                    }
                }
            }
        }

        val xyEpisodes = xyPodcast?.episodes.orEmpty().take(10)
        if (xyEpisodes.isNotEmpty()) {
            item(key = "xy-header", contentType = "section") {
                SectionHeader(
                    title = "小宇宙精选",
                    english = "XIAOYUZHOU",
                    trailing = "全部",
                    onTrailingClick = onXyAllClick,
                )
            }
            item(key = "xy-rail", contentType = "rail") {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(items = xyEpisodes, key = { it.eid }) { episode ->
                        Column(
                            Modifier
                                .width(132.dp)
                                .animateItem()
                                .clickable { onXyEpisodeClick(episode.eid) },
                        ) {
                            Cover(
                                episode.image?.best ?: xyPodcast?.image?.best,
                                size = 132.dp,
                                corner = 4.dp,
                            )
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = episode.title,
                                style = MaterialTheme.typography.titleSmall,
                                color = Ink,
                                maxLines = 2,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            )
                            Spacer(Modifier.height(3.dp))
                            Eyebrow("免费单集", color = InkFaint)
                        }
                    }
                }
            }
        }

        item(key = "footer", contentType = "footer") {
            FooterNote()
        }
    }
}

private const val SECTION_LATEST = -1
private const val SECTION_POPULAR = -3

@Composable
private fun BrandHeader(onSearchClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 16.dp, top = 12.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "飞岩",
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp, letterSpacing = 3.sp),
            color = Ink,
        )
        Spacer(Modifier.width(10.dp))
        Eyebrow("FEIYAN")
        Spacer(Modifier.weight(1f))
        Box(
            Modifier
                .size(40.dp)
                .clickable(onClick = onSearchClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = FeiyanIcons.Search,
                contentDescription = "搜索",
                tint = Ink,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun HeroBlock(latest: ChannelBrief?, onChannelClick: (String) -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 14.dp, bottom = 8.dp),
    ) {
        Text(
            text = "A quiet place\nto hear English.",
            style = MaterialTheme.typography.displaySmall,
            color = Ink,
        )
        Spacer(Modifier.height(18.dp))
        Text(
            text = "在声音里，慢慢学会一门语言。",
            style = MaterialTheme.typography.bodyMedium.copy(letterSpacing = 0.8.sp),
            color = InkMuted,
        )
        Spacer(Modifier.height(26.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(24.dp).height(1.dp).background(Kaki))
            Spacer(Modifier.width(10.dp))
            Text(
                text = "今日 · ${latest?.englishName ?: "英文播客"}",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp),
                color = InkMuted,
                modifier = Modifier.clickable(enabled = latest != null) {
                    latest?.isbn?.let(onChannelClick)
                },
            )
        }
        Spacer(Modifier.height(22.dp))
        Hairline()
    }
}

@Composable
private fun SectionBlock(
    section: HomeSection,
    onChannelClick: (String) -> Unit,
) {
    Column(Modifier.fillMaxWidth()) {
        SectionHeader(
            title = section.name,
            english = sectionEnglish(section),
            trailing = "${section.channels.size} 个节目",
        )
        if (section.id == SECTION_LATEST) {
            section.channels.take(3).forEach { channel ->
                ProgrammeRow(
                    channel = channel,
                    onClick = { onChannelClick(channel.isbn) },
                )
                Hairline(Modifier.padding(start = 84.dp, end = 24.dp))
            }
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(items = section.channels, key = { it.isbn }) { channel ->
                    ChannelCard(
                        channel = channel,
                        onClick = { onChannelClick(channel.isbn) },
                        modifier = Modifier.animateItem(),
                    )
                }
            }
        }
    }
}

private fun sectionEnglish(section: HomeSection): String = when (section.id) {
    -3 -> "POPULAR"
    -2 -> "MEMBERS"
    -1 -> "LATEST"
    3 -> "EDITOR'S PICK"
    else -> section.name.let { if (section.type == 2) "COLLECTION" else "CURATED" }
}

@Composable
private fun FooterNote() {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 40.dp),
    ) {
        Hairline()
        Spacer(Modifier.height(18.dp))
        Text(
            text = "Feiyan English · 播客内容来自 feiyanpod.com",
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.6.sp),
            color = InkFaint,
        )
    }
}
