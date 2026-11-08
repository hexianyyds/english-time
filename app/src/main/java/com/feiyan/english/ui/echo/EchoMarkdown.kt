package com.feiyan.english.ui.echo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.feiyan.english.ui.theme.Hairline
import com.feiyan.english.ui.theme.Ink
import com.feiyan.english.ui.theme.InkMuted
import com.feiyan.english.ui.theme.Kaki
import com.feiyan.english.ui.theme.KakiSoft
import com.feiyan.english.ui.theme.PaperCard
import com.feiyan.english.ui.theme.PaperDeep

/** 离线考研资料阅读器：覆盖标题、段落、引用、列表、表格和代码块。 */
@Composable
fun EchoMarkdownArticle(
    markdown: String,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val blocks = remember(markdown) { parseMarkdown(markdown) }
    SelectionContainer {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = contentPadding,
        ) {
            itemsIndexed(blocks) { index, block ->
                MarkdownBlockView(block, isLast = index == blocks.lastIndex)
            }
        }
    }
}

private sealed interface MarkdownBlock {
    data class Heading(val level: Int, val text: String) : MarkdownBlock
    data class Paragraph(val text: String) : MarkdownBlock
    data class Quote(val text: String) : MarkdownBlock
    data class ListItem(val marker: String, val text: String, val indent: Int) : MarkdownBlock
    data class Code(val text: String) : MarkdownBlock
    data class Table(val rows: List<List<String>>) : MarkdownBlock
}

private val HeadingPattern = Regex("""^(#{1,6})\s+(.+)$""")
private val ListPattern = Regex("""^(\s*)([-*+]|\d+\.)\s+(.+)$""")
private val TableDivider = Regex("""^:?-{2,}:?$""")
private val InlinePattern = Regex(
    """\*\*(.+?)\*\*|`([^`\n]+)`|\[([^\]]+)]\(([^)\s]+)\)|\*(?!\*)([^*\n]+)\*""",
)

private fun parseMarkdown(raw: String): List<MarkdownBlock> {
    var lines = raw.replace("\r\n", "\n").replace('\r', '\n').split('\n')
    if (lines.firstOrNull()?.trim() == "---") {
        val end = lines.drop(1).indexOfFirst { it.trim() == "---" }
        if (end >= 0) lines = lines.drop(end + 2)
    }

    val blocks = mutableListOf<MarkdownBlock>()
    val paragraph = mutableListOf<String>()
    var inComment = false

    fun flushParagraph() {
        if (paragraph.isNotEmpty()) {
            blocks += MarkdownBlock.Paragraph(paragraph.joinToString(" "))
            paragraph.clear()
        }
    }

    var index = 0
    while (index < lines.size) {
        val line = lines[index]
        val trimmed = line.trim()

        if (inComment) {
            if (trimmed.contains("-->")) inComment = false
            index++
            continue
        }
        if (trimmed.startsWith("<!--")) {
            if (!trimmed.contains("-->")) inComment = true
            index++
            continue
        }

        when {
            trimmed.isEmpty() -> {
                flushParagraph()
                index++
            }

            trimmed.startsWith("```") -> {
                flushParagraph()
                val code = mutableListOf<String>()
                index++
                while (index < lines.size && !lines[index].trim().startsWith("```")) {
                    code += lines[index]
                    index++
                }
                if (index < lines.size) index++
                blocks += MarkdownBlock.Code(code.joinToString("\n"))
            }

            HeadingPattern.matches(trimmed) -> {
                flushParagraph()
                val match = HeadingPattern.matchEntire(trimmed)!!
                val level = match.groupValues[1].length
                val text = match.groupValues[2].trim().trimEnd('#').trim()
                if (text.isNotEmpty()) blocks += MarkdownBlock.Heading(level, text)
                index++
            }

            trimmed.startsWith("|") -> {
                flushParagraph()
                val rows = mutableListOf<List<String>>()
                while (index < lines.size && lines[index].trim().startsWith("|")) {
                    val cells = splitTableRow(lines[index])
                    if (cells.isNotEmpty() && !cells.all { it.isBlank() || TableDivider.matches(it) }) {
                        rows += cells
                    }
                    index++
                }
                if (rows.isNotEmpty()) blocks += MarkdownBlock.Table(rows)
            }

            trimmed.startsWith(">") -> {
                flushParagraph()
                val quote = mutableListOf<String>()
                while (index < lines.size && lines[index].trim().startsWith(">")) {
                    quote += lines[index].trim().removePrefix(">").removePrefix(" ")
                    index++
                }
                val text = quote.joinToString("\n").trim()
                if (text.isNotEmpty()) blocks += MarkdownBlock.Quote(text)
            }

            ListPattern.matches(line) -> {
                flushParagraph()
                val match = ListPattern.matchEntire(line)!!
                val indent = (match.groupValues[1].length / 2).coerceIn(0, 3)
                val rawMarker = match.groupValues[2]
                val marker = if (rawMarker.endsWith(".")) rawMarker else "•"
                blocks += MarkdownBlock.ListItem(
                    marker = marker,
                    text = match.groupValues[3],
                    indent = indent,
                )
                index++
            }

            trimmed == "---" || trimmed == "***" -> {
                flushParagraph()
                index++
            }

            else -> {
                paragraph += trimmed
                index++
            }
        }
    }
    flushParagraph()
    return blocks
}

private fun splitTableRow(line: String): List<String> =
    line.trim()
        .removePrefix("|")
        .removeSuffix("|")
        .split("|")
        .map { it.trim() }

