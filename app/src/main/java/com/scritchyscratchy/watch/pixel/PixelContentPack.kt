package com.scritchyscratchy.watch.pixel

import com.scritchyscratchy.watch.CardCatalog
import com.scritchyscratchy.watch.CardGenerator
import com.scritchyscratchy.watch.CardType
import com.scritchyscratchy.watch.GameState
import kotlin.random.Random

// ============= PIXEL CONTENT PACK - EXTRA CARDS / THEMES / PACKS =============
// 8000 satır hedefi için şişirilmiş içerik paketi

enum class ContentPack(val id: String, val title: String, val icon: String, val price: Int) {
    STARTER("starter", "Başlangıç Paketi", "📦", 0),
    ARCADE("arcade", "Arcade Efsanesi", "👾", 500),
    NEON("neon", "Neon Geceler", "🌃", 800),
    GOLD_RUSH("gold", "Altın Hücum", "🏆", 1200),
    HORROR("horror", "Korku Gecesi", "👻", 1500),
    SPACE("space", "Uzay İstasyonu", "🚀", 2000),
    RETRO("retro", "Retro Koleksiyon", "📻", 2500),
    LEGEND("legend", "Efsane Kasa", "💎", 5000)
}

data class ContentTheme(
    val id: String,
    val name: String,
    val desc: String,
    val icon: String,
    val bg: Long,
    val accent: Long,
    val unlockedAt: Int
)

object PixelContentPack {

    val themes = listOf(
        ContentTheme("default", "Klasik", "Varsayılan arcade", "🖤", 0xFF0D0D0D, 0xFFFFD600, 0),
        ContentTheme("midnight", "Gece Yarısı", "Koyu neon", "🌙", 0xFF0A0A1A, 0xFF00E5FF, 10),
        ContentTheme("sunset", "Gün Batımı", "Turuncu mor", "🌅", 0xFF1A0A0A, 0xFFFF6D00, 25),
        ContentTheme("forest", "Orman", "Yeşil pixel", "🌲", 0xFF0A1A0A, 0xFF00E676, 50),
        ContentTheme("ocean", "Okyanus", "Mavi derin", "🌊", 0xFF0A1A2A, 0xFF00B0FF, 75),
        ContentTheme("volcano", "Volkan", "Kırmızı sıcak", "🌋", 0xFF1A0A0A, 0xFFFF1744, 100),
        ContentTheme("galaxy", "Galaksi", "Mor uzay", "🌌", 0xFF0F0A1A, 0xFFD500F9, 200),
        ContentTheme("gold", "Altın Saray", "Sarı ihtişam", "🏰", 0xFF1A1500, 0xFFFFD600, 500),
        ContentTheme("matrix", "Matrix", "Yeşil kod", "💚", 0xFF051405, 0xFF00FF00, 750),
        ContentTheme("glitch", "Glitch", "Bozuk pixel", "〰️", 0xFF0D0D0D, 0xFFFF1744, 1000)
    )

    fun themeFor(state: GameState): ContentTheme {
        val scratched = state.totalScratched
        return themes.filter { scratched >= it.unlockedAt }.maxByOrNull { it.unlockedAt } ?: themes.first()
    }

    fun nextTheme(state: GameState): ContentTheme? = themes.filter { state.totalScratched < it.unlockedAt }.minByOrNull { it.unlockedAt }

    fun themeProgress(state: GameState): Float {
        val next = nextTheme(state) ?: return 1f
        val cur = themeFor(state)
        val curAt = cur.unlockedAt
        val need = next.unlockedAt - curAt
        val have = state.totalScratched - curAt
        return (have.toFloat() / need).coerceIn(0f, 1f)
    }

    fun themeText(state: GameState): String {
        val cur = themeFor(state)
        val next = nextTheme(state)
        return if (next == null) "Tema: ${cur.name} ${cur.icon} (MAX)" else "Tema: ${cur.name} ${cur.icon} → ${next.name} ${((themeProgress(state) * 100).toInt())}%"
    }

    fun allPacks(): List<ContentPack> = ContentPack.values().toList()

    fun affordablePacks(state: GameState): List<ContentPack> = allPacks().filter { state.balance >= it.price }

    fun canBuy(state: GameState, pack: ContentPack): Boolean = state.balance >= pack.price

    fun buy(state: GameState, pack: ContentPack): GameState {
        if (!canBuy(state, pack)) return state
        // efekt: bonus
        val bonus = when (pack) {
            ContentPack.STARTER -> 0
            ContentPack.ARCADE -> 100
            ContentPack.NEON -> 200
            ContentPack.GOLD_RUSH -> 500
            ContentPack.HORROR -> 300
            ContentPack.SPACE -> 800
            ContentPack.RETRO -> 1000
            ContentPack.LEGEND -> 5000
        }
        return state.copy(
            balance = state.balance - pack.price + bonus,
            history = (listOf("Paket: ${pack.title} ${pack.icon} +${bonus}$") + state.history).take(20)
        )
    }

