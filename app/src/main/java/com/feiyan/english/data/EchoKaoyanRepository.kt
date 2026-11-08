package com.feiyan.english.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

enum class EchoExam(
    val key: String,
    val displayName: String,
    val englishName: String,
    val shortName: String,
) {
    ENGLISH_I("english-i", "英语一", "English I", "EN I"),
    ENGLISH_II("english-ii", "英语二", "English II", "EN II");

    companion object {
        fun fromKey(key: String): EchoExam? = entries.firstOrNull { it.key == key }
    }
}

enum class EchoDocumentKind {
    PAPER,
    SECTION,
    ANSWERS,
    GUIDE,
    AUXILIARY,
}

data class EchoYear(
    val exam: EchoExam,
    val year: Int,
    val path: String,
    val sections: List<String>,
)

data class EchoCatalog(val years: List<EchoYear>) {

    fun yearsFor(exam: EchoExam): List<EchoYear> =
        years.filter { it.exam == exam }.sortedByDescending { it.year }

    fun find(examKey: String, year: Int): EchoYear? =
        years.firstOrNull { it.exam.key == examKey && it.year == year }

    val paperCount: Int get() = years.size
    val sectionCount: Int get() = years.sumOf { it.sections.size }
}

data class EchoDocumentInfo(
    val path: String,
    val title: String,
    val subtitle: String,
    val kind: EchoDocumentKind,
    val exam: EchoExam? = null,
    val year: Int? = null,
    val section: String? = null,
)

data class EchoAnswer(
    val number: String,
    val letter: String,
)

data class EchoAnswerGroup(
    val section: String,
    val title: String,
    val answers: List<EchoAnswer>,
)

object EchoKaoyanRepository {

