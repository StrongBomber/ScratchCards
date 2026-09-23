package com.scritchyscratchy.watch.pixel

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.TextStyle
import androidx.compose.runtime.Composable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.Alignment
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.unit.Dp

/**
 * PIXEL TEMALI SCRITCHY SCRATCHY - Galaxy Watch 8 480x480
 * Retro 8-bit, CRT scanline, pixel border, arcade palette
 * En az 8000 satırlık gelişimin temeli - Pixel sistem
 */

// ============= PALETTE - ARCADE PIXEL =============
object PixelPalette {
    // Ana arka plan - koyu arcade
    val BgDeep = Color(0xFF0A0E14)
    val BgDark = Color(0xFF151A23)
    val BgCard = Color(0xFF1F2430)
    val BgCardLight = Color(0xFF2A3042)
    val BgOverlay = Color(0xFF0A0E14).copy(alpha = 0.85f)

    // Pixel vurgu renkleri
    val Gold = Color(0xFFFFD600)
    val GoldDark = Color(0xFFFFAB00)
    val GoldLight = Color(0xFFFFEA00)
    val Emerald = Color(0xFF00E676)
    val EmeraldDark = Color(0xFF00C853)
    val Ruby = Color(0xFFFF1744)
    val RubyDark = Color(0xFFD50000)
    val Sapphire = Color(0xFF2979FF)
    val SapphireDark = Color(0xFF2962FF)
    val Amethyst = Color(0xFFD500F9)
    val AmethystDark = Color(0xFFAA00FF)
    val Topaz = Color(0xFFFF6D00)
    val TopazDark = Color(0xFFDD2C00)

    // Kart renkleri - pixel foil
    val FoilSilver = Color(0xFFB0BEC5)
    val FoilSilverDark = Color(0xFF78909C)
    val FoilGold = Color(0xFFFFD740)
    val FoilHologram = Brush.linearGradient(listOf(Color(0xFF00E5FF), Color(0xFFFF00FF), Color(0xFFFFEA00)))

    // Pixel gri skalası
    val Gray100 = Color(0xFFF5F5F5)
    val Gray300 = Color(0xFFE0E0E0)
    val Gray500 = Color(0xFF9E9E9E)
    val Gray700 = Color(0xFF616161)
    val Gray900 = Color(0xFF212121)

    // Compat aliases for old code (PixelMainScreen etc.)
    val Background = BgDeep
    val Muted = Gray500
    val Surface = BgCard
    val Cyan = Sapphire

    // Neon glow
    val NeonGold = Color(0xFFFFD600).copy(alpha = 0.5f)
    val NeonEmerald = Color(0xFF00E676).copy(alpha = 0.5f)
    val NeonRuby = Color(0xFFFF1744).copy(alpha = 0.5f)

    // Kart tipine göre palet
    fun forCard(type: String): Color = when(type) {
        "quick_cash" -> Gold
        "snake_eyes" -> Ruby
        "apple_tree" -> Emerald
        "lucky_cat" -> GoldLight
        "scratch_my_back" -> Sapphire
        "mega_jackpot" -> Amethyst
        "final_chance" -> Color(0xFF212121)
        else -> Gray500
    }

    // Pixel dither pattern renkleri
    val DitherLight = Color(0xFF2A3042)
    val DitherDark = Color(0xFF1A1F2E)
}

// ============= PIXEL SHAPES =============
object PixelShapes {
    val Pixel2 = RoundedCornerShape(2.dp)
    val Pixel4 = RoundedCornerShape(4.dp)
    val Pixel6 = RoundedCornerShape(6.dp)
    val Pixel8 = RoundedCornerShape(8.dp)
    val Pixel12 = RoundedCornerShape(12.dp)
    val Pixel16 = RoundedCornerShape(16.dp)
    val PixelRound = RoundedCornerShape(50)
}

// ============= PIXEL TYPOGRAPHY =============
object PixelTypography {
    // Monospace pixel font simulation - Press Start 2P stili
    val DisplayPixel = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Black,
        fontSize = 12.sp,
        letterSpacing = 0.5.sp,
        color = PixelPalette.Gold
    )
    val TitlePixel = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        letterSpacing = 0.3.sp,
        color = Color.White
    )
    val BodyPixel = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 8.sp,
        letterSpacing = 0.2.sp,
        color = Color.White
    )
    val LabelPixel = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 7.sp,
        letterSpacing = 0.4.sp,
        color = PixelPalette.Gray300
    )
    val CaptionPixel = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 6.sp,
        letterSpacing = 0.3.sp,
        color = PixelPalette.Gray500
    )
    val MonoGold = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 11.sp,
        letterSpacing = 0.5.sp,
        color = PixelPalette.Gold
    )
    val MonoEmerald = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 9.sp,
        color = PixelPalette.Emerald
    )
    val MonoRuby = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 9.sp,
        color = PixelPalette.Ruby
    )
    // Compat
    val ValuePixel = MonoGold
}

// ============= PIXEL MODIFIERS =============
fun Modifier.pixelBorder(color: Color = PixelPalette.Gold, width: Dp = 2.dp, shape: RoundedCornerShape = PixelShapes.Pixel8): Modifier =
    this.border(width, color, shape)

