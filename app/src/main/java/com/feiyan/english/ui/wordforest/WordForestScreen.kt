package com.feiyan.english.ui.wordforest

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.feiyan.english.core.model.WfArticle
import com.feiyan.english.core.model.WfBook
import com.feiyan.english.data.WordForestRepository
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

private val BookOrder = listOf("3", "1", "2", "4", "9", "8", "5", "6", "7")

@Composable
fun WordForestScreen(
    onBookClick: (String) -> Unit,
    onWordClick: (String) -> Unit,
    onArticleClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    var query by rememberSaveable { mutableStateOf("") }
    var books by remember { mutableStateOf<List<WfBook>>(emptyList()) }
    var articles by remember { mutableStateOf<List<WfArticle>>(emptyList()) }
    var booksLoading by remember { mutableStateOf(true) }
    var booksError by remember { mutableStateOf<String?>(null) }
    var reloadToken by remember { mutableStateOf(0) }
    var category by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(reloadToken) {
        runCatching { WordForestRepository.articles(context) }
            .onSuccess { articles = it }
        runCatching { WordForestRepository.books() }
            .onSuccess {
                books = it.sortedBy { book ->
                    BookOrder.indexOf(book.id).takeIf { index -> index >= 0 } ?: Int.MAX_VALUE
                }
            }
            .onFailure { booksError = it.message ?: "词书加载失败" }
        booksLoading = false
    }

    val submitSearch = {
        val value = query.trim()
        if (value.isNotEmpty()) {
            focusManager.clearFocus()
            onWordClick(value)
        }
    }
    val categories = listOf("全部") + articles.map { it.category }.filter { it.isNotBlank() }.distinct()
    val visibleArticles = if (category == null) {
        articles
    } else {
        articles.filter { it.category == category }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = LocalBottomOverlayPadding.current + 30.dp),
    ) {
        item {
            Column(Modifier.padding(horizontal = 24.dp)) {
                Spacer(Modifier.height(16.dp))
                Eyebrow("WORDFOREST · 单词森林")
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Word Forest",
                    style = MaterialTheme.typography.displaySmall.copy(fontSize = 29.sp),
                    color = Ink,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "词书与词典实时查询，词源短文完整离线保存。",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkMuted,
                )
            }
        }

        item {
            Column(Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                WordSearchCard(
                    value = query,
                    onValueChange = { query = it },
                    onSubmit = { submitSearch() },
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    WordForestStat((if (books.isEmpty()) 9 else books.size).toString(), "本词书", Modifier.weight(1f))
                    WordForestStat(
                        (if (books.isEmpty()) 40842 else books.sumOf { it.total }).toString(),
                        "收录词条",
                        Modifier.weight(1f),
                    )
                    WordForestStat(if (articles.isEmpty()) "25" else articles.size.toString(), "篇短文", Modifier.weight(1f))
                }
            }
        }

        if (booksLoading) {
            item {
                Text(
                    text = "正在读取词书…",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkFaint,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 18.dp),
                )
            }
        } else if (booksError != null) {
            item {
                Column(Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                    Text(
                        text = booksError.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = InkMuted,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "重新加载",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp),
                        color = Kaki,
                        modifier = Modifier.clickable {
                            booksError = null
                            booksLoading = true
                            reloadToken++
                        },
                    )
                }
            }
        }

        item {
            Column(Modifier.padding(horizontal = 24.dp)) {
                SectionTitle("词书", "WORD BOOKS", "${books.size} 本")
                Spacer(Modifier.height(12.dp))
                books.chunked(2).forEach { rowBooks ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        rowBooks.forEach { book ->
                            BookGridCard(
                                book = book,
                                onClick = { onBookClick(book.id) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        if (rowBooks.size == 1) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                }
            }
        }

        item {
            Column(Modifier.padding(top = 16.dp, bottom = 12.dp)) {
                Column(Modifier.padding(horizontal = 24.dp)) {
                    SectionTitle("词源与短文", "READING", "${articles.size} 篇")
                    Spacer(Modifier.height(10.dp))
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    categories.forEach { item ->
                        ArticleCategoryChip(
                            label = item,
                            selected = (item == "全部" && category == null) || category == item,
                            onClick = { category = if (item == "全部") null else item },
                        )
                    }
                }
            }
        }

        items(visibleArticles, key = { it.id }) { article ->
            ArticleRow(article = article, onClick = { onArticleClick(article.id) })
        }

        if (visibleArticles.isEmpty() && !booksLoading) {
            item {
                Text(
                    text = "该分类暂无文章。",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkFaint,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp),
                )
            }
        }

        item {
            Column(Modifier.padding(horizontal = 24.dp, vertical = 26.dp)) {
                Text(
                    text = "数据源 · 单词森林 wordforest.cn",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp),
                    color = InkFaint,
                )
                Spacer(Modifier.height(5.dp))
                Text(
                    text = "词书与词典来自公开接口，文章抓取于 2026-11-08。",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkFaint,
                )
            }
        }
    }
}

