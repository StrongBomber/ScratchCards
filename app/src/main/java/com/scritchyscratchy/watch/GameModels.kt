package com.scritchyscratchy.watch

import kotlin.random.Random

// Galaxy Watch 8 - 480x480 round - Wear OS 6
// Scritchy Scratchy faithful clone models

enum class CardType {
    QUICK_CASH,          // Basit Eşleştirme - 3 alan, 2 eşleşme kazanır
    SNAKE_EYES,          // Zar - 1 gelirse ceza
    SCRATCH_MY_BACK,     // Kaplumbağa - deniz anası vs poşet
    APPLE_TREE,          // Elma Ağacı - solucan ceza
    LUCKY_CAT,           // Şanslı Kedi - kara kedi ceza
    MEGA_JACKPOT,        // Mega kart - çoklu panel
    FINAL_CHANCE         // Son Şans - %1 win, prestige tetikler
}

data class CardDefinition(
    val type: CardType,
    val id: String,
    val name: String,
    val nameTr: String,
    val cost: Int,
    val basePayout: Int,
    val jackpotPayout: Int,
    val description: String,
    val descriptionTr: String,
    val symbolCount: Int, // kaç alan kazınacak
    val hasPenalty: Boolean,
    val icon: String,
    val colorHex: Long
)

object CardCatalog {
    val all = listOf(
        CardDefinition(
            CardType.QUICK_CASH, "quick_cash", "Quick Cash", "Hızlı Nakit",
            cost = 5, basePayout = 10, jackpotPayout = 50,
            description = "Match 2 symbols to win", descriptionTr = "2 sembol eşleştir kazan",
            symbolCount = 3, hasPenalty = false, icon = "💰", colorHex = 0xFF4CAF50
        ),
        CardDefinition(
            CardType.SNAKE_EYES, "snake_eyes", "Snake Eyes", "Yılan Gözü",
            cost = 15, basePayout = 30, jackpotPayout = 150,
            description = "Avoid dice with 1", descriptionTr = "1'li zardan kaçın",
            symbolCount = 3, hasPenalty = true, icon = "🎲", colorHex = 0xFFF44336
        ),
        CardDefinition(
            CardType.APPLE_TREE, "apple_tree", "Apple Tree", "Elma Ağacı",
            cost = 30, basePayout = 60, jackpotPayout = 300,
            description = "Worm = penalty", descriptionTr = "Solucan = ceza",
            symbolCount = 5, hasPenalty = true, icon = "🍎", colorHex = 0xFF8BC34A
        ),
        CardDefinition(
            CardType.LUCKY_CAT, "lucky_cat", "Lucky Cat", "Şanslı Kedi",
            cost = 60, basePayout = 120, jackpotPayout = 600,
            description = "Black cat = lose", descriptionTr = "Kara kedi = kayıp",
            symbolCount = 5, hasPenalty = true, icon = "🐱", colorHex = 0xFFFFC107
        ),
        CardDefinition(
            CardType.SCRATCH_MY_BACK, "scratch_my_back", "Scratch My Back", "Sırtımı Kaşı",
            cost = 150, basePayout = 300, jackpotPayout = 2500,
            description = "Jellyfish BIG win, bag = loss", descriptionTr = "Denizanası BÜYÜK kazan, poşet kayıp",
            symbolCount = 6, hasPenalty = true, icon = "🐢", colorHex = 0xFF00BCD4
        ),
        CardDefinition(
            CardType.MEGA_JACKPOT, "mega_jackpot", "Mega Stack", "Mega Deste",
            cost = 500, basePayout = 1000, jackpotPayout = 10000,
            description = "High risk, massive reward", descriptionTr = "Yüksek risk, dev ödül",
            symbolCount = 9, hasPenalty = true, icon = "💎", colorHex = 0xFF9C27B0
        ),
        CardDefinition(
            CardType.FINAL_CHANCE, "final_chance", "Final Chance", "Son Şans",
            cost = 2500, basePayout = 0, jackpotPayout = 50000,
            description = "1% win - Prestige!", descriptionTr = "%1 kazan - Prestij!",
            symbolCount = 1, hasPenalty = false, icon = "☠️", colorHex = 0xFF212121
        )
    )

    fun byId(id: String) = all.find { it.id == id } ?: all.first()
    fun affordable(balance: Int) = all.filter { it.cost <= balance * 3 } // show reachable
}

// A generated scratch card instance
data class ScratchCardInstance(
    val uid: String = Random.nextInt(100000, 999999).toString(),
    val definition: CardDefinition,
    val level: Int, // card level (upgrade)
    val symbols: List<ScratchSymbol>,
    val payout: Int, // final payout if winner (0 if loser/penalty)
    val isJackpot: Boolean,
    val isPenalty: Boolean,
    val jackpotTier: Int = 0 // 0 none, 1 small, 2 big
)

