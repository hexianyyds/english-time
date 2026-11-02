package com.feiyan.english.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.feiyan.english.ui.components.Hairline
import com.feiyan.english.ui.components.LoadingView
import com.feiyan.english.ui.components.LocalBottomOverlayPadding
import com.feiyan.english.ui.components.ProgrammeRow
import com.feiyan.english.ui.icons.FeiyanIcons
import com.feiyan.english.ui.theme.Ink
import com.feiyan.english.ui.theme.InkFaint
import com.feiyan.english.ui.theme.Kaki

@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onChannelClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: SearchViewModel = viewModel()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val results by viewModel.results.collectAsStateWithLifecycle()
    val searching by viewModel.searching.collectAsStateWithLifecycle()

    Column(modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(start = 8.dp, end = 20.dp, top = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(40.dp).clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = FeiyanIcons.ArrowLeft,
                    contentDescription = "返回",
                    tint = Ink,
                    modifier = Modifier.size(19.dp),
                )
            }
        }

        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 12.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = FeiyanIcons.Search,
                contentDescription = null,
                tint = InkFaint,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(12.dp))
            BasicTextField(
                value = query,
                onValueChange = viewModel::onQueryChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.titleMedium.copy(color = Ink),
                cursorBrush = SolidColor(Kaki),
                modifier = Modifier.weight(1f),
                decorationBox = { inner ->
                    Box {
                        if (query.isEmpty()) {
                            Text(
                                text = "搜索节目、作者或关键词",
                                style = MaterialTheme.typography.bodyMedium,
                                color = InkFaint,
                            )
                        }
                        inner()
                    }
                },
            )
        }
        Hairline()

        when {
            searching && results.isEmpty() -> LoadingView(hint = "正在搜索")
            results.isEmpty() -> Text(
                text = if (query.isBlank()) "试着搜索 BBC、The Daily、Life Kit" else "没有找到相关节目",
                style = MaterialTheme.typography.bodySmall,
                color = InkFaint,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 60.dp),
            )
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    bottom = LocalBottomOverlayPadding.current,
                ),
            ) {
                items(items = results, key = { it.isbn }) { channel ->
                    ProgrammeRow(
                        channel = channel,
                        onClick = { onChannelClick(channel.isbn) },
                    )
                    Hairline(Modifier.padding(start = 84.dp, end = 24.dp))
                }
            }
        }
    }
}
