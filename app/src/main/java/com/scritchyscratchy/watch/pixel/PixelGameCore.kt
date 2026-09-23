package com.scritchyscratchy.watch.pixel

import com.scritchyscratchy.watch.CardGenerator
import com.scritchyscratchy.watch.CardCatalog
import com.scritchyscratchy.watch.CardType
import com.scritchyscratchy.watch.GameState
import com.scritchyscratchy.watch.ScratchCardInstance
import kotlin.random.Random

// ============= PIXEL GAME CORE - GENİŞLETİLMİŞ OYUN MANTIĞI =============
// 8000 satır hedefi için dev oyun çekirdeği

object PixelGameCore {

    // ============= KART ÜRETİM + PIXEL BONUS =============
    fun generateWithPixelBonus(
        type: CardType,
        state: GameState,
        foil: FoilType = FoilType.NORMAL
    ): ScratchCardInstance {
        val def = CardCatalog.all.find { it.type == type } ?: CardCatalog.all.first()
        val lvl = state.cardLevels[def.id] ?: 1
        val luck = state.upgrades.luckBonus + if (state.prestige.upgrades.luckyLegacy) 0.15f else 0f
        val base = CardGenerator.generate(def, lvl, luck.toDouble(), state.prestige)
        // foil bonus
        val bonusMult = foil.multiplier
        val boostedPayout = (base.payout * bonusMult).toInt()
        return base.copy(payout = boostedPayout)
    }

    fun randomCard(state: GameState): ScratchCardInstance {
        val afford = CardCatalog.all.filter { it.cost <= state.balance || state.balance < 20 }
        val def = if (afford.isEmpty()) CardCatalog.all.first() else afford.random()
        return generateWithPixelBonus(def.type, state)
    }

    fun bestCardFor(state: GameState): ScratchCardInstance {
        val best = PixelEconomyUtils.bestCard(state) ?: CardCatalog.all.first()
        return generateWithPixelBonus(best.type, state)
    }

    // ============= KOMBO SİSTEMİ =============
    data class ComboState(
        val count: Int = 0,
        val lastTime: Long = 0L,
        val maxCombo: Int = 0,
        val totalCombos: Int = 0
    ) {
        fun isActive(): Boolean = System.currentTimeMillis() - lastTime < 500
        fun multiplier(): Float = when {
            count >= 10 -> 1.5f
            count >= 7 -> 1.35f
            count >= 5 -> 1.2f
            count >= 3 -> 1.1f
            else -> 1f
        }
        fun title(): String = when {
            count >= 10 -> "EFSANEVİ KOMBO"
            count >= 7 -> "SÜPER KOMBO"
            count >= 5 -> "MEGA KOMBO"
            count >= 3 -> "KOMBO"
            else -> ""
        }
    }

    fun updateCombo(prev: ComboState, scored: Boolean): ComboState {
        if (!scored) return prev.copy(count = 0)
        val now = System.currentTimeMillis()
        val isQuick = now - prev.lastTime < 800
        val newCount = if (isQuick) prev.count + 1 else 1
        return ComboState(
            count = newCount,
            lastTime = now,
            maxCombo = maxOf(prev.maxCombo, newCount),
            totalCombos = prev.totalCombos + 1
        )
    }

    fun comboReward(combo: Int): Long = when {
        combo >= 10 -> 500
        combo >= 7 -> 200
        combo >= 5 -> 100
        combo >= 3 -> 50
        else -> 0
    }

    // ============= GÜNLÜK GÖREV ENTEGRASYON =============
    fun checkDailyProgress(state: GameState, challengeType: String): Int {
        return when (challengeType) {
            "scratch" -> state.totalScratched % 10
            "jackpot" -> state.totalJackpots % 3
            else -> 0
        }
    }

    // ============= EKONOMİ ENTEGRASYON =============
    fun applyMarket(event: MarketEvent?, price: Int): Int {
        if (event == null) return price
        return (price * event.multiplier).toInt()
    }

    fun applyInflation(price: Int, totalScratched: Int): Int {
        return PixelInflation.price(price, totalScratched)
    }

    // ============= KOLEKSİYON ENTEGRASYON =============
    fun collectionBonus(state: GameState): Float = PixelCollection.masteryBonus(state)

    fun foilForLevel(level: Int): FoilType = when {
        level >= 10 -> FoilType.RAINBOW
        level >= 7 -> FoilType.HOLOGRAM
        level >= 5 -> FoilType.GOLD
        level >= 3 -> FoilType.SILVER
        else -> FoilType.NORMAL
    }

    // ============= PRESTIJ ENTEGRASYON =============
    fun prestigeBonusText(state: GameState): String {
        val nodes = PixelPrestigeExpanded.nodes.filter { PixelPrestigeExpanded.unlocked(state, it.id) }
        return nodes.joinToString(" ") { it.icon }
    }

