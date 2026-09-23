package com.scritchyscratchy.watch.pixel

import com.scritchyscratchy.watch.CardDefinition
import com.scritchyscratchy.watch.CardCatalog
import com.scritchyscratchy.watch.GameState
import com.scritchyscratchy.watch.CardType

// ============= PIXEL COLLECTION - KART KOLEKSİYONU =============
// Her kart tipi için seviye, nadirlik, foil, toplama

data class CollectionEntry(
    val def: CardDefinition,
    val level: Int,
    val totalScratched: Int,
    val jackpots: Int,
    val bestPayout: Int,
    val isMastered: Boolean,
    val foil: FoilType
)

enum class FoilType(val label: String, val color: Long, val multiplier: Float) {
    NORMAL("Normal", 0xFF9E9E9E, 1f),
    SILVER("Gümüş", 0xFFB0BEC5, 1.1f),
    GOLD("Altın", 0xFFFFD600, 1.25f),
    HOLOGRAM("Hologram", 0xFF00E5FF, 1.5f),
    RAINBOW("Gökkuşağı", 0xFFD500F9, 2f)
}

object PixelCollection {

    fun entryFor(state: GameState, def: CardDefinition): CollectionEntry {
        val lvl = state.cardLevels[def.id] ?: 1
        // simülasyon: her kart için toplam kazı ve jackpot tahmini
        val total = (state.totalScratched * 0.15f).toInt() + lvl * 2
        val jps = (state.totalJackpots * 0.12f).toInt()
        val best = (def.basePayout * (1 + (lvl - 1) * 0.25f)).toInt()
        val mastered = lvl >= 10
        val foil = when {
            lvl >= 10 -> FoilType.RAINBOW
            lvl >= 7 -> FoilType.HOLOGRAM
            lvl >= 5 -> FoilType.GOLD
            lvl >= 3 -> FoilType.SILVER
            else -> FoilType.NORMAL
        }
        return CollectionEntry(def, lvl, total, jps, best, mastered, foil)
    }

    fun allEntries(state: GameState): List<CollectionEntry> =
        CardCatalog.all.map { entryFor(state, it) }

    fun masteredCount(state: GameState): Int = allEntries(state).count { it.isMastered }

    fun totalLevels(state: GameState): Int = state.cardLevels.values.sum()

    fun maxLevel(): Int = 10

    fun completion(state: GameState): Float {
        val max = CardCatalog.all.size * maxLevel()
        return totalLevels(state).toFloat() / max
    }

    fun nextToMaster(state: GameState): CollectionEntry? =
        allEntries(state).filter { !it.isMastered }.minByOrNull { it.level }

    fun byFoil(state: GameState, foil: FoilType): List<CollectionEntry> =
        allEntries(state).filter { it.foil == foil }

    fun foilProgress(state: GameState): Map<FoilType, Int> {
        return FoilType.values().associateWith { foil -> byFoil(state, foil).size }
    }

    fun rarest(state: GameState): CollectionEntry? =
        allEntries(state).maxByOrNull { it.def.jackpotPayout }

    fun cheapest(state: GameState): CollectionEntry? =
        allEntries(state).minByOrNull { it.def.cost }

    fun mostProfitable(state: GameState): CollectionEntry? =
        allEntries(state).maxByOrNull { it.bestPayout - it.def.cost }

    fun statsText(state: GameState): String {
        val m = masteredCount(state)
        val t = CardCatalog.all.size
        val c = (completion(state) * 100).toInt()
        return "$m/$t mastered • $c% • Lv toplam ${totalLevels(state)}"
    }

    fun foilText(state: GameState): String {
        val map = foilProgress(state)
        return FoilType.values().joinToString(" ") { "${it.label.first()}:${map[it]}" }
    }

    fun rewardForMastery(entry: CollectionEntry): String = when (entry.foil) {
        FoilType.SILVER -> "+5% ödül"
        FoilType.GOLD -> "+10% ödül +1JP"
        FoilType.HOLOGRAM -> "+15% ödül"
        FoilType.RAINBOW -> "+25% + efsane rozet"
        else -> "-"
    }

    fun masteryBonus(state: GameState): Float {
        val m = masteredCount(state)
        return 1f + m * 0.02f // her master %2
    }

    fun totalBonusText(state: GameState): String =
        "+${((masteryBonus(state) - 1) * 100).toInt()}% koleksiyon bonusu"

    fun upgradeCost(entry: CollectionEntry): Int {
        // seviye başına maliyet artar
        return (entry.def.cost * (1 + entry.level * 0.4f)).toInt()
    }

    fun canUpgrade(state: GameState, entry: CollectionEntry): Boolean =
        state.balance >= upgradeCost(entry) && entry.level < maxLevel()

    fun upgrade(state: GameState, def: CardDefinition): GameState {
        val lvl = state.cardLevels[def.id] ?: 1
        if (lvl >= maxLevel()) return state
        val cost = (def.cost * (1 + lvl * 0.4f)).toInt()
        if (state.balance < cost) return state
        val newLevels = state.cardLevels.toMutableMap()
        newLevels[def.id] = lvl + 1
        return state.copy(
            balance = state.balance - cost,
            cardLevels = newLevels,
            history = (listOf("${def.nameTr} Lv${lvl + 1}! ${rewardForMastery(entryFor(state, def))}") + state.history).take(20)
        )
    }

    fun sortByLevel(state: GameState): List<CollectionEntry> =
        allEntries(state).sortedByDescending { it.level }

    fun sortByProfit(state: GameState): List<CollectionEntry> =
        allEntries(state).sortedByDescending { it.bestPayout }

