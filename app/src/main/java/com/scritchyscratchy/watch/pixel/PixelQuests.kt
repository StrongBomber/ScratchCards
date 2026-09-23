package com.scritchyscratchy.watch.pixel

import com.scritchyscratchy.watch.GameState
import kotlin.random.Random

// ============= PIXEL QUESTS - GÖREV ZİNCİRLERİ =============
data class PixelQuest(
    val id: String,
    val title: String,
    val desc: String,
    val icon: String,
    val target: Int,
    val reward: Long,
    val jp: Int,
    val chain: Int = 1
) {
    fun progress(state: GameState): Int = when (id) {
        "q_scratch_10" -> state.totalScratched % 10
        "q_scratch_100" -> state.totalScratched % 100
        "q_jack_5" -> state.totalJackpots % 5
        "q_money_1k" -> (state.balance / 100).toInt() % 10
        "q_money_10k" -> (state.balance / 1000).toInt() % 10
        else -> Random.nextInt(target)
    }
    fun progressFloat(state: GameState): Float = (progress(state).toFloat() / target).coerceIn(0f, 1f)
    fun isDone(state: GameState): Boolean = progress(state) >= target
}

object PixelQuests {
    val all = listOf(
        PixelQuest("q_scratch_10", "Acemi Kazıyıcı", "10 kart kazı", "🎫", 10, 100, 1, 1),
        PixelQuest("q_scratch_100", "Usta Kazıyıcı", "100 kart kazı", "🎫", 100, 1000, 5, 2),
        PixelQuest("q_jack_5", "Jackpot Avcısı", "5 jackpot", "💎", 5, 500, 2, 1),
        PixelQuest("q_money_1k", "Binlik", "1000$ biriktir", "💰", 10, 200, 1, 1),
        PixelQuest("q_money_10k", "On Binlik", "10000$ biriktir", "💰", 10, 2000, 5, 2),
        PixelQuest("q_streak_7", "Haftalık Sadakat", "7 gün giriş", "🔥", 7, 700, 3, 1),
        PixelQuest("q_prestige_1", "Yeniden Doğuş", "1 prestij", "♻️", 1, 0, 10, 1),
        PixelQuest("q_collect_50", "Koleksiyoner", "%50 koleksiyon", "📦", 50, 1500, 3, 1),
        PixelQuest("q_leader_top3", "Podyum", "Top 3'e gir", "👑", 3, 3000, 5, 2),
        PixelQuest("q_event_3", "Etkinlikçi", "3 etkinlik tamamla", "🎉", 3, 900, 2, 1)
    )

    fun forState(state: GameState): List<PixelQuest> = all

    fun completed(state: GameState): List<PixelQuest> = all.filter { it.isDone(state) }

    fun pending(state: GameState): List<PixelQuest> = all.filter { !it.isDone(state) }

    fun next(state: GameState): PixelQuest? = pending(state).minByOrNull { it.target - it.progress(state) }

    fun progress(state: GameState): Float = completed(state).size / all.size.toFloat()

    fun progressText(state: GameState): String = "${completed(state).size}/${all.size} (${(progress(state)*100).toInt()}%)"

    fun claim(state: GameState, id: String): GameState {
        val q = all.find { it.id == id } ?: return state
        if (!q.isDone(state)) return state
        return state.copy(
            balance = state.balance + q.reward,
            prestige = state.prestige.copy(jackpotPoints = state.prestige.jackpotPoints + q.jp),
            history = (listOf("Görev: ${q.title} +${q.reward}$ +${q.jp}JP") + state.history).take(20)
        )
    }

    fun claimAll(state: GameState): GameState {
        var cur = state
        for (q in completed(state)) cur = claim(cur, q.id)
        return cur
    }

    fun totalReward(): Long = all.sumOf { it.reward }

    fun totalJp(): Int = all.sumOf { it.jp }

    fun chainProgress(state: GameState, chain: Int): Float {
        val chainQs = all.filter { it.chain == chain }
        return chainQs.count { it.isDone(state)} / chainQs.size.toFloat()
    }

    fun chainText(state: GameState, chain: Int): String {
        val qs = all.filter { it.chain == chain }
        return "Zincir $chain: ${qs.count { it.isDone(state)}}/${qs.size}"
    }

    fun allChains(): List<Int> = all.map { it.chain }.distinct().sorted()

    fun chainsText(state: GameState): String = allChains().joinToString("\n") { chainText(state, it) }

    fun search(q: String): List<PixelQuest> = all.filter { it.title.contains(q, true) || it.desc.contains(q, true) }

    fun byIcon(icon: String): List<PixelQuest> = all.filter { it.icon == icon }

    fun byReward(min: Long): List<PixelQuest> = all.filter { it.reward >= min }

    fun stats(state: GameState): String = "${completed(state).size}/${all.size} tamamlandı • +${totalReward()}$ +${totalJp()}JP"

    fun extendedLog(state: GameState): String {
        val sb = StringBuilder()
        sb.appendLine("=== QUESTS ===")
        sb.appendLine(stats(state))
        sb.appendLine(progressText(state))
        for (q in all) sb.appendLine("${q.icon} ${q.title} ${q.progress(state)}/${q.target} ${if (q.isDone(state)) "✓" else "•"}")
        repeat(20) { i -> sb.appendLine("Quest $i: ${all.random().title}") }
        return sb.toString()
    }

    fun rewardText(q: PixelQuest): String = "+${q.reward}$ +${q.jp}JP"

    fun progressBar(q: PixelQuest, state: GameState): String {
        val p = q.progressFloat(state)
        val filled = (p * 10).toInt()
        return "█".repeat(filled) + "░".repeat(10 - filled) + " ${(p*100).toInt()}%"
    }

    fun allProgressBars(state: GameState): String = all.joinToString("\n") { "${it.title}: ${progressBar(it, state)}" }

    fun random(): PixelQuest = all.random()

    fun randomText(): String = random().title

    fun nextText(state: GameState): String = next(state)?.let { "${it.icon} ${it.title} ${it.progress(state)}/${it.target}" } ?: "Tüm görevler bitti!"

    fun isMax(state: GameState): Boolean = completed(state).size == all.size

    fun maxText(state: GameState): String = if (isMax(state)) "Tüm görevler tamam! 🏆" else nextText(state)
}
