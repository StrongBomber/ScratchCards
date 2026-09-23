package com.scritchyscratchy.watch.pixel

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.shadow
import androidx.compose.animation.core.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.Text
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalDensity

// ============= PIXEL BUTTON =============
@Composable
fun PixelButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: String = "",
    style: PixelChipStyle = PixelChipStyles.Gold,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    subText: String? = null,
    glow: Boolean = false
) {
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.96f else 1f, tween(80))

    Chip(
        onClick = onClick,
        enabled = enabled && !isLoading,
        label = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth().graphicsLayer(scaleX = scale, scaleY = scale)
            ) {
                if (icon.isNotEmpty()) Text(icon, fontSize = 10.sp, modifier = Modifier.padding(end = 4.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Text(
                        text.uppercase(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = PixelTypography.TitlePixel.fontFamily,
                        color = if (enabled) style.content else PixelPalette.Gray500,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                    if (subText != null) Text(
                        subText,
                        fontSize = 6.sp,
                        fontFamily = PixelTypography.CaptionPixel.fontFamily,
                        color = if (enabled) style.content.copy(alpha = 0.7f) else PixelPalette.Gray500,
                        textAlign = TextAlign.Center
                    )
                }
                if (isLoading) Text("◐", fontSize = 9.sp, modifier = Modifier.padding(start = 4.dp))
            }
        },
        colors = ChipDefaults.chipColors(
            backgroundColor = if (enabled) style.bg else PixelChipStyles.Disabled.bg,
            contentColor = if (enabled) style.content else PixelPalette.Gray500
        ),
        modifier = modifier
            .shadow(
                elevation = if (glow) 6.dp else 2.dp,
                shape = PixelShapes.Pixel8,
                ambientColor = if (glow) style.bg.copy(alpha = 0.4f) else Color.Black.copy(alpha = 0.3f),
                spotColor = if (glow) style.bg.copy(alpha = 0.4f) else Color.Black.copy(alpha = 0.3f)
            )
            .border(
                width = if (isPressed) 2.dp else 1.dp,
                color = if (enabled) style.border else PixelPalette.Gray700,
                shape = PixelShapes.Pixel8
            )
            .clip(PixelShapes.Pixel8),
        interactionSource = interaction
    )
}

// ============= PIXEL PROGRESS BAR (8-bit) =============
@Composable
fun PixelProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 10.dp,
    bg: Color = PixelPalette.BgDark,
    fill: Color = PixelPalette.Gold,
    showSegments: Boolean = true,
    glow: Boolean = true
) {
    val animated by animateFloatAsState(progress.coerceIn(0f, 1f), tween(300))
    Box(
        modifier = modifier
            .height(height)
            .clip(PixelShapes.Pixel4)
            .background(bg)
            .border(1.dp, Color.White.copy(alpha = 0.1f), PixelShapes.Pixel4)
    ) {
        // Fill
        Box(
            modifier = Modifier
                .fillMaxWidth(animated)
                .fillMaxHeight()
                .clip(PixelShapes.Pixel4)
                .background(
                    Brush.horizontalGradient(listOf(fill, fill.lighten(0.15f)))
                )
                .then(if (glow) Modifier.shadow(4.dp, PixelShapes.Pixel4, ambientColor = fill.copy(alpha = 0.5f)) else Modifier)
        ) {
            // shine
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .align(Alignment.TopCenter)
                    .background(Color.White.copy(alpha = 0.25f))
            )
        }
        // Segments
        if (showSegments) {
            Canvas(Modifier.fillMaxSize()) {
                val segW = 4.dp.toPx()
                val gap = 2.dp.toPx()
                var x = segW
                while (x < size.width) {
                    drawLine(Color.Black.copy(alpha = 0.3f), Offset(x, 0f), Offset(x, size.height), strokeWidth = 1.dp.toPx())
                    x += segW + gap
                }
            }
        }
        // Pixel border highlight
        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .align(Alignment.TopCenter)
                .background(Color.White.copy(alpha = 0.15f))
        )
    }
}