fun Modifier.pixelShadow(color: Color = Color.Black.copy(alpha = 0.5f)): Modifier =
    this.shadow(elevation = 4.dp, shape = PixelShapes.Pixel8, clip = false)

fun Modifier.pixelGlow(color: Color = PixelPalette.Gold): Modifier =
    this.shadow(elevation = 8.dp, shape = PixelShapes.Pixel8, ambientColor = color, spotColor = color)

// ============= PIXEL BACKGROUNDS =============
@Composable
fun PixelBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = modifier
            .background(PixelPalette.BgDeep)
            .fillMaxSize(),
        content = content
    )
    // Scanline overlay için üst katman ayrı çizilir
}

@Composable
fun PixelCardBackground(
    modifier: Modifier = Modifier,
    color: Color = PixelPalette.BgCard,
    borderColor: Color = PixelPalette.Gold.copy(alpha = 0.3f),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(PixelShapes.Pixel12)
            .background(color)
            .border(1.5.dp, borderColor, PixelShapes.Pixel12),
        content = content
    )
}

// ============= PIXEL GRID PATTERN =============
@Composable
fun PixelGridOverlay(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val step = 8.dp.toPx()
        val dotColor = PixelPalette.Gray900.copy(alpha = 0.15f)
        for (x in 0..size.width.toInt() step step.toInt()) {
            for (y in 0..size.height.toInt() step step.toInt()) {
                drawCircle(dotColor, radius = 0.6f, center = Offset(x.toFloat(), y.toFloat()))
            }
        }
    }
}

// ============= PIXEL SCANLINE EFFECT =============
@Composable
fun PixelScanlines(modifier: Modifier = Modifier, opacity: Float = 0.08f) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val lineH = 3.dp.toPx()
        val gap = 3.dp.toPx()
        var y = 0f
        while (y < size.height) {
            drawRect(
                color = Color.Black.copy(alpha = opacity),
                topLeft = Offset(0f, y),
                size = Size(size.width, lineH)
            )
            y += lineH + gap
        }
        // vertical scan subtle
        val vLineW = 1.dp.toPx()
        var x = 0f
        while (x < size.width) {
            drawRect(
                color = Color.White.copy(alpha = 0.02f),
                topLeft = Offset(x, 0f),
                size = Size(vLineW, size.height)
            )
            x += 24.dp.toPx()
        }
    }
}

// ============= PIXEL DITHER =============
@Composable
fun PixelDither(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val sz = 4.dp.toPx()
        var y = 0f
        var row = 0
        while (y < size.height) {
            var x = if (row % 2 == 0) 0f else sz / 2
            while (x < size.width) {
                drawRect(
                    color = if ((row % 2) == 0) PixelPalette.DitherLight.copy(alpha = 0.04f) else PixelPalette.DitherDark.copy(alpha = 0.04f),
                    topLeft = Offset(x, y),
                    size = Size(sz, sz)
                )
                x += sz * 2
            }
            y += sz
            row++
        }
    }
}

// ============= PIXEL CORNER BRACKETS =============
@Composable
fun PixelCornerBrackets(
    modifier: Modifier = Modifier,
    color: Color = PixelPalette.Gold,
    size: Dp = 12.dp,
    stroke: Dp = 2.dp
) {
    Box(modifier = modifier.fillMaxSize()) {
        // Top-left
        Canvas(Modifier.align(Alignment.TopStart).size(size)) {
            drawLine(color, Offset(0f, 0f), Offset(size.toPx(), 0f), strokeWidth = stroke.toPx())
            drawLine(color, Offset(0f, 0f), Offset(0f, size.toPx()), strokeWidth = stroke.toPx())
        }
        // Top-right
        Canvas(Modifier.align(Alignment.TopEnd).size(size)) {
            drawLine(color, Offset(size.toPx(), 0f), Offset(0f, 0f), strokeWidth = stroke.toPx())
            drawLine(color, Offset(size.toPx(), 0f), Offset(size.toPx(), size.toPx()), strokeWidth = stroke.toPx())
        }
        // Bottom-left
        Canvas(Modifier.align(Alignment.BottomStart).size(size)) {
            drawLine(color, Offset(0f, size.toPx()), Offset(size.toPx(), size.toPx()), strokeWidth = stroke.toPx())
            drawLine(color, Offset(0f, size.toPx()), Offset(0f, 0f), strokeWidth = stroke.toPx())
        }
        // Bottom-right
        Canvas(Modifier.align(Alignment.BottomEnd).size(size)) {
            drawLine(color, Offset(size.toPx(), size.toPx()), Offset(0f, size.toPx()), strokeWidth = stroke.toPx())
            drawLine(color, Offset(size.toPx(), size.toPx()), Offset(size.toPx(), 0f), strokeWidth = stroke.toPx())
        }
    }
}

