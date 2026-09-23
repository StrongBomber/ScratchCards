package com.scritchyscratchy.watch.pixel

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.wear.compose.material.Text
import com.scritchyscratchy.watch.CardGenerator
import com.scritchyscratchy.watch.ScratchCardInstance
import com.scritchyscratchy.watch.CardType
import kotlin.math.*
import kotlin.random.Random

// ============= ENHANCED SCRATCH ENGINE - PIXEL EDITION =============
// Baya geliştirilmiş kazıma: brush tipleri, basınç, hız, partikül, combo, titreşim, ses

enum class BrushType(val display: String, val baseSize: Float, val hardness: Float, val dust: Int) {
    FINGER("👆", 22f, 0.7f, 3),
    COIN("🪙", 18f, 1.0f, 6),
    ERASER("🧽", 28f, 0.5f, 2),
    CLAW("🐾", 14f, 0.9f, 8),
    LASER("🔦", 10f, 1.0f, 1)
}

data class ScratchStroke(
    val points: List<Offset>,
    val brush: BrushType,
    val pressure: Float,
    val velocity: Float,
    val timestamp: Long = System.currentTimeMillis()
) {
    val length: Float get() = points.zipWithNext().sumOf { (a, b) -> hypot(a.x - b.x, a.y - b.y).toDouble() }.toFloat()
    val avgVelocity: Float get() = if (points.size < 2) 0f else length / points.size
}

data class ScratchMetrics(
    val coverage: Float, // 0..1
    val totalLength: Float,
    val strokeCount: Int,
    val avgPressure: Float,
    val maxVelocity: Float,
    val combo: Int, // ardışık hızlı stroke
    val efficiency: Float // coverage / strokes
)

class EnhancedScratchState(
    val card: ScratchCardInstance,
    var brush: BrushType = BrushType.COIN
) {
    var strokes = mutableListOf<ScratchStroke>()
    var currentPoints = mutableListOf<Offset>()
    var currentPressure = 1f
    var lastVelocity = 0f
    var combo = 0
    var lastStrokeTime = 0L
    var dustParticles = mutableListOf<DustParticle>()

    fun addPoint(offset: Offset, pressure: Float = 1f, velocity: Float = 0f) {
        currentPoints.add(offset)
        currentPressure = (currentPressure * 0.85f + pressure * 0.15f)
        lastVelocity = velocity
        // spawn dust
        if (Random.nextFloat() < 0.3f * brush.dust / 6f) {
            dustParticles.add(
                DustParticle(
                    pos = offset + Offset(Random.nextFloat() * 6 - 3, Random.nextFloat() * 6 - 3),
                    vel = Offset(Random.nextFloat() * 4 - 2, Random.nextFloat() * -3 - 1),
                    life = 1f,
                    size = Random.nextFloat() * 2 + 1f
                )
            )
        }
        if (dustParticles.size > 60) dustParticles = dustParticles.takeLast(40).toMutableList()
    }

    fun endStroke() {
        if (currentPoints.size < 2) {
            currentPoints.clear()
            return
        }
        val now = System.currentTimeMillis()
        val dt = now - lastStrokeTime
        // combo: hızlı ardışık stroke (300ms içinde)
        if (dt < 300 && currentPoints.size > 8) combo++ else if (dt > 500) combo = 0
        lastStrokeTime = now
        val vel = if (currentPoints.size > 1) {
            val d = hypot(
                currentPoints.last().x - currentPoints.first().x,
                currentPoints.last().y - currentPoints.first().y
            )
            d / max(1f, dt.toFloat() / 16f)
        } else 0f
        strokes.add(
            ScratchStroke(
                points = currentPoints.toList(),
                brush = brush,
                pressure = currentPressure,
                velocity = vel
            )
        )
        currentPoints.clear()
        currentPressure = 1f
    }

    fun totalPoints(): Int = strokes.sumOf { it.points.size } + currentPoints.size

    fun metrics(canvasWidth: Float, canvasHeight: Float): ScratchMetrics {
        val total = totalPoints()
        val area = canvasWidth * canvasHeight
        // coverage approx: her point ~ brush area
        val brushArea = strokes.sumOf { it.points.size * (it.brush.baseSize * it.brush.baseSize * PI).toDouble() } +
                currentPoints.size * (brush.baseSize * brush.baseSize * PI)
        val cov = (brushArea / area).coerceIn(0.0, 1.0).toFloat().coerceIn(0f, 1f)
        // daha doğru: stroke sayısına göre normalize
        val normalized = (total / 180f).coerceIn(0f, 1f) * 0.9f + cov * 0.1f
        val covFinal = normalized.coerceIn(0f, 1f)
        val totalLen = strokes.sumOf { it.length.toDouble() }.toFloat()
        val avgPress = if (strokes.isEmpty()) 1f else strokes.map { it.pressure }.average().toFloat()
        val maxVel = strokes.maxOfOrNull { it.velocity } ?: 0f
        val eff = if (strokes.isEmpty()) 0f else covFinal / strokes.size.coerceAtLeast(1)
        return ScratchMetrics(covFinal, totalLen, strokes.size, avgPress, maxVel, combo, eff)
    }

    fun reset() {
        strokes.clear()
        currentPoints.clear()
        combo = 0
        dustParticles.clear()
    }
}

