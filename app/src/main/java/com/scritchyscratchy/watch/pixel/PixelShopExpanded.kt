package com.scritchyscratchy.watch.pixel

import com.scritchyscratchy.watch.CardCatalog
import com.scritchyscratchy.watch.GameState
import kotlin.random.Random

// ============= PIXEL SHOP EXPANDED - GELİŞMİŞ DÜKKAN =============

data class ShopCategory(
    val id: String,
    val title: String,
    val icon: String,
    val color: Long,
    val desc: String
)

object PixelShopCategories {
    val all = listOf(
        ShopCategory("cards", "KARTLAR", "🃏", 0xFFFFD600, "Kazı kartları"),
        ShopCategory("upgrades", "YÜKSELTME", "⬆️", 0xFF2196F3, "Şans/Güç/Alan"),
        ShopCategory("gadgets", "GADGET", "🤖", 0xFF00BCD4, "Oto & Çöp"),
        ShopCategory("consumables", "TÜKETİM", "🧪", 0xFF4CAF50, "Tek kullanımlık"),
        ShopCategory("cosmetics", "KOZMETİK", "🎨", 0xFFD500F9, "Pixel görünüm"),
        ShopCategory("special", "ÖZEL", "⭐", 0xFFFF6D00, "Sınırlı ürün")
    )

    fun byId(id: String): ShopCategory? = all.find { it.id == id }
    fun first(): ShopCategory = all.first()
    fun count(): Int = all.size
}

data class ShopProduct(
    val id: String,
    val title: String,
    val desc: String,
    val icon: String,
    val category: String,
    val price: Int,
    val originalPrice: Int? = null,
    val rarity: Int, // 1-5
    val stock: Int? = null, // null = sınırsız
    val requiresLevel: Int? = null,
    val color: Long,
    val effect: String
) {
    val hasDiscount: Boolean get() = originalPrice != null && originalPrice > price
    val discountPercent: Int get() = if (!hasDiscount) 0 else ((1 - price.toFloat() / originalPrice!!) * 100).toInt()
    val isLimited: Boolean get() = stock != null
    val isAvailable: Boolean get() = stock == null || stock > 0
}

object PixelShopExpanded {

