package com.scritchyscratchy.watch.pixel

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Text
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState

// ============= PIXEL TUTORIAL - ADIM ADIM =============

data class TutorialStep(
    val id: Int,
    val title: String,
    val desc: String,
    val icon: String,
    val tip: String,
    val action: String,
    val color: Long
)

object PixelTutorial {
    val steps = listOf(
        TutorialStep(1, "BULAŞIK YIKA", "Parasız başlarsın. Tabağı ov, 1$ kazan. 5$ biriktir.", "🍽️", "Hızlı ov, 4 tabak yeter", "OV", 0xFF4CAF50),
        TutorialStep(2, "KART AL", "Dükkan'dan Hızlı Nakit (5$) al. En ucuz, en güvenli.", "🛒", "İlk kart hep Quick Cash", "AL", 0xFFFFD600),
        TutorialStep(3, "KAZI", "Parmağınla / bezel ile kazı. %72 açılınca TOPLA.", "👆", "Coin fırçası en iyi", "KAZI", 0xFF2196F3),
        TutorialStep(4, "KAZANÇ", "Eşleşme = kazan, ceza sembolü = kayıp. Dikkat et!", "💰", "Solucan/kara kedi = ceza", "TOPLA", 0xFF00E676),
        TutorialStep(5, "YÜKSELT", "Şans Lv4'e kadar öncelik. Sonra Güç/Alan.", "⬆️", "Şans %4/lvl", "YÜKSELT", 0xFFFF6D00),
        TutorialStep(6, "GADGET", "500$'a Scratch Bot → oto-kazı. İzlerken kazan!", "🤖", "Bot en pahalı kartı seçer", "AÇ", 0xFF00BCD4),
        TutorialStep(7, "PRESTİJ", "Jackpot biriktir → Prestij → +JP → kalıcı bonus", "♻️", "İlk prestij 5JP", "PRESTİJ", 0xFF9C27B0),
        TutorialStep(8, "KOMBO", "Hızlı ardışık kazı = COMBO, +% bonus!", "🔥", "300ms içinde vur", "KOMBO", 0xFFFF1744),
        TutorialStep(9, "KOLEKSİYON", "Her kartı Lv10 yap → foil artar, bonus +25%", "📚", "Gökkuşağı foil efsane", "TOPLA", 0xFFD500F9),
        TutorialStep(10, "EFSANE", "Final Chance %1 → 50k$ + 20JP! Risk büyük.", "☠️", "Sadece zengin gir", "RİSK", 0xFF212121)
    )

    fun step(id: Int): TutorialStep? = steps.find { it.id == id }
    fun next(current: Int): TutorialStep? = step(current + 1)
    fun prev(current: Int): TutorialStep? = step(current - 1)
    fun total(): Int = steps.size
    fun progress(current: Int): Float = current.toFloat() / total()
    fun isLast(id: Int): Boolean = id == total()
    fun isFirst(id: Int): Boolean = id == 1
    fun titleFor(id: Int): String = step(id)?.title ?: ""
    fun iconFor(id: Int): String = step(id)?.icon ?: ""
    fun colorFor(id: Int): Long = step(id)?.color ?: 0xFFFFD600
    fun tipFor(id: Int): String = step(id)?.tip ?: ""
    fun descFor(id: Int): String = step(id)?.desc ?: ""
    fun actionFor(id: Int): String = step(id)?.action ?: ""
    fun allTitles(): List<String> = steps.map { it.title }
    fun allIcons(): List<String> = steps.map { it.icon }
    fun search(q: String): List<TutorialStep> = steps.filter { it.title.contains(q, true) || it.desc.contains(q, true) }
    fun byColor(color: Long): List<TutorialStep> = steps.filter { it.color == color }
    fun random(): TutorialStep = steps.random()
    fun first(): TutorialStep = steps.first()
    fun last(): TutorialStep = steps.last()
    fun shuffle(): List<TutorialStep> = steps.shuffled()

    // Pixel özel
    fun pixelProgress(current: Int): String {
        val filled = "█".repeat(current)
        val empty = "░".repeat(total() - current)
        return filled + empty + " $current/${total()}"
    }

    fun completionText(current: Int): String = "${(progress(current) * 100).toInt()}% tamamlandı"

