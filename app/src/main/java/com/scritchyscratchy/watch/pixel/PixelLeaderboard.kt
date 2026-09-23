package com.scritchyscratchy.watch.pixel

import com.scritchyscratchy.watch.GameState
import kotlin.random.Random

// ============= PIXEL LEADERBOARD - LİDERLİK TABLOSU (WATCH LOCAL) =============

data class LeaderEntry(
    val rank: Int,
    val name: String,
    val score: Long,
    val avatar: String,
    val isPlayer: Boolean = false,
    val title: String
)

object PixelLeaderboard {

    private val botNames = listOf(
        "ScratchKing" to "👑", "LuckyBot" to "🍀", "PixelPro" to "👾",
        "GoldFingers" to "🪙", "NeonQueen" to "💜", "TurboScratch" to "⚡",
        "MafiaBoss" to "🎩", "CoinMaster" to "💰", "Hologram" to "🌈",
        "VoidWalker" to "🕳️", "ArcadeKid" to "🕹️", "DustDevil" to "🌪️"
    )

    private val titles = listOf(
        "ACEMİ", "ÇIRAK", "USTA", "EFSANE", "MİLYONER", "KOLEKSİYONCU",
        "JACKPOTÇU", "PRESTİJ", "HIZLI", "ŞANSLI", "SABIRLI", "ÇILGIN"
    )

    fun generateFor(state: GameState): List<LeaderEntry> {
        val seed = state.totalScratched + state.balance
        val rnd = Random(seed)
        val playerScore = state.balance + state.totalWon / 10 + state.prestige.jackPoints * 1000L
        val list = mutableListOf<LeaderEntry>()
        // player
        val playerRank = rnd.nextInt(1, 8)
        // bots
        for (i in 1..10) {
            val isPlayer = i == playerRank
            val name = if (isPlayer) "SEN" else botNames[(i + rnd.nextInt(botNames.size)) % botNames.size].first
            val avatar = if (isPlayer) "😎" else botNames[i % botNames.size].second
            val base = rnd.nextLong(500, 50000) + i * 1000L
            val score = if (isPlayer) playerScore else base + rnd.nextLong(0, 10000)
            val title = titles[rnd.nextInt(titles.size)]
            list.add(LeaderEntry(i, name, score, avatar, isPlayer, title))
        }
        return list.sortedByDescending { it.score }.mapIndexed { idx, e -> e.copy(rank = idx + 1) }
    }

    fun playerEntry(state: GameState): LeaderEntry? = generateFor(state).find { it.isPlayer }

    fun playerRank(state: GameState): Int = playerEntry(state)?.rank ?: 10

    fun top3(state: GameState): List<LeaderEntry> = generateFor(state).take(3)

    fun isTop3(state: GameState): Boolean = playerRank(state) <= 3

    fun isFirst(state: GameState): Boolean = playerRank(state) == 1

    fun rankText(state: GameState): String {
        val r = playerRank(state)
        return when (r) {
            1 -> "1. LİDERSİN! 👑"
            2 -> "2. Çok yakın!"
            3 -> "3. Podyumdasın!"
            else -> "$r. sıra"
        }
    }

    fun rankColor(rank: Int): Long = when (rank) {
        1 -> 0xFFFFD600
        2 -> 0xFFB0BEC5
        3 -> 0xFFFF6D00
        else -> 0xFF9E9E9E
    }

    fun rankIcon(rank: Int): String = when (rank) {
        1 -> "🥇"
        2 -> "🥈"
        3 -> "🥉"
        else -> "#$rank"
    }

    fun scoreFor(state: GameState): Long {
        return state.balance + state.totalWon / 10 + state.prestige.jackPoints * 1000L + state.totalJackpots * 500L
    }

    fun nextRankScore(state: GameState): Long {
        val list = generateFor(state)
        val player = playerEntry(state) ?: return 0
        val idx = list.indexOf(player)
        if (idx == 0) return 0
        val next = list[idx - 1]
        return (next.score - player.score).coerceAtLeast(0)
    }

    fun toNextRankText(state: GameState): String {
        val diff = nextRankScore(state)
        return if (diff == 0L) "Zirvedesin!" else "${diff}$ sonra ${playerRank(state) - 1}. sıra"
    }

    fun allRanks(state: GameState): List<LeaderEntry> = generateFor(state)

    fun byScore(state: GameState): List<LeaderEntry> = generateFor(state).sortedByDescending { it.score }

    fun byName(state: GameState): List<LeaderEntry> = generateFor(state).sortedBy { it.name }

