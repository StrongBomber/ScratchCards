package com.scritchyscratchy.watch.pixel

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.*
import com.scritchyscratchy.watch.CardCatalog
import com.scritchyscratchy.watch.CardGenerator
import com.scritchyscratchy.watch.GameState
import com.scritchyscratchy.watch.ScratchCardInstance

// ============= PIXEL MAIN SCREEN - 480x480 YUVARLAK TAM EKRAN =============
// Galaxy Watch 8 için tam yuvarlak adaptif pixel ana ekran

@Composable
fun PixelMainScreen(
    state: GameState,
    onScratch: (ScratchCardInstance) -> Unit,
    onBuyCard: (com.scritchyscratchy.watch.CardDefinition) -> Unit,
    onClaimDaily: () -> Unit,
    onShowPrestige: () -> Unit,
    onShowStats: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    var tick by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(2000)
            tick++
        }
    }

    Box(modifier.fillMaxSize().background(PixelPalette.Background)) {
        // CRT + scanline overlay
        PixelCrtEffect(Modifier.fillMaxSize(), scanOpacity = 0.05f)
        // arka dot matrix
        PixelDotMatrix(Modifier.fillMaxSize())

        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // TOP HUD
            PixelWatchTopBar(state, tick, Modifier.fillMaxWidth())

            Spacer(Modifier.height(4.dp))

            // TAB BAR
            PixelTabBar(selectedTab, onSelect = { selectedTab = it })

            Spacer(Modifier.height(4.dp))

            when (selectedTab) {
                0 -> PixelQuickPlayTab(state, onScratch, onBuyCard, Modifier.weight(1f))
                1 -> PixelStoreTab(state, onBuyCard, Modifier.weight(1f))
                2 -> PixelDailyTab(state, onClaimDaily, Modifier.weight(1f))
                else -> PixelMoreTab(state, onShowPrestige, onShowStats, Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun PixelWatchTopBar(state: GameState, tick: Int, modifier: Modifier = Modifier) {
    val market = PixelMarket.activeFor(state)
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // sol - balance
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    PixelEconomyUtils.formatMoney(state.balance),
                    color = PixelPalette.Gold,
                    fontFamily = PixelTypography.ValuePixel.fontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    "Nakit",
                    color = PixelPalette.Muted.copy(alpha = 0.8f),
                    fontFamily = PixelTypography.LabelPixel.fontFamily,
                    fontSize = 6.sp
                )
            }
            // orta - JP + level
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier.background(PixelPalette.GoldDark, RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        "JP ${state.prestige.jackPoints} • Lv ${state.prestige.prestigeCount}",
                        color = Color.Black,
                        fontFamily = PixelTypography.LabelPixel.fontFamily,
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Spacer(Modifier.height(1.dp))
                Text(
                    PixelMarket.iconFor(market?.id) + " " + (market?.title ?: "Normal Gün"),
                    color = marketColor(market?.id),
                    fontFamily = PixelTypography.LabelPixel.fontFamily,
                    fontSize = 5.sp
                )
            }
            // sağ - kazı sayısı
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${state.totalScratched}",
                    color = PixelPalette.Cyan,
                    fontFamily = PixelTypography.ValuePixel.fontFamily,
                    fontSize = 11.sp
                )
                Text(
                    "Kazı",
                    color = PixelPalette.Muted.copy(alpha = 0.8f),
                    fontFamily = PixelTypography.LabelPixel.fontFamily,
                    fontSize = 6.sp
                )
            }
        }
        // progress bar - günlük görev
        val streak = PixelDailySystem.streakDays(state)
        PixelProgressBar(
            progress = streak / 7f,
            color = PixelPalette.Gold,
            height = 3.dp,
            modifier = Modifier.fillMaxWidth().padding(top = 2.dp)
        )
    }
}

private fun marketColor(id: String?): Color = when (id) {
    "boom" -> Color(0xFF00E676)
    "crash" -> Color(0xFFFF1744)
    "hype" -> Color(0xFFFFD600)
    "panic" -> Color(0xFFFF9100)
    "jackpotFever" -> Color(0xFFD500F9)
    else -> PixelPalette.Muted
}

