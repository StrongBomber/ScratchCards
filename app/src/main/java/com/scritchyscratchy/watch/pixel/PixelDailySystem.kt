package com.scritchyscratchy.watch.pixel

import com.scritchyscratchy.watch.GameState
import kotlin.random.Random

// ============= PIXEL DAILY SYSTEM - GÜNLÜK ÖDÜL, GÖREV, STREAK =============

data class DailyStreak(
    val day: Int,
    val reward: Long,
    val bonus: String,
    val icon: String,
    val claimed: Boolean,
    val isToday: Boolean
)

object PixelDailySystem {
    private val weekRewards = listOf(
        Triple(50L, "50$", "🪙"),
        Triple(100L, "100$", "💰"),
        Triple(150L, "150$ + Şans", "🍀"),
        Triple(200L, "200$", "💵"),
        Triple(300L, "300$ + Güç", "💪"),
        Triple(500L, "500$", "💎"),
        Triple(1000L, "1000$ + 5JP", "🎉")
    )

    fun generateWeek(state: GameState): List<DailyStreak> {
        val streak = (state.totalScratched / 10) % 7
        return weekRewards.mapIndexed { idx, (amount, bonus, icon) ->
            DailyStreak(
                day = idx + 1,
                reward = amount,
                bonus = bonus,
                icon = icon,
                claimed = idx < streak,
                isToday = idx == streak
            )
        }
    }

    fun today(state: GameState): DailyStreak? = generateWeek(state).find { it.isToday }
    fun tomorrow(state: GameState): DailyStreak? {
        val week = generateWeek(state)
        val todayIdx = week.indexOfFirst { it.isToday }
        return if (todayIdx >= 0 && todayIdx + 1 < week.size) week[todayIdx + 1] else week.firstOrNull()
    }

    fun streakDays(state: GameState): Int = (state.totalScratched / 10).coerceAtMost(365)

    fun streakRewardFor(day: Int): Long = weekRewards.getOrNull((day - 1) % 7)?.first ?: 50L

    fun canClaim(state: GameState): Boolean = today(state)?.let { !it.claimed } ?: false

    fun claim(state: GameState): Pair<GameState, String> {
        val today = today(state) ?: return state to "Yok"
        if (today.claimed) return state to "Zaten alındı"
        var ns = state.copy(balance = state.balance + today.reward)
        if (today.day == 3) ns = ns.copy(upgrades = ns.upgrades.copy(luckLevel = (ns.upgrades.luckLevel + 1).coerceAtMost(10)))
        if (today.day == 5) ns = ns.copy(upgrades = ns.upgrades.copy(scratchPowerLevel = (ns.upgrades.scratchPowerLevel + 1).coerceAtMost(10)))
        if (today.day == 7) ns = ns.copy(prestige = ns.prestige.copy(jackPoints = ns.prestige.jackPoints + 5))
        ns = ns.copy(history = (listOf("Günlük ${today.day}: +${today.reward}$") + ns.history).take(20))
        return ns to "Günlük ödül +${today.reward}$!"
    }

    fun streakBonus(streak: Int): String = when {
        streak >= 30 -> "Ay ustası +20JP"
        streak >= 14 -> "2 hafta +10JP"
        streak >= 7 -> "Hafta +5JP"
        streak >= 3 -> "3 gün +2JP"
        else -> "Seri devam ediyor"
    }

    fun streakProgress(state: GameState): Float = (streakDays(state) % 7) / 7f

    fun weekProgress(state: GameState): Float {
        val week = generateWeek(state)
        val claimed = week.count { it.claimed }
        return claimed.toFloat() / week.size
    }

    fun nextResetHours(): Int = 24 - (System.currentTimeMillis() / 3600000 % 24).toInt()

    fun nextResetText(): String = "${nextResetHours()}s sonra yenilenir"

    fun totalClaimed(state: GameState): Int = generateWeek(state).count { it.claimed }

    fun totalRewardsClaimed(state: GameState): Long = generateWeek(state).filter { it.claimed }.sumOf { it.reward }

    fun unclaimed(state: GameState): List<DailyStreak> = generateWeek(state).filter { !it.claimed }

    fun claimed(state: GameState): List<DailyStreak> = generateWeek(state).filter { it.claimed }

    fun todayProgress(state: GameState): Float = if (canClaim(state)) 0.5f else 1f

    fun weekCompletion(state: GameState): Int = (weekProgress(state) * 100).toInt()

    fun streakIcon(day: Int): String = weekRewards.getOrNull((day - 1) % 7)?.third ?: "🎁"

    fun rewardText(amount: Long): String = "+$amount $"

    fun bonusText(bonus: String): String = bonus

    fun dayLabel(day: Int): String = "GÜN $day"

    fun isWeekComplete(state: GameState): Boolean = weekProgress(state) >= 1f

    fun weekReward(): Long = 1000L

    fun weekBonus(): String = "Hafta tamam: 1000$ + 5JP"

