package com.scritchyscratchy.watch.pixel

import androidx.compose.ui.graphics.Color
import com.scritchyscratchy.watch.CardDefinition
import com.scritchyscratchy.watch.GameState
import com.scritchyscratchy.watch.PrestigeState

// CardDefinition aliases
val CardDefinition.emoji: String get() = icon
val CardDefinition.descTr: String get() = descriptionTr

// Prestige aliases
val PrestigeState.jackpotPoints: Int get() = jackPoints
val PrestigeState.jackPointsCompat: Int get() = jackPoints

// GameState alias for direct access
val GameState.jackpotPoints: Int get() = prestige.jackPoints

// DailyStreak alias
val DailyStreak.dayLabel: String get() = "Gün $day"

// Market iconFor
fun PixelMarket.iconFor(id: String?): String = when(id) {
    "bull" -> "📈"
    "bear" -> "📉"
    "luck_day" -> "🍀"
    "dust_storm" -> "🌪️"
    "neon_night" -> "🌃"
    "inflation" -> "💸"
    else -> "•"
}

// DailySystem texts
fun PixelDailySystem.questsText(state: GameState): String = "Görevler"
fun PixelDailySystem.weekCompleteText(state: GameState): String = "Hafta tamamlandı"
val PixelDailySystem.weekCompleteText: String get() = "Hafta tamamlandı"

// Prestige nextTitle
fun PixelPrestigeExpanded.nextTitle(state: GameState): String = "Sonraki Prestij"

// Leaderboard boardTitle
fun PixelLeaderboard.boardTitle(state: GameState): String = "Liderlik"

// Economy priceRarity
data class PriceRarity(val icon: String, val name: String)
fun PixelEconomyUtils.priceRarity(price: Long): PriceRarity = PriceRarity("💰","Normal")
fun PixelEconomyUtils.priceRarity(price: Int): PriceRarity = PriceRarity("💰","Normal")

// Challenges pixelProgress
fun PixelChallenges.pixelProgress(state: GameState): Float = 0.5f

// Bank extension
val BankAccount.jackpotPointsCompat: Int get() = 0

// Collection foil etc already exists