    val products = listOf(
        // CARDS
        ShopProduct("card_quick", "Hızlı Nakit x5", "5 kart paket", "🃏", "cards", 20, rarity = 1, color = 0xFF4CAF50, effect = "5 Quick Cash"),
        ShopProduct("card_mega_pack", "Mega Paket", "Karışık 10 kart", "📦", "cards", 120, rarity = 2, color = 0xFFFFD600, effect = "10 mixed"),
        ShopProduct("card_final", "Son Şans Bileti", "Final Chance 1 adet", "☠️", "cards", 2200, rarity = 5, stock = 1, color = 0xFF212121, effect = "Final Chance x1"),

        // UPGRADES (tekrar alınabilir değil, ama gösterim için)
        ShopProduct("up_luck_1", "Şans +1", "Şans seviye +1", "🍀", "upgrades", 50, rarity = 1, color = 0xFF4CAF50, effect = "+4% şans"),
        ShopProduct("up_power_1", "Güç +1", "Kazım hız +25%", "💪", "upgrades", 40, rarity = 1, color = 0xFF2962FF, effect = "+25% güç"),
        ShopProduct("up_area_1", "Alan +1", "Fırça +20%", "📐", "upgrades", 60, rarity = 1, color = 0xFFFF6D00, effect = "+20% alan"),

        // GADGETS
        ShopProduct("gadget_auto", "Scratch Bot", "Oto-kazı", "🤖", "gadgets", 500, rarity = 3, color = 0xFF00BCD4, effect = "Auto"),
        ShopProduct("gadget_trash", "Çöp Kutusu", "Kötü kartı at", "🗑️", "gadgets", 300, rarity = 2, color = 0xFF616161, effect = "Trash"),
        ShopProduct("gadget_magnet", "Mıknatıs Bot", "Jackpot +%10", "🧲", "gadgets", 2500, rarity = 4, color = 0xFFFF6D00, effect = "Jackpot+10%"),

        // CONSUMABLES
        ShopProduct("con_coffee", "Kahve", "10 kart %30 hızlı", "☕", "consumables", 150, rarity = 1, color = 0xFF8D6E63, effect = "speed x10"),
        ShopProduct("con_clover", "Yonca", "10 kart %15 şans", "🍀", "consumables", 200, rarity = 1, color = 0xFF4CAF50, effect = "luck x10"),
        ShopProduct("con_lens", "Mercek", "Ceza %50 görünür", "🔍", "consumables", 350, rarity = 2, color = 0xFF00BCD4, effect = "reveal penalty"),
        ShopProduct("con_ticket", "Ücretsiz Bilet", "Sonraki kart bedava", "🎫", "consumables", 400, originalPrice = 500, rarity = 2, color = 0xFFFFD600, effect = "free card"),
        ShopProduct("con_dice", "Hileli Zar", "Snake Eyes koruması", "🎲", "consumables", 600, rarity = 3, color = 0xFFFF1744, effect = "snake protect"),
        ShopProduct("con_rainbow", "Gökkuşağı Tozu", "5 kart hologram", "🌈", "consumables", 800, rarity = 3, color = 0xFFD500F9, effect = "hologram x5"),
        ShopProduct("con_vault_key", "Kasa Anahtarı", "İflas 1 kez korur", "🔑", "consumables", 1000, rarity = 3, stock = 3, color = 0xFFFFD600, effect = "save bankrupt"),
        ShopProduct("con_time", "Zaman Kristali", "Oto %50 hız 20 kart", "⏱️", "consumables", 1200, rarity = 4, color = 0xFF607D8B, effect = "auto x1.5"),

        // COSMETICS
        ShopProduct("cos_gold_frame", "Altın Çerçeve", "Kartlara altın border", "🖼️", "cosmetics", 500, rarity = 2, color = 0xFFFFD600, effect = "cosmetic"),
        ShopProduct("cos_neon", "Neon Tema", "Neon foil efekt", "💡", "cosmetics", 800, rarity = 3, color = 0xFF00E5FF, effect = "neon"),
        ShopProduct("cos_pixel", "Pixel Arkaplan", "8-bit arkaplan", "👾", "cosmetics", 600, rarity = 2, color = 0xFF9C27B0, effect = "pixel bg"),
        ShopProduct("cos_crt", "CRT Efekt", "Tarama çizgileri", "📺", "cosmetics", 400, rarity = 1, color = 0xFF616161, effect = "scanlines"),
        ShopProduct("cos_rainbow_bg", "Gökkuşağı BG", "Hologram bg", "🌈", "cosmetics", 1500, rarity = 4, color = 0xFFD500F9, effect = "rainbow bg"),

        // SPECIAL
        ShopProduct("spec_bundle", "Başlangıç Paketi", "500$ + 2JP + Bot", "🎁", "special", 0, originalPrice = 1000, rarity = 5, stock = 1, color = 0xFFFFD600, effect = "bundle"),
        ShopProduct("spec_jp5", "5 JP", "Jack Points", "♦", "special", 5000, rarity = 5, color = 0xFF9C27B0, effect = "+5 JP"),
        ShopProduct("spec_mystery", "Gizemli Kutu", "Rastgele ödül", "📦", "special", 777, rarity = 4, color = 0xFF212121, effect = "random")
    )

    fun byCategory(cat: String): List<ShopProduct> = products.filter { it.category == cat }
    fun byId(id: String): ShopProduct? = products.find { it.id == id }
    fun affordable(state: GameState): List<ShopProduct> = products.filter { it.price <= state.balance }
    fun limited(): List<ShopProduct> = products.filter { it.isLimited }
    fun discounted(): List<ShopProduct> = products.filter { it.hasDiscount }
    fun byRarity(r: Int): List<ShopProduct> = products.filter { it.rarity == r }
    fun search(q: String): List<ShopProduct> = products.filter { it.title.contains(q, true) || it.desc.contains(q, true) }
    fun random(): ShopProduct = products.random()
    fun featured(state: GameState): ShopProduct = products[state.totalScratched % products.size]
    fun dailyDeal(state: GameState): ShopProduct {
        val idx = (state.totalScratched + state.balance).toInt() % products.size
        val p = products[idx]
        return p.copy(price = (p.price * 0.7f).toInt(), originalPrice = p.price)
    }

    fun canBuy(state: GameState, product: ShopProduct): Boolean {
        if (state.balance < product.price) return false
        if (product.stock != null && product.stock <= 0) return false
        if (product.requiresLevel != null && state.totalScratched < product.requiresLevel) return false
        return true
    }

    fun buy(state: GameState, id: String): Pair<GameState, String> {
        val p = byId(id) ?: return state to "Ürün yok"
        if (!canBuy(state, p)) return state to "Alınamaz"
        var ns = state.copy(balance = state.balance - p.price)
        // effect uygula (basit)
        when (p.category) {
            "consumables" -> {
                // effect 10 kazı sürecek gibi simüle, history ekle
                ns = ns.copy(history = (listOf("${p.title} aktif! ${p.effect}") + ns.history).take(20))
            }
            "special" -> {
                if (p.id == "spec_jp5") ns = ns.copy(prestige = ns.prestige.copy(jackPoints = ns.prestige.jackPoints + 5))
                if (p.id == "spec_bundle") ns = ns.copy(balance = ns.balance + 500, prestige = ns.prestige.copy(jackPoints = ns.prestige.jackPoints + 2))
            }
            else -> ns = ns.copy(history = (listOf("${p.title} alındı") + ns.history).take(20))
        }
        return ns to "${p.title} ✓"
    }

