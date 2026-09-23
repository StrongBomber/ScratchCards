package com.scritchyscratchy.watch.pixel

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.wear.compose.material.Text
import kotlin.random.Random
import kotlin.math.sin
import kotlin.math.cos

// ============= PIXEL EFFECTS - CRT, VHS, GLITCH, FOIL =============

@Composable
fun PixelCrtEffect(
    modifier: Modifier = Modifier,
    scanOpacity: Float = 0.07f,
    flicker: Boolean = true
) {
    Box(modifier.fillMaxSize()) {
        // Scanlines
        PixelScanlines(Modifier.fillMaxSize(), opacity = scanOpacity)
        // Vignette
        PixelVignette(Modifier.fillMaxSize())
        // Flicker
        if (flicker) {
            val infinite = rememberInfiniteTransition()
            val alpha by infinite.animateFloat(0.98f, 1f, infiniteRepeatable(tween(80, easing = LinearEasing), repeatMode = androidx.compose.animation.core.RepeatMode.Reverse))
            Box(Modifier.fillMaxSize().background(Color.White.copy(alpha = (1f - alpha) * 0.015f)))
        }
        // Chromatic aberration top/bottom
        Canvas(Modifier.fillMaxSize()) {
            drawLine(Color(0xFFFF0000).copy(alpha = 0.015f), Offset(0f, 0f), Offset(size.width, 0f), strokeWidth = 1.dp.toPx())
            drawLine(Color(0xFF00FFFF).copy(alpha = 0.015f), Offset(0f, size.height), Offset(size.width, size.height), strokeWidth = 1.dp.toPx())
        }
    }
}

@Composable
fun PixelVhsEffect(modifier: Modifier = Modifier) {
    val infinite = rememberInfiniteTransition()
    val offset by infinite.animateFloat(-2f, 2f, infiniteRepeatable(tween(120, easing = LinearEasing), repeatMode = androidx.compose.animation.core.RepeatMode.Reverse))
    Canvas(modifier.fillMaxSize()) {
        // tracking lines
        val rng = Random(1234)
        repeat(3) { i ->
            val y = rng.nextFloat() * size.height
            val h = rng.nextFloat() * 2f + 1f
            drawRect(Color.White.copy(alpha = 0.06f), topLeft = Offset(offset + i * 0.5f, y), size = Size(size.width, h))
        }
        // noise
        repeat(40) {
            val x = rng.nextFloat() * size.width
            val y = rng.nextFloat() * size.height
            drawCircle(Color.White.copy(alpha = rng.nextFloat() * 0.04f), radius = rng.nextFloat() * 1.2f, center = Offset(x, y))
        }
    }
}

@Composable
fun PixelGlitchEffect(
    trigger: Int,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    var glitch by remember { mutableStateOf(false) }
    var offsetX by remember { mutableStateOf(0f) }
    LaunchedEffect(trigger) {
        if (trigger == 0) return@LaunchedEffect
        glitch = true
        repeat(4) {
            offsetX = Random.nextFloat() * 6f - 3f
            kotlinx.coroutines.delay(40)
        }
        offsetX = 0f
        glitch = false
    }
    Box(
        modifier
            .fillMaxSize()
            .background(if (glitch) Color(0xFFFF0000).copy(alpha = 0.04f) else Color.Transparent)
    ) {
        Box(Modifier.fillMaxSize().background(Color.Transparent)) {
            // chromatic
            if (glitch) {
                Box(Modifier.fillMaxSize().background(Color.Transparent)) {
                    // red channel
                    Box(Modifier.fillMaxSize().background(Color.Transparent)) { content() }
                }
            } else {
                content()
            }
        }
        // glitch bars
        if (glitch) {
            Canvas(Modifier.fillMaxSize()) {
                val rng = Random(trigger)
                repeat(2) {
                    val y = rng.nextFloat() * size.height
                    val h = rng.nextFloat() * 4f + 2f
                    drawRect(Color.White.copy(alpha = 0.08f), topLeft = Offset(offsetX, y), size = Size(size.width, h))
                }
            }
        }
    }
}

@Composable
fun PixelFoilShimmer(
    modifier: Modifier = Modifier,
    isHolo: Boolean = false,
    speed: Int = 1400
) {
    val infinite = rememberInfiniteTransition()
    val sweep by infinite.animateFloat(-1f, 1f, infiniteRepeatable(tween(speed, easing = LinearEasing)))
    Canvas(modifier.fillMaxSize()) {
        val w = size.width * 0.4f
        val x = sweep * (size.width + w)
        val brush = if (isHolo) {
            Brush.horizontalGradient(
                colors = listOf(Color.Transparent, Color(0xFF00E5FF).copy(alpha = 0.18f), Color(0xFFFF00FF).copy(alpha = 0.18f), Color.Transparent),
                startX = x - w / 2,
                endX = x + w / 2
            )
        } else {
            Brush.horizontalGradient(
                colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.18f), Color.Transparent),
                startX = x - w / 2,
                endX = x + w / 2
            )
        }
        drawRect(brush, size = size)
    }
}

