package com.scritchyscratchy.watch.pixel

import com.scritchyscratchy.watch.CardDefinition
import com.scritchyscratchy.watch.GameState
import com.scritchyscratchy.watch.UpgradePricing
import kotlin.math.pow
import kotlin.random.Random

// ============= PIXEL ECONOMY - GELİŞTİRİLMİŞ EKONOMİ SİSTEMİ =============
// Borsa, kredi, faiz, enflasyon, günlük ödül, kombolar

data class MarketEvent(
    val id: String,
    val title: String,
    val desc: String,
    val icon: String,
    val multiplier: Float, // fiyatlara etki
    val durationScratched: Int, // kaç kazı sürecek
    val color: Long
)

object PixelMarket {
    val events = listOf(
        MarketEvent("bull", "Boğa Piyasası", "Kart fiyatları +%15 ama ödüller +25%", "📈", 1.15f, 20, 0xFF00E676),
        MarketEvent("bear", "Ayı Piyasası", "Kart fiyatları -20%, ödüller -10%", "📉", 0.8f, 15, 0xFFFF1744),
        MarketEvent("luck_day", "Şans Günü", "Şans +%10", "🍀", 1.0f, 10, 0xFFFFD600),
        MarketEvent("dust_storm", "Toz Fırtınası", "Kazı %20 yavaş", "🌪️", 1.0f, 12, 0xFF9E9E9E),
        MarketEvent("neon_night", "Neon Gecesi", "Jackpot 2x", "🌃", 1.0f, 8, 0xFFD500F9),
        MarketEvent("inflation", "Enflasyon", "Fiyatlar her 10 kazıda +%5", "💸", 1.05f, 30, 0xFFFF6D00)
    )

    fun random(): MarketEvent = events.random()
    fun activeFor(state: GameState): MarketEvent? {
        // basit: toplam kazı sayısına göre döngü
        if (state.totalScratched < 20) return null
        val idx = (state.totalScratched / 25) % (events.size + 2)
        return if (idx < events.size) events[idx.toInt()] else null
    }
}

data class LoanOffer(
    val id: String,
    val amount: Long,
    val interest: Float, // tek sefer
    val title: String,
    val desc: String,
    val icon: String,
    val color: Long
)

object PixelLoans {
    val offers = listOf(
        LoanOffer("shark_small", 400, 0.0f, "Küçük Tefeci", "400$ hemen, faiz yok (ilk)", "🦈", 0xFFFF1744),
        LoanOffer("shark_med", 1200, 0.25f, "Orta Tefeci", "1200$ +25% tek sefer", "🦈", 0xFFD50000),
        LoanOffer("bank", 2500, 0.15f, "Pixel Bank", "2500$ +15% faiz", "🏦", 0xFF2962FF),
        LoanOffer("mafia", 6000, 0.6f, "Mafya", "6000$ +60% ama hızlı", "🎩", 0xFF212121),
        LoanOffer("angel", 1000, 0.0f, "Melek Yatırımcı", "1000$ faizsiz, sadece 1 kez", "👼", 0xFF00E676)
    )

    fun available(state: GameState): List<LoanOffer> {
        if (state.balance >= 0) return emptyList()
        return offers.filter { it.id != "angel" || state.loanTaken == 0 }
    }
}

data class DailyReward(
    val day: Int,
    val amount: Long,
    val bonus: String,
    val icon: String,
    val claimed: Boolean = false
)

object PixelDaily {
    fun week(): List<DailyReward> = listOf(
        DailyReward(1, 50, "50$", "🪙"),
        DailyReward(2, 100, "100$", "💰"),
        DailyReward(3, 150, "150$ + Şans+1", "🍀"),
        DailyReward(4, 200, "200$", "💵"),
        DailyReward(5, 300, "300$ + Güç+1", "💪"),
        DailyReward(6, 500, "500$", "💎"),
        DailyReward(7, 1000, "1000$ + 5JP!", "🎉")
    )

    fun today(state: GameState): DailyReward? {
        val d = (state.totalScratched / 10) % 7
        return week()[d]
    }
}

data class ComboBonus(
    val combo: Int,
    val multiplier: Float,
    val title: String,
    val color: Long
)

object PixelCombo {
    fun forCombo(c: Int): ComboBonus? = when {
        c >= 10 -> ComboBonus(c, 1.5f, "EFSANEVİ KOMBO", 0xFFFFD600)
        c >= 7 -> ComboBonus(c, 1.35f, "SÜPER KOMBO", 0xFFD500F9)
        c >= 5 -> ComboBonus(c, 1.2f, "MEGA KOMBO", 0xFFFF6D00)
        c >= 3 -> ComboBonus(c, 1.1f, "KOMBO", 0xFF00E676)
        else -> null
    }

    fun payout(base: Int, combo: Int): Int {
        val b = forCombo(combo) ?: return base
        return (base * b.multiplier).toInt()
    }
}

// ============= ENFLASYON =============
object PixelInflation {
    fun price(base: Int, totalScratched: Int): Int {
        val infl = 1 + (totalScratched / 100) * 0.03f // her 100 kazıda %3
        return (base * infl).toInt()
    }

    fun adjustForEvent(base: Int, event: MarketEvent?): Int {
        if (event == null) return base
        return (base * event.multiplier).toInt()
    }
}

// ============= SHOP PLUS =============
data class ShopItem(
    val id: String,
    val title: String,
    val desc: String,
    val icon: String,
    val price: Int,
    val effect: String,
    val rarity: Int,
    val limited: Boolean = false
)

