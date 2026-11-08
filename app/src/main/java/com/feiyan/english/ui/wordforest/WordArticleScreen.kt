package com.feiyan.english.ui.wordforest

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.feiyan.english.core.model.WfArticle
import com.feiyan.english.core.model.WfArticleBlock
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
fun WordArticleScreen(
    articleId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var article by remember(articleId) { mutableStateOf<WfArticle?>(null) }
    var error by remember(articleId) { mutableStateOf<String?>(null) }
    var reloadToken by remember { mutableIntStateOf(0) }

    LaunchedEffect(articleId, reloadToken) {
        runCatching { WordForestRepository.article(context, articleId) }
            .onSuccess {
                article = it
                if (it == null) error = "找不到这篇内置文章"
            }
            .onFailure { error = it.message ?: "读取文章失败" }
    }

    Column(modifier.fillMaxSize()) {
        WordForestTopBar(eyebrow = "WORDFOREST · 阅读", onBack = onBack)
        Hairline()

        when {
            error != null -> WordForestError(error.orEmpty()) { reloadToken++ }
            article == null -> WordForestLoading("正在打开文章…")
            else -> ArticleContent(article = article!!)
        }
    }
}

@Composable
private fun ArticleContent(article: WfArticle) {
    val uriHandler = LocalUriHandler.current
    SelectionContainer {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 24.dp,
                end = 24.dp,
                top = 20.dp,
                bottom = LocalBottomOverlayPadding.current + 36.dp,
            ),
        ) {
            item {
                Eyebrow(article.category.ifBlank { "WORDFOREST" }, color = Kaki)
                Spacer(Modifier.height(7.dp))
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.displaySmall.copy(fontSize = 30.sp, lineHeight = 38.sp),
                    color = Ink,
                )
                Spacer(Modifier.height(7.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = article.date,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = InkFaint,
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "离线内置",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = Kaki,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(KakiSoft.copy(alpha = 0.55f))
                            .padding(horizontal = 7.dp, vertical = 3.dp),
                    )
                }
                Spacer(Modifier.height(18.dp))
            }

            article.blocks.forEachIndexed { index, block ->
                item(key = "block-$index") {
                    ArticleBlockView(block = block, onLink = { uriHandler.openUri(it) })
                }
            }

            item {
                Spacer(Modifier.height(24.dp))
                Text(
                    text = "内容来源 · wordforest.cn · 内置快照",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, letterSpacing = 0.5.sp),
                    color = InkFaint,
                )
            }
        }
    }
}

@Composable
private fun ArticleBlockView(
    block: WfArticleBlock,
    onLink: (String) -> Unit,
) {
    when (block.type) {
        "heading" -> {
            val style = when (block.level) {
                1 -> MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp)
                2 -> MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp)
                else -> MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp)
            }
            Text(
                text = block.text,
                style = style,
                color = Ink,
                modifier = Modifier.padding(top = if (block.level <= 1) 16.dp else 14.dp, bottom = 7.dp),
            )
        }

        "list" -> {
            Column(Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
                block.items.forEachIndexed { index, item ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                        Text(
                            text = if (block.ordered) "${index + 1}." else "·",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 16.sp,
                                lineHeight = 26.sp,
                            ),
                            color = Kaki,
                            modifier = Modifier.width(24.dp),
                        )
                        Text(
                            text = item,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 16.sp,
                                lineHeight = 26.sp,
                            ),
                            color = Ink,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }

        "quote" -> {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(KakiSoft.copy(alpha = 0.32f))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                Text(
                    text = block.text,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        lineHeight = 23.sp,
                        fontStyle = FontStyle.Italic,
                    ),
                    color = InkMuted,
                )
            }
        }

        "image" -> {
            val url = if (block.url.startsWith("http")) block.url else WordForestRepository.WEB_BASE + block.url
            AsyncImage(
                model = url,
                contentDescription = block.alt.ifBlank { null },
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(12.dp)),
            )
        }

        else -> {
            if (block.text.isBlank()) return
            Text(
                text = block.text,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 16.sp,
                    lineHeight = 28.sp,
                ),
                color = Ink,
                modifier = Modifier.padding(vertical = 5.dp),
            )
            if (block.links.isNotEmpty()) {
                Row(
                    Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    block.links.forEach { link ->
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(PaperCard)
                                .clickable { onLink(link.url) }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                        ) {
                            Text(
                                text = "${link.text} ↗",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Medium,
                                ),
                                color = Kaki,
                            )
                        }
                    }
                }
            }
        }
    }
}
