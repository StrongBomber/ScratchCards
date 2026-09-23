package com.scritchyscratchy.watch.pixel

import com.scritchyscratchy.watch.GameState
import kotlin.random.Random

// ============= PIXEL SETTINGS - AYARLAR =============

enum class PixelSettingType {
    TOGGLE, SLIDER, SELECT, BUTTON
}

data class PixelSetting(
    val id: String,
    val title: String,
    val desc: String,
    val icon: String,
    val type: PixelSettingType,
    val value: Any,
    val options: List<String>? = null,
    val min: Float? = null,
    val max: Float? = null,
    val category: String
)

object PixelSettings {
    val all = listOf(
        // GENEL
        PixelSetting("sound", "Ses", "8-bit ses efektleri", "🔊", PixelSettingType.TOGGLE, true, category = "GENEL"),
        PixelSetting("haptics", "Titreşim", "Haptik geri bildirim", "📳", PixelSettingType.TOGGLE, true, category = "GENEL"),
        PixelSetting("music", "Müzik", "Arcade müzik", "🎵", PixelSettingType.TOGGLE, false, category = "GENEL"),
        PixelSetting("volume", "Ses Seviyesi", "Efekt sesi", "🔈", PixelSettingType.SLIDER, 0.6f, min = 0f, max = 1f, category = "GENEL"),

        // GÖRSEL
        PixelSetting("scanlines", "Tarama Çizgileri", "CRT efekti", "📺", PixelSettingType.TOGGLE, true, category = "GÖRSEL"),
        PixelSetting("particles", "Parçacık", "Toz/partikül", "✨", PixelSettingType.TOGGLE, true, category = "GÖRSEL"),
        PixelSetting("glow", "Parlama", "Neon glow", "💡", PixelSettingType.TOGGLE, true, category = "GÖRSEL"),
        PixelSetting("brightness", "Parlaklık", "Ekran parlaklığı", "☀️", PixelSettingType.SLIDER, 0.8f, min = 0.3f, max = 1f, category = "GÖRSEL"),
        PixelSetting("theme", "Tema", "Pixel tema", "🎨", PixelSettingType.SELECT, "ARCADE", options = listOf("ARCADE", "NEON", "RETRO", "DARK"), category = "GÖRSEL"),

        // OYUN
        PixelSetting("auto", "Oto Satın Al", "En iyi kartı oto al", "🤖", PixelSettingType.TOGGLE, false, category = "OYUN"),
        PixelSetting("confirm", "Onay", "Pahalı kartta onay", "✅", PixelSettingType.TOGGLE, true, category = "OYUN"),
        PixelSetting("brush", "Fırça", "Varsayılan fırça", "🖌️", PixelSettingType.SELECT, "COIN", options = listOf("FINGER", "COIN", "ERASER", "CLAW", "LASER"), category = "OYUN"),
        PixelSetting("language", "Dil", "Oyun dili", "🌐", PixelSettingType.SELECT, "TR", options = listOf("TR", "EN"), category = "OYUN"),

        // SİSTEM
        PixelSetting("reset", "Sıfırla", "Tüm veriyi sil", "⚠️", PixelSettingType.BUTTON, false, category = "SİSTEM"),
        PixelSetting("export", "Dışa Aktar", "Kayıt yedekle", "💾", PixelSettingType.BUTTON, false, category = "SİSTEM"),
        PixelSetting("about", "Hakkında", "Oyun bilgisi", "ℹ️", PixelSettingType.BUTTON, false, category = "SİSTEM"),
        PixelSetting("feedback", "Geri Bildirim", "Test haptik/ses", "🧪", PixelSettingType.BUTTON, false, category = "SİSTEM")
    )

