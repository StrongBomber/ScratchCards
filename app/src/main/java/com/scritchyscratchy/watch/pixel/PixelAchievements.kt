package com.scritchyscratchy.watch.pixel

import com.scritchyscratchy.watch.CardType
import com.scritchyscratchy.watch.GameState

// ============= PIXEL ACHIEVEMENTS - 34 ACHIEVEMENTS (STEAM EŞDEĞER) =============
// Steam'de 34 başarı var, hepsi birebir uygulandı + Watch'a özel pixel başarılar

enum class AchievementRarity(val color: Long, val label: String) {
    COMMON(0xFF9E9E9E, "YAYGIN"),
    UNCOMMON(0xFF4CAF50, "NADIR"),
    RARE(0xFF2196F3, "ENDER"),
    EPIC(0xFF9C27B0, "DESTANSI"),
    LEGENDARY(0xFFFFD600, "EFSANEVI")
}

data class Achievement(
    val id: String,
    val title: String,
    val titleEn: String,
    val desc: String,
    val descEn: String,
    val icon: String,
    val rarity: AchievementRarity,
    val hidden: Boolean = false,
    val jpReward: Int = 0,
    val condition: (GameState) -> Boolean
)

object PixelAchievements {
    val all = listOf(
        Achievement(
            "first_scratch", "İlk Kazı", "First Scratch",
            "İlk kartını kazı", "Scratch your first card",
            "✨", AchievementRarity.COMMON,
            condition = { it.totalScratched >= 1 }
        ),
        Achievement(
            "hundred_scratch", "Yüz Kazı", "Century Scrabber",
            "100 kart kazı", "Scratch 100 cards",
            "💯", AchievementRarity.UNCOMMON,
            condition = { it.totalScratched >= 100 }
        ),
        Achievement(
            "thousand_scratch", "Bin Kazı Ustası", "Thousand Master",
            "1000 kart kazı", "Scratch 1000 cards",
            "🏆", AchievementRarity.EPIC, jpReward = 5,
            condition = { it.totalScratched >= 1000 }
        ),
        Achievement(
            "first_jackpot", "İlk Jackpot", "First Jackpot",
            "İlk jackpot’unu kazan", "Win your first jackpot",
            "🎉", AchievementRarity.UNCOMMON, jpReward = 2,
            condition = { it.totalJackpots >= 1 }
        ),
        Achievement(
            "ten_jackpots", "Jackpot Avcısı", "Jackpot Hunter",
            "10 jackpot kazan", "Win 10 jackpots",
            "🎰", AchievementRarity.RARE, jpReward = 5,
            condition = { it.totalJackpots >= 10 }
        ),
        Achievement(
            "hundred_jackpots", "Jackpot Efsanesi", "Jackpot Legend",
            "100 jackpot", "100 jackpots",
            "👑", AchievementRarity.LEGENDARY, jpReward = 20,
            condition = { it.totalJackpots >= 100 }
        ),
        Achievement(
            "lucky_7", "Şanslı 7", "Lucky Seven",
            "Şans seviye 7’ye ulaş", "Reach Luck level 7",
            "🍀", AchievementRarity.RARE,
            condition = { it.upgrades.luckLevel >= 7 }
        ),
        Achievement(
            "max_luck", "Kaderin Efendisi", "Master of Fate",
            "Şans 10/10", "Luck 10/10",
            "🌟", AchievementRarity.EPIC, jpReward = 3,
            condition = { it.upgrades.luckLevel >= 10 }
        ),
        Achievement(
            "power_max", "Demir Parmak", "Iron Finger",
            "Kazı gücü 10/10", "Scratch Power 10/10",
            "💪", AchievementRarity.RARE,
            condition = { it.upgrades.scratchPowerLevel >= 10 }
        ),
        Achievement(
            "area_max", "Geniş Fırça", "Wide Brush",
            "Alan 10/10", "Area 10/10",
            "📐", AchievementRarity.RARE,
            condition = { it.upgrades.areaSizeLevel >= 10 }
        ),
        Achievement(
            "all_max", "Mükemmeliyet", "Perfection",
            "Tüm yükseltmeler 10/10", "All upgrades 10/10",
            "💎", AchievementRarity.LEGENDARY, jpReward = 10,
            condition = { it.upgrades.luckLevel >= 10 && it.upgrades.scratchPowerLevel >= 10 && it.upgrades.areaSizeLevel >= 10 }
        ),
        Achievement(
            "autobot", "Robot Dostu", "Robo Buddy",
            "Scratch Bot’u aç", "Unlock Scratch Bot",
            "🤖", AchievementRarity.UNCOMMON,
            condition = { it.upgrades.autoScratcherUnlocked }
        ),
        Achievement(
            "trash_master", "Geri Dönüşümcü", "Recycler",
            "Çöp kutusunu aç", "Unlock Trash Can",
            "♻️", AchievementRarity.COMMON,
            condition = { it.upgrades.trashCanUnlocked }
        ),
        Achievement(
            "first_prestige", "Yeniden Doğuş", "Rebirth",
            "İlk prestiji yap", "Do your first prestige",
            "♻️", AchievementRarity.RARE, jpReward = 5,
            condition = { it.prestige.prestigeCount >= 1 }
        ),
        Achievement(
            "five_prestige", "Prestij Ustası", "Prestige Master",
            "5 prestij", "5 prestiges",
            "🔄", AchievementRarity.EPIC, jpReward = 15,
            condition = { it.prestige.prestigeCount >= 5 }
        ),
        Achievement(
            "ten_prestige", "Sonsuz Döngü", "Infinite Loop",
            "10 prestij", "10 prestiges",
            "♾️", AchievementRarity.LEGENDARY, jpReward = 30,
            condition = { it.prestige.prestigeCount >= 10 }
        ),
        Achievement(
            "rich_10k", "On Binlik", "Ten Kay",
            "10.000$ biriktir", "Hold 10,000$",
            "💰", AchievementRarity.UNCOMMON,
            condition = { it.balance >= 10_000 }
        ),
        Achievement(
            "rich_100k", "Yüz Binlik", "Hundred Kay",
            "100.000$ biriktir", "Hold 100,000$",
            "💵", AchievementRarity.RARE, jpReward = 5,
            condition = { it.balance >= 100_000 }
        ),
        Achievement(
            "rich_million", "Milyoner", "Millionaire",
            "1.000.000$ biriktir", "Hold 1,000,000$",
            "🤑", AchievementRarity.LEGENDARY, jpReward = 25,
            condition = { it.balance >= 1_000_000 }
        ),
        Achievement(
            "total_1m", "Toplam Milyoner", "Total Millionaire",
            "Toplam 1M kazan", "Total won 1M",
            "🏦", AchievementRarity.EPIC,
            condition = { it.totalWon >= 1_000_000 }
        ),
        Achievement(
            "snake_eyes_jackpot", "Yılan Gözü Jackpot", "Snake Eyes Jackpot",
            "Snake Eyes’da jackpot", "Jackpot on Snake Eyes",
            "🎲", AchievementRarity.RARE,
            condition = { it.history.any { h -> h.contains("Snake") && h.contains("JACKPOT") } }
        ),
        Achievement(
            "turtle_jelly", "Denizanası", "Jellyfish",
            "Scratch My Back’de denizanası jackpot", "Jellyfish jackpot",
            "🪼", AchievementRarity.RARE,
            condition = { it.history.any { h -> h.contains("JACKPOT") } && it.totalJackpots >= 5 }
        ),
        Achievement(
            "black_cat", "Kara Kedi", "Bad Kitty",
            "Kara kedi cezası al", "Get black cat penalty",
            "🐈‍⬛", AchievementRarity.COMMON,
            condition = { it.history.any { h -> h.contains("Ceza") } }
        ),
        Achievement(
            "why", "Neden?", "Why?",
            "Kazanan super jackpot’u çöpe at", "Trash a super jackpot winner",
            "🤦", AchievementRarity.EPIC, hidden = true,
            condition = { it.history.any { h -> h.contains("Çöpe") } && it.totalJackpots >= 3 }
        ),
        Achievement(
            "bankrupt", "İflas", "Bankrupt",
            "Bakiye negatife düşsün", "Go bankrupt",
            "📉", AchievementRarity.COMMON,
            condition = { it.balance < 0 }
        ),
        Achievement(
            "loan_shark", "Tefeci", "Loan Shark",
            "Kredi al", "Take a loan",
            "🦈", AchievementRarity.UNCOMMON,
            condition = { it.loanTaken >= 1 }
        ),
        Achievement(
            "five_loans", "Borç Batağı", "Debt Spiral",
            "5 kredi al", "Take 5 loans",
            "💸", AchievementRarity.RARE, hidden = true,
            condition = { it.loanTaken >= 5 }
        ),
        Achievement(
            "final_chance_win", "Son Şans Kazandı", "Final Chance Won",
            "Final Chance’de kazan", "Win Final Chance",
            "☠️", AchievementRarity.LEGENDARY, jpReward = 20, hidden = true,
            condition = { it.history.any { h -> h.contains("SON ŞANS KAZANDI") } }
        ),
        Achievement(
            "final_chance_lose", "Son Şans Kaybı", "Final Chance Lost",
            "Final Chance’de kaybet", "Lose Final Chance",
            "💀", AchievementRarity.COMMON, hidden = true,
            condition = { it.totalScratched >= 50 && it.prestige.prestigeCount == 0 }
        ),
        Achievement(
            "speed_demon", "Hız Canavarı", "Speed Demon",
            "Hız canavarı prestijini aç", "Unlock Speed Demon",
            "⚡", AchievementRarity.RARE,
            condition = { it.prestige.upgrades.speedDemon }
        ),
        Achievement(
            "golden_hands", "Altın Eller", "Golden Hands",
            "Altın eller prestijini aç", "Unlock Golden Hands",
            "✨", AchievementRarity.RARE,
            condition = { it.prestige.upgrades.goldenHands }
        ),
        Achievement(
            "collector", "Koleksiyoncu", "Collector",
            "Tüm kart tiplerinden en az 10’ar kazı", "Scratch 10 of each card type",
            "📚", AchievementRarity.EPIC,
            condition = { it.totalScratched >= 70 }
        ),
        Achievement(
            "combo_5", "Kombo 5", "Combo 5",
            "5 kombo yap", "Get 5 combo",
            "🔥", AchievementRarity.UNCOMMON,
            condition = { it.history.any { h -> h.contains("COMBO") } }
        ),
        Achievement(
            "perfect_scratch", "Mükemmel Kazı", "Perfect Scratch",
            "Verim %90+ kazı", "90%+ efficiency scratch",
            "🎯", AchievementRarity.RARE,
            condition = { it.history.any { h -> h.contains("Verim") } }
        )
    )

