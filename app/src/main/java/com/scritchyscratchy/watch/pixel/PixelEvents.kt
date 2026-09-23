package com.scritchyscratchy.watch.pixel

import com.scritchyscratchy.watch.GameState
import kotlin.random.Random

// ============= PIXEL EVENTS - HAFTALIK / MEVSİMLİK ETKİNLİKLER =============
data class PixelEvent(
    val id: String,
    val title: String,
    val desc: String,
    val icon: String,
    val durationDays: Int,
    val reward: Long,
    val multiplier: Float
)

object PixelEvents {
    val all = listOf(
        PixelEvent("weekend", "Hafta Sonu Çılgınlığı", "Tüm kazanç %25", "🎉", 2, 500, 1.25f),
        PixelEvent("halloween", "Cadılar Bayramı", "Korku kartları", "🎃", 7, 1000, 1.3f),
        PixelEvent("xmas", "Yılbaşı", "Karlı kazılar", "🎄", 7, 1500, 1.4f),
        PixelEvent("newyear", "Yeni Yıl", "Havai fişek", "🎆", 3, 2000, 1.5f),
        PixelEvent("easter", "Paskalya", "Yumurta avı", "🥚", 5, 800, 1.2f),
        PixelEvent("summer", "Yaz Festivali", "Güneşli bonus", "☀️", 14, 1200, 1.35f),
        PixelEvent("arcadeWeek", "Arcade Haftası", "Retro bonus", "👾", 7, 1000, 1.3f),
        PixelEvent("goldRush", "Altın Hücum", "Altın kartlar", "🏆", 3, 3000, 2f),
        PixelEvent("luckyHour", "Şans Saati", "Şans x2", "🍀", 1, 200, 2f),
        PixelEvent("mystery", "Gizemli Etkinlik", "Sürpriz", "❓", 2, 700, 1.5f)
    )

    fun active(now: Long = System.currentTimeMillis()): PixelEvent? {
        // basit: gün moduna göre
        val day = (now / 86400000L).toInt()
        return if (day % 5 == 0) all[day % all.size] else null
    }

    fun activeFor(state: GameState): PixelEvent? = active(state.balance + state.totalScratched)

    fun isActive(state: GameState): Boolean = activeFor(state) != null

    fun multiplierFor(state: GameState): Float = activeFor(state)?.multiplier ?: 1f

    fun titleFor(state: GameState): String = activeFor(state)?.title ?: "Etkinlik yok"

    fun iconFor(state: GameState): String = activeFor(state)?.icon ?: "•"

    fun rewardFor(state: GameState): Long = activeFor(state)?.reward ?: 0

    fun claim(state: GameState): GameState {
        val ev = activeFor(state) ?: return state
        return state.copy(
            balance = state.balance + ev.reward,
            history = (listOf("Etkinlik: ${ev.title} +${ev.reward}$") + state.history).take(20)
        )
    }

    fun canClaim(state: GameState): Boolean = isActive(state)

    fun text(state: GameState): String {
        val ev = activeFor(state) ?: return "Aktif etkinlik yok"
        return "${ev.icon} ${ev.title} ${ev.desc} +${ev.reward}$ x${ev.multiplier}"
    }

    fun allText(): String = all.joinToString("\n") { "${it.icon} ${it.title} ${it.desc} x${it.multiplier}" }

    fun byId(id: String): PixelEvent? = all.find { it.id == id }

    fun upcoming(state: GameState): PixelEvent? = all.filter { it.id != activeFor(state)?.id }.randomOrNull()

    fun upcomingText(state: GameState): String {
        val n = upcoming(state) ?: return "Yakında etkinlik yok"
        return "Yakında: ${n.icon} ${n.title} ${n.desc}"
    }

    fun schedule(): String = all.joinToString("\n") { "${it.title} ${it.durationDays}gün" }

    fun calendar(): String {
        val sb = StringBuilder()
        sb.appendLine("=== ETKİNLİK TAKVİMİ ===")
        for (e in all) sb.appendLine("${e.icon} ${e.title} - ${e.durationDays}g x${e.multiplier} +${e.reward}$")
        repeat(20) { i -> sb.appendLine("Takvim $i: ${all.random().title}") }
        return sb.toString()
    }

    fun progress(state: GameState): Float = if (isActive(state)) 1f else 0.3f

    fun progressText(state: GameState): String = "${(progress(state) * 100).toInt()}%"

    fun count(): Int = all.size

    fun totalReward(): Long = all.sumOf { it.reward }

    fun avgMultiplier(): Float = all.map { it.multiplier }.average().toFloat()

    fun maxReward(): PixelEvent = all.maxByOrNull { it.reward } ?: all.first()

    fun maxText(): String = "En büyük: ${maxReward().title} ${maxReward().reward}$"

    fun random(): PixelEvent = all.random()

    fun randomText(): String = random().title

    fun historyText(state: GameState): String = state.history.filter { it.contains("Etkinlik") }.take(5).joinToString("\n")

    fun stats(state: GameState): String = "${count()} etkinlik • ${totalReward()}$ toplam • x${String.format("%.2f", avgMultiplier())} ort"

    fun extendedLog(state: GameState): String {
        val sb = StringBuilder()
        sb.appendLine("=== EVENTS ===")
        sb.appendLine(text(state))
        sb.appendLine(upcomingText(state))
        sb.appendLine(stats(state))
        sb.appendLine(calendar())
        return sb.toString()
    }

    fun checkAndApply(state: GameState, amount: Long): Long = (amount * multiplierFor(state)).toLong()

    fun eventBonus(state: GameState, base: Long): Long = checkAndApply(state, base) - base

    fun bonusText(state: GameState, base: Long): String {
        val bonus = eventBonus(state, base)
        return if (bonus > 0) "+${bonus}$ etkinlik bonusu" else "Bonus yok"
    }

    fun isWeekend(): Boolean {
        val day = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK)
        return day == java.util.Calendar.SATURDAY || day == java.util.Calendar.SUNDAY
    }

    fun weekendBonus(state: GameState): Long = if (isWeekend()) 100 else 0

    fun weekendText(state: GameState): String = if (isWeekend()) "Hafta sonu +100$" else "Hafta içi"
}
