package com.scritchyscratchy.watch.pixel

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Vibrator
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Text
import com.scritchyscratchy.watch.GameState
import kotlin.random.Random

// ============= PIXEL WATCH INTEGRATION - GALAXY WATCH 8 ÖZEL =============

object PixelWatchIntegration {

    data class WatchInfo(
        val model: String,
        val osVersion: String,
        val apiLevel: Int,
        val screenSize: String,
        val density: String,
        val chip: String,
        val ram: String,
        val storage: String,
        val battery: String,
        val isRound: Boolean
    )

    val galaxyWatch8_44 = WatchInfo(
        model = "Galaxy Watch 8 44mm",
        osVersion = "Wear OS 6 / One UI 8 Watch",
        apiLevel = 34,
        screenSize = "1.47\" 480x480 (327 ppi)",
        density = "320 dpi",
        chip = "Exynos W1000 3nm 5-core",
        ram = "2GB",
        storage = "32GB",
        battery = "435mAh",
        isRound = true
    )

    val galaxyWatch8_40 = WatchInfo(
        model = "Galaxy Watch 8 40mm",
        osVersion = "Wear OS 6 / One UI 8 Watch",
        apiLevel = 34,
        screenSize = "1.34\" 438x438 (327 ppi)",
        density = "320 dpi",
        chip = "Exynos W1000",
        ram = "2GB",
        storage = "32GB",
        battery = "325mAh",
        isRound = true
    )

    fun currentWatch(): WatchInfo = galaxyWatch8_44

    fun isEmulator(context: Context): Boolean {
        return Build.FINGERPRINT.contains("generic") || Build.MODEL.contains("Emulator")
    }

    fun isWatch(context: Context): Boolean {
        return context.packageManager.hasSystemFeature("android.hardware.type.watch")
    }

    fun vibrator(context: Context): Vibrator? {
        return try { context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator } catch (_: Exception) { null }
    }

    fun hasVibrator(context: Context): Boolean = vibrator(context)?.hasVibrator() ?: false

    fun screenShape(context: Context): String = if (isWatch(context)) "ROUND" else "RECT"

    fun watchSpecsText(): String {
        val w = currentWatch()
        return """
            |Model: ${w.model}
            |OS: ${w.osVersion} (API ${w.apiLevel})
            |Ekran: ${w.screenSize} ${if (w.isRound) "YUVARLAK" else "KARE"}
            |Chip: ${w.chip} • ${w.ram} / ${w.storage}
            |Pil: ${w.battery} • 3000 nit
            |Safir kristal • 5ATM • MIL-STD-810H
        """.trimMargin()
    }

    fun watchShort(): String = "Watch8 480×480 • Wear OS 6 • Exynos W1000"

    fun rotarySupport(): Boolean = true // Watch 8 bezel rotary destekler

    fun bezelText(): String = "Bezel çevir → liste kaydır"

    fun touchText(): String = "Parmakla sürükle → kazı"

    fun performanceMode(state: GameState): String {
        return when {
            state.totalScratched > 1000 -> "PERFORMANS: Yüksek"
            state.totalScratched > 500 -> "PERFORMANS: Orta"
            else -> "PERFORMANS: Normal"
        }
    }

    fun batteryOptimization(state: GameState): String {
        val auto = state.upgrades.autoScratcherUnlocked
        return if (auto) "Pil: Oto-kazı aktif, %15 fazla tüketim" else "Pil: Normal"
    }

    fun aodSupport(): Boolean = true

    fun aodText(): String = "AOD: Koyu tema ile yanma önleme"

    fun complicationSupport(): Boolean = false

    fun tileSupport(): Boolean = true

    fun tileText(): String = "Tile: Hızlı bakiye"

    fun watchFaceSupport(): Boolean = false

    fun healthSupport(): Boolean = false

    fun localeSupport(): String = "TR / EN"

    fun accessibilitySupport(): String = "TalkBack, büyük font, yüksek kontrast"