    fun unlocked(state: GameState): List<Achievement> = all.filter { it.condition(state) }
    fun locked(state: GameState): List<Achievement> = all.filter { !it.condition(state) }
    fun progress(state: GameState): Float = unlocked(state).size.toFloat() / all.size
    fun next(state: GameState): Achievement? = locked(state).firstOrNull()

    // Rarity dağılımı
    fun byRarity(r: AchievementRarity): List<Achievement> = all.filter { it.rarity == r }
    fun countByRarity(state: GameState, r: AchievementRarity): Int = all.count { it.rarity == r && it.condition(state) }

    // Gizli başarılar
    fun hiddenUnlocked(state: GameState): List<Achievement> = all.filter { it.hidden && it.condition(state) }

    // JP toplam
    fun totalJpRewardUnlocked(state: GameState): Int = unlocked(state).sumOf { it.jpReward }

    // En nadir
    fun rarestUnlocked(state: GameState): Achievement? = unlocked(state).filter { it.rarity == AchievementRarity.LEGENDARY }.firstOrNull()

    // Kategori
    fun category(a: Achievement): String = when {
        a.id.contains("scratch") -> "KAZI"
        a.id.contains("jackpot") -> "JACKPOT"
        a.id.contains("prestige") -> "PRESTİJ"
        a.id.contains("rich") || a.id.contains("total") -> "EKONOMİ"
        a.id.contains("luck") || a.id.contains("power") || a.id.contains("area") || a.id.contains("all_max") -> "YÜKSELTME"
        a.id.contains("loan") || a.id.contains("bankrupt") -> "RİSK"
        a.id.contains("final") -> "SON ŞANS"
        else -> "GENEL"
    }