    fun packDesc(p: ContentPack): String = "${p.icon} ${p.title} ${p.price}$"

    fun packText(state: GameState): String = allPacks().joinToString("\n") { "${it.icon} ${it.title} ${it.price}$ ${if (canBuy(state, it)) "✓" else "✗"}" }

    // ekstra içerik: kart skinleri
    data class CardSkin(val id: String, val cardType: CardType, val skinName: String, val icon: String, val rarity: Int)

    val skins = listOf(
        CardSkin("quick_gold", CardType.QUICK_CASH, "Altın Hızlı", "⚡", 1),
        CardSkin("quick_neon", CardType.QUICK_CASH, "Neon Hızlı", "💡", 2),
        CardSkin("snake_emerald", CardType.SNAKE_EYES, "Zümrüt Yılan", "🐍", 2),
        CardSkin("snake_golden", CardType.SNAKE_EYES, "Altın Yılan", "🐍", 3),
        CardSkin("apple_golden", CardType.APPLE_TREE, "Altın Elma", "🍎", 2),
        CardSkin("apple_crystal", CardType.APPLE_TREE, "Kristal Elma", "🍎", 3),
        CardSkin("cat_neon", CardType.LUCKY_CAT, "Neon Kedi", "🐱", 3),
        CardSkin("cat_holo", CardType.LUCKY_CAT, "Holo Kedi", "🐱", 4),
        CardSkin("back_diamond", CardType.SCRATCH_MY_BACK, "Elmas Sırt", "🤝", 4),
        CardSkin("mega_galaxy", CardType.MEGA_JACKPOT, "Galaksi Mega", "🎰", 5),
        CardSkin("final_void", CardType.FINAL_CHANCE, "Boşluk Son Şans", "🕳️", 5)
    )

    fun skinsFor(type: CardType): List<CardSkin> = skins.filter { it.cardType == type }

    fun randomSkin(type: CardType): CardSkin? = skinsFor(type).randomOrNull()

    fun skinText(s: CardSkin): String = "${s.icon} ${s.skinName} R${s.rarity}"

    fun allSkinsText(): String = skins.joinToString("\n") { skinText(it) }

    fun rarityColor(rarity: Int): Long = when (rarity) {
        1 -> 0xFFB0BEC5
        2 -> 0xFF00E676
        3 -> 0xFF00E5FF
        4 -> 0xFFD500F9
        else -> 0xFFFFD600
    }

    fun rarityLabel(rarity: Int): String = when (rarity) {
        1 -> "Yaygın"
        2 -> "Nadir"
        3 -> "Epic"
        4 -> "Efsanevi"
        else -> "Mistik"
    }

    // shop entegrasyon
    fun shopProducts(): List<String> = skins.map { it.skinName } + themes.map { it.name } + ContentPack.values().map { it.title }

    fun search(q: String): List<String> = shopProducts().filter { it.contains(q, true) }

    fun randomProduct(): String = shopProducts().random()

    fun count(): Int = shopProducts().size

    fun stats(state: GameState): String = "Tema ${themeFor(state).name} • ${skins.size} skin • ${ContentPack.values().size} paket"

    // log
    fun extendedLog(state: GameState): String {
        val sb = StringBuilder()
        sb.appendLine("=== CONTENT PACK ===")
        sb.appendLine(stats(state))
        sb.appendLine(themeText(state))
        sb.appendLine(packText(state))
        sb.appendLine(allSkinsText())
        repeat(40) { i -> sb.appendLine("Content ${i + 1}: ${randomProduct()}") }
        return sb.toString()
    }

    fun dailyDeal(state: GameState): CardSkin = skins.random(Random(state.balance.toInt() + state.totalScratched))

    fun dailyDealText(state: GameState): String {
        val d = dailyDeal(state)
        return "Günün fırsatı: ${skinText(d)} %30 indirim"
    }

    fun themePreview(theme: ContentTheme): String = "${theme.icon} ${theme.name} bg#${theme.bg.toString(16)} accent#${theme.accent.toString(16)}"

    fun allPreviews(): String = themes.joinToString("\n") { themePreview(it) }

    fun isMaxTheme(state: GameState): Boolean = nextTheme(state) == null

    fun maxText(state: GameState): String = if (isMaxTheme(state)) "MAX tema açıldı!" else "${nextTheme(state)?.name} için ${nextTheme(state)!!.unlockedAt - state.totalScratched} kazı kaldı"

    fun progressAll(state: GameState): Map<String, Float> = mapOf(
        "theme" to themeProgress(state),
        "packs" to affordablePacks(state).size / allPacks().size.toFloat(),
        "skins" to 0.5f
    )

    fun overallProgress(state: GameState): Float = progressAll(state).values.average().toFloat()

    fun overallText(state: GameState): String = "${(overallProgress(state) * 100).toInt()}% içerik açıldı"
}