enum class ScratchSymbol(val display: String, val displayTr: String) {
    CHERRY("🍒","🍒"), LEMON("🍋","🍋"), SEVEN("7️⃣","7️⃣"),
    DIAMOND("💎","💎"), CROWN("👑","👑"),
    DICE_1("⚀","⚀"), DICE_2("⚁","⚁"), DICE_3("⚂","⚂"), DICE_4("⚃","⚃"), DICE_5("⚄","⚄"), DICE_6("⚅","⚅"),
    APPLE("🍎","🍎"), WORM("🪱","🪱"),
    CAT_GOOD("😺","😺"), CAT_BAD("🐈‍⬛","🐈‍⬛"),
    TURTLE("🐢","🐢"), JELLYFISH("🪼","🪼"), BAG("🛍️","🛍️"),
    COIN("🪙","🪙"), STAR("⭐","⭐"),
    SKULL("💀","💀"), TROPHY("🏆","🏆")
}

data class PlayerUpgrades(
    val luckLevel: Int = 0,       // max 10
    val scratchPowerLevel: Int = 0, // max 10
    val areaSizeLevel: Int = 0,     // max 10
    val autoScratcherUnlocked: Boolean = false,
    val trashCanUnlocked: Boolean = false
) {
    val luckBonus: Double get() = luckLevel * 0.04 // +4% per level
    val scratchPower: Float get() = 1f + scratchPowerLevel * 0.25f
    val areaSize: Float get() = 1f + areaSizeLevel * 0.20f
}

data class PrestigeUpgrades(
    val headStartCapital: Boolean = false, // 500$ start
    val luckyLegacy: Boolean = false, // +15% luck
    val scratchMemory: Boolean = false, // auto from start
    val goldenHands: Boolean = false, // +25% payouts
    val jackpotMagnet: Boolean = false, // 2x jackpot
    val speedDemon: Boolean = false, // power tier 2
    val banco: Boolean = false, // no bankruptcy
    val recyclerPro: Boolean = false // 10% trash back
)

data class PrestigeState(
    val jackPoints: Int = 0,
    val totalJackPointsEarned: Int = 0,
    val prestigeCount: Int = 0,
    val upgrades: PrestigeUpgrades = PrestigeUpgrades()
)

data class GameState(
    val balance: Long = 50, // start 50$ like demo after plates
    val totalScratched: Int = 0,
    val totalWon: Long = 0,
    val totalJackpots: Int = 0,
    val cardLevels: Map<String, Int> = CardCatalog.all.associate { it.id to 1 },
    val upgrades: PlayerUpgrades = PlayerUpgrades(),
    val prestige: PrestigeState = PrestigeState(),
    val autoQueue: Int = 0, // cards in auto
    val hasCompletedDishJob: Boolean = false, // intro
    val loanTaken: Int = 0,
    val currentCard: ScratchCardInstance? = null,
    val history: List<String> = emptyList()
)

// Prices for upgrades - exponential
object UpgradePricing {
    fun luckCost(level: Int) = (50 * Math.pow(1.8, level.toDouble())).toInt()
    fun powerCost(level: Int) = (40 * Math.pow(1.7, level.toDouble())).toInt()
    fun areaCost(level: Int) = (60 * Math.pow(1.75, level.toDouble())).toInt()
    fun autoCost() = 500
    fun trashCost() = 300

    fun prestigeCost(id: String): Int = when(id) {
        "headStart" -> 10
        "luckyLegacy" -> 15
        "scratchMemory" -> 20
        "goldenHands" -> 25
        "jackpotMagnet" -> 30
        "speedDemon" -> 20
        "banco" -> 35
        "recyclerPro" -> 15
        else -> 999
    }
}

// Core RNG logic - replicates Scritchy Scratchy odds, luck-modified
object CardGenerator {
    fun generate(def: CardDefinition, cardLevel: Int, playerLuck: Double, prestige: PrestigeState): ScratchCardInstance {
        // Base win chance per card type
        val baseWinChance = when(def.type) {
            CardType.QUICK_CASH -> 0.42
            CardType.SNAKE_EYES -> 0.30
            CardType.APPLE_TREE -> 0.32
            CardType.LUCKY_CAT -> 0.28
            CardType.SCRATCH_MY_BACK -> 0.18
            CardType.MEGA_JACKPOT -> 0.12
            CardType.FINAL_CHANCE -> 0.01
        }
        val luckBonus = playerLuck + if(prestige.upgrades.luckyLegacy) 0.15 else 0.0
        val winChance = (baseWinChance + luckBonus).coerceIn(0.01, 0.85)
        val isJackpotRoll = Random.nextDouble() < (0.05 + luckBonus*0.3 + if(prestige.upgrades.jackpotMagnet) 0.05 else 0.0)
        val isWinner = Random.nextDouble() < winChance

        // Penalty chance reduced by luck
        val penaltyChance = if(def.hasPenalty) (0.18 - luckBonus*0.15).coerceIn(0.02, 0.18) else 0.0
        val isPenalty = !isWinner && def.hasPenalty && Random.nextDouble() < penaltyChance

        val symbols = generateSymbols(def, isWinner, isPenalty, isJackpotRoll)
        val levelMultiplier = 1.0 + (cardLevel - 1) * 0.25 // +25% per level
        val prestigeMultiplier = if(prestige.upgrades.goldenHands) 1.25 else 1.0
        val rawPayout = when {
            isPenalty -> -(def.cost * 2 + Random.nextInt(def.cost))
            isJackpotRoll && isWinner -> (def.jackpotPayout * levelMultiplier * prestigeMultiplier).toInt()
            isWinner -> (def.basePayout * levelMultiplier * prestigeMultiplier).toInt()
            else -> 0
        }
        return ScratchCardInstance(
            definition = def,
            level = cardLevel,
            symbols = symbols,
            payout = rawPayout,
            isJackpot = isJackpotRoll && isWinner,
            isPenalty = isPenalty,
            jackpotTier = if(isJackpotRoll && isWinner) 2 else if(isWinner) 1 else 0
        )
    }