object PixelShopPlus {
    val items = listOf(
        ShopItem("magnet", "Mıknatıs", "Sonraki 3 kartta jackpot +%20", "🧲", 800, "jackpot+20% x3", 2),
        ShopItem("glasses", "Şans Gözlüğü", "Ceza kartlarını %50 belli eder", "👓", 600, "penalty reveal", 1),
        ShopItem("coffee", "Pixel Kahve", "Kazım %30 hızlanır 10 kart", "☕", 400, "speed+30% x10", 1),
        ShopItem("dice", "Hileli Zar", "Snake Eyes’de 1 gelme %50 az", "🎲", 1000, "snake-50%", 2),
        ShopItem("clover", "4 Yaprak", "10 kazı boyunca şans +15%", "🍀", 1200, "luck+15% x10", 3),
        ShopItem("ticket", "Altın Bilet", "Bir sonraki kart ücretsiz", "🎫", 1500, "free next", 3, limited = true),
        ShopItem("vault", "Kasa", "İflas koruması 1 kez", "🔐", 2000, "bankrupt save", 2),
        ShopItem("rainbow", "Gökkuşağı Kazıyıcı", "5 kart boyunca foil hologram", "🌈", 3000, "hologram x5", 4, limited = true)
    )

    fun affordable(state: GameState): List<ShopItem> = items.filter { it.price <= state.balance }

    fun purchase(state: GameState, id: String): Pair<GameState, String> {
        val item = items.find { it.id == id } ?: return state to "Yok"
        if (state.balance < item.price) return state to "Para yok"
        val ns = state.copy(balance = state.balance - item.price)
        return ns to "${item.title} alındı! ${item.effect}"
    }
}

// ============= KASA & BORÇ =============
data class DebtState(
    val totalDebt: Long = 0,
    val loansTaken: Int = 0,
    val lastLoanAt: Int = 0 // totalScratched at loan
)

object PixelDebt {
    fun applyLoan(state: GameState, offer: LoanOffer): GameState {
        val debt = (offer.amount * offer.interest).toLong()
        return state.copy(
            balance = state.balance + offer.amount,
            loanTaken = state.loanTaken + 1,
            history = (listOf("${offer.title} +${offer.amount}$ (borç +${debt}$)") + state.history).take(20)
        )
    }

    fun debtDue(state: GameState): Long {
        // her 50 kazıda borç tahsili %10
        if (state.loanTaken == 0) return 0
        val due = state.loanTaken * 40L
        return due
    }

    fun collectDebt(state: GameState): GameState {
        val due = debtDue(state)
        if (due == 0L || state.balance < due) return state
        return state.copy(
            balance = state.balance - due,
            history = (listOf("Borç tahsil: -${due}$") + state.history).take(20)
        )
    }
}

// ============= EKONOMİ UTILS =============
object PixelEconomyUtils {
    fun formatMoney(v: Long): String = when {
        v >= 1_000_000 -> "${v / 1_000_000}M"
        v >= 1_000 -> "${v / 1000}K"
        v < 0 -> "-${kotlin.math.abs(v)}"
        else -> "$v"
    }

    fun formatMoneyFull(v: Long): String = "$v $"

    fun canAfford(state: GameState, price: Int): Boolean = state.balance >= price

    fun afterPurchase(state: GameState, price: Int): Long = state.balance - price

    fun profit(state: GameState, card: CardDefinition, payout: Int): Long = payout - card.cost.toLong()

    fun bestCard(state: GameState): CardDefinition? {
        return state.let {
            // en karlı kart hesap (seviye ve şans ile)
            com.scritchyscratchy.watch.CardCatalog.all.maxByOrNull { c ->
                val lvl = state.cardLevels[c.id] ?: 1
                val base = c.basePayout * (1 + (lvl - 1) * 0.25f)
                val win = when (c.type) {
                    com.scritchyscratchy.watch.CardType.QUICK_CASH -> 0.42f
                    com.scritchyscratchy.watch.CardType.SNAKE_EYES -> 0.30f
                    else -> 0.25f
                }
                base * win - c.cost
            }
        }
    }

    fun suggestUpgrade(state: GameState): String {
        val luckCost = UpgradePricing.luckCost(state.upgrades.luckLevel)
        val powerCost = UpgradePricing.powerCost(state.upgrades.scratchPowerLevel)
        val areaCost = UpgradePricing.areaCost(state.upgrades.areaSizeLevel)
        val cheapest = listOf("Şans" to luckCost, "Güç" to powerCost, "Alan" to areaCost).minByOrNull { it.second }
        return cheapest?.first ?: "Yok"
    }

    fun inflationWarning(totalScratched: Int): String? {
        if (totalScratched > 500 && totalScratched % 100 == 0) return "Enflasyon +%3 fiyat artışı!"
        return null
    }

    fun marketWarning(event: MarketEvent?): String? = event?.let { "Piyasa: ${it.title} (${it.desc})" }

    // Rastgele indirim
    fun randomDiscount(): Int = if (Random.nextFloat() < 0.08f) Random.nextInt(10, 30) else 0

    fun discountedPrice(base: Int, discount: Int): Int = (base * (1 - discount / 100f)).toInt()

    // Kasa anahtarı bonus
    fun vaultBonus(state: GameState): Long = if (state.balance < 0 && state.totalJackpots > 5) 100 else 0
}
