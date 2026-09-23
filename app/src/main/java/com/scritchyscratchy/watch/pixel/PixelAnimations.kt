package com.scritchyscratchy.watch.pixel

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import kotlin.random.Random
import kotlin.math.sin
import kotlin.math.cos

// ============= PIXEL ANIMATIONS - RETRO 8-BIT =============

@Composable
fun PixelJackpotConfetti(
    modifier: Modifier = Modifier,
    count: Int = 30,
    colors: List<Color> = listOf(PixelPalette.Gold, PixelPalette.Emerald, PixelPalette.Ruby, PixelPalette.Sapphire, PixelPalette.Amethyst)
) {
    val infinite = rememberInfiniteTransition()
    val progress by infinite.animateFloat(0f, 1f, infiniteRepeatable(tween(1200, easing = LinearEasing)))

    Canvas(modifier.fillMaxSize()) {
        val rng = Random(123)
        repeat(count) { i ->
            val startX = rng.nextFloat() * size.width
            val startY = -20f - rng.nextFloat() * 100f
            val speed = rng.nextFloat() * 3f + 2f
            val sway = rng.nextFloat() * 4f - 2f
            val t = (progress + i * 0.05f) % 1f
            val y = startY + t * (size.height + 200f) * speed * 0.3f
            val x = startX + sin(t * 6f + i) * 10f + sway * t * 10f
            val rot = t * 720f + i * 30f
            val alpha = (1f - t * 0.8f).coerceIn(0f, 1f)
            val c = colors[i % colors.size].copy(alpha = alpha)
            // square confetti 4x4 pixel
            val sz = 4.dp.toPx()
            // we simulate rotation via offset
            drawRect(c, topLeft = Offset(x - sz / 2, y - sz / 2), size = androidx.compose.ui.geometry.Size(sz, sz))
            // small highlight
            drawRect(Color.White.copy(alpha = alpha * 0.5f), topLeft = Offset(x - sz / 2, y - sz / 2), size = androidx.compose.ui.geometry.Size(sz, 1.dp.toPx()))
        }
    }
}

@Composable
fun PixelScratchDustBurst(
    center: Offset,
    modifier: Modifier = Modifier,
    particleCount: Int = 12
) {
    var progress by remember { mutableStateOf(0f) }
    LaunchedEffect(center) {
        animate(0f, 1f, animationSpec = tween(600, easing = FastOutSlowInEasing)) { v, _ -> progress = v }
    }
    Canvas(modifier.fillMaxSize()) {
        val rng = Random(center.x.toInt() + center.y.toInt())
        repeat(particleCount) { i ->
            val angle = (i.toFloat() / particleCount) * 360f + rng.nextFloat() * 20f - 10f
            val dist = progress * (20f + rng.nextFloat() * 15f)
            val x = center.x + cos(Math.toRadians(angle.toDouble())).toFloat() * dist
            val y = center.y + sin(Math.toRadians(angle.toDouble())).toFloat() * dist - progress * 10f
            val alpha = (1f - progress).coerceIn(0f, 1f)
            val sz = (2.dp.toPx() + rng.nextFloat() * 1.5f) * (1f - progress * 0.5f)
            drawCircle(Color(0xFFB0BEC5).copy(alpha = alpha * 0.8f), radius = sz, center = Offset(x, y))
        }
    }
}

@Composable
fun PixelCoinRain(
    modifier: Modifier = Modifier,
    coins: Int = 20
) {
    val infinite = rememberInfiniteTransition()
    val drop by infinite.animateFloat(0f, 1f, infiniteRepeatable(tween(1500, easing = LinearEasing)))
    Canvas(modifier.fillMaxSize()) {
        val rng = Random(456)
        repeat(coins) { i ->
            val x = rng.nextFloat() * size.width
            val startY = -30f - rng.nextFloat() * 80f
            val speed = rng.nextFloat() * 2f + 1f
            val t = (drop * speed + i * 0.07f) % 1f
            val y = startY + t * (size.height + 120f)
            val sway = sin(t * 4f + i) * 5f
            val alpha = (1f - t * 0.3f).coerceIn(0f, 1f)
            val sz = 8.dp.toPx()
            // coin
            drawCircle(PixelPalette.Gold, radius = sz / 2, center = Offset(x + sway, y))
            drawCircle(PixelPalette.GoldDark, radius = sz / 2, center = Offset(x + sway, y), style = androidx.compose.ui.graphics.drawscope.Stroke(1.dp.toPx()))
            // shine
            drawCircle(Color.White.copy(alpha = alpha * 0.5f), radius = 2.dp.toPx(), center = Offset(x + sway - 2.dp.toPx(), y - 2.dp.toPx()))
        }
    }
}