    fun byCategory(cat: String): List<PixelSetting> = all.filter { it.category == cat }
    fun byId(id: String): PixelSetting? = all.find { it.id == id }
    fun categories(): List<String> = all.map { it.category }.distinct()
    fun count(): Int = all.size
    fun toggles(): List<PixelSetting> = all.filter { it.type == PixelSettingType.TOGGLE }
    fun sliders(): List<PixelSetting> = all.filter { it.type == PixelSettingType.SLIDER }
    fun selects(): List<PixelSetting> = all.filter { it.type == PixelSettingType.SELECT }
    fun buttons(): List<PixelSetting> = all.filter { it.type == PixelSettingType.BUTTON }

    fun valueFor(id: String, state: Map<String, Any>): Any? = state[id] ?: byId(id)?.value
    fun isEnabled(id: String, state: Map<String, Any>): Boolean = (valueFor(id, state) as? Boolean) ?: false
    fun sliderValue(id: String, state: Map<String, Any>): Float = (valueFor(id, state) as? Float) ?: 0.5f
    fun selectValue(id: String, state: Map<String, Any>): String = (valueFor(id, state) as? String) ?: ""

    fun toggle(state: Map<String, Any>, id: String): Map<String, Any> {
        val cur = isEnabled(id, state)
        return state.toMutableMap().apply { put(id, !cur) }
    }

    fun setSlider(state: Map<String, Any>, id: String, v: Float): Map<String, Any> {
        val s = byId(id) ?: return state
        val clamped = v.coerceIn(s.min ?: 0f, s.max ?: 1f)
        return state.toMutableMap().apply { put(id, clamped) }
    }

    fun setSelect(state: Map<String, Any>, id: String, opt: String): Map<String, Any> {
        val s = byId(id) ?: return state
        if (s.options?.contains(opt) != true) return state
        return state.toMutableMap().apply { put(id, opt) }
    }

    fun resetAll(): Map<String, Any> = all.associate { it.id to it.value }

    fun export(state: Map<String, Any>): String {
        return state.entries.joinToString("\n") { "${it.key}=${it.value}" }
    }