    fun claimWeek(state: GameState): Pair<GameState, String> {
        if (!isWeekComplete(state)) return state to "Hafta bitmedi"
        val ns = state.copy(
            balance = state.balance + weekReward(),
            prestige = state.prestige.copy(jackPoints = state.prestige.jackPoints + 5)
        )
        return ns to "Hafta ödülü +1000$ +5JP!"
    }

    fun streakText(state: GameState): String {
        val s = streakDays(state)
        return "$s gün seri • ${streakBonus(s)}"
    }

    fun calendarText(state: GameState): String {
        return generateWeek(state).joinToString(" ") { if (it.claimed) "✓" else if (it.isToday) "●" else "○" }
    }

    fun pixelCalendar(state: GameState): String {
        val week = generateWeek(state)
        val sb = StringBuilder()
        for (d in week) {
            val box = when {
                d.claimed -> "█"
                d.isToday -> "▓"
                else -> "░"
            }
            sb.append("$box${d.day} ")
        }
        return sb.toString()
    }

    fun streakHistory(state: GameState): List<Int> = (0 until 7).map { streakDays(state) - it }

    fun averageReward(): Long = weekRewards.map { it.first }.average().toLong()

    fun maxReward(): Long = weekRewards.maxOf { it.first }

    fun minReward(): Long = weekRewards.minOf { it.first }

    fun totalWeekReward(): Long = weekRewards.sumOf { it.first }

    fun dailyQuest(): String = "Günlük: 10 kazı yap, ödül al"

    fun questProgress(state: GameState): Float = (state.totalScratched % 10) / 10f

    fun questRemaining(state: GameState): Int = 10 - (state.totalScratched % 10)

    fun isQuestDone(state: GameState): Boolean = questRemaining(state) == 0

    fun questReward(): Long = 100

    fun claimQuest(state: GameState): Pair<GameState, String> {
        if (!isQuestDone(state)) return state to "Görev bitmedi"
        return state.copy(balance = state.balance + questReward()) to "+${questReward()}$ görev ödülü!"
    }

    fun extendedLog(state: GameState): String {
        val sb = StringBuilder()
        sb.appendLine("=== DAILY LOG ===")
        for (d in generateWeek(state)) sb.appendLine("Gün ${d.day}: ${d.reward}$ ${if (d.claimed) "✓" else if (d.isToday) "●" else "○"}")
        sb.appendLine("Seri: ${streakDays(state)} gün")
        sb.appendLine("Toplam: ${totalRewardsClaimed(state)}$")
        repeat(30) { i -> sb.appendLine("Log ${i + 1}: ${state.balance + i * 50} sim") }
        return sb.toString()
    }

    fun pixelWeekView(state: GameState): String {
        val week = generateWeek(state)
        return week.joinToString("\n") { d ->
            val status = when {
                d.claimed -> "✓ ALINDI"
                d.isToday -> "● BUGÜN"
                else -> "○ BEKLİYOR"
            }
            "${d.icon} GÜN ${d.day}: ${d.reward}$ [$status]"
        }
    }

    fun streakRank(state: GameState): Pair<String, String> {
        val s = streakDays(state)
        return when {
            s >= 30 -> "EFSANE SERİ" to "👑"
            s >= 14 -> "SADIK" to "🔥"
            s >= 7 -> "DÜZENLİ" to "⭐"
            s >= 3 -> "BAŞLANGIÇ" to "🌱"
            else -> "YENİ" to "🐣"
        }
    }

    fun nextStreakReward(state: GameState): String {
        val s = streakDays(state) + 1
        return when {
            s % 7 == 0 -> "Hafta bonusu 1000$ +5JP"
            s % 3 == 0 -> "3 gün bonus 150$"
            else -> "${streakRewardFor(s)}$"
        }
    }

    fun streakMilestone(state: GameState): Int {
        val s = streakDays(state)
        return when {
            s < 3 -> 3
            s < 7 -> 7
            s < 14 -> 14
            s < 30 -> 30
            else -> 60
        }
    }

    fun toNextMilestone(state: GameState): Int = streakMilestone(state) - streakDays(state)

    fun milestoneText(state: GameState): String = "${toNextMilestone(state)} gün sonra ${streakBonus(streakMilestone(state))}"

    fun dailyTip(): String = listOf(
        "Her gün giriş yap, seri bozulmasın!",
        "7. gün büyük ödül var!",
        "Seri uzadıkça bonus artar",
        "Günlük görevi unutma"
    ).random()

    fun randomReward(): Long = weekRewards.random().first

    fun allRewards(): List<Long> = weekRewards.map { it.first }

    fun rewardForDay(day: Int): Long = weekRewards.getOrNull((day - 1) % 7)?.first ?: 0L

    fun isTodayClaimed(state: GameState): Boolean = today(state)?.claimed ?: true

    fun claimableToday(state: GameState): DailyStreak? = if (canClaim(state)) today(state) else null
}