    // ============= BAŞARI ENTEGRASYON =============
    fun checkAchievements(old: GameState, new: GameState): List<String> {
        val before = PixelAchievements.unlocked(old).map { it.id }.toSet()
        val after = PixelAchievements.unlocked(new).map { it.id }.toSet()
        return (after - before).toList()
    }

    // ============= İSTATİSTİK ENTEGRASYON =============
    fun statsSummary(state: GameState): String = PixelStats.detailText(state)

    // ============= ZORLUK AYARI =============
    enum class Difficulty(val label: String, val luckMod: Float, val priceMod: Float, val payoutMod: Float) {
        EASY("KOLAY", 0.1f, 0.8f, 1.2f),
        NORMAL("NORMAL", 0f, 1f, 1f),
        HARD("ZOR", -0.05f, 1.1f, 1f),
        NIGHTMARE("KABUS", -0.1f, 1.3f, 0.9f),
        PIXEL_HELL("PİXEL CEHENNEM", -0.15f, 1.5f, 0.8f)
    }

    fun difficultyFor(state: GameState): Difficulty {
        return when {
            state.prestige.prestigeCount >= 5 -> Difficulty.PIXEL_HELL
            state.prestige.prestigeCount >= 3 -> Difficulty.NIGHTMARE
            state.totalScratched >= 500 -> Difficulty.HARD
            else -> Difficulty.NORMAL
        }
    }

    fun adjustForDifficulty(base: Int, difficulty: Difficulty, type: String): Int {
        return when (type) {
            "price" -> (base * difficulty.priceMod).toInt()
            "payout" -> (base * difficulty.payoutMod).toInt()
            else -> base
        }
    }

    // ============= RANDOM EVENT =============
    data class RandomEvent(
        val title: String,
        val desc: String,
        val icon: String,
        val effect: (GameState) -> GameState
    )

    fun randomEvent(): RandomEvent? {
        if (Random.nextFloat() > 0.08f) return null
        val events = listOf(
            RandomEvent("Altın Rüzgar", "+100$ bonus", "🌬️", { it.copy(balance = it.balance + 100) }),
            RandomEvent("Şans Kuşu", "Şans +5% 3 kazı", "🐦", { it }),
            RandomEvent("Toz Fırtınası", "Kazım yavaş", "🌪️", { it }),
            RandomEvent("Mystery Box", "Rastgele kart", "📦", { it }),
            RandomEvent("Vergi", "-50$ vergi", "🏛️", { it.copy(balance = (it.balance - 50).coerceAtLeast(0)) })
        )
        return events.random()
    }

    fun triggerRandomEvent(state: GameState): Pair<GameState, String?> {
        val ev = randomEvent() ?: return state to null
        val ns = ev.effect(state)
        return ns.copy(history = (listOf("Olay: ${ev.title} ${ev.icon}") + ns.history).take(20)) to ev.title
    }

    // ============= KART ÖNERİSİ =============
    fun recommendCard(state: GameState): CardType {
        val affordable = CardCatalog.all.filter { it.cost <= state.balance }
        if (affordable.isEmpty()) return CardType.QUICK_CASH
        // en yüksek EV
        return affordable.maxByOrNull { def ->
            val lvl = state.cardLevels[def.id] ?: 1
            val payout = def.basePayout * (1 + (lvl - 1) * 0.25f)
            val win = when (def.type) {
                CardType.QUICK_CASH -> 0.42f
                CardType.SNAKE_EYES -> 0.30f
                CardType.APPLE_TREE -> 0.32f
                CardType.LUCKY_CAT -> 0.28f
                CardType.SCRATCH_MY_BACK -> 0.18f
                CardType.MEGA_JACKPOT -> 0.12f
                CardType.FINAL_CHANCE -> 0.01f
            }
            payout * win - def.cost
        }?.type ?: CardType.QUICK_CASH
    }

    fun recommendText(state: GameState): String {
        val rec = recommendCard(state)
        val def = CardCatalog.all.find { it.type == rec }!!
        return "Öneri: ${def.nameTr} (${def.cost}$) - En karlı"
    }

    // ============= HIZLI İŞLEMLER =============
    fun quickBuy(state: GameState, count: Int = 5): Pair<GameState, String> {
        var cur = state
        var bought = 0
        repeat(count) {
            val rec = recommendCard(cur)
            val def = CardCatalog.all.find { it.type == rec }!!
            if (cur.balance < def.cost) return@repeat
            val card = generateWithPixelBonus(rec, cur)
            cur = cur.copy(
                balance = cur.balance - def.cost + maxOf(0, card.payout),
                totalScratched = cur.totalScratched + 1,
                history = (listOf("Hızlı: ${def.nameTr} ${if (card.payout > 0) "+${card.payout}$" else "kayıp"}") + cur.history).take(20)
            )
            bought++
        }
        return cur to "$bought kart hızlı alındı"
    }

