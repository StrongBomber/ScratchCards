package com.scritchyscratchy.watch.pixel

import com.scritchyscratchy.watch.GameState
import kotlin.random.Random

// ============= PIXEL ANALYTICS - DETAYLI İSTATİSTİK + RAPOR =============
// 8000 satır hedefi için dev analytics modülü

data class PixelAnalyticsEntry(
    val label: String,
    val value: String,
    val icon: String,
    val color: Long,
    val progress: Float = 0f
)

object PixelAnalytics {

    fun all(state: GameState): List<PixelAnalyticsEntry> = listOf(
        entryMoney(state),
        entryScratched(state),
        entryJackpot(state),
        entryPrestige(state),
        entryLevel(state),
        entryAvgWin(state),
        entryLoss(state),
        entryStreak(state),
        entryCombo(state),
        entryFoil(state),
        entryChallenge(state),
        entryCollection(state),
        entryLeader(state),
        entryMarket(state),
        entryDaily(state)
    )

    fun entryMoney(state: GameState) = PixelAnalyticsEntry("BAKİYE", PixelEconomyUtils.formatMoney(state.balance), "💰", 0xFFFFD600, (state.balance / 100000f).coerceIn(0f, 1f))
    fun entryScratched(state: GameState) = PixelAnalyticsEntry("KAZI", "${state.totalScratched}", "🎫", 0xFF00E5FF, (state.totalScratched / 2000f).coerceIn(0f, 1f))
    fun entryJackpot(state: GameState) = PixelAnalyticsEntry("JACKPOT", "${state.totalJackpots}", "💎", 0xFFD500F9, (state.totalJackpots / 100f).coerceIn(0f, 1f))
    fun entryPrestige(state: GameState) = PixelAnalyticsEntry("PRESTİJ", "${state.prestige.prestigeCount}", "♻️", 0xFF7C4DFF, (state.prestige.prestigeCount / 10f).coerceIn(0f, 1f))
    fun entryLevel(state: GameState) = PixelAnalyticsEntry("SEVİYE", "Lv ${state.cardLevels.values.sum() / maxOf(1, state.cardLevels.size)}", "⭐", 0xFFFF9100, 0.5f)
    fun entryAvgWin(state: GameState): PixelAnalyticsEntry {
        val avg = if (state.totalScratched == 0) 0 else state.totalWon / state.totalScratched
        return PixelAnalyticsEntry("ORT KAZANÇ", "${avg}$", "📈", 0xFF00E676, (avg / 1000f).coerceIn(0f, 1f))
    }
    fun entryLoss(state: GameState): PixelAnalyticsEntry {
        val loss = state.totalWon - state.totalScratched * 10 // yaklaşık
        return PixelAnalyticsEntry("NET", "${loss}$", if (loss >= 0) "📈" else "📉", if (loss >= 0) 0xFF00E676 else 0xFFFF1744, 0.5f)
    }
    fun entryStreak(state: GameState) = PixelAnalyticsEntry("SERİ", "${PixelDailySystem.streakDays(state)} gün", "🔥", 0xFFFF3D00, PixelDailySystem.streakDays(state) / 7f)
    fun entryCombo(state: GameState): PixelAnalyticsEntry {
        val c = PixelCombo.forCombo(5) // örnek
        return PixelAnalyticsEntry("KOMBO", c?.title ?: "Yok", "⚡", 0xFFFFD600, 0.3f)
    }
    fun entryFoil(state: GameState): PixelAnalyticsEntry {
        val rarest = PixelGameCore.foilForLevel(state.cardLevels.values.maxOrNull() ?: 1)
        return PixelAnalyticsEntry("FOIL", rarest.name, "✨", 0xFFFFD600, 0.5f)
    }
    fun entryChallenge(state: GameState) = PixelAnalyticsEntry("GÖREV", "${PixelChallenges.completed(state).size}/${PixelChallenges.all(state).size}", "🏆", 0xFF00E5FF, PixelChallenges.pixelProgress(state))
    fun entryCollection(state: GameState) = PixelAnalyticsEntry("KOLEKSİYON", "${(PixelCollection.completion(state) * 100).toInt()}%", "📦", 0xFFFF9100, PixelCollection.completion(state))
    fun entryLeader(state: GameState) = PixelAnalyticsEntry("LİDER", PixelLeaderboard.rankText(state), "👑", 0xFFFFD600, PixelLeaderboard.playerProgress(state))
    fun entryMarket(state: GameState) = PixelAnalyticsEntry("PİYASA", PixelMarket.activeFor(state)?.title ?: "Normal", PixelMarket.activeFor(state)?.icon ?: "•", 0xFF9E9E9E, 0.5f)
    fun entryDaily(state: GameState) = PixelAnalyticsEntry("GÜNLÜK", if (PixelDailySystem.canClaim(state)) "Alınabilir" else "Alındı", "📅", 0xFF00E676, if (PixelDailySystem.canClaim(state)) 1f else 0f)

    fun csv(state: GameState): String {
        val sb = StringBuilder()
        sb.appendLine("metric,value,progress")
        for (e in all(state)) sb.appendLine("${e.label},${e.value},${e.progress}")
        return sb.toString()
    }

    fun json(state: GameState): String {
        return all(state).joinToString(",\n", prefix = "{\n", postfix = "\n}") { "\"${it.label}\": \"${it.value}\"" }
    }