@Composable
private fun MarkdownBlockView(block: MarkdownBlock, isLast: Boolean) {
    when (block) {
        is MarkdownBlock.Heading -> {
            val style = when (block.level) {
                1 -> MaterialTheme.typography.displaySmall.copy(
                    fontSize = 27.sp,
                    lineHeight = 35.sp,
                )
                2 -> MaterialTheme.typography.titleLarge.copy(
                    fontSize = 21.sp,
                    lineHeight = 29.sp,
                )
                3 -> MaterialTheme.typography.titleMedium.copy(
                    fontSize = 17.sp,
                    lineHeight = 25.sp,
                )
                else -> MaterialTheme.typography.titleSmall.copy(
                    fontSize = 15.sp,
                    lineHeight = 23.sp,
                )
            }
            Text(
                text = inlineMarkdown(
                    block.text,
                    boldWeight = FontWeight.SemiBold,
                ),
                style = style,
                color = if (block.level <= 2) Ink else Kaki,
                modifier = Modifier.padding(top = if (block.level <= 2) 18.dp else 12.dp, bottom = 7.dp),
            )
        }

        is MarkdownBlock.Paragraph -> {
            Text(
                text = inlineMarkdown(block.text),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 16.sp,
                    lineHeight = 28.sp,
                ),
                color = Ink,
                modifier = Modifier.padding(vertical = 6.dp),
            )
        }

        is MarkdownBlock.Quote -> {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .height(IntrinsicSize.Min),
            ) {
                Box(
                    Modifier
                        .width(3.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(2.dp))
                        .background(Kaki),
                )
                Column(
                    Modifier
                        .weight(1f)
                        .background(PaperDeep, RoundedCornerShape(topEnd = 10.dp, bottomEnd = 10.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                ) {
                    Text(
                        text = inlineMarkdown(block.text),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            lineHeight = 24.sp,
                        ),
                        color = InkMuted,
                    )
                }
            }
        }

        is MarkdownBlock.ListItem -> {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(start = (block.indent * 14).dp, top = 3.dp, bottom = 3.dp),
            ) {
                Text(
                    text = block.marker,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 16.sp,
                        lineHeight = 27.sp,
                    ),
                    color = Kaki,
                    modifier = Modifier.width(24.dp),
                )
                Text(
                    text = inlineMarkdown(block.text),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 16.sp,
                        lineHeight = 27.sp,
                    ),
                    color = Ink,
                )
            }
        }

        is MarkdownBlock.Code -> {
            Text(
                text = block.text,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    lineHeight = 20.sp,
                ),
                color = Ink,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(PaperDeep)
                    .border(0.8.dp, Hairline, RoundedCornerShape(10.dp))
                    .horizontalScroll(rememberScrollState())
                    .padding(12.dp),
            )
        }

        is MarkdownBlock.Table -> {
            MarkdownTable(block.rows)
        }
    }

    if (isLast) {
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun MarkdownTable(rows: List<List<String>>) {
    val columns = rows.maxOfOrNull { it.size }?.coerceAtLeast(1) ?: 1
    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .clip(RoundedCornerShape(10.dp))
            .border(0.8.dp, Hairline, RoundedCornerShape(10.dp))
            .background(PaperCard),
    ) {
        rows.forEachIndexed { rowIndex, row ->
            if (rowIndex > 0) {
                Box(Modifier.fillMaxWidth().height(0.8.dp).background(Hairline))
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(if (rowIndex == 0) KakiSoft.copy(alpha = 0.45f) else Color.Transparent),
                horizontalArrangement = Arrangement.Start,
            ) {
                repeat(columns) { columnIndex ->
                    val text = row.getOrElse(columnIndex) { "" }
                    Text(
                        text = inlineMarkdown(text),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            lineHeight = 19.sp,
                            fontWeight = if (rowIndex == 0) FontWeight.SemiBold else FontWeight.Normal,
                        ),
                        color = Ink,
                        modifier = Modifier
                            .weight(1f)
                            .widthIn(min = 64.dp)
                            .padding(horizontal = 9.dp, vertical = 8.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun inlineMarkdown(
    text: String,
    boldWeight: FontWeight = FontWeight.SemiBold,
): AnnotatedString {
    val accent = Kaki
    val codeBackground = PaperDeep
    return remember(text, boldWeight, accent, codeBackground) {
        buildAnnotatedString {
            var cursor = 0
            InlinePattern.findAll(text).forEach { match ->
                if (match.range.first > cursor) {
                    append(text.substring(cursor, match.range.first))
                }
                when {
                    match.groupValues[1].isNotEmpty() -> {
                        pushStyle(SpanStyle(fontWeight = boldWeight))
                        append(match.groupValues[1])
                        pop()
                    }
                    match.groupValues[2].isNotEmpty() -> {
                        pushStyle(
                            SpanStyle(
                                fontFamily = FontFamily.Monospace,
                                background = codeBackground,
                            ),
                        )
                        append(match.groupValues[2])
                        pop()
                    }
                    match.groupValues[3].isNotEmpty() -> {
                        pushStyle(
                            SpanStyle(
                                color = accent,
                                textDecoration = TextDecoration.Underline,
                            ),
                        )
                        append(match.groupValues[3])
                        pop()
                    }
                    match.groupValues[5].isNotEmpty() -> {
                        pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                        append(match.groupValues[5])
                        pop()
                    }
                    else -> append(match.value)
                }
                cursor = match.range.last + 1
            }
            if (cursor < text.length) append(text.substring(cursor))
        }
    }
}