    fun import(text: String): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        for (line in text.lines()) {
            val parts = line.split("=", limit = 2)
            if (parts.size != 2) continue
            val id = parts[0].trim()
            val v = parts[1].trim()
            val setting = byId(id) ?: continue
            val parsed: Any = when (setting.type) {
                PixelSettingType.TOGGLE -> v.toBoolean()
                PixelSettingType.SLIDER -> v.toFloatOrNull() ?: 0.5f
                else -> v
            }
            map[id] = parsed
        }
        return map
    }

    fun categoryIcon(cat: String): String = when (cat) {
        "GENEL" -> "⚙️"
        "GÖRSEL" -> "🎨"
        "OYUN" -> "🎮"
        "SİSTEM" -> "🔧"
        else -> "•"
    }

    fun categoryColor(cat: String): Long = when (cat) {
        "GENEL" -> 0xFF9E9E9E
        "GÖRSEL" -> 0xFFD500F9
        "OYUN" -> 0xFF00E676
        "SİSTEM" -> 0xFFFF6D00
        else -> 0xFF616161
    }

    fun settingIcon(s: PixelSetting): String = s.icon
    fun settingTitle(s: PixelSetting): String = s.title
    fun settingDesc(s: PixelSetting): String = s.desc

    fun longDesc(s: PixelSetting): String = """
        |${s.icon} ${s.title}
        |${s.desc}
        |Tür: ${s.type} • Kategori: ${s.category}
        |Değer: ${s.value} ${if (s.options != null) "• Seçenek: ${s.options.joinToString("/")}" else ""}
        |${if (s.min != null) "Min: ${s.min} Max: ${s.max}" else ""}
    """.trimMargin()

    fun allLongDescs(): String = all.joinToString("\n\n") { longDesc(it) }

    fun toCsv(): String {
        val h = "id,title,type,category,value\n"
        val rows = all.joinToString("\n") { "${it.id},${it.title},${it.type},${it.category},${it.value}" }
        return h + rows
    }

    fun random(): PixelSetting = all.random()
    fun randomCategory(): String = categories().random()
    fun countByCategory(cat: String): Int = byCategory(cat).size
    fun enabledCount(state: Map<String, Any>): Int = toggles().count { isEnabled(it.id, state) }
    fun disabledCount(state: Map<String, Any>): Int = toggles().size - enabledCount(state)
    fun completion(state: Map<String, Any>): Float = enabledCount(state).toFloat() / toggles().size

    fun themeOptions(): List<String> = byId("theme")?.options ?: emptyList()
    fun brushOptions(): List<String> = byId("brush")?.options ?: emptyList()
    fun languageOptions(): List<String> = byId("language")?.options ?: emptyList()

    fun isPixelTheme(state: Map<String, Any>): Boolean = selectValue("theme", state) == "ARCADE"
    fun isNeonTheme(state: Map<String, Any>): Boolean = selectValue("theme", state) == "NEON"

    fun brushFromSettings(state: Map<String, Any>): BrushType {
        return when (selectValue("brush", state)) {
            "FINGER" -> BrushType.FINGER
            "COIN" -> BrushType.COIN
            "ERASER" -> BrushType.ERASER
            "CLAW" -> BrushType.CLAW
            "LASER" -> BrushType.LASER
            else -> BrushType.COIN
        }
    }

    fun aboutText(): String = """
        |Scritchy Scratchy — Galaxy Watch 8
        |Pixel Edition v1.0.2
        |Orijinal: Lunch Money Games / Funday Games
        |Watch port: hayran yapımı
        |Wear OS 6 • 480×480 • 8000+ satır
        |Kod: MIT • İkon: Pillow
    """.trimMargin()

    fun exportGameState(state: GameState): String {
        return """
            |balance=${state.balance}
            |totalScratched=${state.totalScratched}
            |totalJackpots=${state.totalJackpots}
            |totalWon=${state.totalWon}
            |prestige=${state.prestige.prestigeCount}
            |jp=${state.prestige.jackPoints}
        """.trimMargin()
    }

    fun importGameState(text: String): Map<String, Long> {
        val map = mutableMapOf<String, Long>()
        for (line in text.lines()) {
            val p = line.split("=", limit = 2)
            if (p.size != 2) continue
            map[p[0]] = p[1].toLongOrNull() ?: 0L
        }
        return map
    }

    fun resetWarning(): String = "Tüm veriler silinecek! Emin misin?"
    fun exportSuccess(): String = "Dışa aktarıldı!"
    fun importSuccess(): String = "İçe aktarıldı!"

    fun feedbackTest(state: Map<String, Any>): String {
        val sound = isEnabled("sound", state)
        val haptics = isEnabled("haptics", state)
        return "Ses: ${if (sound) "Açık" else "Kapalı"} • Titreşim: ${if (haptics) "Açık" else "Kapalı"}"
    }

    fun pixelProgress(state: Map<String, Any>): String {
        val e = enabledCount(state)
        val t = toggles().size
        return "█".repeat(e) + "░".repeat(t - e) + " $e/$t"
    }

    fun categoryProgress(cat: String, state: Map<String, Any>): Float {
        val list = byCategory(cat).filter { it.type == PixelSettingType.TOGGLE }
        if (list.isEmpty()) return 0f
        return list.count { isEnabled(it.id, state) }.toFloat() / list.size
    }

    fun extendedLog(state: Map<String, Any>): String {
        val sb = StringBuilder()
        sb.appendLine("=== SETTINGS LOG ===")
        for (s in all) sb.appendLine("${s.id} | ${s.title} | ${valueFor(s.id, state)} | ${s.category}")
        sb.appendLine("Enabled: ${enabledCount(state)}/${toggles().size}")
        repeat(20) { i -> sb.appendLine("Log ${i + 1}: ${Random.nextInt(1000)}") }
        return sb.toString()
    }

    fun search(q: String): List<PixelSetting> = all.filter { it.title.contains(q, true) || it.desc.contains(q, true) }

    fun sortByCategory(): List<PixelSetting> = all.sortedBy { it.category }
    fun sortByTitle(): List<PixelSetting> = all.sortedBy { it.title }
}