@Composable
fun PixelShineSweep(
    modifier: Modifier = Modifier,
    color: Color = Color.White.copy(alpha = 0.15f)
) {
    val infinite = rememberInfiniteTransition()
    val offset by infinite.animateFloat(-1f, 1f, infiniteRepeatable(tween(1400, easing = LinearEasing)))
    Canvas(modifier.fillMaxSize()) {
        val w = size.width * 0.3f
        val x = (offset * (size.width + w) + size.width / 2)
        drawRect(
            brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                colors = listOf(Color.Transparent, color, Color.Transparent),
                startX = x - w / 2,
                endX = x + w / 2
            ),
            size = size
        )
    }
}

@Composable
fun PixelPulse(
    modifier: Modifier = Modifier,
    color: Color = PixelPalette.Gold,
    pulseCount: Int = 3
) {
    val infinite = rememberInfiniteTransition()
    val scale by infinite.animateFloat(1f, 1.15f, infiniteRepeatable(tween(600, easing = FastOutSlowInEasing), RepeatMode.Reverse))
    val alpha by infinite.animateFloat(0.6f, 0f, infiniteRepeatable(tween(600, easing = LinearEasing)))
    repeat(pulseCount) { i ->
        val delay = i * 200
        val s = scale - i * 0.05f
        val a = (alpha - i * 0.15f).coerceIn(0f, 1f)
        Box(
            modifier
                .fillMaxSize()
                .graphicsLayer(scaleX = s, scaleY = s, alpha = a)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Transparent)
                .border(2.dp, color.copy(alpha = a), RoundedCornerShape(12.dp))
        )
    }
}

@Composable
fun PixelTypewriterText(
    text: String,
    modifier: Modifier = Modifier,
    speedMs: Long = 40,
    onComplete: (() -> Unit)? = null
) {
    var visible by remember(text) { mutableStateOf(0) }
    LaunchedEffect(text) {
        visible = 0
        for (i in text.indices) {
            kotlinx.coroutines.delay(speedMs)
            visible = i + 1
        }
        onComplete?.invoke()
    }
    Text(
        text.take(visible),
        modifier = modifier,
        fontSize = 7.sp,
        fontFamily = PixelTypography.BodyPixel.fontFamily,
        color = Color.White,
        textAlign = TextAlign.Center
    )
    if (visible < text.length) {
        // cursor blink
        val infinite = rememberInfiniteTransition()
        val alpha by infinite.animateFloat(1f, 0f, infiniteRepeatable(tween(400, easing = LinearEasing), RepeatMode.Reverse))
        Text(
            "█",
            color = PixelPalette.Gold.copy(alpha = alpha),
            fontSize = 7.sp,
            modifier = Modifier.padding(start = 2.dp)
        )
    }
}

@Composable
fun PixelLevelUpBurst(
    level: Int,
    modifier: Modifier = Modifier
) {
    var scale by remember(level) { mutableStateOf(0.5f) }
    var alpha by remember(level) { mutableStateOf(0f) }
    LaunchedEffect(level) {
        animate(0.5f, 1.2f, tween(300, easing = OvershootInterpolator().toEasing())) { v, _ -> scale = v }
        animate(0f, 1f, tween(200)) { v, _ -> alpha = v }
        kotlinx.coroutines.delay(600)
        animate(1f, 0f, tween(300)) { v, _ -> alpha = v }
    }
    Box(
        modifier
            .graphicsLayer(scaleX = scale, scaleY = scale, alpha = alpha)
            .clip(RoundedCornerShape(10.dp))
            .background(PixelPalette.Gold)
            .border(2.dp, Color.White, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "LEVEL UP! $level",
            color = Color.Black,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            fontFamily = PixelTypography.TitlePixel.fontFamily
        )
    }
}

fun OvershootInterpolator(): android.view.animation.OvershootInterpolator = android.view.animation.OvershootInterpolator()

fun android.view.animation.Interpolator.toEasing(): Easing = Easing { f -> getInterpolation(f) }

@Composable
fun PixelShake(
    trigger: Int,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    var offset by remember { mutableStateOf(0f) }
    LaunchedEffect(trigger) {
        if (trigger == 0) return@LaunchedEffect
        repeat(6) { i ->
            animate(0f, if (i % 2 == 0) 6f else -6f, tween(40)) { v, _ -> offset = v }
        }
        animate(offset, 0f, tween(80)) { v, _ -> offset = v }
    }
    Box(modifier.graphicsLayer(translationX = offset), content = content)
}

@Composable
fun PixelCountUp(
    target: Long,
    modifier: Modifier = Modifier,
    duration: Int = 600,
    prefix: String = "",
    suffix: String = " $"
) {
    var current by remember(target) { mutableStateOf(0L) }
    LaunchedEffect(target) {
        animate(0f, 1f, tween(duration, easing = FastOutSlowInEasing)) { v, _ ->
            current = (target * v).toLong()
        }
        current = target
    }
    Text(
        "$prefix$current$suffix",
        modifier = modifier,
        fontSize = 12.sp,
        fontWeight = FontWeight.Black,
        fontFamily = PixelTypography.MonoGold.fontFamily,
        color = PixelPalette.Gold
    )
}