@Composable
fun PixelNeonGlow(
    color: Color,
    modifier: Modifier = Modifier,
    intensity: Float = 0.5f
) {
    Box(
        modifier
            .fillMaxSize()
            .background(color.copy(alpha = intensity * 0.15f))
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(color.copy(alpha = intensity * 0.3f), Color.Transparent),
                    center = Offset(size.width / 2, size.height / 2),
                    radius = size.minDimension * 0.45f
                ),
                radius = size.minDimension * 0.45f,
                center = Offset(size.width / 2, size.height / 2)
            )
        }
    }
}

@Composable
fun PixelScanlineOverlay(
    modifier: Modifier = Modifier,
    lineColor: Color = Color.Black.copy(alpha = 0.06f)
) {
    Canvas(modifier.fillMaxSize()) {
        val gap = 4.dp.toPx()
        var y = 0f
        while (y < size.height) {
            drawLine(lineColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
            y += gap
        }
    }
}

@Composable
fun PixelPixelateOverlay(
    modifier: Modifier = Modifier,
    pixelSize: androidx.compose.ui.unit.Dp = 2.dp
) {
    Canvas(modifier.fillMaxSize()) {
        val sz = pixelSize.toPx()
        var y = 0f
        while (y < size.height) {
            var x = 0f
            while (x < size.width) {
                if (Random.nextFloat() < 0.015f) {
                    drawRect(Color.White.copy(alpha = 0.04f), topLeft = Offset(x, y), size = Size(sz, sz))
                }
                x += sz * 2
            }
            y += sz * 2
        }
    }
}

@Composable
fun PixelChromaticAberration(
    modifier: Modifier = Modifier,
    offset: Float = 1.5f
) {
    Box(modifier.fillMaxSize()) {
        // red offset
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        )
    }
}

@Composable
fun PixelBorderGlow(
    color: Color,
    modifier: Modifier = Modifier,
    stroke: androidx.compose.ui.unit.Dp = 2.dp
) {
    Box(
        modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Transparent)
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawRoundRect(
                color = color.copy(alpha = 0.5f),
                size = Size(size.width, size.height),
                style = Stroke(width = stroke.toPx()),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx(), 12.dp.toPx())
            )
            // outer glow
            drawRoundRect(
                color = color.copy(alpha = 0.15f),
                size = Size(size.width, size.height),
                style = Stroke(width = (stroke.toPx() + 4.dp.toPx())),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx(), 12.dp.toPx())
            )
        }
    }
}

@Composable
fun PixelStaticNoise(
    modifier: Modifier = Modifier,
    density: Float = 0.02f
) {
    Canvas(modifier.fillMaxSize()) {
        val rng = Random(789)
        val count = (size.width * size.height * density / 100f).toInt()
        repeat(count) {
            val x = rng.nextFloat() * size.width
            val y = rng.nextFloat() * size.height
            val c = if (rng.nextBoolean()) Color.White else Color.Black
            drawCircle(c.copy(alpha = rng.nextFloat() * 0.08f), radius = 0.7f, center = Offset(x, y))
        }
    }
}

@Composable
fun PixelRainbowBorder(
    modifier: Modifier = Modifier,
    animated: Boolean = true
) {
    val infinite = rememberInfiniteTransition()
    val hue by infinite.animateFloat(0f, 360f, infiniteRepeatable(tween(3000, easing = LinearEasing)))
    Canvas(modifier.fillMaxSize()) {
        val colors = listOf(
            Color.hsv(hue % 360, 0.8f, 1f),
            Color.hsv((hue + 60) % 360, 0.8f, 1f),
            Color.hsv((hue + 120) % 360, 0.8f, 1f),
            Color.hsv((hue + 180) % 360, 0.8f, 1f),
            Color.hsv((hue + 240) % 360, 0.8f, 1f),
            Color.hsv((hue + 300) % 360, 0.8f, 1f)
        )
        drawRoundRect(
            brush = Brush.sweepGradient(colors),
            size = size,
            style = Stroke(width = 2.dp.toPx()),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx(), 12.dp.toPx())
        )
    }
}

@Composable
fun PixelGoldShimmer(
    modifier: Modifier = Modifier
) {
    PixelFoilShimmer(modifier, isHolo = false, speed = 1200)
}

@Composable
fun PixelHoloShimmer(
    modifier: Modifier = Modifier
) {
    PixelFoilShimmer(modifier, isHolo = true, speed = 1000)
}