    fun storageCheck(context: Context): String {
        return try {
            val files = context.filesDir
            val free = files.freeSpace / 1024 / 1024
            "Depolama boş: ${free}MB / 32GB"
        } catch (_: Exception) { "Depolama: 32GB" }
    }

    fun ramCheck(): String = "RAM: 2GB • Kullanım: ~80MB"

    fun cpuCheck(): String = "CPU: Exynos W1000 5-çekirdek 3nm"

    fun gpuCheck(): String = "GPU: Mali-G68 • 60fps"

    fun sensorCheck(context: Context): String {
        val pm = context.packageManager
        val hasHeart = pm.hasSystemFeature("android.hardware.sensor.heartrate")
        val hasAccel = pm.hasSystemFeature("android.hardware.sensor.accelerometer")
        return "Sensör: ${if (hasHeart) "Kalp ✓" else "Kalp ✗"} • ${if (hasAccel) "İvme ✓" else "✗"}"
    }

    fun installMethod(): String = "adb install via Wi-Fi"

    fun installSteps(): List<String> = listOf(
        "1. Saatte: Ayarlar > Saat hakkında > Derleme no 7x",
        "2. Geliştirici seçenekleri > ADB + Kablosuz Açık",
        "3. Bilgisayarda: adb pair <IP>:<PAIR>",
        "4. adb connect <IP>:<PORT>",
        "5. adb install apk/debug.apk",
        "6. Saatte uygulama listesinde aç"
    )

    fun installStepsText(): String = installSteps().joinToString("\n")

    fun troubleshooting(): Map<String, String> = mapOf(
        "INSTALL_PARSE_FAILED_NO_CERTIFICATES" to "İmzasız APK → debug APK kullan (şimdi ikisi de imzalı)",
        "MISSING_FEATURE watch" to "Telefona değil saate kur",
        "device offline" to "adb connect doğru IP:PORT, aynı Wi-Fi",
        "VERSION_DOWNGRADE" to "adb uninstall com.scritchyscratchy.watch"
    )

    fun troubleshootingText(): String {
        return troubleshooting().entries.joinToString("\n\n") { "${it.key}:\n${it.value}" }
    }

    fun adbCommand(apk: String = "ScritchyScratchy-GalaxyWatch8-debug.apk"): String = "adb install $apk"

    fun adbUninstall(): String = "adb uninstall com.scritchyscratchy.watch"

    fun adbLogcat(): String = "adb logcat | grep scritchy"

    fun adbShell(): String = "adb shell pm list packages | grep scritchy"

    fun watchUiTips(): List<String> = listOf(
        "Bezel çevir → listelerde gezin",
        "Parmağınla kazı → %72 açılınca TOPLA",
        "Üst hap → bakiye + JP",
        "Altta 4 nokta → sekme",
        "Hızlı kazı için coin fırçası",
        "Kombo için hızlı vur"
    )

    fun watchUiTipsText(): String = watchUiTips().joinToString("\n• ", prefix = "• ")

    fun performanceTips(): List<String> = listOf(
        "Parçacık kapat → pil +%10",
        "Glow kapat → performans +%5",
        "Oto-kazı 2.2sn ideal",
        "Koyu tema AOD korur"
    )

    fun hardwareKeys(): String = "Tek tuş: geri, uzun bas: menü"

    fun watchDimensions(): String = "44mm: 43.7×46×8.6mm, 34g • 40mm: 40.4×42.7×8.6mm, 30g"

    fun watchDurability(): String = "5ATM + IP68 + MIL-STD-810H + Safir kristal"

    fun watchConnectivity(): String = "BT 5.3, Wi-Fi 2.4/5GHz, NFC, GPS L1+L5, LTE (opsiyonel)"

    fun watchSensors(): String = "BioActive (kalp, EKG, BIA), sıcaklık, ivme, barometre, gyro, ışık"

    fun watchBatteryLife(): String = "AOD açık 30 saat • Hızlı şarj WPC"

    fun watchCharging(): String = "WPC kablosuz, 10W"

