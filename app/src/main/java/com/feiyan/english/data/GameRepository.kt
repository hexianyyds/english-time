package com.feiyan.english.data

import android.content.Context
import com.feiyan.english.core.model.GameWord
import com.feiyan.english.core.model.GameWordBank
import com.feiyan.english.core.model.GrammarItem
import com.feiyan.english.core.model.GrammarPack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.random.Random

/**
 * 趣味闯关的词库与出题器。
 *
 * 词库为 App 自有内容（assets/game/wordbank.json），不依赖任何第三方站点，
 * 因此离线可用；emoji 代替图片，避免引入有版权的图片素材。
 */
data class GameQuestion(
    val answer: GameWord,
    val options: List<GameWord>,
)

object GameRepository {

    private const val ASSET = "game/wordbank.json"

    /** 站点素材包：食物主题的真实插画词表，运行时与主词库合并。 */
    private const val FOOD_PACK = "game/food_pack.json"

    /** 站点语法题库（superlatives + comparatives）。 */
    private const val GRAMMAR_PACK = "game/grammar_pack.json"

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    @Volatile
    private var cache: GameWordBank? = null

    suspend fun bank(context: Context): GameWordBank {
        cache?.let { return it }
        return withContext(Dispatchers.IO) {
            val main = json.decodeFromString(
                GameWordBank.serializer(),
                readAsset(context, ASSET),
            )
            // 站点素材包里的食物词带真实插画，整体替换内置的 emoji 食物词
            val pack = runCatching {
                json.decodeFromString(FoodPack.serializer(), readAsset(context, FOOD_PACK)).words
            }.getOrDefault(emptyList())

            val words = if (pack.isEmpty()) {
                main.words
            } else {
                main.words.filter { it.topic != "food" } + pack
            }
            // 兜底：同一个词/图只保留一条，避免选项出现两个一样的答案
            val safe = main.copy(words = words.distinctBy { it.word })
            cache = safe
            safe
        }
    }

    private fun readAsset(context: Context, path: String): String =
        context.assets.open(path).bufferedReader(Charsets.UTF_8).use { it.readText() }

    @Serializable
    private data class FoodPack(
        val source: String = "",
        val words: List<GameWord> = emptyList(),
    )

    /**
     * 抽词：按主题轮转，保证一局里主题分散，不会连续出现同一类词。
     */
    suspend fun drawWords(
        context: Context,
        count: Int,
        predicate: (GameWord) -> Boolean = { true },
    ): List<GameWord> {
        val pool = bank(context).words.filter(predicate)
        if (pool.isEmpty()) return emptyList()
        val buckets = pool.groupBy { it.topic }
            .mapValues { (_, words) -> words.shuffled() }
            .toMutableMap()
        val result = mutableListOf<GameWord>()
        while (result.size < count) {
            val remaining = buckets.filterValues { it.isNotEmpty() }
            if (remaining.isEmpty()) break
            remaining.keys.shuffled().forEach { key ->
                if (result.size >= count) return@forEach
                val bucket = buckets[key].orEmpty()
                if (bucket.isEmpty()) return@forEach
                result += bucket.first()
                buckets[key] = bucket.drop(1)
            }
        }
        return result.shuffled()
    }

    /** 生成「一问四选」题目，干扰项优先取同主题词。 */
    suspend fun drawQuestions(
        context: Context,
        count: Int,
        optionCount: Int = 4,
        predicate: (GameWord) -> Boolean = { true },
    ): List<GameQuestion> {
        val all = bank(context).words.filter(predicate)
        val answers = drawWords(context, count, predicate)
        return answers.map { answer ->
            // 词是唯一答案，图由词决定，因此只按 word 去重即可
            val sameTopic = all.filter {
                it.topic == answer.topic && it.word != answer.word
            }.shuffled()
            val others = all.filter {
                it.topic != answer.topic && it.word != answer.word
            }.shuffled()
            val distractors = (sameTopic + others)
                .distinctBy { it.word }
                .take((optionCount - 1).coerceAtLeast(1))
            GameQuestion(
                answer = answer,
                options = (distractors + answer).shuffled(),
            )
        }
    }

    /**
     * 语法题：从站点 superlatives / comparatives 题库里随机抽，并把选项打乱。
     * 正确答案通过 [GrammarItem.answer] 标识，不依赖选项顺序。
     */
    suspend fun drawGrammar(context: Context, count: Int): List<GrammarItem> {
        val pack = withContext(Dispatchers.IO) {
            runCatching {
                json.decodeFromString(
                    GrammarPack.serializer(),
                    readAsset(context, GRAMMAR_PACK),
                )
            }.getOrNull()
        } ?: return emptyList()

        val pool = pack.items
            .filter { it.options.size >= 3 && it.answer.isNotBlank() }
            .distinctBy { it.prompt }
        if (pool.isEmpty()) return emptyList()

        // 保证比较级 / 最高级都有，避免一整局只有一种题型
        val comparative = pool.filter { it.type == "comparative" }.shuffled()
        val superlative = pool.filter { it.type == "superlative" }.shuffled()
        val picked = mutableListOf<GrammarItem>()
        picked += superlative.take(minOf(2, count))
        picked += comparative.take(count - picked.size)
        if (picked.size < count) {
            picked += (superlative.drop(2) + pool.filterNot { it.type == "comparative" || it.type == "superlative" })
                .take(count - picked.size)
        }

        return picked.distinctBy { it.prompt }.map { item ->
            item.copy(options = item.options.shuffled())
        }
    }

    /**
     * 拼写题只取单词短、无空格的词条，字母块才有得玩。
     */
    suspend fun drawSpellWords(context: Context, count: Int): List<GameWord> =
        drawWords(context, count) { word ->
            word.word.isNotBlank() &&
                !word.word.contains(' ') &&
                word.word.length in 3..10
        }

    /** 记忆配对：成对的 emoji 与单词，必须是同一批词，故直接抽词。 */
    suspend fun drawPairs(context: Context, pairs: Int): List<GameWord> =
        drawWords(context, pairs) { it.word.isNotBlank() && it.emoji.isNotBlank() }

    /**
     * 拼写字母块：正确字母 + 少量干扰字母，返回洗好的字符列表。
     */
    fun letterPool(word: String, extra: Int = 2, random: Random = Random.Default): List<Char> {
        val letters = word.lowercase().toMutableList()
        val alphabet = ('a'..'z').toList()
        repeat(extra) {
            letters += alphabet[random.nextInt(alphabet.size)]
        }
        return letters.shuffled(random)
    }
}