    const val ROOT = "echo_kaoyan"
    const val REFERENCES = "$ROOT/references"

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }
    private val prettyJsonFormat = Json { prettyPrint = true }

    private val sectionTitles = mapOf(
        "paper" to "完整试卷",
        "cloze" to "完形填空",
        "reading-text-1" to "阅读 Text 1",
        "reading-text-2" to "阅读 Text 2",
        "reading-text-3" to "阅读 Text 3",
        "reading-text-4" to "阅读 Text 4",
        "new-question-type" to "新题型",
        "translation" to "翻译",
        "writing" to "写作",
        "answers" to "参考答案",
    )

    private val sectionEnglish = mapOf(
        "paper" to "Complete Paper",
        "cloze" to "Use of English",
        "reading-text-1" to "Reading Comprehension",
        "reading-text-2" to "Reading Comprehension",
        "reading-text-3" to "Reading Comprehension",
        "reading-text-4" to "Reading Comprehension",
        "new-question-type" to "New Question Type",
        "translation" to "Translation",
        "writing" to "Writing",
        "answers" to "Answer Key",
    )

    val guideDocuments: List<EchoDocumentInfo> = listOf(
        EchoDocumentInfo(
            path = "$ROOT/GUIDE.md",
            title = "Echo 使用指南",
            subtitle = "资料范围、检索方式与学习流程",
            kind = EchoDocumentKind.GUIDE,
        ),
        EchoDocumentInfo(
            path = "$REFERENCES/corpus-guide.md",
            title = "真题资料库说明",
            subtitle = "目录结构、题号映射与数据边界",
            kind = EchoDocumentKind.GUIDE,
        ),
        EchoDocumentInfo(
            path = "$REFERENCES/rubrics/reading-analysis.md",
            title = "阅读精析规范",
            subtitle = "证据链、选项陷阱与八段式讲解",
            kind = EchoDocumentKind.GUIDE,
        ),
        EchoDocumentInfo(
            path = "$REFERENCES/rubrics/cloze-analysis.md",
            title = "完形精析规范",
            subtitle = "搭配、语义与篇章逻辑",
            kind = EchoDocumentKind.GUIDE,
        ),
        EchoDocumentInfo(
            path = "$REFERENCES/rubrics/passage-translation.md",
            title = "全文翻译规范",
            subtitle = "逐段译文、重点词汇与长难句",
            kind = EchoDocumentKind.GUIDE,
        ),
        EchoDocumentInfo(
            path = "$REFERENCES/rubrics/translation-analysis.md",
            title = "翻译评分规范",
            subtitle = "英一 / 英二分制与意群扣分",
            kind = EchoDocumentKind.GUIDE,
        ),
        EchoDocumentInfo(
            path = "$REFERENCES/rubrics/writing-rubric.md",
            title = "作文评分规范",
            subtitle = "分档评分、逐句修改与范文",
            kind = EchoDocumentKind.GUIDE,
        ),
        EchoDocumentInfo(
            path = "$REFERENCES/strategies/simulation-generation.md",
            title = "模拟题生成策略",
            subtitle = "外刊改编、难度控制与训练闭环",
            kind = EchoDocumentKind.GUIDE,
        ),
        EchoDocumentInfo(
            path = "$REFERENCES/vocabulary/README.md",
            title = "词汇资源说明",
            subtitle = "词表格式与覆盖率检查",
            kind = EchoDocumentKind.GUIDE,
        ),
    )

    suspend fun loadCatalog(context: Context): EchoCatalog = withContext(Dispatchers.IO) {
        val text = context.assets.open("$REFERENCES/corpus-index.json")
            .bufferedReader(Charsets.UTF_8)
            .use { it.readText() }
        val root = json.parseToJsonElement(text).jsonObject
        val years = buildList {
            EchoExam.entries.forEach examLoop@{ exam ->
                val examObject = root[exam.key]?.jsonObject ?: return@examLoop
                examObject.forEach yearLoop@{ (yearKey, value) ->
                    val year = yearKey.toIntOrNull() ?: return@yearLoop
                    val item = value.jsonObject
                    val path = item["path"]?.jsonPrimitive?.contentOrNull.orEmpty()
                    val sections = item["sections"]?.jsonArray
                        ?.mapNotNull { it.jsonPrimitive.contentOrNull }
                        .orEmpty()
                    if (path.isNotBlank()) {
                        add(EchoYear(exam = exam, year = year, path = path, sections = sections))
                    }
                }
            }
        }
        EchoCatalog(years = years)
    }

    fun documents(year: EchoYear): List<EchoDocumentInfo> = buildList {
        val contextLabel = "${year.exam.displayName} · ${year.year}"
        add(
            EchoDocumentInfo(
                path = "$ROOT/${year.path}/paper.md",
                title = sectionTitle("paper"),
                subtitle = "$contextLabel · 整卷原文与题目",
                kind = EchoDocumentKind.PAPER,
                exam = year.exam,
                year = year.year,
                section = "paper",
            ),
        )
        year.sections.forEach { section ->
            add(
                EchoDocumentInfo(
                    path = "$ROOT/${year.path}/$section.md",
                    title = sectionTitle(section),
                    subtitle = "$contextLabel · ${sectionEnglish(section)}",
                    kind = EchoDocumentKind.SECTION,
                    exam = year.exam,
                    year = year.year,
                    section = section,
                ),
            )
        }
        add(
            EchoDocumentInfo(
                path = "$ROOT/${year.path}/answers.json",
                title = "参考答案",
                subtitle = "$contextLabel · 客观题答案",
                kind = EchoDocumentKind.ANSWERS,
                exam = year.exam,
                year = year.year,
            ),
        )
    }

    fun describe(path: String): EchoDocumentInfo {
        guideDocuments.firstOrNull { it.path == path }?.let { return it }
        val match = Regex(
            """(?:^|/)references/papers/(english-i|english-ii)/(\d{4})/([^/]+)$""",
        ).find(path)
        if (match != null) {
            val exam = EchoExam.fromKey(match.groupValues[1])
            val year = match.groupValues[2].toIntOrNull()
            val file = match.groupValues[3]
            val section = file.substringBeforeLast('.').substringBeforeLast('?')
            val contextLabel = listOfNotNull(exam?.displayName, year?.toString()).joinToString(" · ")
            return when (file) {
                "answers.json" -> EchoDocumentInfo(
                    path, "参考答案", "$contextLabel · 客观题答案",
                    EchoDocumentKind.ANSWERS, exam, year,
                )
                "question-map.json" -> EchoDocumentInfo(
                    path, "题号映射", "$contextLabel · 题目与篇目对应关系",
                    EchoDocumentKind.AUXILIARY, exam, year,
                )
                "meta.json" -> EchoDocumentInfo(
                    path, "年度数据", "$contextLabel · 资料元数据",
                    EchoDocumentKind.AUXILIARY, exam, year,
                )
                else -> EchoDocumentInfo(
                    path = path,
                    title = sectionTitle(section),
                    subtitle = "$contextLabel · ${sectionEnglish(section)}",
                    kind = if (section == "paper") EchoDocumentKind.PAPER else EchoDocumentKind.SECTION,
                    exam = exam,
                    year = year,
                    section = section,
                )
            }
        }
        val name = path.substringAfterLast('/').substringBeforeLast('.')
        return EchoDocumentInfo(
            path = path,
            title = if (name.isBlank()) "内置资料" else name,
            subtitle = "Echo 考研英语 · 离线资料",
            kind = EchoDocumentKind.AUXILIARY,
        )
    }

    suspend fun readDocument(context: Context, path: String): String = withContext(Dispatchers.IO) {
        val assetPath = if (path.startsWith("$ROOT/")) path else "$ROOT/$path"
        context.assets.open(assetPath).bufferedReader(Charsets.UTF_8).use { it.readText() }
    }

    suspend fun loadAnswers(
        context: Context,
        path: String,
    ): List<EchoAnswerGroup> = withContext(Dispatchers.IO) {
        val assetPath = if (path.startsWith("$ROOT/")) path else "$ROOT/$path"
        val text = context.assets.open(assetPath).bufferedReader(Charsets.UTF_8).use { it.readText() }
        val answers = json.parseToJsonElement(text).jsonObject["answers"]?.jsonObject
            ?: return@withContext emptyList()
        answers.map { (section, value) ->
            val entries = when (val element = value) {
                is JsonObject -> element.mapNotNull { (number, answer) ->
                    answer.jsonPrimitive.contentOrNull?.let { EchoAnswer(number, it) }
                }
                else -> emptyList()
            }.sortedWith(compareBy { it.number.toIntOrNull() ?: Int.MAX_VALUE })
            EchoAnswerGroup(
                section = section,
                title = sectionTitle(section),
                answers = entries,
            )
        }
    }

    suspend fun countReferenceFiles(context: Context): Int = withContext(Dispatchers.IO) {
        collectFiles(context, REFERENCES).size
    }

    suspend fun prettyJson(text: String): String = withContext(Dispatchers.Default) {
        runCatching {
            val element = json.parseToJsonElement(text)
            prettyJsonFormat.encodeToString(JsonElement.serializer(), element)
        }.getOrDefault(text)
    }

    fun sectionTitle(section: String): String = sectionTitles[section] ?: section

    fun sectionEnglish(section: String): String = sectionEnglish[section] ?: "Echo Study Material"

    private fun collectFiles(context: Context, directory: String): List<String> {
        val children = runCatching { context.assets.list(directory) }.getOrNull().orEmpty()
        return buildList {
            children.forEach { child ->
                val childPath = "$directory/$child"
                if (child.endsWith(".md", ignoreCase = true) ||
                    child.endsWith(".json", ignoreCase = true)
                ) {
                    add(childPath)
                } else if (!child.contains('.')) {
                    addAll(collectFiles(context, childPath))
                }
            }
        }
    }
}