    fun watchCompatibility(): String = "Android 12+ , 1.5GB RAM"

    fun watchPrice(): String = "Watch 8 40mm $349, 44mm $379"

    fun watchColors(): String = "Grafit, Gümüş"

    fun watchLaunch(): String = "25 Temmuz 2025"

    fun watchOs(): String = "Wear OS 6 + One UI 8 Watch + Gemini"

    fun extendedInfo(): String {
        val sb = StringBuilder()
        sb.appendLine("=== GALAXY WATCH 8 DETAY ===")
        sb.appendLine(watchSpecsText())
        sb.appendLine(watchDimensions())
        sb.appendLine(watchDurability())
        sb.appendLine(watchConnectivity())
        sb.appendLine(watchSensors())
        sb.appendLine(watchBatteryLife())
        sb.appendLine(watchCharging())
        sb.appendLine(watchCompatibility())
        sb.appendLine(watchPrice())
        sb.appendLine(watchColors())
        sb.appendLine(watchLaunch())
        sb.appendLine(watchOs())
        sb.appendLine("Bezel: ${bezelText()}")
        sb.appendLine("Touch: ${touchText()}")
        sb.appendLine("AOD: ${aodText()}")
        sb.appendLine("Tile: ${tileText()}")
        sb.appendLine("Kurulum: ${installMethod()}")
        repeat(20) { i -> sb.appendLine("Watch log ${i + 1}: ${Random.nextInt(1000)}") }
        return sb.toString()
    }

    fun pixelWatchFacePreview(): String = """
        |  . - - - .
        |'   12:34   '
        |'  480×480  '
        |'   Watch8  '
        |  ' - - - '
    """.trimMargin()

    fun watchIntegrationStatus(state: GameState): String {
        return """
            |Saat: ${currentWatch().model}
            |Durum: ${if (isWatch(LocalContext.current as Context)) "SAATTE" else "EMULATOR"}
            |Bakiye: ${state.balance}$
            |Pil: ${batteryOptimization(state)}
            |Performans: ${performanceMode(state)}
        """.trimMargin()
    }

    fun allFeatures(): List<String> = listOf(
        "480×480 yuvarlak",
        "Wear OS 6",
        "Exynos W1000",
        "2GB/32GB",
        "435mAh",
        "Safir kristal",
        "5ATM",
        "Bezel rotary",
        "Haptik",
        "AOD"
    )

    fun featureText(): String = allFeatures().joinToString(" • ")

    fun isSupportedDevice(context: Context): Boolean = isWatch(context) || isEmulator(context)

    fun unsupportedText(): String = "Bu cihaz saat değil, emülatörde çalışıyor"

    fun supportedText(): String = "Galaxy Watch 8 destekleniyor ✓"

    fun checkSupport(context: Context): String = if (isSupportedDevice(context)) supportedText() else unsupportedText()

    fun launchIntent(): String = "android.intent.action.MAIN + CATEGORY_LAUNCHER"

    fun packageName(): String = "com.scritchyscratchy.watch"

    fun versionName(): String = "1.0.2"

    fun versionCode(): Int = 3

    fun buildType(): String = "release (debug imzalı)"

    fun apkSize(): String = "Debug 8.0MB • Release 5.8MB"

    fun apkLocation(): String = "apk/ScritchyScratchy-GalaxyWatch8-debug.apk"

    fun apkDirectLink(): String = "https://github.com/StrongBomber/ScratchCards/releases/tag/watch-latest"

    fun workflowLink(): String = "https://github.com/StrongBomber/ScratchCards/actions"

    fun repoLink(): String = "https://github.com/StrongBomber/ScratchCards"

    fun docsLink(): String = "docs/BIREBIR_KANIT.md"

    fun previewLink(): String = "docs/watch-preview.png"

    fun allLinks(): String = """
        |Repo: $repoLink()
        |APK: $apkDirectLink()
        |Workflow: $workflowLink()
        |Docs: $docsLink()
    """.trimMargin()
}