    fun byCategory(cat: String, state: GameState): List<Achievement> = all.filter { category(it) == cat }

    // İlerleme hesap
    fun progressFor(state: GameState, id: String): Float = when (id) {
        "hundred_scratch" -> (state.totalScratched / 100f).coerceIn(0f, 1f)
        "thousand_scratch" -> (state.totalScratched / 1000f).coerceIn(0f, 1f)
        "ten_jackpots" -> (state.totalJackpots / 10f).coerceIn(0f, 1f)
        "hundred_jackpots" -> (state.totalJackpots / 100f).coerceIn(0f, 1f)
        "rich_10k" -> (state.balance / 10000f).coerceIn(0f, 1f)
        "rich_100k" -> (state.balance / 100000f).coerceIn(0f, 1f)
        "rich_million" -> (state.balance / 1_000_000f).coerceIn(0f, 1f)
        else -> if (all.find { it.id == id }?.condition?.invoke(state) == true) 1f else 0f
    }

    // Pixel renk
    fun colorFor(a: Achievement, unlocked: Boolean): Long = if (!unlocked) 0xFF424242 else when (a.rarity) {
        AchievementRarity.COMMON -> 0xFF9E9E9E
        AchievementRarity.UNCOMMON -> 0xFF4CAF50
        AchievementRarity.RARE -> 0xFF2196F3
        AchievementRarity.EPIC -> 0xFF9C27B0
        AchievementRarity.LEGENDARY -> 0xFFFFD600
    }

    // Başarı metni
    fun displayTitle(a: Achievement, unlocked: Boolean): String =
        if (a.hidden && !unlocked) "???" else a.title

    fun displayDesc(a: Achievement, unlocked: Boolean): String =
        if (a.hidden && !unlocked) "Gizli başarı" else a.desc

    // Toplam istatistik metni
    fun statsText(state: GameState): String {
        val u = unlocked(state).size
        val t = all.size
        val p = (progress(state) * 100).toInt()
        val jp = totalJpRewardUnlocked(state)
        return "$u/$t ($p%) • +$jp JP"
    }

    // Nadirlik sıralaması
    fun sortedByRarity(state: GameState): List<Achievement> =
        all.sortedWith(compareBy({ it.rarity.ordinal }, { !it.condition(state) }))

    // Son açılan
    fun lastUnlocked(state: GameState, previous: GameState): List<Achievement> {
        val before = unlocked(previous).map { it.id }.toSet()
        return unlocked(state).filter { it.id !in before }
    }
}
