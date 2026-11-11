package com.feiyan.english.data

/** 五种玩法的元信息，大厅卡片与路由共用。 */
data class GameDefinition(
    val id: String,
    val title: String,
    val english: String,
    val tagline: String,
    val howTo: String,
    val emoji: String,
    val rounds: Int,
    /** 0 / 1 交替，让大厅卡片强调色有节奏感。 */
    val accentIndex: Int = 0,
)

object GameCatalog {

    val games: List<GameDefinition> = listOf(
        GameDefinition(
            id = "reveal",
            title = "翻图猜词",
            english = "Reveal & Pick",
            tagline = "揭开得越少，分数越高",
            howTo = "图片被四块纸片盖住。点「再揭一块」可以看到更多，但揭得越多这一题得分越低。猜对后单词会自动朗读。",
            emoji = "🃏",
            rounds = 8,
            accentIndex = 0,
        ),
        GameDefinition(
            id = "listen",
            title = "听音选图",
            english = "Listen & Pick",
            tagline = "只用耳朵，选出那张图",
            howTo = "系统会朗读一个单词，请从四张图里选出正确的一张。没听清可以点喇叭重放，但重放会少拿一点分。",
            emoji = "🎧",
            rounds = 10,
            accentIndex = 1,
        ),
        GameDefinition(
            id = "memory",
            title = "记忆配对",
            english = "Memory Match",
            tagline = "把单词和图配成一对",
            howTo = "六张单词牌、六张图片牌随机打乱。每次翻开两张，配对成功即消除。步数越少星级越高。",
            emoji = "🧩",
            rounds = 6,
            accentIndex = 1,
        ),
        GameDefinition(
            id = "spell",
            title = "拼写挑战",
            english = "Spell It",
            tagline = "看图把单词拼出来",
            howTo = "看图拼出英文单词。字母块里混了两个干扰字母，点错可以退格重来。",
            emoji = "🔤",
            rounds = 8,
            accentIndex = 0,
        ),
        GameDefinition(
            id = "fast",
            title = "极速判断",
            english = "Fast Quiz",
            tagline = "60 秒内看图秒判真假",
            howTo = "屏幕上会出现一张图和它的英文单词。相配就点「对」，不相配就点「错」。答得越快、连击越高，得分越高。",
            emoji = "⚡",
            rounds = 20,
            accentIndex = 1,
        ),
        GameDefinition(
            id = "grammar",
            title = "语法选择",
            english = "Grammar Pick",
            tagline = "比较级 / 最高级，一句话选对",
            howTo = "每题给一个英文句子或问句，从三个选项里选出语法正确的那句。句子来自英语母语教学题库，干扰项都是常见错误形式。",
            emoji = "✍️",
            rounds = 10,
            accentIndex = 0,
        ),
    )

    fun find(id: String): GameDefinition? = games.firstOrNull { it.id == id }
}