@Composable
private fun WordSearchCard(
    value: String,
    onValueChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(PaperCard)
            .border(0.8.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
            .padding(start = 14.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = FeiyanIcons.Search,
            contentDescription = null,
            tint = InkFaint,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(10.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = Ink, fontSize = 14.sp),
            cursorBrush = SolidColor(Kaki),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Ascii,
                imeAction = ImeAction.Search,
            ),
            keyboardActions = KeyboardActions(onSearch = { onSubmit() }),
            modifier = Modifier.weight(1f),
            decorationBox = { inner ->
                Box {
                    if (value.isEmpty()) {
                        Text(
                            text = "输入英文单词查词典",
                            style = MaterialTheme.typography.bodySmall,
                            color = InkFaint,
                        )
                    }
                    inner()
                }
            },
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "查词",
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                letterSpacing = 0.8.sp,
            ),
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .clip(RoundedCornerShape(11.dp))
                .background(Kaki)
                .clickable(onClick = onSubmit)
                .padding(horizontal = 14.dp, vertical = 11.dp),
        )
    }
}

@Composable
private fun WordForestStat(value: String, label: String, modifier: Modifier = Modifier) {
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
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
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
private fun BookGridCard(
    book: WfBook,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .clip(RoundedCornerShape(15.dp))
            .background(PaperCard)
            .border(0.8.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(15.dp))
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val cover = WordForestRepository.coverUrl(book)
        Box(
            Modifier
                .size(width = 44.dp, height = 58.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(KakiSoft),
        ) {
            if (cover != null) {
                AsyncImage(
                    model = cover,
                    contentDescription = book.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        Spacer(Modifier.width(9.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = book.name,
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.sp),
                color = Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = book.properties.description.ifBlank { book.type },
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                color = InkFaint,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(5.dp))
            Eyebrow("${book.total} 词", color = Kaki)
        }
    }
}

@Composable
private fun ArticleCategoryChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall.copy(
            fontSize = 10.sp,
            letterSpacing = 0.4.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        ),
        color = if (selected) MaterialTheme.colorScheme.onPrimary else InkMuted,
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) Kaki else PaperCard)
            .border(
                0.8.dp,
                if (selected) Color.Transparent else MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(18.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 9.dp),
    )
}

@Composable
private fun ArticleRow(article: WfArticle, onClick: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Eyebrow(article.category, color = Kaki)
            Spacer(Modifier.width(8.dp))
            Eyebrow(article.date, color = InkFaint)
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = article.title,
            style = MaterialTheme.typography.titleSmall.copy(fontSize = 16.sp),
            color = Ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(5.dp))
        Text(
            text = article.summary,
            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 19.sp),
            color = InkMuted,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
    Hairline(Modifier.padding(horizontal = 24.dp))
}

@Composable
private fun SectionTitle(title: String, english: String, trailing: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
            color = Ink,
        )
        Spacer(Modifier.width(7.dp))
        Eyebrow(english)
        Spacer(Modifier.weight(1f))
        Text(
            text = trailing,
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.5.sp),
            color = InkFaint,
        )
    }
}