    fun filterByFoil(state: GameState, foil: FoilType): List<CollectionEntry> =
        allEntries(state).filter { it.foil == foil }

    fun search(state: GameState, query: String): List<CollectionEntry> =
        allEntries(state).filter { it.def.nameTr.contains(query, true) || it.def.id.contains(query, true) }

    fun randomEntry(state: GameState): CollectionEntry =
        allEntries(state).random()

    fun dailyFeatured(state: GameState): CollectionEntry {
        val idx = (state.totalScratched / 15) % CardCatalog.all.size
        return entryFor(state, CardCatalog.all[idx])
    }

    fun featuredBonus(entry: CollectionEntry): String =
        "Günün kartı: ${entry.def.nameTr} +%20 ödül!"

    fun foilColor(foil: FoilType): Long = foil.color

    fun levelDots(level: Int): String = "●".repeat(level) + "○".repeat(10 - level)

    fun levelBar(level: Int): String {
        val filled = "█".repeat(level)
        val empty = "░".repeat(10 - level)
        return filled + empty + " $level/10"
    }

    fun collectionRank(state: GameState): Pair<String, String> {
        val c = completion(state)
        return when {
            c >= 0.9f -> "EFSANE KOLEKSİYONCU" to "👑"
            c >= 0.7f -> "USTA" to "🏆"
            c >= 0.5f -> "UZMAN" to "💎"
            c >= 0.3f -> "DENEYİMLİ" to "⭐"
            else -> "ÇIRAK" to "📚"
        }
    }

    fun nextFoilProgress(state: GameState): String {
        val next = nextToMaster(state) ?: return "Hepsi master!"
        val need = maxLevel() - next.level
        return "${next.def.nameTr} için $need seviye kaldı → ${next.foil.next()?.label ?: "MAX"}"
    }

    private fun FoilType.next(): FoilType? = when (this) {
        FoilType.NORMAL -> FoilType.SILVER
        FoilType.SILVER -> FoilType.GOLD
        FoilType.GOLD -> FoilType.HOLOGRAM
        FoilType.HOLOGRAM -> FoilType.RAINBOW
        FoilType.RAINBOW -> null
    }

    fun totalCardsText(state: GameState): String =
        "Toplam kart: ${CardCatalog.all.size} • Seviye toplam: ${totalLevels(state)}"

    fun jackpotRate(entry: CollectionEntry): Float {
        // simülasyon
        return when (entry.def.type) {
            CardType.QUICK_CASH -> 0.05f
            CardType.MEGA_JACKPOT -> 0.02f
            CardType.FINAL_CHANCE -> 0.01f
            else -> 0.03f
        }
    }

    fun expectedValue(entry: CollectionEntry): Float {
        val win = jackpotRate(entry)
        return entry.bestPayout * win - entry.def.cost
    }

    fun bestExpected(state: GameState): CollectionEntry? =
        allEntries(state).maxByOrNull { expectedValue(it) }

    fun worstExpected(state: GameState): CollectionEntry? =
        allEntries(state).minByOrNull { expectedValue(it) }

    fun advice(state: GameState): String {
        val best = bestExpected(state) ?: return "Bekle"
        return "En karlı: ${best.def.nameTr} (EV ${"%.1f".format(expectedValue(best))}$)"
    }

    fun historyFor(entry: CollectionEntry, state: GameState): String {
        return "${entry.def.nameTr} Lv${entry.level} • ${entry.totalScratched} kazı • ${entry.jackpots} jackpot • En iyi ${entry.bestPayout}$ • Foil ${entry.foil.label}"
    }

    fun exportCsv(state: GameState): String {
        val header = "id,level,foil,jackpots,best\n"
        val rows = allEntries(state).joinToString("\n") { "${it.def.id},${it.level},${it.foil.label},${it.jackpots},${it.bestPayout}" }
        return header + rows
    }

    fun pixelArtFor(entry: CollectionEntry): String = when (entry.def.type) {
        CardType.QUICK_CASH -> "▓▓░░▓▓\n▓▓▓▓▓▓\n░░▓▓░░"
        CardType.SNAKE_EYES -> "⚀⚁⚂\n⚃⚄⚅"
        CardType.APPLE_TREE -> "🍎🍎🍎\n🪱🍎🪱"
        CardType.LUCKY_CAT -> "😺😺\n🐈‍⬛😺"
        CardType.SCRATCH_MY_BACK -> "🐢🪼\n🛍️🐢"
        CardType.MEGA_JACKPOT -> "💎💎💎\n👑💎👑"
        CardType.FINAL_CHANCE -> "☠️☠️☠️"
    }

    fun longDescription(entry: CollectionEntry): String {
        return """
            |${entry.def.nameTr} (${entry.def.id})
            |Seviye: ${entry.level}/10 ${levelBar(entry.level)}
            |Foil: ${entry.foil.label} (+${((entry.foil.multiplier - 1) * 100).toInt()}%)
            |Fiyat: ${entry.def.cost}$ • Ödül: ${entry.bestPayout}$ • Jackpot: ${entry.def.jackpotPayout}$
            |Açıklama: ${entry.def.descriptionTr}
            |Toplam kazı: ${entry.totalScratched} • Jackpot: ${entry.jackpots}
            |Master: ${if (entry.isMastered) "EVET 👑" else "HAYIR"}
            |Bonus: ${rewardForMastery(entry)}
            |EV: ${"%.2f".format(expectedValue(entry))}$
            |Pixel: ${pixelArtFor(entry)}
        """.trimMargin()
    }
}