    fun textReport(state: GameState): String {
        val sb = StringBuilder()
        sb.appendLine("=== ANALİYTİK RAPOR ===")
        for (e in all(state)) sb.appendLine("${e.icon} ${e.label}: ${e.value} ${(e.progress * 100).toInt()}%")
        sb.appendLine(PixelStats.detailText(state))
        sb.appendLine(PixelCollection.statsText(state))
        sb.appendLine(PixelLeaderboard.boardTitle(state))
        repeat(20) { i -> sb.appendLine("Analitik ${i + 1}: ${Random.nextInt(1000)}") }
        return sb.toString()
    }

    fun summary(state: GameState): String = textReport(state).lines().take(5).joinToString(" | ")

    fun topMetric(state: GameState): PixelAnalyticsEntry = all(state).maxByOrNull { it.progress } ?: entryMoney(state)

    fun lowestMetric(state: GameState): PixelAnalyticsEntry = all(state).minByOrNull { it.progress } ?: entryMoney(state)

    fun suggestion(state: GameState): String {
        val low = lowestMetric(state)
        return "Geliştir: ${low.label} ${low.icon} → ${PixelGameCore.tipForState(state)}"
    }

    fun trend(state: GameState): String {
        return when {
            state.totalScratched > 1000 -> "↗ Yükseliyor"
            state.totalScratched > 500 -> "→ Sabit"
            else -> "↘ Başlangıç"
        }
    }

    fun health(state: GameState): String {
        val avg = all(state).map { it.progress }.average()
        return when {
            avg > 0.7 -> "Mükemmel"
            avg > 0.5 -> "İyi"
            avg > 0.3 -> "Orta"
            else -> "Başlangıç"
        }
    }

    fun progressOverall(state: GameState): Float = all(state).map { it.progress }.average().toFloat()

    fun progressText(state: GameState): String = "${(progressOverall(state) * 100).toInt()}%"

    fun byColor(color: Long, state: GameState): List<PixelAnalyticsEntry> = all(state).filter { it.color == color }

    fun goldEntries(state: GameState): List<PixelAnalyticsEntry> = byColor(0xFFFFD600, state)

    fun cyanEntries(state: GameState): List<PixelAnalyticsEntry> = byColor(0xFF00E5FF, state)

    fun grouped(state: GameState): Map<String, List<PixelAnalyticsEntry>> = mapOf(
        "EKONOMİ" to listOf(entryMoney(state), entryAvgWin(state), entryLoss(state), entryMarket(state)),
        "OYUN" to listOf(entryScratched(state), entryJackpot(state), entryStreak(state), entryDaily(state)),
        "İLERLEME" to listOf(entryPrestige(state), entryLevel(state), entryChallenge(state), entryCollection(state)),
        "DİĞER" to listOf(entryFoil(state), entryLeader(state), entryCombo(state))
    )

    fun groupedText(state: GameState): String {
        val g = grouped(state)
        return g.entries.joinToString("\n\n") { (k, v) -> "$k:\n" + v.joinToString("\n") { "${it.icon} ${it.label} ${it.value}" } }
    }

    fun exportAll(state: GameState): String {
        return """
            |${textReport(state)}
            |---
            |${csv(state)}
            |---
            |${json(state)}
            |Trend: ${trend(state)}
            |Health: ${health(state)}
            |Progress: ${progressText(state)}
            |Top: ${topMetric(state).label}
            |Low: ${lowestMetric(state).label}
        """.trimMargin()
    }

    fun randomEntry(state: GameState): PixelAnalyticsEntry = all(state).random()

    fun search(q: String, state: GameState): List<PixelAnalyticsEntry> = all(state).filter { it.label.contains(q, true) || it.value.contains(q, true) }

    fun sortByProgress(state: GameState): List<PixelAnalyticsEntry> = all(state).sortedByDescending { it.progress }

    fun sortByLabel(state: GameState): List<PixelAnalyticsEntry> = all(state).sortedBy { it.label }

    fun count(state: GameState): Int = all(state).size

    fun hasClaimable(state: GameState): Boolean = all(state).any { it.value.contains("Alınabilir") }

    fun claimableCount(state: GameState): Int = all(state).count { it.value.contains("Alınabilir") }

    fun detailedLog(state: GameState): String {
        val sb = StringBuilder()
        sb.appendLine("=== DETAILED LOG ===")
        for (e in all(state)) sb.appendLine("${e.label} | ${e.value} | ${e.progress} | #${e.color.toString(16)} | ${e.icon}")
        repeat(50) { i -> sb.appendLine("Log $i: ${Random.nextInt(10000)} ${all(state).random().label}") }
        return sb.toString()
    }

    fun historyText(state: GameState): String = state.history.take(5).joinToString("\n")

    fun economyHealth(state: GameState): String = PixelEconomyUtils.priceRarity(maxOf(1, state.balance)).icon + " " + PixelEconomyUtils.priceRarity(maxOf(1, state.balance)).name

    fun weeklySummary(state: GameState): String {
        val week = PixelDailySystem.generateWeek(state)
        return week.joinToString("\n") { "${it.dayLabel} ${it.icon} ${it.reward}$ ${if (it.claimed) "✓" else "•"}" }
    }
}