    private fun generateSymbols(def: CardDefinition, isWinner: Boolean, isPenalty: Boolean, isJackpot: Boolean): List<ScratchSymbol> {
        return when(def.type) {
            CardType.QUICK_CASH -> {
                if(isWinner) {
                    val sym = listOf(ScratchSymbol.CHERRY, ScratchSymbol.LEMON, ScratchSymbol.STAR).random()
                    listOf(sym, sym, ScratchSymbol.values().filter { it != sym }.random())
                        .shuffled()
                } else {
                    listOf(ScratchSymbol.CHERRY, ScratchSymbol.LEMON, ScratchSymbol.STAR).shuffled().take(3)
                }
            }
            CardType.SNAKE_EYES -> {
                if(isPenalty) listOf(ScratchSymbol.DICE_1, ScratchSymbol.DICE_3, ScratchSymbol.DICE_5)
                else if(isWinner) listOf(ScratchSymbol.DICE_4, ScratchSymbol.DICE_5, ScratchSymbol.DICE_6)
                else listOf(ScratchSymbol.DICE_2, ScratchSymbol.DICE_3, ScratchSymbol.DICE_6)
            }
            CardType.APPLE_TREE -> {
                if(isPenalty) listOf(ScratchSymbol.APPLE, ScratchSymbol.APPLE, ScratchSymbol.WORM, ScratchSymbol.APPLE, ScratchSymbol.WORM)
                else if(isWinner) List(5){ ScratchSymbol.APPLE }
                else listOf(ScratchSymbol.APPLE, ScratchSymbol.APPLE, ScratchSymbol.APPLE, ScratchSymbol.WORM, ScratchSymbol.APPLE)
            }
            CardType.LUCKY_CAT -> {
                if(isPenalty) listOf(ScratchSymbol.CAT_BAD, ScratchSymbol.CAT_GOOD, ScratchSymbol.CAT_BAD, ScratchSymbol.CAT_GOOD, ScratchSymbol.CAT_GOOD)
                else if(isWinner) List(5){ ScratchSymbol.CAT_GOOD }
                else listOf(ScratchSymbol.CAT_GOOD, ScratchSymbol.CAT_GOOD, ScratchSymbol.CAT_BAD, ScratchSymbol.CAT_GOOD, ScratchSymbol.CAT_GOOD)
            }
            CardType.SCRATCH_MY_BACK -> {
                if(isJackpot) listOf(ScratchSymbol.JELLYFISH, ScratchSymbol.JELLYFISH, ScratchSymbol.TURTLE, ScratchSymbol.JELLYFISH, ScratchSymbol.TURTLE, ScratchSymbol.TURTLE)
                else if(isPenalty) listOf(ScratchSymbol.BAG, ScratchSymbol.BAG, ScratchSymbol.TURTLE, ScratchSymbol.TURTLE, ScratchSymbol.BAG, ScratchSymbol.TURTLE)
                else if(isWinner) listOf(ScratchSymbol.JELLYFISH, ScratchSymbol.TURTLE, ScratchSymbol.TURTLE, ScratchSymbol.TURTLE, ScratchSymbol.JELLYFISH, ScratchSymbol.TURTLE)
                else listOf(ScratchSymbol.TURTLE, ScratchSymbol.BAG, ScratchSymbol.TURTLE, ScratchSymbol.TURTLE, ScratchSymbol.TURTLE, ScratchSymbol.TURTLE)
            }
            CardType.MEGA_JACKPOT -> {
                if(isJackpot) List(9){ if(Random.nextBoolean()) ScratchSymbol.DIAMOND else ScratchSymbol.CROWN }
                else if(isWinner) List(9){ if(Random.nextDouble()<0.6) ScratchSymbol.STAR else ScratchSymbol.COIN }
                else List(9){ listOf(ScratchSymbol.COIN, ScratchSymbol.STAR, ScratchSymbol.BAG).random() }
            }
            CardType.FINAL_CHANCE -> {
                if(isWinner) listOf(ScratchSymbol.TROPHY) else listOf(ScratchSymbol.SKULL)
            }
        }
    }
}
