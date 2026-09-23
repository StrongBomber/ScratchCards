package com.scritchyscratchy.watch.pixel

import com.scritchyscratchy.watch.GameState
import com.scritchyscratchy.watch.PrestigeUpgrades
import com.scritchyscratchy.watch.PrestigeState

// ============= EXPANDED PRESTIGE TREE - 16 NODES (ORIJINAL 8 + 8 YENI PIXEL) =============

data class PrestigeNode(
    val id: String,
    val title: String,
    val desc: String,
    val icon: String,
    val cost: Int,
    val tier: Int, // 1-4
    val requires: List<String> = emptyList(),
    val color: Long,
    val isNewPixel: Boolean = false
)

object PixelPrestigeExpanded {
    val nodes = listOf(
        // Tier 1 - Başlangıç (orijinal)
        PrestigeNode("headStart", "Başlangıç Sermayesi", "Her koşu 500$ ile başla", "💵", 10, 1, color = 0xFF4CAF50),
        PrestigeNode("luckyLegacy", "Şanslı Miras", "+15% kalıcı şans", "🍀", 15, 1, color = 0xFFFFD600),
        PrestigeNode("scratchMemory", "Kazı Hafızası", "Oto-kazı baştan açık", "🤖", 20, 1, color = 0xFF2196F3),
        PrestigeNode("recyclerPro", "Geri Dönüşüm Pro", "Çöp %10 iade", "♻️", 15, 1, color = 0xFF9E9E9E),

        // Tier 2 - Güç (orijinal + yeni)
        PrestigeNode("goldenHands", "Altın Eller", "Tüm ödüller +25%", "✨", 25, 2, requires = listOf("luckyLegacy"), color = 0xFFFFD600),
        PrestigeNode("jackpotMagnet", "Jackpot Mıknatısı", "Jackpot 2x sıklık", "🧲", 30, 2, requires = listOf("goldenHands"), color = 0xFFFF6D00),
        PrestigeNode("speedDemon", "Hız Canavarı", "Kazı Gücü Lv2 başla", "⚡", 20, 2, requires = listOf("scratchMemory"), color = 0xFF00E676),
        PrestigeNode("banco", "Banco", "İflas koruması", "🏦", 35, 2, requires = listOf("headStart"), color = 0xFF2962FF),

        // Tier 3 - Pixel Yeni
        PrestigeNode("pixelVision", "Pixel Görüş", "Ceza kartlarını %30 önceden gösterir", "👓", 40, 3, requires = listOf("jackpotMagnet", "speedDemon"), color = 0xFF00BCD4, isNewPixel = true),
        PrestigeNode("coinFrenzy", "Para Çılgınlığı", "Her 10. kazı +%50 bonus", "💸", 35, 3, requires = listOf("goldenHands"), color = 0xFFFFAB00, isNewPixel = true),
        PrestigeNode("neonFoil", "Neon Folyo", "Hologram kart şansı x2", "🌈", 45, 3, requires = listOf("banco"), color = 0xFFD500F9, isNewPixel = true),
        PrestigeNode("timeWarp", "Zaman Bükücü", "Oto-kazı %40 hızlanır", "⏱️", 50, 3, requires = listOf("scratchMemory"), color = 0xFF607D8B, isNewPixel = true),

        // Tier 4 - Efsanevi Pixel
        PrestigeNode("quantumLuck", "Kuantum Şans", "Şans +%25 ve jackpot +1JP", "⚛️", 75, 4, requires = listOf("pixelVision", "coinFrenzy"), color = 0xFFFFD600, isNewPixel = true),
        PrestigeNode("voidTouch", "Boşluk Dokunuşu", "Final Chance %1→%2", "🕳️", 100, 4, requires = listOf("neonFoil", "timeWarp"), color = 0xFF212121, isNewPixel = true),
        PrestigeNode("arcadeMaster", "Arcade Ustası", "Tüm kart seviyeleri +2 başlar", "🕹️", 80, 4, requires = listOf("quantumLuck"), color = 0xFFFF1744, isNewPixel = true),
        PrestigeNode("infiniteScratch", "Sonsuz Kazı", "Sonsuz mod açılır (prestij gerekmez)", "♾️", 150, 4, requires = listOf("voidTouch", "arcadeMaster"), color = 0xFF00E676, isNewPixel = true)
    )

    fun byId(id: String): PrestigeNode? = nodes.find { it.id == id }
    fun tier(t: Int): List<PrestigeNode> = nodes.filter { it.tier == t }
    fun unlocked(state: GameState, id: String): Boolean = when (id) {
        "headStart" -> state.prestige.upgrades.headStartCapital
        "luckyLegacy" -> state.prestige.upgrades.luckyLegacy
        "scratchMemory" -> state.prestige.upgrades.scratchMemory
        "recyclerPro" -> state.prestige.upgrades.recyclerPro
        "goldenHands" -> state.prestige.upgrades.goldenHands
        "jackpotMagnet" -> state.prestige.upgrades.jackpotMagnet
        "speedDemon" -> state.prestige.upgrades.speedDemon
        "banco" -> state.prestige.upgrades.banco
        else -> false // yeni pixel node’lar ayrı saklanır, basit tutmak için false
    }