// ============= PIXEL STAT BADGE =============
@Composable
fun PixelBadge(
    text: String,
    color: Color = PixelPalette.Gold,
    modifier: Modifier = Modifier,
    icon: String? = null
) {
    Row(
        modifier = modifier
            .clip(PixelShapes.Pixel6)
            .background(color.copy(alpha = 0.18f))
            .border(1.dp, color.copy(alpha = 0.5f), PixelShapes.Pixel6)
            .padding(horizontal = 6.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        if (icon != null) Text(icon, fontSize = 8.sp)
        Text(
            text,
            fontSize = 7.sp,
            fontWeight = FontWeight.Black,
            fontFamily = PixelTypography.LabelPixel.fontFamily,
            color = color
        )
    }
}

// ============= PIXEL BALANCE PILL =============
@Composable
fun PixelBalancePill(
    balance: Long,
    jackPoints: Int,
    hasAuto: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(PixelPalette.BgDark.copy(alpha = 0.92f))
            .border(1.dp, PixelPalette.Gold.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Coin icon
        Box(
            Modifier
                .size(14.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(PixelPalette.Gold)
                .border(1.dp, PixelPalette.GoldDark, RoundedCornerShape(7.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("$", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color.Black)
        }
        Text(
            "${balance} $",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            fontFamily = PixelTypography.TitlePixel.fontFamily,
            color = if (balance < 0) PixelPalette.Ruby else PixelPalette.Gold
        )
        if (jackPoints > 0) PixelBadge("${jackPoints} JP", PixelPalette.Amethyst)
        if (hasAuto) Text("🤖", fontSize = 9.sp)
    }
}

// ============= PIXEL SECTION HEADER =============
@Composable
fun PixelSectionHeader(
    title: String,
    icon: String = "▣",
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    color: Color = PixelPalette.Gold
) {
    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(Modifier.size(6.dp).background(color).clip(RoundedCornerShape(1.dp)))
            Text(icon, fontSize = 10.sp)
            Text(
                title.uppercase(),
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                fontFamily = PixelTypography.TitlePixel.fontFamily,
                color = color,
                letterSpacing = 1.sp
            )
            Box(Modifier.size(6.dp).background(color).clip(RoundedCornerShape(1.dp)))
        }
        if (subtitle != null) Text(
            subtitle,
            fontSize = 6.sp,
            fontFamily = PixelTypography.CaptionPixel.fontFamily,
            color = PixelPalette.Gray500,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 2.dp)
        )
        // underline
        Box(
            Modifier
                .padding(top = 4.dp)
                .width(32.dp)
                .height(2.dp)
                .background(color.copy(alpha = 0.5f))
        )
    }
}

// ============= PIXEL DIVIDER WITH ICON =============
@Composable
fun PixelDividerWithIcon(icon: String = "◆", modifier: Modifier = Modifier, color: Color = PixelPalette.Gold.copy(alpha = 0.3f)) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(Modifier.weight(1f).height(1.dp).background(color))
        Text(icon, fontSize = 8.sp, color = PixelPalette.Gold)
        Box(Modifier.weight(1f).height(1.dp).background(color))
    }
}

// ============= PIXEL LEVEL DOTS =============
@Composable
fun PixelLevelDots(level: Int, max: Int = 10, modifier: Modifier = Modifier, activeColor: Color = PixelPalette.Gold) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        repeat(max) { i ->
            Box(
                Modifier
                    .size(if (i < level) 5.dp else 4.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(if (i < level) activeColor else PixelPalette.Gray700)
                    .border(0.5.dp, if (i < level) activeColor.lighten(0.2f) else Color.Transparent, RoundedCornerShape(1.dp))
            )
        }
    }
}

// ============= PIXEL RARITY BORDER =============
@Composable
fun Modifier.pixelRarityBorder(rarity: Int): Modifier {
    val color = when (rarity) {
        0 -> PixelPalette.Gray500
        1 -> PixelPalette.Emerald
        2 -> PixelPalette.Sapphire
        3 -> PixelPalette.Amethyst
        4 -> PixelPalette.Gold
        5 -> PixelPalette.Ruby
        else -> PixelPalette.Gold
    }
    return this.border(1.5.dp, color, PixelShapes.Pixel8)
}

// ============= PIXEL JACKPOT FLASH =============
@Composable
fun PixelJackpotFlash(modifier: Modifier = Modifier) {
    val infinite = rememberInfiniteTransition()
    val alpha by infinite.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(tween(300, easing = LinearEasing), RepeatMode.Reverse)
    )
    Box(
        modifier
            .fillMaxSize()
            .background(PixelPalette.Gold.copy(alpha = alpha * 0.15f))
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val step = 12.dp.toPx()
            for (x in -size.width.toInt()..size.width.toInt() step step.toInt()) {
                drawLine(
                    PixelPalette.Gold.copy(alpha = alpha * 0.12f),
                    Offset(x.toFloat(), 0f),
                    Offset(x + size.height, size.height),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                )
            }
        }
    }
}

// ============= PIXEL TOOLTIP =============
@Composable
fun PixelTooltip(text: String, modifier: Modifier = Modifier, color: Color = PixelPalette.BgCardLight) {
    Box(
        modifier
            .clip(PixelShapes.Pixel6)
            .background(color)
            .border(1.dp, PixelPalette.Gold.copy(alpha = 0.3f), PixelShapes.Pixel6)
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Text(
            text,
            fontSize = 6.sp,
            fontFamily = PixelTypography.CaptionPixel.fontFamily,
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }
}

// ============= PIXEL EMPTY STATE =============
@Composable
fun PixelEmptyState(
    icon: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .fillMaxWidth()
            .clip(PixelShapes.Pixel12)
            .background(PixelPalette.BgCard)
            .border(1.dp, PixelPalette.Gray700, PixelShapes.Pixel12)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(icon, fontSize = 24.sp)
        Text(title, fontSize = 9.sp, fontWeight = FontWeight.Black, fontFamily = PixelTypography.TitlePixel.fontFamily, color = Color.White, textAlign = TextAlign.Center)
        Text(subtitle, fontSize = 7.sp, fontFamily = PixelTypography.CaptionPixel.fontFamily, color = PixelPalette.Gray500, textAlign = TextAlign.Center)
    }
}

// ============= PIXEL COIN FLIP ANIMATION =============
@Composable
fun PixelCoinFlip(amount: Long, modifier: Modifier = Modifier) {
    val infinite = rememberInfiniteTransition()
    val rotation by infinite.animateFloat(0f, 360f, infiniteRepeatable(tween(800, easing = LinearEasing)))
    Box(
        modifier
            .size(18.dp)
            .graphicsLayer(rotationY = rotation)
            .clip(RoundedCornerShape(9.dp))
            .background(PixelPalette.Gold)
            .border(1.dp, PixelPalette.GoldDark, RoundedCornerShape(9.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text("$", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.Black)
    }
}