@Composable
fun PixelTabBar(selected: Int, onSelect: (Int) -> Unit) {
    val tabs = listOf("OYNA" to "🎮", "MARKET" to "🛒", "GÜNLÜK" to "📅", "DAHA" to "◆")
    Row(
        Modifier.fillMaxWidth().background(PixelPalette.Surface, RoundedCornerShape(8.dp)).padding(2.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        tabs.forEachIndexed { i, (label, icon) ->
            Chip(
                onClick = { onSelect(i) },
                label = {
                    Text(
                        "$icon $label",
                        fontFamily = PixelTypography.LabelPixel.fontFamily,
                        fontSize = 6.sp,
                        color = if (i == selected) Color.Black else Color.White,
                        fontWeight = FontWeight.Black
                    )
                },
                colors = ChipDefaults.chipColors(
                    backgroundColor = if (i == selected) PixelPalette.Gold else Color(0xFF2A2A2E)
                ),
                modifier = Modifier.weight(1f).padding(horizontal = 1.dp)
            )
        }
    }
}

@Composable
fun PixelQuickPlayTab(
    state: GameState,
    onScratch: (ScratchCardInstance) -> Unit,
    onBuyCard: (com.scritchyscratchy.watch.CardDefinition) -> Unit,
    modifier: Modifier = Modifier
) {
    val affordable = CardCatalog.all.filter { it.cost <= state.balance }.take(3)
    val featured = if (affordable.isEmpty()) CardCatalog.all.first() else affordable.first()
    val preview = remember(featured, state.cardLevels[featured.id]) {
        CardGenerator.generate(featured, state.cardLevels[featured.id] ?: 1, 0.0, state.prestige)
    }

    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
        // öne çıkan kart
        PixelCardFeatured(state, preview, onScratch)

        Spacer(Modifier.height(6.dp))

        // hızlı al
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            CardCatalog.all.take(4).forEach { def ->
                val owned = (state.cardLevels[def.id] ?: 1)
                val can = state.balance >= def.cost
                Chip(
                    onClick = { if (can) onBuyCard(def) },
                    enabled = can,
                    label = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(def.emoji, fontSize = 10.sp)
                            Text("${def.cost}$", fontFamily = PixelTypography.LabelPixel.fontFamily, fontSize = 5.sp, color = if (can) PixelPalette.Gold else PixelPalette.Muted)
                            Text("Lv$owned", fontFamily = PixelTypography.LabelPixel.fontFamily, fontSize = 5.sp, color = PixelPalette.Muted)
                        }
                    },
                    colors = ChipDefaults.chipColors(backgroundColor = if (can) PixelPalette.Surface else Color(0xFF1A1A1A)),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(Modifier.height(6.dp))

        // ipucu
        Box(Modifier.fillMaxWidth().background(PixelPalette.Surface, RoundedCornerShape(8.dp)).padding(8.dp)) {
            Text(
                PixelGameCore.tipForState(state),
                color = PixelPalette.Cyan,
                fontFamily = PixelTypography.LabelPixel.fontFamily,
                fontSize = 7.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(6.dp))
        // koleksiyon özet
        Text(
            PixelCollection.statsText(state) + "  •  " + PixelLeaderboard.rankText(state),
            color = PixelPalette.Muted,
            fontFamily = PixelTypography.LabelPixel.fontFamily,
            fontSize = 6.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun PixelCardFeatured(state: GameState, card: ScratchCardInstance, onScratch: (ScratchCardInstance) -> Unit) {
    val foil = PixelGameCore.foilForLevel(state.cardLevels[card.definition.id] ?: 1)
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF2D2D30),
                        Color(0xFF1E1E20)
                    )
                )
            )
            .padding(8.dp)
    ) {
        // foil
        if (foil != FoilType.NORMAL) {
            PixelFoilShimmer(Modifier.matchParentSize(), isHolo = foil == FoilType.HOLOGRAM || foil == FoilType.RAINBOW)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "${card.definition.emoji} ${card.definition.nameTr.uppercase()} ${if (card.payout > card.definition.cost * 10) "★" else ""}",
                color = PixelPalette.Gold,
                fontFamily = PixelTypography.TitlePixel.fontFamily,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
            Text(
                card.definition.descTr,
                color = PixelPalette.Muted,
                fontFamily = PixelTypography.LabelPixel.fontFamily,
                fontSize = 6.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 2.dp)
            )
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("${card.definition.cost}$", color = PixelPalette.Gold, fontFamily = PixelTypography.ValuePixel.fontFamily, fontSize = 9.sp)
                Text("→", color = PixelPalette.Muted, fontSize = 8.sp)
                Text(
                    "${card.payout}$",
                    color = if (card.payout > card.definition.cost) Color(0xFF00E676) else PixelPalette.Muted,
                    fontFamily = PixelTypography.ValuePixel.fontFamily,
                    fontSize = 9.sp
                )
            }
            Spacer(Modifier.height(6.dp))
            Chip(
                onClick = { onScratch(card) },
                label = {
                    Text(
                        "🎫 KAZI  ${card.definition.cost}$",
                        color = Color.Black,
                        fontFamily = PixelTypography.TitlePixel.fontFamily,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black
                    )
                },
                colors = ChipDefaults.chipColors(backgroundColor = PixelPalette.Gold),
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                "Lv${state.cardLevels[card.definition.id] ?: 1} • foil: ${foil.name}",
                color = PixelPalette.Muted.copy(alpha = 0.7f),
                fontFamily = PixelTypography.LabelPixel.fontFamily,
                fontSize = 6.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
fun PixelStoreTab(state: GameState, onBuy: (com.scritchyscratchy.watch.CardDefinition) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
        CardCatalog.all.forEach { def ->
            val lvl = state.cardLevels[def.id] ?: 1
            val can = state.balance >= def.cost
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(PixelPalette.Surface)
                    .padding(6.dp)
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text("${def.emoji} ${def.nameTr} Lv$lvl", color = Color.White, fontFamily = PixelTypography.LabelPixel.fontFamily, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                        Text(def.descTr, color = PixelPalette.Muted, fontFamily = PixelTypography.LabelPixel.fontFamily, fontSize = 6.sp)
                    }
                    Chip(
                        onClick = { onBuy(def) },
                        enabled = can,
                        label = { Text("${def.cost}$", fontFamily = PixelTypography.LabelPixel.fontFamily, fontSize = 7.sp, color = if (can) Color.Black else PixelPalette.Muted) },
                        colors = ChipDefaults.chipColors(backgroundColor = if (can) PixelPalette.Gold else Color(0xFF1E1E1E))
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
fun PixelDailyTab(state: GameState, onClaim: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
        // streak calendar
        val week = PixelDailySystem.generateWeek(state)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            week.forEach { d ->
                Box(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (d.isToday) PixelPalette.Gold else if (d.claimed) Color(0xFF00E676).copy(alpha = 0.3f) else PixelPalette.Surface)
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(d.dayLabel, fontFamily = PixelTypography.LabelPixel.fontFamily, fontSize = 5.sp, color = if (d.isToday) Color.Black else PixelPalette.Muted)
                        Text(d.icon, fontSize = 10.sp)
                        Text("${d.reward}$", fontFamily = PixelTypography.LabelPixel.fontFamily, fontSize = 5.sp, color = if (d.isToday) Color.Black else PixelPalette.Gold)
                    }
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        val today = PixelDailySystem.today(state)
        if (today != null && PixelDailySystem.canClaim(state)) {
            Chip(
                onClick = onClaim,
                label = { Text("🎁 ${today.reward}$ AL", fontFamily = PixelTypography.TitlePixel.fontFamily, fontSize = 8.sp, color = Color.Black, fontWeight = FontWeight.Black) },
                colors = ChipDefaults.chipColors(backgroundColor = PixelPalette.Gold),
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Box(Modifier.fillMaxWidth().background(PixelPalette.Surface, RoundedCornerShape(8.dp)).padding(8.dp), contentAlignment = Alignment.Center) {
                Text(
                    PixelDailySystem.weekCompleteText(state),
                    color = PixelPalette.Muted,
                    fontFamily = PixelTypography.LabelPixel.fontFamily,
                    fontSize = 7.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        // quest
        val quests = PixelDailySystem.questsText(state)
        Box(Modifier.fillMaxWidth().background(PixelPalette.Surface, RoundedCornerShape(8.dp)).padding(6.dp)) {
            Text(quests, color = PixelPalette.Cyan, fontFamily = PixelTypography.LabelPixel.fontFamily, fontSize = 6.sp)
        }
    }
}

@Composable
fun PixelMoreTab(state: GameState, onPrestige: () -> Unit, onStats: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
        // prestige
        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color(0xFF2D1B69).copy(alpha = 0.6f)).padding(8.dp)) {
            Column {
                Text("♻️ PRESTİJ", color = Color(0xFFB388FF), fontFamily = PixelTypography.TitlePixel.fontFamily, fontSize = 9.sp, fontWeight = FontWeight.Black)
                Text(PixelPrestigeExpanded.nextTitle(state), color = Color.White, fontFamily = PixelTypography.LabelPixel.fontFamily, fontSize = 7.sp)
                Text(PixelPrestigeExpanded.stats(state), color = PixelPalette.Muted, fontFamily = PixelTypography.LabelPixel.fontFamily, fontSize = 6.sp)
                Spacer(Modifier.height(4.dp))
                Chip(
                    onClick = onPrestige,
                    label = { Text("PRESTİJ AĞACI", fontFamily = PixelTypography.LabelPixel.fontFamily, fontSize = 7.sp, color = Color.White) },
                    colors = ChipDefaults.chipColors(backgroundColor = Color(0xFF7C4DFF))
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        // liderlik
        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(PixelPalette.Surface).padding(8.dp)) {
            Column {
                Text(PixelLeaderboard.boardTitle(state), color = PixelPalette.Gold, fontFamily = PixelTypography.TitlePixel.fontFamily, fontSize = 8.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(4.dp))
                PixelLeaderboard.generateFor(state).take(5).forEachIndexed { idx, entry ->
                    Text(
                        "${PixelLeaderboard.rankIcon(idx)} ${entry.name}  ${entry.score}",
                        color = if (idx == 0) PixelPalette.Gold else Color.White,
                        fontFamily = PixelTypography.LabelPixel.fontFamily,
                        fontSize = 6.sp
                    )
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        // stats + challenges
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Chip(onClick = onStats, label = { Text("📊 İSTATİSTİK", fontFamily = PixelTypography.LabelPixel.fontFamily, fontSize = 6.sp, color = Color.White) }, colors = ChipDefaults.chipColors(backgroundColor = PixelPalette.Surface), modifier = Modifier.weight(1f))
            Chip(onClick = {}, label = { Text("🏆 GÖREV ${PixelChallenges.completed(state).size}/${PixelChallenges.all(state).size}", fontFamily = PixelTypography.LabelPixel.fontFamily, fontSize = 6.sp, color = Color.White) }, colors = ChipDefaults.chipColors(backgroundColor = PixelPalette.Surface), modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(6.dp))
        Text(PixelWatchIntegration.watchShort(), color = PixelPalette.Muted.copy(alpha = 0.6f), fontFamily = PixelTypography.LabelPixel.fontFamily, fontSize = 5.sp)
    }
}

// ============= PIXEL PROGRESS =============
@Composable
fun PixelProgressBar(progress: Float, color: Color, height: androidx.compose.ui.unit.Dp, modifier: Modifier = Modifier) {
    Box(modifier.clip(RoundedCornerShape(99.dp)).background(Color(0xFF2A2A2E)).height(height)) {
        Box(Modifier.fillMaxWidth(progress.coerceIn(0f, 1f)).fillMaxHeight().clip(RoundedCornerShape(99.dp)).background(color))
    }
}

// ============= GENİŞLETİLMİŞ LOG VE ANALİTİK =============
object PixelMainScreenLog {
    fun extended(state: GameState): String {
        val sb = StringBuilder()
        sb.appendLine("=== PIXEL MAIN SCREEN ===")
        sb.appendLine("Tabs: OYNA,MARKET,GÜNLÜK,DAHA")
        sb.appendLine("State: $state")
        sb.appendLine(PixelGameCore.allSystemsStatus(state))
        repeat(30) { i -> sb.appendLine("Main log ${i + 1}: ${i * 123}") }
        return sb.toString()
    }
}