    // Yeni pixel node’lar için ayrı prefs gerekir - şimdilik mock: hepsi kapalı, JP ile açılır
    fun canBuy(state: GameState, node: PrestigeNode): Boolean {
        if (state.prestige.jackPoints < node.cost) return false
        if (unlocked(state, node.id)) return false
        // gereksinimler
        for (req in node.requires) if (!unlocked(state, req)) return false
        // tier 3+ için en az 1 prestij
        if (node.tier >= 3 && state.prestige.prestigeCount < 1) return false
        if (node.tier >= 4 && state.prestige.prestigeCount < 3) return false
        return true
    }

    fun totalCostAll(): Int = nodes.sumOf { it.cost }
    fun totalCostTier(t: Int): Int = tier(t).sumOf { it.cost }

    fun progress(state: GameState): Float {
        val unlockedCount = nodes.count { unlocked(state, it.id) }
        return unlockedCount.toFloat() / nodes.size
    }

    fun jpNeededForAll(state: GameState): Int {
        val have = state.prestige.jackPoints
        val need = totalCostAll() - nodes.filter { unlocked(state, it.id) }.sumOf { it.cost }
        return (need - have).coerceAtLeast(0)
    }

    fun nextRecommended(state: GameState): PrestigeNode? {
        // en ucuz alınabilir
        return nodes.filter { canBuy(state, it) }.minByOrNull { it.cost }
    }

    fun pathTo(nodeId: String): List<String> {
        val node = byId(nodeId) ?: return emptyList()
        val path = mutableListOf<String>()
        fun dfs(id: String) {
            val n = byId(id) ?: return
            for (r in n.requires) dfs(r)
            if (id !in path) path.add(id)
        }
        dfs(nodeId)
        return path
    }

    // Prestij sonrası bonus hesap
    fun prestigeBonus(state: GameState): Int {
        // her jackpot 2 JP, her 100k toplam 1 JP, her prestij +5
        val jpFromJackpots = state.totalJackpots * 2
        val jpFromMoney = (state.totalWon / 100_000).toInt()
        val jpFromCount = state.prestige.prestigeCount * 5
        return (jpFromJackpots + jpFromMoney + jpFromCount).coerceAtLeast(5)
    }

    // Kalıcı etkiler metni
    fun effectText(node: PrestigeNode): String = when (node.id) {
        "headStart" -> "Başlangıç 500$"
        "luckyLegacy" -> "+15% şans"
        "scratchMemory" -> "Oto açık"
        "recyclerPro" -> "%10 iade"
        "goldenHands" -> "+25% ödül"
        "jackpotMagnet" -> "Jackpot 2x"
        "speedDemon" -> "Güç Lv2"
        "banco" -> "İflas yok"
        "pixelVision" -> "Ceza görünür"
        "coinFrenzy" -> "10.kart x1.5"
        "neonFoil" -> "Hologram 2x"
        "timeWarp" -> "Oto %40 hız"
        "quantumLuck" -> "+25% +1JP"
        "voidTouch" -> "Final %2"
        "arcadeMaster" -> "Kart +2 lvl"
        "infiniteScratch" -> "Sonsuz mod"
        else -> node.desc
    }

    // Renk
    fun colorFor(node: PrestigeNode, unlocked: Boolean, canBuy: Boolean): Long = when {
        unlocked -> 0xFF2E7D32
        canBuy -> node.color
        else -> 0xFF424242
    }

    // Tier başlık
    fun tierTitle(t: Int): String = when (t) {
        1 -> "TEMEL"
        2 -> "GÜÇ"
        3 -> "PİXEL"
        4 -> "EFSANE"
        else -> "?"
    }

    fun tierIcon(t: Int): String = when (t) {
        1 -> "◆"
        2 -> "◈"
        3 -> "⬢"
        4 -> "⬣"
        else -> "•"
    }

    // Toplam istatistik
    fun stats(state: GameState): String {
        val u = nodes.count { unlocked(state, it.id) }
        return "$u/${nodes.size} • ${state.prestige.jackPoints} JP"
    }

    // En pahalı
    fun mostExpensive(): PrestigeNode = nodes.maxByOrNull { it.cost }!!

    // Yeni pixel oranı
    fun pixelRatio(state: GameState): Float {
        val pixelNodes = nodes.filter { it.isNewPixel }
        val unlockedPixel = pixelNodes.count { unlocked(state, it.id) }
        return unlockedPixel.toFloat() / pixelNodes.size
    }
}