    fun search(state: GameState, q: String): List<LeaderEntry> =
        generateFor(state).filter { it.name.contains(q, true) || it.title.contains(q, true) }

    fun randomEntry(state: GameState): LeaderEntry = generateFor(state).random()

    fun averageScore(state: GameState): Long = generateFor(state).map { it.score }.average().toLong()

    fun medianScore(state: GameState): Long {
        val sorted = generateFor(state).map { it.score }.sorted()
        return sorted[sorted.size / 2]
    }

    fun maxScore(state: GameState): Long = generateFor(state).maxOf { it.score }

    fun minScore(state: GameState): Long = generateFor(state).minOf { it.score }

    fun playerProgress(state: GameState): Float {
        val max = maxScore(state).toFloat()
        val player = scoreFor(state).toFloat()
        return (player / max).coerceIn(0f, 1f)
    }

    fun playerProgressText(state: GameState): String = "${(playerProgress(state) * 100).toInt()}% liderliğe"

    fun leaderboardText(state: GameState): String {
        val sb = StringBuilder()
        sb.appendLine("=== LİDERLİK ===")
        for (e in generateFor(state)) {
            val marker = if (e.isPlayer) "← SEN" else ""
            sb.appendLine("${e.rank}. ${e.avatar} ${e.name} ${e.score}$ ${e.title} $marker")
        }
        return sb.toString()
    }

    fun pixelBoard(state: GameState): String {
        val list = generateFor(state)
        return list.joinToString("\n") { e ->
            val bar = "█".repeat((e.score / maxScore(state).toFloat() * 10).toInt()) + "░".repeat(10 - (e.score / maxScore(state).toFloat() * 10).toInt())
            "${rankIcon(e.rank)} ${e.name.padEnd(10)} $bar ${e.score}$"
        }
    }

    fun titleForScore(score: Long): String = when {
        score >= 100000 -> "EFSANE"
        score >= 50000 -> "USTA"
        score >= 20000 -> "UZMAN"
        score >= 10000 -> "DENEYİMLİ"
        score >= 5000 -> "ÇIRAK"
        else -> "ACEMİ"
    }

    fun playerTitle(state: GameState): String = titleForScore(scoreFor(state))

    fun rankUpText(state: GameState): String {
        val r = playerRank(state)
        return if (r == 1) "Zirvedesin!" else "${r - 1}. sıraya çıkmak için ${nextRankScore(state)}$"
    }

    fun weeklyReward(rank: Int): Long = when (rank) {
        1 -> 5000
        2 -> 3000
        3 -> 1500
        in 4..10 -> 500
        else -> 0
    }

    fun weeklyRewardText(rank: Int): String = "+${weeklyReward(rank)}$ haftalık ödül"

    fun isRewardClaimable(state: GameState): Boolean = isTop3(state)

    fun claimWeekly(state: GameState): Pair<GameState, String> {
        val r = playerRank(state)
        val rew = weeklyReward(r)
        if (rew == 0L) return state to "Ödül yok"
        return state.copy(balance = state.balance + rew) to "Haftalık +${rew}$!"
    }

    fun history(state: GameState): List<Int> = (0 until 7).map { Random(state.totalScratched + it.toLong()).nextInt(1, 11) }

    fun trend(state: GameState): String {
        val hist = history(state)
        val avg = hist.average()
        val last = hist.last()
        return when {
            last > avg -> "Yükselişte 📈"
            last < avg -> "Düşüşte 📉"
            else -> "Stabil ➡️"
        }
    }

    fun extendedLog(state: GameState): String {
        val sb = StringBuilder()
        sb.appendLine("=== LEADERBOARD LOG ===")
        for (e in generateFor(state)) sb.appendLine("${e.rank} | ${e.name} | ${e.score} | ${e.title} | ${if (e.isPlayer) "PLAYER" else "BOT"}")
        sb.appendLine("Player: ${scoreFor(state)}$ rank ${playerRank(state)}")
        repeat(20) { i -> sb.appendLine("Log ${i + 1}: ${Random.nextInt(10000)}") }
        return sb.toString()
    }

    fun randomRank(): Int = Random.nextInt(1, 11)

    fun randomScore(): Long = Random.nextLong(500, 50000)

    fun topPlayer(state: GameState): LeaderEntry = generateFor(state).first()

    fun bottomPlayer(state: GameState): LeaderEntry = generateFor(state).last()

    fun playerVsTop(state: GameState): String {
        val player = scoreFor(state)
        val top = maxScore(state)
        val diff = top - player
        return if (diff <= 0) "Geçtin!" else "${diff}$ geridesin"
    }
}