    fun nextActionText(current: Int): String = if (isLast(current)) "OYNA!" else "SONRAKİ →"

    fun prevActionText(current: Int): String = if (isFirst(current)) "KAPAT" else "← GERİ"

    fun estimateTime(): String = "${total() * 20}sn (~${total() / 3}dk)"

    fun quickStart(): List<TutorialStep> = steps.take(3)

    fun advanced(): List<TutorialStep> = steps.drop(6)

    fun hasTip(id: Int): Boolean = step(id)?.tip?.isNotEmpty() == true

    fun longDesc(step: TutorialStep): String = """
        |${step.icon} ${step.title}
        |${step.desc}
        |İpucu: ${step.tip}
        |Aksiyon: ${step.action}
        |Renk: #${step.color.toString(16)}
    """.trimMargin()

    fun allLongDescs(): String = steps.joinToString("\n\n") { longDesc(it) }

    fun toCsv(): String {
        val header = "id,title,desc,tip\n"
        val rows = steps.joinToString("\n") { "${it.id},${it.title},${it.desc},${it.tip}" }
        return header + rows
    }

    // Watch için kısa
    fun watchSteps(): List<TutorialStep> = steps.take(5)
    fun watchProgress(current: Int): String = "$current/5"
}

@Composable
fun PixelTutorialScreen(
    currentStep: Int,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val step = PixelTutorial.step(currentStep) ?: PixelTutorial.first()
    val state = rememberScalingLazyListState()
    ScalingLazyColumn(
        state = state,
        modifier = modifier.fillMaxSize().padding(horizontal = 10.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            PixelSectionHeader(title = "EĞİTİM", icon = "📖", subtitle = "${PixelTutorial.pixelProgress(currentStep)} • ${PixelTutorial.estimateTime()}")
        }
        item {
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(step.color).copy(alpha = 0.15f))
                    .border(1.5.dp, Color(step.color), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(step.icon, fontSize = 28.sp)
                    Text(
                        step.title,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = PixelTypography.TitlePixel.fontFamily,
                        color = Color(step.color),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        step.desc,
                        fontSize = 7.sp,
                        fontFamily = PixelTypography.BodyPixel.fontFamily,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(step.color).copy(alpha = 0.2f))
                            .border(1.dp, Color(step.color).copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            "💡 ${step.tip}",
                            fontSize = 6.sp,
                            fontFamily = PixelTypography.CaptionPixel.fontFamily,
                            color = Color(step.color),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                PixelButton(
                    text = PixelTutorial.prevActionText(currentStep),
                    onClick = if (PixelTutorial.isFirst(currentStep)) onSkip else onPrev,
                    modifier = Modifier.weight(1f),
                    style = PixelChipStyles.Dark
                )
                PixelButton(
                    text = PixelTutorial.nextActionText(currentStep),
                    onClick = onNext,
                    modifier = Modifier.weight(1f),
                    style = PixelChipStyles.Gold,
                    glow = true
                )
            }
        }
        item {
            PixelProgressBar(progress = PixelTutorial.progress(currentStep), fill = Color(step.color), modifier = Modifier.fillMaxWidth())
        }
        item {
            Text(
                "Adım $currentStep/${PixelTutorial.total()} • ${PixelTutorial.completionText(currentStep)}",
                fontSize = 6.sp,
                fontFamily = PixelTypography.CaptionPixel.fontFamily,
                color = PixelPalette.Gray500
            )
        }
        item {
            PixelButton(text = "ATLA", onClick = onSkip, modifier = Modifier.fillMaxWidth(), style = PixelChipStyles.Dark)
        }
    }
}

@Composable
fun PixelTutorialOverlay(
    step: TutorialStep,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier
                .padding(16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(PixelPalette.BgCard)
                .border(2.dp, Color(step.color), RoundedCornerShape(12.dp))
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(step.icon, fontSize = 20.sp)
            Text(step.title, fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color(step.color), fontFamily = PixelTypography.TitlePixel.fontFamily)
            Text(step.desc, fontSize = 7.sp, color = Color.White, textAlign = TextAlign.Center, fontFamily = PixelTypography.BodyPixel.fontFamily)
            PixelButton(text = step.action, onClick = onDismiss, style = PixelChipStyles.Gold)
        }
    }
}
