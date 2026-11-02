package com.feiyan.english.ui.eg

import androidx.compose.foundation.background
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.feiyan.english.core.model.EgArticleDetail
import com.feiyan.english.ui.components.ErrorView
import com.feiyan.english.ui.components.Eyebrow
import com.feiyan.english.ui.components.FavoriteButton
import com.feiyan.english.ui.components.Hairline
import com.feiyan.english.ui.components.LoadingView
import com.feiyan.english.ui.components.LocalBottomOverlayPadding
import com.feiyan.english.ui.icons.FeiyanIcons
import com.feiyan.english.ui.theme.Ink
import com.feiyan.english.ui.theme.InkFaint
import com.feiyan.english.ui.theme.InkMuted
import com.feiyan.english.ui.theme.Kaki
import com.feiyan.english.ui.theme.KakiSoft

@Composable
fun EgArticleScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: EgArticleViewModel = viewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

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
            Spacer(Modifier.width(4.dp))
            Eyebrow("ARTICLE · 精读", modifier = Modifier.weight(1f))
            state.article?.let { article ->
                FavoriteButton(
                    item = com.feiyan.english.data.LibraryItem(
                        id = "eg:${article.id}",
                        title = article.title,
                        subtitle = "Englio · ${article.level}",
                        cover = null,
                        route = "eg/${article.id}",
                    ),
                )
            }
        }

        when {
            state.loading -> LoadingView()
            state.article == null -> ErrorView(state.error ?: "载入失败", onRetry = viewModel::load)
            else -> Reader(state.article!!)
        }
    }
}

@Composable
private fun Reader(article: EgArticleDetail) {
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 24.dp,
            end = 24.dp,
            bottom = LocalBottomOverlayPadding.current + 40.dp,
        ),
    ) {
        item(key = "head") {
            Column {
                Spacer(Modifier.height(18.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (article.level.isNotBlank()) {
                        Text(
                            text = article.level,
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
                            color = Kaki,
                            modifier = Modifier
                                .clip(RoundedCornerShape(2.dp))
                                .background(KakiSoft)
                                .padding(horizontal = 7.dp, vertical = 3.dp),
                        )
                        Spacer(Modifier.width(10.dp))
                    }
                    Eyebrow("${article.sentences.size} 句", color = InkFaint)
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = FontFamily.Serif,
                        fontSize = 27.sp,
                        lineHeight = 36.sp,
                    ),
                    color = Ink,
                )
                Spacer(Modifier.height(18.dp))
                Hairline()
            }
        }

        if (article.summaryZh.isNotBlank()) {
            item(key = "summary") {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 18.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(KakiSoft.copy(alpha = 0.4f))
                        .padding(14.dp),
                ) {
                    Eyebrow("中文摘要")
                    Spacer(Modifier.height(7.dp))
                    Text(
                        text = article.summaryZh,
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                        color = InkMuted,
                    )
                }
            }
        }

        itemsIndexed(items = article.sentences, key = { index, _ -> index }) { _, sentence ->
            Text(
                text = sentence,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontFamily = FontFamily.Serif,
                    fontSize = 17.sp,
                    lineHeight = 30.sp,
                ),
                color = Ink,
                modifier = Modifier.padding(top = 18.dp),
            )
        }

        item(key = "footer") {
            Column(Modifier.padding(top = 34.dp)) {
                Hairline()
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "内容来源 englio.ai · 仅展示原文难度版本",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.3.sp),
                    color = InkFaint,
                )
            }
        }
    }
}
