package com.scritchyscratchy.watch.pixel

import com.scritchyscratchy.watch.GameState
import kotlin.random.Random

// ============= PIXEL BANK - FAİZ, KREDİ, YATIRIM =============
data class BankAccount(
    val balance: Long = 0,
    val interestRate: Float = 0.02f,
    val level: Int = 1
) {
    fun dailyInterest(): Long = (balance * interestRate).toLong()
    fun nextLevelCost(): Long = level * 5000L
    fun maxBalance(): Long = level * 100000L
}

data class Loan(
    val amount: Long,
    val interest: Float,
    val dueIn: Int, // gün
    val takenAt: Long
) {
    fun totalDue(): Long = (amount * (1 + interest)).toLong()
    fun isOverdue(now: Long): Boolean = now - takenAt > dueIn * 86400000L
    fun remaining(now: Long): Long = maxOf(0, dueIn * 86400000L - (now - takenAt))
}

object PixelBank {
    fun deposit(state: GameState, amount: Long): GameState {
        if (amount <= 0 || amount > state.balance) return state
        return state.copy(balance = state.balance - amount, history = (listOf("Banka yatır: $amount$") + state.history).take(20))
    }
    fun withdraw(state: GameState, amount: Long): GameState {
        return state.copy(balance = state.balance + amount, history = (listOf("Banka çek: $amount$") + state.history).take(20))
    }
    fun interestFor(state: GameState): Long = (state.balance * 0.01f).toLong()
    fun canLoan(state: GameState): Boolean = state.totalScratched >= 50
    fun maxLoan(state: GameState): Long = (state.balance * 2).coerceAtMost(10000)
    fun takeLoan(state: GameState, amount: Long): GameState {
        if (!canLoan(state) || amount > maxLoan(state)) return state
        return state.copy(balance = state.balance + amount, history = (listOf("Kredi: $amount$ %5") + state.history).take(20))
    }
    fun repayLoan(state: GameState, amount: Long): GameState {
        if (amount > state.balance) return state
        return state.copy(balance = state.balance - amount, history = (listOf("Kredi öde: $amount$") + state.history).take(20))
    }
    fun investmentReturn(invest: Long, days: Int): Long = (invest * (1 + 0.05f * days)).toLong()
    fun vaultValue(state: GameState): Long = state.balance + state.totalWon
    fun netWorth(state: GameState): Long = vaultValue(state) + state.prestige.jackPoints * 1000L
    fun rankByWorth(state: GameState): String = when {
        netWorth(state) > 1000000 -> "Milyoner"
        netWorth(state) > 100000 -> "Zengin"
        netWorth(state) > 10000 -> "Orta"
        else -> "Fakir"
    }
    fun bankText(state: GameState): String = "Nakit: ${state.balance}$ • Değer: ${netWorth(state)}$ • ${rankByWorth(state)}"
    fun dailyBonus(state: GameState): Long = when {
        state.totalScratched > 1000 -> 500
        state.totalScratched > 500 -> 200
        state.totalScratched > 100 -> 50
        else -> 10
    }
    fun canDailyBonus(state: GameState): Boolean = true
    fun claimDaily(state: GameState): GameState = state.copy(balance = state.balance + dailyBonus(state), history = (listOf("Günlük banka: +${dailyBonus(state)}$") + state.history).take(20))
    fun tax(state: GameState): Long = (state.balance * 0.01f).toLong()
    fun afterTax(state: GameState): GameState = state.copy(balance = state.balance - tax(state))
    fun taxText(state: GameState): String = "Vergi: ${tax(state)}$ (%1)"
    fun savingsGoal(state: GameState, target: Long): String {
        val need = target - state.balance
        return if (need <= 0) "Hedef tamam!" else "$need$ kaldı"
    }
    fun goals(): List<Long> = listOf(1000, 5000, 10000, 50000, 100000, 500000, 1000000)
    fun nextGoal(state: GameState): Long? = goals().firstOrNull { it > state.balance }
    fun nextGoalText(state: GameState): String = nextGoal(state)?.let { "Sonraki: ${it}$ (${savingsGoal(state, it)})" } ?: "MAX hedef!"
    fun progressToGoal(state: GameState): Float {
        val next = nextGoal(state) ?: return 1f
        val prev = goals().filter { it < next }.maxOrNull() ?: 0
        val range = next - prev
        val have = state.balance - prev
        return (have.toFloat() / range).coerceIn(0f, 1f)
    }
    fun extendedLog(state: GameState): String {
        val sb = StringBuilder()
        sb.appendLine("=== BANK ===")
        sb.appendLine(bankText(state))
        sb.appendLine(nextGoalText(state))
        sb.appendLine(taxText(state))
        repeat(30) { i -> sb.appendLine("Bank $i: ${Random.nextInt(10000)}$") }
        return sb.toString()
    }
    fun allTransactions(): List<String> = listOf("Yatır", "Çek", "Kredi", "Öde", "Yatırım", "Vergi", "Faiz")
    fun randomTransaction(): String = allTransactions().random()
    fun transactionIcon(t: String): String = when(t) {
        "Yatır" -> "⬇️"
        "Çek" -> "⬆️"
        "Kredi" -> "🏦"
        "Öde" -> "💸"
        "Yatırım" -> "📈"
        "Vergi" -> "🏛️"
        "Faiz" -> "💹"
        else -> "•"
    }
    fun allWithIcons(): String = allTransactions().joinToString("\n") { "${transactionIcon(it)} $it" }
    fun count(): Int = allTransactions().size
    fun vaultText(state: GameState): String = "Kasa: ${vaultValue(state)}$"
    fun interestText(state: GameState): String = "Faiz: +${interestFor(state)}$/gün"
    fun loanText(state: GameState): String = if (canLoan(state)) "Kredi max ${maxLoan(state)}$" else "Kredi kilitli (50 kazı)"
    fun summary(state: GameState): String = "${bankText(state)} | ${loanText(state)} | ${interestText(state)}"
}
