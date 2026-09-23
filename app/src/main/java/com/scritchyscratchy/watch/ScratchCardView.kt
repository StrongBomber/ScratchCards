package com.scritchyscratchy.watch

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import kotlin.math.roundToInt

@Composable
fun ScratchCardCanvas(
    card: ScratchCardInstance,
    scratchPower: Float,
    areaSize: Float,
    onScratchProgress: (Float) -> Unit,
    onRevealComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Scratch overlay using bitmap erasing technique
    // We track paths and compute coverage approx
    var paths by remember(card.uid) { mutableStateOf(listOf<List<Offset>>()) }
    var currentPath by remember(card.uid) { mutableStateOf(mutableListOf<Offset>()) }
    var revealed by remember(card.uid) { mutableStateOf(0f) }
    var hasCompleted by remember { mutableStateOf(false) }

    val density = LocalDensity.current
    // approximate coverage via grid sampling
    LaunchedEffect(paths) {
        if(paths.isEmpty()) {
            revealed = 0f
            onScratchProgress(0f)
            return@LaunchedEffect
        }
        // crude coverage estimate: count segments
        val totalPoints = paths.sumOf { it.size } + currentPath.size
        // Need ~150 points for full reveal at power 1, less at higher power
        val needed = (160 / scratchPower).roundToInt()
        val prog = (totalPoints.toFloat() / needed).coerceIn(0f, 1f)
        revealed = prog
        onScratchProgress(prog)
        if(prog >= 0.72f && !hasCompleted) {
            hasCompleted = true
            onRevealComplete()
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF5F5F5))
    ) {
        // Underlying symbols
        CardSymbolsGrid(card, revealed >= 0.35f)

        // Scratch overlay - offscreen for BlendMode.Clear to work on Galaxy Watch 8
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                .pointerInput(card.uid, scratchPower, areaSize) {
                    detectDragGestures(
                        onDragStart = {
                            currentPath = mutableListOf(it)
                        },
                        onDrag = { change, _ ->
                            currentPath.add(change.position)
                            // sample areaSize enlarges effective brush
                            val extra = (areaSize * 6).toInt()
                            // add extra points around for coverage simulation
                            repeat(extra) {
                                currentPath.add(change.position + Offset((it-3)*2f, 0f))
                            }
                        },
                        onDragEnd = {
                            if (currentPath.isNotEmpty()) {
                                paths = paths + listOf(currentPath.toList())
                                currentPath = mutableListOf()
                            }
                        },
                        onDragCancel = {
                            if (currentPath.isNotEmpty()) {
                                paths = paths + listOf(currentPath.toList())
                                currentPath = mutableListOf()
                            }
                        }
                    )
                }
        ) {
            val w = size.width
            val h = size.height
            // overlay color - metallic scratch
            val overlayColor = Color(0xFF9E9E9E)
            val strokeWidth = 22f * areaSize * scratchPower

            // Draw overlay rect
            drawRect(color = overlayColor, size = size)

            // Texture pattern - diagonal lines
            val lineColor = Color(0xFFBDBDBD)
            for (i in -20..(w+h).toInt() step 14) {
                drawLine(lineColor, Offset(i.toFloat(), 0f), Offset(i+20f, 20f), strokeWidth = 1.2f)
            }
            // Shimmer
            drawRoundRect(
                color = Color.White.copy(alpha = 0.18f),
                topLeft = Offset(w*0.08f, h*0.12f),
                size = androidx.compose.ui.geometry.Size(w*0.84f, h*0.18f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f,8f)
            )

            // Erase paths - use BlendMode.Clear
            // We draw paths with Clear to reveal underneath
            // Need to draw on top layer with alpha

            // This technique: draw transparent paths
            // Since Canvas has no layer, we simulate by drawing gaps
            // We will draw paths as transparent strokes that "cut" overlay
            // Using BlendMode.Clear requires a saveLayer - we can achieve by drawing with destination out

            // For Wear OS, BlendMode.Clear works when we use graphicsLayer compositing
            // So we draw erase strokes
            val allPaths = paths + if(currentPath.isNotEmpty()) listOf(currentPath) else emptyList()
            for (pathPoints in allPaths) {
                if(pathPoints.size < 2) continue
                val p = Path()
                p.moveTo(pathPoints[0].x, pathPoints[0].y)
                for(i in 1 until pathPoints.size) {
                    val prev = pathPoints[i-1]
                    val cur = pathPoints[i]
                    // smooth with quad
                    val midX = (prev.x + cur.x)/2
                    val midY = (prev.y + cur.y)/2
                    p.quadraticBezierTo(prev.x, prev.y, midX, midY)
                }
                drawPath(
                    path = p,
                    color = Color.Transparent,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round),
                    blendMode = BlendMode.Clear
                )
                // also draw circles at points for solid
                for(pt in pathPoints) {
                    drawCircle(
                        color = Color.Transparent,
                        radius = strokeWidth/2,
                        center = pt,
                        blendMode = BlendMode.Clear
                    )
                }
            }

            // Brushed text hint if not scratched much
            if(revealed < 0.3f) {
                // hint text will be rendered as overlay text below via Box, not canvas
            }
            // coverage indicator is handled outside
        }

        // Hint overlay
        if(revealed < 0.25f) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "KAZI! →",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // Result badge when revealed
        if(revealed >= 0.72f) {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.55f)), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        when {
                            card.isPenalty -> "💸 CEZA ${card.payout} $"
                            card.isJackpot -> "🎉 JACKPOT ${card.payout} $"
                            card.payout > 0 -> "✨ +${card.payout} $"
                            else -> "😢 KAYIP"
                        },
                        color = when {
                            card.isPenalty -> Color(0xFFFF5252)
                            card.isJackpot -> Color(0xFFFFD600)
                            card.payout > 0 -> Color(0xFF69F0AE)
                            else -> Color.White
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )
                    if(card.isJackpot) Text("+5 JP", color = Color(0xFFFFD600), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun CardSymbolsGrid(card: ScratchCardInstance, semiVisible: Boolean) {
    val cols = when(card.symbols.size) {
        1 -> 1
        3 -> 3
        5,6 -> 3
        9 -> 3
        else -> 3
    }
    Column(
        modifier = Modifier.fillMaxSize().padding(8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(card.definition.icon, fontSize = 10.sp, textAlign = TextAlign.Center)
        Text(card.definition.nameTr, fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color(0xFF212121), textAlign = TextAlign.Center, maxLines = 1)
        Text("Lv${card.level} • ${card.definition.cost}$", fontSize = 6.sp, color = Color.Gray)
        Spacer(Modifier.height(4.dp))
        // symbols grid
        val chunked = card.symbols.chunked(cols)
        for(row in chunked) {
            Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                for(sym in row) {
                    Box(
                        modifier = Modifier
                            .padding(2.dp)
                            .size(22.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.White)
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(sym.display, fontSize = 10.sp, textAlign = TextAlign.Center)
                    }
                }
            }
        }
        if(semiVisible && card.symbols.size <= 3) {
            Spacer(Modifier.height(4.dp))
            Text(
                if(card.isPenalty) "TEHLİKE!" else if(card.isJackpot) "JACKPOT ŞANSI!" else "",
                fontSize = 6.sp, color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold
            )
        }
    }
}
