package com.scritchyscratchy.watch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ScritchyApp()
        }
    }
}

@Composable
fun ScritchyApp(vm: GameViewModel = viewModel()) {
    val context = LocalContext.current
    LaunchedEffect(Unit) { vm.attachContext(context) }
    val state by vm.state.collectAsState()
    var selectedTab by remember { mutableStateOf(0) } // 0 scratch, 1 shop, 2 upgrades, 3 prestige

    // Watch round theme - dark casino
    MaterialTheme(
        colors = Colors(
            primary = Color(0xFFFFD600),
            primaryVariant = Color(0xFFFFAB00),
            secondary = Color(0xFF00E676),
            secondaryVariant = Color(0xFF00C853),
            error = Color(0xFFFF5252),
            onPrimary = Color.Black,
            onSecondary = Color.Black,
            onError = Color.White,
            background = Color(0xFF121212),
            onBackground = Color.White,
            surface = Color(0xFF1E1E1E),
            onSurface = Color.White,
            onSurfaceVariant = Color(0xFFB0B0B0)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0A0A0A)),
            contentAlignment = Alignment.Center
        ) {
            // Round watch container - inset for Galaxy Watch 8 480x480
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(6.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF121212))
            ) {
                when (selectedTab) {
                    0 -> ScratchScreen(state, vm, onNavigate = { selectedTab = it })
                    1 -> ShopScreen(state, vm, onBack = { selectedTab = 0 })
                    2 -> UpgradesScreen(state, vm, onBack = { selectedTab = 0 })
                    3 -> PrestigeScreen(state, vm, onBack = { selectedTab = 0 })
                }

                // Bottom tab bar overlay for quick nav when not in scratch with active card? Always show small dots
                // Use PositionIndicator like Wear
            }

            // Top balance bar - always visible
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 10.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1F1F1F).copy(alpha = 0.92f))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "${state.balance} $",
                        color = if (state.balance < 0) Color(0xFFFF5252) else Color(0xFFFFD600),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    if (state.prestige.jackPoints > 0) {
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF6A1B9A))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text("${state.prestige.jackPoints} JP", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (state.upgrades.autoScratcherUnlocked) Text("🤖", fontSize = 8.sp)
                }
            }

            // Rotary hint bottom nav dots
            Row(
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (i in 0..3) {
                    Box(
                        Modifier
                            .size(if (selectedTab == i) 6.dp else 4.dp)
                            .clip(CircleShape)
                            .background(if (selectedTab == i) Color(0xFFFFD600) else Color.White.copy(alpha = 0.4f))
                    )
                }
            }
        }
    }
}