// ============= PIXEL DIVIDER =============
@Composable
fun PixelDivider(
    modifier: Modifier = Modifier,
    color: Color = PixelPalette.Gold.copy(alpha = 0.3f),
    dotColor: Color = PixelPalette.Gold
) {
    Canvas(modifier = modifier.fillMaxWidth().height(4.dp)) {
        val dotSize = 2.dp.toPx()
        val gap = 4.dp.toPx()
        var x = 0f
        while (x < size.width) {
            drawLine(color, Offset(x, size.height / 2), Offset(x + gap, size.height / 2), strokeWidth = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(2f, 4f)))
            x += gap + dotSize
        }
        // center dot
        drawCircle(dotColor, radius = 2.dp.toPx(), center = Offset(size.width / 2, size.height / 2))
    }
}

// ============= PIXEL GLOW TEXT =============
@Composable
fun PixelGlowBox(
    color: Color = PixelPalette.Gold,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .shadow(8.dp, PixelShapes.Pixel8, ambientColor = color.copy(alpha = 0.4f), spotColor = color.copy(alpha = 0.4f))
            .clip(PixelShapes.Pixel8)
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.5f), PixelShapes.Pixel8),
        content = content
    )
}

// ============= PIXEL CHIP STYLE =============
object PixelChipStyles {
    val Gold = PixelChipStyle(PixelPalette.Gold, Color.Black, PixelPalette.GoldDark)
    val Emerald = PixelChipStyle(PixelPalette.Emerald, Color.Black, PixelPalette.EmeraldDark)
    val Ruby = PixelChipStyle(PixelPalette.Ruby, Color.White, PixelPalette.RubyDark)
    val Dark = PixelChipStyle(PixelPalette.BgCardLight, Color.White, PixelPalette.BgDark)
    val Disabled = PixelChipStyle(PixelPalette.Gray700, PixelPalette.Gray500, PixelPalette.Gray900)
}

data class PixelChipStyle(val bg: Color, val content: Color, val border: Color)

// ============= PIXEL ANIMATION SPECS =============
object PixelAnim {
    const val DurationShort = 120
    const val DurationMedium = 220
    const val DurationLong = 400
    const val DurationJackpot = 600
    // Easing: pixel step
    const val Steps = 4
}

// ============= CRT FLICKER =============
@Composable
fun PixelCrtFlicker(modifier: Modifier = Modifier, intensity: Float = 0.03f) {
    Canvas(modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.White.copy(alpha = intensity * 0.5f),
                    Color.Transparent
                ),
                startY = 0f,
                endY = size.height
            ),
            size = size
        )
    }
}

// ============= PIXEL FOIL EFFECT =============
@Composable
fun PixelFoilBorder(
    modifier: Modifier = Modifier,
    isHolographic: Boolean = false
) {
    Box(
        modifier = modifier
            .clip(PixelShapes.Pixel12)
            .border(
                width = 1.5.dp,
                brush = if (isHolographic) PixelPalette.FoilHologram else Brush.linearGradient(
                    listOf(PixelPalette.FoilSilver, PixelPalette.FoilSilverDark, PixelPalette.FoilSilver)
                ),
                shape = PixelShapes.Pixel12
            )
    )
}

// ============= PIXEL STARFIELD =============
@Composable
fun PixelStarfield(modifier: Modifier = Modifier, starCount: Int = 40) {
    Canvas(modifier.fillMaxSize()) {
        val rng = java.util.Random(42)
        repeat(starCount) {
            val x = rng.nextFloat() * size.width
            val y = rng.nextFloat() * size.height
            val r = rng.nextFloat() * 1.2f + 0.4f
            val alpha = rng.nextFloat() * 0.6f + 0.2f
            drawCircle(Color.White.copy(alpha = alpha), radius = r, center = Offset(x, y))
            if (rng.nextFloat() > 0.85f) {
                // cross sparkle
                val l = 3.dp.toPx()
                drawLine(Color.White.copy(alpha = alpha * 0.5f), Offset(x - l, y), Offset(x + l, y), strokeWidth = 0.6f)
                drawLine(Color.White.copy(alpha = alpha * 0.5f), Offset(x, y - l), Offset(x, y + l), strokeWidth = 0.6f)
            }
        }
    }
}

// ============= PIXEL VIGNETTE =============
@Composable
fun PixelVignette(modifier: Modifier = Modifier) {
    Canvas(modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.65f)),
                center = Offset(size.width / 2, size.height / 2),
                radius = size.minDimension * 0.75f
            ),
            size = size
        )
    }
}

// ============= PIXEL UTILITIES =============
fun Color.darken(factor: Float = 0.2f): Color {
    return Color(
        red = (red * (1 - factor)).coerceIn(0f, 1f),
        green = (green * (1 - factor)).coerceIn(0f, 1f),
        blue = (blue * (1 - factor)).coerceIn(0f, 1f),
        alpha = alpha
    )
}

fun Color.lighten(factor: Float = 0.2f): Color {
    return Color(
        red = (red + (1 - red) * factor).coerceIn(0f, 1f),
        green = (green + (1 - green) * factor).coerceIn(0f, 1f),
        blue = (blue + (1 - blue) * factor).coerceIn(0f, 1f),
        alpha = alpha
    )
}