data class DustParticle(
    var pos: Offset,
    var vel: Offset,
    var life: Float,
    var size: Float
)

// ============= ENHANCED SCRATCH CANVAS PIXEL =============
@Composable
fun EnhancedPixelScratchCanvas(
    card: ScratchCardInstance,
    scratchPower: Float,
    areaSize: Float,
    brush: BrushType = BrushType.COIN,
    onProgress: (Float, ScratchMetrics) -> Unit,
    onReveal: (ScratchMetrics) -> Unit,
    modifier: Modifier = Modifier
) {
    var state by remember(card.uid) { mutableStateOf(EnhancedScratchState(card, brush)) }
    var revealed by remember(card.uid) { mutableStateOf(false) }
    var metrics by remember { mutableStateOf(ScratchMetrics(0f, 0f, 0, 1f, 0f, 0, 0f)) }
    var lastPos by remember { mutableStateOf(Offset.Zero) }
    var lastTime by remember { mutableStateOf(0L) }

    // dışarıdan brush değişirse
    LaunchedEffect(brush) { state.brush = brush }

    // dust anim loop
    LaunchedEffect(state) {
        while (true) {
            kotlinx.coroutines.delay(16)
            val list = state.dustParticles
            var changed = false
            val iter = list.listIterator()
            while (iter.hasNext()) {
                val p = iter.next()
                p.pos += p.vel
                p.vel += Offset(0f, 0.35f) // gravity
                p.life -= 0.035f
                if (p.life <= 0) {
                    iter.remove()
                    changed = true
                } else changed = true
            }
            if (changed) {
                // trigger recompose via copy? state is object, need to trigger
                // we use a dummy state
            }
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFF5F5F5))
            .shadow(4.dp, RoundedCornerShape(14.dp))
    ) {
        // Alt: kart sembolleri
        PixelCardSymbols(card = card, coverage = metrics.coverage)

        // Scratch overlay
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                .pointerInput(card.uid, scratchPower, areaSize, brush) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            lastPos = offset
                            lastTime = System.currentTimeMillis()
                            state.addPoint(offset, pressure = 1f, velocity = 0f)
                        },
                        onDrag = { change, _ ->
                            val now = System.currentTimeMillis()
                            val dt = (now - lastTime).coerceAtLeast(1)
                            val dist = hypot(change.position.x - lastPos.x, change.position.y - lastPos.y)
                            val vel = dist / dt * 16f
                            // basınç simülasyonu: hızlı = az basınç, yavaş = çok
                            val pressure = (1.2f - (vel / 20f).coerceIn(0f, 0.6f)).coerceIn(0.6f, 1f)
                            // areaSize ve scratchPower ile brush büyütme
                            val extra = (areaSize * 3).toInt()
                            repeat(1 + extra) {
                                val jitter = Offset(
                                    Random.nextFloat() * 4 - 2,
                                    Random.nextFloat() * 4 - 2
                                )
                                state.addPoint(change.position + jitter, pressure, vel)
                            }
                            lastPos = change.position
                            lastTime = now
                            // metrics hesapla
                            val m = state.metrics(size.width.toFloat(), size.height.toFloat())
                            metrics = m
                            onProgress(m.coverage, m)
                            if (m.coverage >= 0.72f && !revealed) {
                                revealed = true
                                onReveal(m)
                            }
                        },
                        onDragEnd = {
                            state.endStroke()
                            val m = state.metrics(size.width.toFloat(), size.height.toFloat())
                            metrics = m
                            onProgress(m.coverage, m)
                            if (m.coverage >= 0.72f && !revealed) {
                                revealed = true
                                onReveal(m)
                            }
                        },
                        onDragCancel = {
                            state.endStroke()
                        }
                    )
                }
        ) {
            val w = size.width
            val h = size.height
            val foil = Color(0xFF9E9E9E)
            val strokeW = (state.brush.baseSize * areaSize * scratchPower * 0.9f)

            // Foil base
            drawRect(foil, size = size)

            // Pixel dither foil texture - 2px checker
            val ditherColor = Color(0xFF8D8D8D)
            var y = 0f
            val d = 4.dp.toPx()
            var row = 0
            while (y < h) {
                var x = if (row % 2 == 0) 0f else d / 2
                while (x < w) {
                    drawRect(ditherColor.copy(alpha = 0.08f), topLeft = Offset(x, y), size = Size(d, d))
                    x += d * 2
                }
                y += d
                row++
            }

            // Diagonal foil lines
            val lineC = Color(0xFFB0BEC5).copy(alpha = 0.35f)
            for (i in -w.toInt()..(w + h).toInt() step 10) {
                drawLine(lineC, Offset(i.toFloat(), 0f), Offset(i + 16f, 16f), strokeWidth = 0.8f)
            }

            // Shine top
            drawRoundRect(
                Color.White.copy(alpha = 0.18f),
                topLeft = Offset(w * 0.07f, h * 0.10f),
                size = Size(w * 0.86f, h * 0.16f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
            )
            // edge highlight
            drawRoundRect(
                Color.White.copy(alpha = 0.12f),
                topLeft = Offset(w * 0.04f, h * 0.04f),
                size = Size(w * 0.92f, h * 0.92f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f),
                style = Stroke(width = 1.dp.toPx())
            )

            // Erase strokes
            val all = state.strokes.map { it.points } + listOf(state.currentPoints).filter { it.isNotEmpty() }
            for (pts in all) {
                if (pts.size < 2) continue
                val p = Path()
                p.moveTo(pts[0].x, pts[0].y)
                for (i in 1 until pts.size) {
                    val a = pts[i - 1]
                    val b = pts[i]
                    val mid = Offset((a.x + b.x) / 2, (a.y + b.y) / 2)
                    p.quadraticBezierTo(a.x, a.y, mid.x, mid.y)
                }
                // find brush for this stroke
                val br = state.strokes.find { it.points === pts }?.brush ?: brush
                val sw = (br.baseSize * areaSize * scratchPower * 0.9f)
                // main stroke
                drawPath(
                    p,
                    Color.Transparent,
                    style = Stroke(width = sw, cap = StrokeCap.Round, join = StrokeJoin.Round),
                    blendMode = BlendMode.Clear
                )
                // hard center for coin
                if (br.hardness > 0.8f) {
                    drawPath(
                        p,
                        Color.Transparent,
                        style = Stroke(width = sw * 0.55f, cap = StrokeCap.Round, join = StrokeJoin.Round),
                        blendMode = BlendMode.Clear
                    )
                }
                // dots for gaps
                for (pt in pts) {
                    drawCircle(Color.Transparent, radius = sw / 2, center = pt, blendMode = BlendMode.Clear)
                }
            }

            // Combo highlight stroke
            if (state.combo > 1) {
                drawRoundRect(
                    Color(0xFFFFD600).copy(alpha = (0.08f * state.combo).coerceAtMost(0.3f)),
                    size = size,
                    style = Stroke(width = (1.5f * state.combo).dp.toPx()),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f)
                )
            }
        }

        // Dust particles
        Canvas(Modifier.fillMaxSize()) {
            for (p in state.dustParticles) {
                val alpha = p.life.coerceIn(0f, 1f)
                drawCircle(
                    Color(0xFFB0BEC5).copy(alpha = alpha * 0.7f),
                    radius = p.size,
                    center = p.pos
                )
            }
        }

        // Top hint
        if (metrics.coverage < 0.22f) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Row(
                    Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.55f))
                        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    androidx.wear.compose.material.Text(
                        brush.display,
                        fontSize = 9.sp,
                        color = Color.White
                    )
                    androidx.wear.compose.material.Text(
                        "KAZI! ${brush.name}",
                        color = Color.White,
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = PixelTypography.LabelPixel.fontFamily
                    )
                }
            }
        }

        // Coverage bar top
        Box(
            Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(3.dp)
                .background(Color.Black.copy(alpha = 0.25f))
        ) {
            Box(
                Modifier
                    .fillMaxWidth(metrics.coverage)
                    .fillMaxHeight()
                    .background(
                        when {
                            metrics.coverage >= 0.72f -> PixelPalette.Emerald
                            metrics.coverage >= 0.5f -> PixelPalette.Gold
                            else -> PixelPalette.Sapphire
                        }
                    )
            )
        }

        // Combo indicator
        if (state.combo > 1) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(PixelPalette.Topaz.copy(alpha = 0.9f))
                    .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    "COMBO x${state.combo}!",
                    color = Color.White,
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = PixelTypography.LabelPixel.fontFamily
                )
            }
        }

        // Reveal result
        if (metrics.coverage >= 0.72f) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.62f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    // pixel border
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                when {
                                    card.isPenalty -> PixelPalette.Ruby.copy(alpha = 0.95f)
                                    card.isJackpot -> PixelPalette.Gold.copy(alpha = 0.95f)
                                    card.payout > 0 -> PixelPalette.Emerald.copy(alpha = 0.95f)
                                    else -> PixelPalette.BgCardLight.copy(alpha = 0.95f)
                                }
                            )
                            .border(2.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                when {
                                    card.isPenalty -> "💀 CEZA ${card.payout} $"
                                    card.isJackpot -> "🎉 JACKPOT ${card.payout} $"
                                    card.payout > 0 -> "✦ +${card.payout} $"
                                    else -> "✖ KAYIP"
                                },
                                color = when {
                                    card.isPenalty -> Color.White
                                    card.isJackpot -> Color.Black
                                    card.payout > 0 -> Color.Black
                                    else -> Color.White
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = PixelTypography.TitlePixel.fontFamily,
                                textAlign = TextAlign.Center
                            )
                            if (card.isJackpot) Text(
                                "+5 JP  •  COMBO x${state.combo}",
                                color = Color.Black.copy(alpha = 0.7f),
                                fontSize = 6.sp,
                                fontWeight = FontWeight.Bold
                            )
                            // efficiency
                            Text(
                                "Verim ${(metrics.efficiency * 100).toInt()}%  •  ${metrics.strokeCount} vuruş",
                                color = Color.Black.copy(alpha = 0.6f),
                                fontSize = 5.sp,
                                fontFamily = PixelTypography.CaptionPixel.fontFamily
                            )
                        }
                    }
                    // brush reward
                    if (metrics.combo > 2) {
                        Text(
                            "🔥 HIZ BONUSU +${metrics.combo * 2}%",
                            color = PixelPalette.Gold,
                            fontSize = 6.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}

// ============= PIXEL CARD SYMBOLS (ENHANCED) =============
@Composable
private fun PixelCardSymbols(card: ScratchCardInstance, coverage: Float) {
    val bg = when (card.definition.type) {
        CardType.QUICK_CASH -> Color(0xFF1B5E20)
        CardType.SNAKE_EYES -> Color(0xFFB71C1C)
        CardType.APPLE_TREE -> Color(0xFF33691E)
        CardType.LUCKY_CAT -> Color(0xFFFF8F00)
        CardType.SCRATCH_MY_BACK -> Color(0xFF006064)
        CardType.MEGA_JACKPOT -> Color(0xFF4A148C)
        CardType.FINAL_CHANCE -> Color(0xFF212121)
    }
    Column(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(bg, bg.darken(0.3f)))
            )
            .padding(8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // header pixel
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(Modifier.size(6.dp).background(PixelPalette.Gold).clip(RoundedCornerShape(1.dp)))
            Text(card.definition.icon, fontSize = 10.sp)
            Text(
                card.definition.nameTr.uppercase(),
                fontSize = 7.sp,
                fontWeight = FontWeight.Black,
                fontFamily = PixelTypography.LabelPixel.fontFamily,
                color = Color.White,
                letterSpacing = 0.5.sp
            )
            Box(Modifier.size(6.dp).background(PixelPalette.Gold).clip(RoundedCornerShape(1.dp)))
        }
        Text(
            "LV${card.level} • ${card.definition.cost}$ → ${card.definition.basePayout}$",
            fontSize = 6.sp,
            fontFamily = PixelTypography.CaptionPixel.fontFamily,
            color = Color.White.copy(alpha = 0.7f)
        )
        Spacer(Modifier.height(4.dp))
        // Foil pattern under symbols
        Box(
            Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.08f))
                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                .padding(6.dp)
        ) {
            val cols = when (card.symbols.size) {
                1 -> 1; 3 -> 3; 5, 6 -> 3; 9 -> 3; else -> 3
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                card.symbols.chunked(cols).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        row.forEach { sym ->
                            Box(
                                Modifier
                                    .size(22.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.White)
                                    .border(1.dp, PixelPalette.Gray300, RoundedCornerShape(4.dp))
                                    .padding(1.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(sym.display, fontSize = 11.sp, textAlign = TextAlign.Center)
                                // pixel shine
                                Box(
                                    Modifier
                                        .align(Alignment.TopStart)
                                        .size(6.dp)
                                        .background(Color.White.copy(alpha = 0.6f), RoundedCornerShape(1.dp))
                                )
                            }
                        }
                    }
                }
            }
        }
        if (coverage >= 0.35f && card.symbols.size <= 3) {
            Spacer(Modifier.height(3.dp))
            Text(
                if (card.isPenalty) "⚠ TEHLİKE" else if (card.isJackpot) "★ JACKPOT ŞANSI" else "",
                fontSize = 6.sp,
                fontWeight = FontWeight.Black,
                fontFamily = PixelTypography.LabelPixel.fontFamily,
                color = if (card.isPenalty) PixelPalette.Ruby else PixelPalette.Gold
            )
        }
    }
}