@Composable
fun ScratchScreen(state: GameState, vm: GameViewModel, onNavigate: (Int) -> Unit) {
    val context = LocalContext.current
    // intro: dish washing job if not completed and balance <10
    if (!state.hasCompletedDishJob && state.balance < 15 && state.totalScratched == 0) {
        DishJobIntro(state, vm)
        return
    }

    val listState = rememberScalingLazyListState()
    var scratchProgress by remember { mutableStateOf(0f) }
    var cardRevealed by remember(state.currentCard?.uid) { mutableStateOf(false) }

    ScalingLazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                "SCRITCHY SCRATCHY",
                color = Color(0xFFFFD600),
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                letterSpacing = 0.5.sp
            )
        }

        if (state.currentCard == null) {
            item {
                // No card - prompt to buy
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF1E1E1E))
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Kartın yok", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("Dükkandan bir kart al ve kazı!", color = Color.Gray, fontSize = 7.sp, textAlign = TextAlign.Center)
                    }
                }
            }
            item {
                Chip(
                    onClick = { onNavigate(1) },
                    label = { Text("🛒 DÜKKAN", fontSize = 9.sp, fontWeight = FontWeight.Bold) },
                    colors = ChipDefaults.chipColors(backgroundColor = Color(0xFF2E7D32)),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            // Quick buy row for cheapest
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                    CardCatalog.all.take(3).forEach { def ->
                        val lvl = state.cardLevels[def.id] ?: 1
                        CompactCardButton(def, lvl, enabled = state.balance >= def.cost) {
                            vm.buyCard(context, def)
                        }
                    }
                }
            }
            // history
            if (state.history.isNotEmpty()) {
                item {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF1E1E1E))
                            .padding(6.dp)
                    ) {
                        Text("SON OLAYLAR", color = Color.Gray, fontSize = 6.sp, fontWeight = FontWeight.Bold)
                        state.history.take(3).forEach {
                            Text("• $it", color = Color.White.copy(alpha = 0.85f), fontSize = 6.sp, maxLines = 1)
                        }
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Chip(onClick = { onNavigate(2) }, label = { Text("⬆️ Yükselt", fontSize = 7.sp) }, colors = ChipDefaults.chipColors(backgroundColor = Color(0xFF1565C0)), modifier = Modifier.weight(1f))
                    Chip(onClick = { onNavigate(3) }, label = { Text("♻️ Prestij", fontSize = 7.sp) }, colors = ChipDefaults.chipColors(backgroundColor = Color(0xFF6A1B9A)), modifier = Modifier.weight(1f))
                }
            }
            if (state.balance < 0) {
                item {
                    Chip(
                        onClick = { vm.takeLoan(context) },
                        label = { Text("💳 KREDİ AL +400$", fontSize = 8.sp, fontWeight = FontWeight.Bold) },
                        colors = ChipDefaults.chipColors(backgroundColor = Color(0xFFD32F2F)),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        } else {
            // Active card scratch area
            item {
                val card = state.currentCard!!
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    ScratchCardCanvas(
                        card = card,
                        scratchPower = state.upgrades.scratchPower,
                        areaSize = state.upgrades.areaSize,
                        onScratchProgress = { scratchProgress = it },
                        onRevealComplete = { cardRevealed = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(138.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                }
            }
            item {
                // progress bar
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFF2A2A2A))
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth(scratchProgress)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                when {
                                    cardRevealed -> Color(0xFF00E676)
                                    scratchProgress > 0.5 -> Color(0xFFFFD600)
                                    else -> Color(0xFF90CAF9)
                                }
                            )
                    )
                }
            }
            item {
                Text("${(scratchProgress * 100).toInt()}% kazındı • ${state.upgrades.scratchPowerLevel} güç • ${state.upgrades.areaSizeLevel} alan", color = Color.Gray, fontSize = 6.sp)
            }
            if (cardRevealed) {
                item {
                    Chip(
                        onClick = { vm.scratchDone(context, scratchProgress) },
                        label = { Text("💰 TOPLA", fontSize = 9.sp, fontWeight = FontWeight.Bold) },
                        colors = ChipDefaults.chipColors(backgroundColor = Color(0xFF00C853)),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (state.upgrades.trashCanUnlocked) {
                    item {
                        Chip(
                            onClick = { vm.trashCard(context) },
                            label = { Text("🗑️ ÇÖPE AT", fontSize = 8.sp) },
                            colors = ChipDefaults.chipColors(backgroundColor = Color(0xFF424242)),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            } else {
                item {
                    Text("Parmağınla / bezel ile kazı!", color = Color(0xFFFFD600), fontSize = 7.sp, fontWeight = FontWeight.Bold)
                }
                if (state.upgrades.trashCanUnlocked) {
                    item {
                        Chip(
                            onClick = { vm.trashCard(context) },
                            label = { Text("🗑️ Vazgeç (çöp)", fontSize = 7.sp) },
                            colors = ChipDefaults.chipColors(backgroundColor = Color(0xFF424242)),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            item {
                // also show balance under
                Text("Bakiye: ${state.balance}$ • Toplam kazanç: ${state.totalWon}$", color = Color.Gray, fontSize = 6.sp)
            }
        }
    }
}

@Composable
private fun CompactCardButton(def: CardDefinition, level: Int, enabled: Boolean, onClick: () -> Unit) {
    Chip(
        onClick = onClick,
        enabled = enabled,
        label = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(def.icon, fontSize = 10.sp)
                Text(def.nameTr, fontSize = 6.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                Text("${def.cost}$ Lv$level", fontSize = 6.sp, color = if (enabled) Color(0xFF69F0AE) else Color.Gray)
            }
        },
        colors = ChipDefaults.chipColors(backgroundColor = Color(0xFF212121), contentColor = Color.White),
        modifier = Modifier.width(52.dp)
    )
}

@Composable
fun ShopScreen(state: GameState, vm: GameViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp, vertical = 30.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text("🛒 DÜKKAN", color = Color(0xFFFFD600), fontSize = 10.sp, fontWeight = FontWeight.Black)
        }
        item {
            Chip(onClick = onBack, label = { Text("← Geri", fontSize = 8.sp) }, colors = ChipDefaults.chipColors(backgroundColor = Color(0xFF424242)), modifier = Modifier.fillMaxWidth())
        }
        items(CardCatalog.all.size) { idx ->
            val def = CardCatalog.all[idx]
            val lvl = state.cardLevels[def.id] ?: 1
            val canBuy = state.balance >= def.cost
            val cardGenPreview = CardGenerator.generate(def, lvl, state.upgrades.luckBonus, state.prestige) // preview not accurate but ok
            Chip(
                onClick = { if (canBuy) vm.buyCard(context, def) },
                enabled = canBuy,
                label = {
                    Column(Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                            Text(def.icon, fontSize = 14.sp)
                            Column(Modifier.weight(1f)) {
                                Text(def.nameTr, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text(def.descriptionTr, color = Color.Gray, fontSize = 6.sp, maxLines = 1)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("${def.cost}$", color = if (canBuy) Color(0xFF69F0AE) else Color(0xFFFF5252), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text("Lv$lvl", color = Color(0xFFFFD600), fontSize = 7.sp)
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Ödül ${def.basePayout}$", color = Color.White.copy(alpha=0.7f), fontSize = 6.sp)
                            Text("Jackpot ${def.jackpotPayout}$", color = Color(0xFFFFD600), fontSize = 6.sp, fontWeight = FontWeight.Bold)
                            if(def.hasPenalty) Text("⚠️ Ceza var", color = Color(0xFFFF5252), fontSize = 6.sp)
                        }
                    }
                },
                colors = ChipDefaults.chipColors(backgroundColor = Color(0xFF1E1E1E)),
                modifier = Modifier.fillMaxWidth()
            )
        }
        item { Text("Seviye arttıkça ödül +25%", color = Color.Gray, fontSize = 6.sp) }
    }
}

@Composable
fun UpgradesScreen(state: GameState, vm: GameViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp, vertical = 30.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item { Text("⬆️ YÜKSELTME", color = Color(0xFF42A5F5), fontSize = 10.sp, fontWeight = FontWeight.Black) }
        item { Chip(onClick = onBack, label = { Text("← Geri", fontSize = 8.sp) }, colors = ChipDefaults.chipColors(backgroundColor = Color(0xFF424242)), modifier = Modifier.fillMaxWidth()) }
        item {
            UpgradeRow("🍀 Şans", "Kazanma +4%", state.upgrades.luckLevel, UpgradePricing.luckCost(state.upgrades.luckLevel), state.balance, onBuy = { vm.upgradeLuck(context) })
        }
        item {
            UpgradeRow("💪 Kazı Gücü", "Daha hızlı kazı", state.upgrades.scratchPowerLevel, UpgradePricing.powerCost(state.upgrades.scratchPowerLevel), state.balance, onBuy = { vm.upgradePower(context) })
        }
        item {
            UpgradeRow("📐 Alan", "Daha geniş fırça", state.upgrades.areaSizeLevel, UpgradePricing.areaCost(state.upgrades.areaSizeLevel), state.balance, onBuy = { vm.upgradeArea(context) })
        }
        item {
            // Gadgets
            Text("GADGET'LAR", color = Color.Gray, fontSize = 7.sp, fontWeight = FontWeight.Bold)
        }
        item {
            Chip(
                onClick = { vm.buyAuto(context) },
                enabled = !state.upgrades.autoScratcherUnlocked && state.balance >= UpgradePricing.autoCost(),
                label = {
                    Column {
                        Text("🤖 Scratch Bot", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(if(state.upgrades.autoScratcherUnlocked) "Aktif - otomatik kazıyor" else "Oto-kazı - 500$", fontSize = 7.sp, color = Color.Gray)
                    }
                },
                colors = ChipDefaults.chipColors(backgroundColor = if(state.upgrades.autoScratcherUnlocked) Color(0xFF2E7D32) else Color(0xFF1E1E1E)),
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Chip(
                onClick = { vm.buyTrash(context) },
                enabled = !state.upgrades.trashCanUnlocked && state.balance >= UpgradePricing.trashCost(),
                label = {
                    Column {
                        Text("🗑️ Çöp Kutusu", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(if(state.upgrades.trashCanUnlocked) "Kötü kartı at" else "Kötü kartı iptal - 300$", fontSize = 7.sp, color = Color.Gray)
                    }
                },
                colors = ChipDefaults.chipColors(backgroundColor = if(state.upgrades.trashCanUnlocked) Color(0xFF2E7D32) else Color(0xFF1E1E1E)),
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            // stats
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Color(0xFF1E1E1E)).padding(8.dp)) {
                Text("İSTATİSTİK", color = Color.Gray, fontSize = 6.sp, fontWeight = FontWeight.Bold)
                Text("Kazınan: ${state.totalScratched} • Jackpot: ${state.totalJackpots}", color = Color.White, fontSize = 7.sp)
                Text("Toplam kazanç: ${state.totalWon}$", color = Color(0xFF69F0AE), fontSize = 7.sp)
                Text("Prestij: #${state.prestige.prestigeCount} • JP: ${state.prestige.jackPoints}", color = Color(0xFFCE93D8), fontSize = 7.sp)
            }
        }
        // debug hidden
        item {
            Chip(onClick = { vm.addMoney(context, 500) }, label = { Text("+500$ (test)", fontSize = 7.sp) }, colors = ChipDefaults.chipColors(backgroundColor = Color(0xFF37474F)), modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun UpgradeRow(title: String, desc: String, level: Int, cost: Int, balance: Long, onBuy: () -> Boolean) {
    val canBuy = balance >= cost && level < 10
    Chip(
        onClick = { onBuy() },
        enabled = canBuy || level >= 10,
        label = {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Text("$title Lv$level/10", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(desc, fontSize = 6.sp, color = Color.Gray)
                }
                if(level >= 10) Text("MAX", color = Color(0xFF69F0AE), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                else Text("${cost}$", color = if(canBuy) Color(0xFF69F0AE) else Color(0xFFFF5252), fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
        },
        colors = ChipDefaults.chipColors(backgroundColor = Color(0xFF1E1E1E)),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun PrestigeScreen(state: GameState, vm: GameViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp, vertical = 30.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item { Text("♻️ PRESTİJ", color = Color(0xFFCE93D8), fontSize = 10.sp, fontWeight = FontWeight.Black) }
        item { Chip(onClick = onBack, label = { Text("← Geri", fontSize = 8.sp) }, colors = ChipDefaults.chipColors(backgroundColor = Color(0xFF424242)), modifier = Modifier.fillMaxWidth()) }
        item {
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFF2A1B3D)).padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Jack Points: ${state.prestige.jackPoints} JP", color = Color(0xFFFFD600), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                Text("Prestij #${state.prestige.prestigeCount} • Toplam ${state.prestige.totalJackPointsEarned} JP", color = Color.White.copy(alpha=0.7f), fontSize = 7.sp)
                Text("Sıfırla ve kalıcı bonus kazan!", color = Color.Gray, fontSize = 7.sp, textAlign = TextAlign.Center)
            }
        }
        item {
            Chip(
                onClick = { vm.doPrestige(context) },
                label = { Text("🔄 PRESTİJ YAP (+${(state.totalJackpots*2).coerceAtLeast(5)} JP)", fontSize = 8.sp, fontWeight = FontWeight.Bold) },
                colors = ChipDefaults.chipColors(backgroundColor = Color(0xFF6A1B9A)),
                modifier = Modifier.fillMaxWidth()
            )
        }
        item { Text("KALICI AĞAÇ", color = Color.Gray, fontSize = 7.sp, fontWeight = FontWeight.Bold) }
        // prestige upgrades list
        val ups = listOf(
            Triple("headStart", "💵 Başlangıç Sermayesi", "Her koşuya 500$ ile başla"),
            Triple("luckyLegacy", "🍀 Şanslı Miras", "+15% kalıcı şans"),
            Triple("scratchMemory", "🤖 Kazı Hafızası", "Oto-kazı baştan açık"),
            Triple("goldenHands", "✨ Altın Eller", "Tüm ödüller +25%"),
            Triple("jackpotMagnet", "🧲 Jackpot Mıknatısı", "Jackpot 2x sıklık"),
            Triple("speedDemon", "⚡ Hız Canavarı", "Kazı Gücü Lv2 başla"),
            Triple("banco", "🏦 Banco", "İflas koruması"),
            Triple("recyclerPro", "♻️ Geri Dönüşüm Pro", "Çöp %10 iade"),
        )
        items(ups.size) { i ->
            val (id, title, desc) = ups[i]
            val cost = UpgradePricing.prestigeCost(id)
            val owned = when(id) {
                "headStart" -> state.prestige.upgrades.headStartCapital
                "luckyLegacy" -> state.prestige.upgrades.luckyLegacy
                "scratchMemory" -> state.prestige.upgrades.scratchMemory
                "goldenHands" -> state.prestige.upgrades.goldenHands
                "jackpotMagnet" -> state.prestige.upgrades.jackpotMagnet
                "speedDemon" -> state.prestige.upgrades.speedDemon
                "banco" -> state.prestige.upgrades.banco
                "recyclerPro" -> state.prestige.upgrades.recyclerPro
                else -> false
            }
            Chip(
                onClick = { if(!owned) vm.buyPrestigeUpgrade(context, id) },
                enabled = !owned && state.prestige.jackPoints >= cost,
                label = {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) {
                            Text(title, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = if(owned) Color(0xFF69F0AE) else Color.White)
                            Text(desc, fontSize = 6.sp, color = Color.Gray)
                        }
                        if(owned) Text("✓", color = Color(0xFF69F0AE), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        else Text("${cost} JP", color = if(state.prestige.jackPoints >= cost) Color(0xFFFFD600) else Color(0xFFFF5252), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                },
                colors = ChipDefaults.chipColors(backgroundColor = if(owned) Color(0xFF1B5E20) else Color(0xFF1E1E1E)),
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Chip(onClick = { vm.resetAll(context) }, label = { Text("⚠️ Tüm ilerlemeyi sıfırla", fontSize = 7.sp, color = Color(0xFFFF5252)) }, colors = ChipDefaults.chipColors(backgroundColor = Color(0xFF1E1E1E)), modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun DishJobIntro(state: GameState, vm: GameViewModel) {
    val context = LocalContext.current
    var plates by remember { mutableStateOf(0) }
    var dirty by remember { mutableStateOf(1f) } // 1 = dirty
    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 30.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text("🍽️ BULAŞIKÇI İŞİ", color = Color(0xFFFFD600), fontSize = 10.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
        }
        item {
            Text("Parasızsın! Önce bulaşık yıka. Her tabak 1$. 5$ biriktirip ilk kartını al.", color = Color.Gray, fontSize = 7.sp, textAlign = TextAlign.Center)
        }
        item {
            // interactive plate
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF5F5F5))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    drawCircle(Color.White, radius = size.minDimension/2)
                    // dirty spots if dirty > 0
                    if(dirty > 0.1f) {
                        drawCircle(Color(0xFF8D6E63).copy(alpha = 0.5f * dirty), radius = size.minDimension*0.3f*dirty, center = center)
                        drawCircle(Color(0xFFFFCC80).copy(alpha = 0.6f*dirty), radius = size.minDimension*0.15f*dirty, center = center + androidx.compose.ui.geometry.Offset(10f, -10f))
                    }
                }
                if(dirty < 0.15f) Text("✨", fontSize = 24.sp)
                else Text("🍝", fontSize = 18.sp)
            }
        }
        item {
            Chip(
                onClick = {
                    // wash gesture: reduce dirty
                    dirty -= 0.25f
                    if(dirty <= 0f) {
                        // completed one plate
                        plates++
                        vm.completeDishWashing(context)
                        dirty = 1f
                        // haptic
                    }
                },
                label = { Text(if(dirty<0.15f) "✨ Tabak temiz! +1$" else "🧽 OV / SÜRÜKLE - YIKA", fontSize = 8.sp, fontWeight = FontWeight.Bold) },
                colors = ChipDefaults.chipColors(backgroundColor = Color(0xFF2E7D32)),
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Text("Bakiye: ${state.balance}$ • Yıkanan: $plates", color = Color.White, fontSize = 8.sp)
        }
        item {
            // progress to 5
            val prog = (state.balance.toFloat() / 5f).coerceIn(0f,1f)
            Box(Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(Color(0xFF2A2A2A))) {
                Box(Modifier.fillMaxWidth(prog).fillMaxHeight().clip(RoundedCornerShape(3.dp)).background(Color(0xFF69F0AE)))
            }
        }
        if(state.balance >= 5) {
            item {
                Text("Hazırsın! Dükkana git.", color = Color(0xFF69F0AE), fontSize = 8.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