    fun autoPlay(state: GameState, ticks: Int = 10): GameState {
        var cur = state
        repeat(ticks) {
            if (cur.balance < 5) return@repeat
            val card = randomCard(cur)
            val cost = card.definition.cost
            if (cur.balance < cost) return@repeat
            cur = cur.copy(
                balance = cur.balance - cost + maxOf(0, card.payout),
                totalScratched = cur.totalScratched + 1
            )
        }
        return cur
    }

    // ============= UZUN LOG =============
    fun extendedLog(state: GameState): String {
        val sb = StringBuilder()
        sb.appendLine("=== PIXEL GAME CORE LOG ===")
        sb.appendLine("Difficulty: ${difficultyFor(state).label}")
        sb.appendLine("Recommended: ${recommendText(state)}")
        sb.appendLine("Collection bonus: ${collectionBonus(state)}")
        sb.appendLine("Prestige: ${prestigeBonusText(state)}")
        sb.appendLine("Market: ${PixelMarket.activeFor(state)?.title ?: "Yok"}")
        sb.appendLine("Combo: ${PixelCombo.forCombo(5)?.title ?: "Yok"}")
        repeat(50) { i -> sb.appendLine("Core log ${i + 1}: ${Random.nextInt(10000)}") }
        return sb.toString()
    }

    fun allSystemsStatus(state: GameState): String {
        return """
            |Market: ${PixelMarket.activeFor(state)?.icon ?: "-"} 
            |Daily: ${PixelDailySystem.today(state)?.icon ?: "-"}
            |Challenge: ${PixelChallenges.all(state).size} görev
            |Collection: ${PixelCollection.statsText(state)}
            |Leader: ${PixelLeaderboard.rankText(state)}
            |Prestige: ${PixelPrestigeExpanded.stats(state)}
            |Economy: ${PixelEconomyUtils.formatMoney(state.balance)}
        """.trimMargin()
    }

    fun pixelProgressAll(state: GameState): Map<String, Float> = mapOf(
        "scratch" to (state.totalScratched / 1000f).coerceIn(0f, 1f),
        "jackpot" to (state.totalJackpots / 100f).coerceIn(0f, 1f),
        "money" to (state.balance / 100000f).coerceIn(0f, 1f),
        "prestige" to (state.prestige.prestigeCount / 10f).coerceIn(0f, 1f),
        "collection" to PixelCollection.completion(state),
        "leader" to PixelLeaderboard.playerProgress(state)
    )

    fun overallProgress(state: GameState): Float = pixelProgressAll(state).values.average().toFloat()

    fun overallProgressText(state: GameState): String = "${(overallProgress(state) * 100).toInt()}% tamamlandı"

    fun nextGoal(state: GameState): String {
        val prog = pixelProgressAll(state)
        val lowest = prog.minByOrNull { it.value } ?: return "Bitti!"
        return "Sonraki hedef: ${lowest.key} (${(lowest.value * 100).toInt()}%)"
    }

    fun randomTip(): String = listOf(
        "Şans Lv4 kritik eşik!",
        "Hızlı Nakit en güvenli başlangıç",
        "Kara kediden kaç!",
        "Jackpot Magnet 2x yapar",
        "Prestij her şeyi hızlandırır",
        "Kombo bonusu kaçırma",
        "Enflasyona dikkat",
        "Günlük ödülü al"
    ).random()

    fun tipForState(state: GameState): String {
        if (state.balance < 10) return "Bulaşık yıka, 5$ biriktir"
        if (state.upgrades.luckLevel < 4) return "Şansı 4 yap!"
        if (!state.upgrades.autoScratcherUnlocked && state.balance >= 500) return "Botu aç!"
        if (state.prestige.prestigeCount == 0 && state.totalJackpots >= 5) return "Prestij zamanı!"
        return randomTip()
    }

    fun debugInfo(state: GameState): String {
        return """
            |=== DEBUG ===
            |State: $state
            |Combo: ${PixelEconomyUtils.suggestUpgrade(state)}
            |Market: ${PixelMarket.activeFor(state)}
            |Daily: ${PixelDailySystem.streakDays(state)}
            |Leader: ${PixelLeaderboard.playerRank(state)}
            |Collection: ${PixelCollection.masteredCount(state)}
            |Challenges: ${PixelChallenges.completed(state).size}
            |Sound: ${PixelSoundManager.enabled}
            |Haptics: ${PixelHaptics.enabled}
        """.trimMargin()
    }
}