@Composable
fun PixelScanningLine(modifier: Modifier = Modifier) {
    val infinite = rememberInfiniteTransition()
    val y by infinite.animateFloat(0f, 1f, infiniteRepeatable(tween(2000, easing = LinearEasing)))
    Canvas(modifier.fillMaxSize()) {
        val ly = y * size.height
        drawLine(Color(0xFF00E5FF).copy(alpha = 0.5f), Offset(0f, ly), Offset(size.width, ly), strokeWidth = 1.5f)
        drawRect(
            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                colors = listOf(Color.Transparent, Color(0xFF00E5FF).copy(alpha = 0.1f), Color.Transparent),
                startY = ly - 12.dp.toPx(),
                endY = ly + 12.dp.toPx()
            ),
            topLeft = Offset(0f, ly - 12.dp.toPx()),
            size = androidx.compose.ui.geometry.Size(size.width, 24.dp.toPx())
        )
    }
}

@Composable
fun PixelGlitchText(
    text: String,
    modifier: Modifier = Modifier,
    glitch: Boolean = false
) {
    val infinite = rememberInfiniteTransition()
    val offset by infinite.animateFloat(-1f, 1f, infiniteRepeatable(tween(120, easing = LinearEasing), RepeatMode.Reverse))
    Box(modifier) {
        if (glitch) {
            Text(text, color = PixelPalette.Ruby.copy(alpha = 0.7f), fontSize = 9.sp, fontFamily = PixelTypography.TitlePixel.fontFamily, modifier = Modifier.graphicsLayer(translationX = offset * 1.5f))
            Text(text, color = PixelPalette.Sapphire.copy(alpha = 0.7f), fontSize = 9.sp, fontFamily = PixelTypography.TitlePixel.fontFamily, modifier = Modifier.graphicsLayer(translationX = -offset * 1.5f))
        }
        Text(text, color = Color.White, fontSize = 9.sp, fontFamily = PixelTypography.TitlePixel.fontFamily, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun PixelProgressDotsAnimated(
    progress: Float,
    modifier: Modifier = Modifier,
    dotCount: Int = 10
) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        val filled = (progress * dotCount).toInt()
        repeat(dotCount) { i ->
            val isFilled = i < filled
            val infinite = rememberInfiniteTransition()
            val scale by infinite.animateFloat(1f, 1.2f, infiniteRepeatable(tween(500 + i * 50, easing = LinearEasing), RepeatMode.Reverse))
            Box(
                Modifier
                    .size(if (isFilled) 6.dp else 4.dp)
                    .graphicsLayer(scaleX = if (isFilled) scale else 1f, scaleY = if (isFilled) scale else 1f)
                    .clip(RoundedCornerShape(1.dp))
                    .background(if (isFilled) PixelPalette.Gold else PixelPalette.Gray700)
            )
        }
    }
}

@Composable
fun PixelFloatingText(
    text: String,
    start: Offset,
    color: Color = PixelPalette.Gold,
    modifier: Modifier = Modifier,
    onComplete: () -> Unit = {}
) {
    var offsetY by remember { mutableStateOf(0f) }
    var alpha by remember { mutableStateOf(1f) }
    LaunchedEffect(start) {
        animate(0f, -30f, tween(800, easing = FastOutSlowInEasing)) { v, _ -> offsetY = v }
        animate(1f, 0f, tween(800, delayMillis = 200)) { v, _ -> alpha = v }
        onComplete()
    }
    Text(
        text,
        color = color.copy(alpha = alpha),
        fontSize = 10.sp,
        fontWeight = FontWeight.Black,
        fontFamily = PixelTypography.TitlePixel.fontFamily,
        modifier = modifier.graphicsLayer(translationY = offsetY)
    )
}

// 8000 satır için ekstra animasyonlar
@Composable
fun PixelBreathingGlow(modifier: Modifier = Modifier, color: Color = PixelPalette.Gold) {
    val infinite = rememberInfiniteTransition()
    val alpha by infinite.animateFloat(0.15f, 0.35f, infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse))
    Box(modifier.fillMaxSize().background(color.copy(alpha = alpha)))
}

@Composable
fun PixelSparkleField(modifier: Modifier = Modifier, count: Int = 15) {
    val infinite = rememberInfiniteTransition()
    val t by infinite.animateFloat(0f, 1f, infiniteRepeatable(tween(2000, easing = LinearEasing)))
    Canvas(modifier.fillMaxSize()) {
        val rng = Random(789)
        repeat(count) { i ->
            val x = rng.nextFloat() * size.width
            val y = rng.nextFloat() * size.height
            val phase = (t + i * 0.1f) % 1f
            val alpha = (sin(phase * Math.PI * 2).toFloat() * 0.5f + 0.5f)
            val sz = 1.5f + rng.nextFloat() * 1.5f
            drawCircle(Color.White.copy(alpha = alpha * 0.8f), radius = sz, center = Offset(x, y))
        }
    }
}