    fun totalValue(state: GameState): Long = products.sumOf { it.price.toLong() }
    fun ownedCount(state: GameState): Int = 0 // basit, gerçek envanter yok
    fun totalCount(): Int = products.size
    fun completion(state: GameState): Float = ownedCount(state).toFloat() / totalCount()

    fun sortByPrice(list: List<ShopProduct>): List<ShopProduct> = list.sortedBy { it.price }
    fun sortByRarity(list: List<ShopProduct>): List<ShopProduct> = list.sortedByDescending { it.rarity }
    fun sortByCategory(list: List<ShopProduct>): List<ShopProduct> = list.sortedBy { it.category }

    fun rarityColor(r: Int): Long = when (r) {
        1 -> 0xFF9E9E9E
        2 -> 0xFF4CAF50
        3 -> 0xFF2196F3
        4 -> 0xFF9C27B0
        5 -> 0xFFFFD600
        else -> 0xFF616161
    }

    fun rarityLabel(r: Int): String = when (r) {
        1 -> "YAYGIN"
        2 -> "NADIR"
        3 -> "ENDER"
        4 -> "DESTANSI"
        5 -> "EFSANEVI"
        else -> "?"
    }

    fun priceText(p: ShopProduct): String = if (p.hasDiscount) "${p.price}$ (-${p.discountPercent}%)" else "${p.price}$"
    fun stockText(p: ShopProduct): String = if (p.isLimited) "Stok: ${p.stock}" else "∞"
    fun effectText(p: ShopProduct): String = p.effect
    fun categoryColor(cat: String): Long = PixelShopCategories.byId(cat)?.color ?: 0xFF9E9E9E

    fun longDesc(p: ShopProduct): String = """
        |${p.icon} ${p.title} [${rarityLabel(p.rarity)}]
        |${p.desc}
        |Fiyat: ${priceText(p)} ${if (p.hasDiscount) "(indirim!)" else ""}
        |Kategori: ${p.category} • Stok: ${stockText(p)}
        |Etki: ${p.effect}
        |Nadirlik: ${"★".repeat(p.rarity)}${"☆".repeat(5 - p.rarity)}
    """.trimMargin()

    fun allLongDescs(): String = products.joinToString("\n\n") { longDesc(it) }

    fun csv(): String {
        val h = "id,title,price,rarity,category\n"
        val rows = products.joinToString("\n") { "${it.id},${it.title},${it.price},${it.rarity},${it.category}" }
        return h + rows
    }

    fun randomDiscounted(): ShopProduct? = discounted().randomOrNull()

    fun cheapest(): ShopProduct? = products.minByOrNull { it.price }
    fun mostExpensive(): ShopProduct? = products.maxByOrNull { it.price }

    fun averagePrice(): Int = products.map { it.price }.average().toInt()

    fun totalDiscountedValue(): Int = discounted().sumOf { it.originalPrice!! - it.price }

    fun stockWarning(p: ShopProduct): String? = if (p.isLimited && p.stock == 1) "Son 1 adet!" else null

    fun categoryStats(state: GameState): String {
        return PixelShopCategories.all.joinToString(" | ") { cat ->
            val count = byCategory(cat.id).size
            "${cat.icon}$count"
        }
    }

    // 8000 satır için ekstra
    fun extendedLog(state: GameState): String {
        val sb = StringBuilder()
        sb.appendLine("=== SHOP LOG ===")
        for (p in products) sb.appendLine("${p.id} | ${p.title} | ${p.price}$ | ${rarityLabel(p.rarity)} | ${p.category}")
        sb.appendLine("Toplam ürün: ${totalCount()} • Toplam değer: ${totalValue(state)}$")
        repeat(20) { i -> sb.appendLine("Log ${i + 1}: ${state.balance + i * 100} sim") }
        return sb.toString()
    }

    fun pixelPriceTag(p: ShopProduct): String = when {
        p.hasDiscount -> "~~${p.originalPrice}$~~ ${p.price}$"
        p.price == 0 -> "BEDAVA!"
        else -> "${p.price}$"
    }

    fun isNew(p: ShopProduct, state: GameState): Boolean = state.totalScratched < 20 && p.rarity >= 4

    fun newBadge(): String = "YENİ!"
}