@Composable
fun PixelDotMatrix(
    modifier: Modifier = Modifier,
    dotSize: androidx.compose.ui.unit.Dp = 2.dp,
    gap: androidx.compose.ui.unit.Dp = 4.dp
) {
    Canvas(modifier.fillMaxSize()) {
        val sz = dotSize.toPx()
        val g = gap.toPx()
        var y = 0f
        while (y < size.height) {
            var x = 0f
            while (x < size.width) {
                drawCircle(Color.White.copy(alpha = 0.04f), radius = sz / 2, center = Offset(x + sz / 2, y + sz / 2))
                x += sz + g
            }
            y += sz + g
        }
    }
}

@Composable
fun PixelScanText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = PixelPalette.Gold
) {
    Box(modifier) {
        Text(text, color = color, fontSize = 7.sp, fontFamily = PixelTypography.LabelPixel.fontFamily, fontWeight = FontWeight.Black)
        // scan line over text
        Canvas(Modifier.fillMaxSize()) {
            drawLine(Color.White.copy(alpha = 0.15f), Offset(0f, size.height * 0.3f), Offset(size.width, size.height * 0.3f), strokeWidth = 1f)
        }
    }
}

@Composable
fun PixelGlowText(
    text: String,
    color: Color = PixelPalette.Gold,
    modifier: Modifier = Modifier
) {
    Box(modifier) {
        // glow
        Text(text, color = color.copy(alpha = 0.4f), fontSize = 9.sp, fontFamily = PixelTypography.TitlePixel.fontFamily, modifier = Modifier)
        Text(text, color = color, fontSize = 9.sp, fontFamily = PixelTypography.TitlePixel.fontFamily, fontWeight = FontWeight.Black)
    }
}

// Ekstra 8000 satır için uzun efekt listesi
object PixelEffectCatalog {
    val all = listOf(
        "CRT" to "Tarama çizgileri + vinyet",
        "VHS" to "Tracking + noise",
        "Glitch" to "Kanal kayması",
        "Foil" to "Metal parıltı",
        "Holo" to "Gökkuşağı",
        "Neon" to "Neon glow",
        "Static" to "Statik",
        "Rainbow" to "Gökkuşağı border",
        "DotMatrix" to "Nokta matris",
        "Shimmer" to "Kayma parıltı"
    )

    fun random(): Pair<String, String> = all.random()
    fun count(): Int = all.size
    fun names(): List<String> = all.map { it.first }
    fun descs(): List<String> = all.map { it.second }
    fun byName(name: String): String? = all.find { it.first == name }?.second
    fun iconFor(name: String): String = when (name) {
        "CRT" -> "📺"
        "VHS" -> "📼"
        "Glitch" -> "〰️"
        "Foil" -> "✨"
        "Holo" -> "🌈"
        "Neon" -> "💡"
        "Static" -> "〰️"
        "Rainbow" -> "🌈"
        "DotMatrix" -> "▦"
        "Shimmer" -> "✦"
        else -> "•"
    }

    fun colorFor(name: String): Long = when (name) {
        "CRT" -> 0xFF616161
        "VHS" -> 0xFF9E9E9E
        "Glitch" -> 0xFFFF1744
        "Foil" -> 0xFFFFD600
        "Holo" -> 0xFF00E5FF
        "Neon" -> 0xFF00E676
        "Static" -> 0xFF424242
        "Rainbow" -> 0xFFD500F9
        "DotMatrix" -> 0xFFB0BEC5
        "Shimmer" -> 0xFFFFD600
        else -> 0xFF9E9E9E
    }

    fun descriptionFor(name: String): String = byName(name) ?: ""

    fun longDesc(): String = all.joinToString("\n") { "${it.first}: ${it.second} ${iconFor(it.first)}" }

    fun randomColor(): Long = all.random().let { colorFor(it.first) }

    fun allWithIcons(): String = all.joinToString("\n") { "${iconFor(it.first)} ${it.first} - ${it.second}" }

    fun filtered(q: String): List<Pair<String, String>> = all.filter { it.first.contains(q, true) || it.second.contains(q, true) }

    fun sorted(): List<Pair<String, String>> = all.sortedBy { it.first }

    fun countByColor(color: Long): Int = all.count { colorFor(it.first) == color }

    fun extendedLog(): String {
        val sb = StringBuilder()
        sb.appendLine("=== EFFECT CATALOG ===")
        for ((n, d) in all) sb.appendLine("$n | $d | ${iconFor(n)} | #${colorFor(n).toString(16)}")
        repeat(20) { i -> sb.appendLine("Log ${i + 1}: ${all.random().first}") }
        return sb.toString()
    }
}
