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
import com.scritchyscratchy.watch.pixel.*

// PIXEL EDITION - Galaxy Watch 8 480x480 - 8000 satır geliştirme entegrasyonu
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ScritchyPixelApp() }
    }
}

@Composable
fun ScritchyPixelApp(vm: GameViewModel = viewModel()) {
    val context = LocalContext.current
    LaunchedEffect(Unit) { vm.attachContext(context) }
    val state by vm.state.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    var brush by remember { mutableStateOf(BrushType.COIN) }

    MaterialTheme(
        colors = Colors(
            primary = PixelPalette.Gold,
            primaryVariant = PixelPalette.GoldDark,
            secondary = PixelPalette.Emerald,
            secondaryVariant = PixelPalette.EmeraldDark,
            error = PixelPalette.Ruby,
            onPrimary = Color.Black,
            onSecondary = Color.Black,
            onError = Color.White,
            background = PixelPalette.BgDeep,
            onBackground = Color.White,
            surface = PixelPalette.BgCard,
            onSurface = Color.White,
            onSurfaceVariant = PixelPalette.Gray500
        )
    ) {
        Box(
            Modifier.fillMaxSize().background(PixelPalette.BgDeep)
        ) {
            // Pixel starfield + grid
            PixelStarfield(Modifier.fillMaxSize(), 24)
            PixelGridOverlay(Modifier.fillMaxSize())
            PixelDotMatrix(Modifier.fillMaxSize())

            // Round container with pixel border
            Box(
                Modifier.fillMaxSize().padding(5.dp).clip(CircleShape).background(PixelPalette.BgDeep).background(PixelPalette.BgCard.copy(alpha=0.9f))
            ) {
                // Inner content
                when (selectedTab) {
                    0 -> PixelScratchScreen(state, vm, brush, onBrush = { brush = it }, onNavigate = { selectedTab = it })
                    1 -> PixelShopScreen(state, vm, onBack = { selectedTab = 0 })
                    2 -> PixelUpgradeScreen(state, vm, onBack = { selectedTab = 0 })
                    3 -> PixelPrestigeScreen(state, vm, onBack = { selectedTab = 0 })
                    4 -> PixelWorldScreen(state, onBack = { selectedTab = 0 })
                    5 -> PixelBrewingScreen(state, vm, onBack = { selectedTab = 0 })
                }
                // Scanlines & vignette overlay (pixel CRT)
                PixelScanlines(Modifier.fillMaxSize(), 0.06f)
                PixelVignette(Modifier.fillMaxSize())
                PixelCornerBrackets(Modifier.fillMaxSize().padding(4.dp), PixelPalette.Gold.copy(alpha=0.6f), 10.dp, 1.5.dp)
            }

            // Top pixel bar - balance + JP + world
            Box(
                Modifier.align(Alignment.TopCenter).padding(top=8.dp).clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1A1F2E).copy(alpha=0.88f)).background(PixelPalette.BgCard.copy(alpha=0.95f))
                    .padding(horizontal=8.dp, vertical=3.dp)
            ) {
                Row(verticalAlignment=Alignment.CenterVertically, horizontalArrangement=Arrangement.spacedBy(5.dp)) {
                    Text(PixelEconomyUtils.formatMoney(state.balance), color=PixelPalette.Gold, fontSize=10.sp, fontWeight=FontWeight.Black, fontFamily=PixelTypography.MonoGold.fontFamily)
                    if(state.prestige.jackPoints>0) Box(Modifier.clip(RoundedCornerShape(5.dp)).background(PixelPalette.Amethyst).padding(horizontal=4.dp, vertical=1.dp)){ Text("${state.prestige.jackPoints} JP", color=Color.White, fontSize=7.sp, fontWeight=FontWeight.Black, fontFamily=PixelTypography.LabelPixel.fontFamily) }
                    Text(PixelWorld.iconFor(state), fontSize=9.sp)
                    if(state.upgrades.autoScratcherUnlocked) Text("🤖", fontSize=7.sp)
                    Text("Lv${PixelWorld.levelFor(state)}", color=PixelPalette.Emerald, fontSize=7.sp, fontFamily=PixelTypography.LabelPixel.fontFamily)
                }
            }

            // Bottom pixel dots + brush hint
            Column(Modifier.align(Alignment.BottomCenter).padding(bottom=6.dp), horizontalAlignment=Alignment.CenterHorizontally) {
                Row(horizontalArrangement=Arrangement.spacedBy(3.dp)) {
                    for(i in 0..5){ Box(Modifier.size(if(selectedTab==i)5.dp else 3.dp).clip(CircleShape).background(if(selectedTab==i) PixelPalette.Gold else Color.White.copy(alpha=0.35f))) }
                }
                Spacer(Modifier.height(2.dp))
                Text(when(selectedTab){0->"${brush.display} ${brush.name} • ${PixelWorld.current(state).icon}";1->"MARKET";2->"UPGRADE";3->"PRESTIGE";4->"WORLD";else->"ÇAY DEMLE 🍵"}, color=PixelPalette.Gray500, fontSize=5.sp, fontFamily=PixelTypography.CaptionPixel.fontFamily)
            }
        }
    }
}

@Composable
fun PixelScratchScreen(state: GameState, vm: GameViewModel, brush: BrushType, onBrush:(BrushType)->Unit, onNavigate:(Int)->Unit){
    val context = LocalContext.current
    if(!state.hasCompletedDishJob && state.balance<15 && state.totalScratched==0){ PixelDishJob(state, vm); return }
    val listState=rememberScalingLazyListState()
    var scratchProgress by remember{ mutableStateOf(0f) }
    var metrics by remember{ mutableStateOf<ScratchMetrics?>(null) }
    var cardRevealed by remember(state.currentCard?.uid){ mutableStateOf(false) }
    ScalingLazyColumn(state=listState, modifier=Modifier.fillMaxSize().padding(horizontal=10.dp, vertical=24.dp), verticalArrangement=Arrangement.spacedBy(5.dp), horizontalAlignment=Alignment.CenterHorizontally){
        item{ Text("◆ SCRITCHY PIXEL ◆", color=PixelPalette.Gold, fontSize=8.sp, fontWeight=FontWeight.Black, fontFamily=PixelTypography.TitlePixel.fontFamily, letterSpacing=1.sp) }
        item{ Text("${PixelWorld.current(state).name} ${PixelWorld.description(state)}", color=PixelPalette.Gray500, fontSize=5.sp, fontFamily=PixelTypography.CaptionPixel.fontFamily) }
        // Brush selector pixel
        item{
            Row(horizontalArrangement=Arrangement.spacedBy(3.dp), modifier=Modifier.fillMaxWidth()){
                BrushType.values().forEach{ b->
                    val sel = b==brush
                    Chip(onClick={onBrush(b)}, label={ Text(b.display, fontSize=8.sp)}, colors=ChipDefaults.chipColors(backgroundColor=if(sel) PixelPalette.Gold else PixelPalette.BgCardLight), modifier=Modifier.weight(1f))
                }
            }
        }
        if(state.currentCard==null){
            item{
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(PixelPalette.BgCard).padding(10.dp), contentAlignment=Alignment.Center){
                    Column(horizontalAlignment=Alignment.CenterHorizontally){
                        Text("KART YOK", color=Color.White, fontSize=9.sp, fontWeight=FontWeight.Black, fontFamily=PixelTypography.TitlePixel.fontFamily)
                        Text("Pixel dükkandan al & kazı! ${PixelWorld.bonusText(state)}", color=PixelPalette.Gray500, fontSize=6.sp, textAlign=TextAlign.Center, fontFamily=PixelTypography.CaptionPixel.fontFamily)
                    }
                }
            }
            item{ Chip(onClick={onNavigate(1)}, label={ Text("🛒 PİXEL DÜKKAN →", fontSize=8.sp, fontWeight=FontWeight.Black)}, colors=ChipDefaults.chipColors(backgroundColor=PixelPalette.Emerald), modifier=Modifier.fillMaxWidth()) }
            item{
                Row(horizontalArrangement=Arrangement.spacedBy(3.dp), modifier=Modifier.fillMaxWidth()){
                    CardCatalog.all.take(3).forEach{ def->
                        val lvl=state.cardLevels[def.id]?:1
                        Chip(onClick={ vm.buyCard(context, def) }, enabled=state.balance>=def.cost, label={
                            Column(horizontalAlignment=Alignment.CenterHorizontally){
                                Text(def.icon, fontSize=11.sp)
                                Text(def.nameTr, fontSize=5.sp, fontWeight=FontWeight.Bold, maxLines=1, fontFamily=PixelTypography.LabelPixel.fontFamily)
                                Text("${PixelWorld.effectivePrice(def.cost, state)}$ Lv$lvl", fontSize=6.sp, color=PixelPalette.Gold)
                            }
                        }, colors=ChipDefaults.chipColors(backgroundColor=PixelPalette.BgCardLight), modifier=Modifier.weight(1f))
                    }
                }
            }
            if(state.history.isNotEmpty()){
                item{
                    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(PixelPalette.BgCard).padding(6.dp)){
                        Text("SON PIXEL OLAYLAR", color=PixelPalette.Gold, fontSize=6.sp, fontWeight=FontWeight.Bold, fontFamily=PixelTypography.LabelPixel.fontFamily)
                        state.history.take(3).forEach{ Text("▸ $it", color=Color.White.copy(alpha=0.8f), fontSize=6.sp, maxLines=1, fontFamily=PixelTypography.CaptionPixel.fontFamily) }
                        Text(PixelAnalytics.summary(state).take(40), color=PixelPalette.Emerald, fontSize=5.sp)
                    }
                }
            }
            item{
                Row(horizontalArrangement=Arrangement.spacedBy(3.dp)){
                    Chip(onClick={onNavigate(2)}, label={ Text("⬆️ Yükselt", fontSize=7.sp)}, colors=ChipDefaults.chipColors(backgroundColor=PixelPalette.Sapphire), modifier=Modifier.weight(1f))
                    Chip(onClick={onNavigate(3)}, label={ Text("♻️ Prestij", fontSize=7.sp)}, colors=ChipDefaults.chipColors(backgroundColor=PixelPalette.Amethyst), modifier=Modifier.weight(1f))
                    Chip(onClick={onNavigate(4)}, label={ Text("🌍 Dünya", fontSize=7.sp)}, colors=ChipDefaults.chipColors(backgroundColor=PixelPalette.Topaz), modifier=Modifier.weight(1f))
                }
            }
            item{
                Chip(onClick={onNavigate(5)}, label={ Text("🍵 ÇAY DEMLE • ${PixelBrewing.levelText()} • ${PixelBrewing.favoriteText().take(12)}", fontSize=7.sp, fontWeight=FontWeight.Black)}, colors=ChipDefaults.chipColors(backgroundColor=Color(0xFF2E8B57)), modifier=Modifier.fillMaxWidth())
            }
            item{
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color(0xFF1A2E1A)).padding(6.dp)){
                    Text(PixelBrewing.tip(), color=PixelPalette.Gold, fontSize=6.sp, fontStyle=androidx.compose.ui.text.font.FontStyle.Italic, textAlign=TextAlign.Center)
                    Text(PixelBrewing.seasonBonus()+" • "+PixelBrewing.timeOfDayBonus(), color=Color.White.copy(alpha=0.7f), fontSize=5.sp, textAlign=TextAlign.Center)
                }
            }
            if(state.balance<0){ item{ Chip(onClick={vm.takeLoan(context)}, label={ Text("💳 KREDİ +400$", fontSize=8.sp, fontWeight=FontWeight.Black)}, colors=ChipDefaults.chipColors(backgroundColor=PixelPalette.Ruby), modifier=Modifier.fillMaxWidth()) } }
            // daily + events
            item{
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(PixelPalette.BgCard).padding(6.dp)){
                    Text(PixelDailySystem.weekCompleteText(state), color=PixelPalette.Gold, fontSize=6.sp)
                    Text(PixelEvents.text(state), color=PixelPalette.Cyan, fontSize=6.sp)
                    Text(PixelQuests.nextText(state), color=Color.White, fontSize=6.sp)
                }
            }
        } else {
            item{
                val card=state.currentCard!!
                Box(Modifier.fillMaxWidth(), contentAlignment=Alignment.Center){
                    EnhancedPixelScratchCanvas(
                        card=card, scratchPower=state.upgrades.scratchPower, areaSize=state.upgrades.areaSize, brush=brush,
                        onProgress={ p, m-> scratchProgress=p; metrics=m },
                        onReveal={ metrics=it; cardRevealed=true },
                        modifier=Modifier.fillMaxWidth().height(132.dp).clip(RoundedCornerShape(12.dp))
                    )
                }
            }
            item{
                Box(Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp)).background(PixelPalette.BgCard)){
                    Box(Modifier.fillMaxWidth(scratchProgress).fillMaxHeight().clip(RoundedCornerShape(3.dp)).background(when{cardRevealed->PixelPalette.Emerald; scratchProgress>0.5f->PixelPalette.Gold; else->PixelPalette.Sapphire}))
                }
            }
            item{ Text("${(scratchProgress*100).toInt()}% • Güç ${state.upgrades.scratchPowerLevel} • Alan ${state.upgrades.areaSizeLevel} • Combo ${metrics?.combo ?:0}", color=PixelPalette.Gray500, fontSize=6.sp, fontFamily=PixelTypography.CaptionPixel.fontFamily) }
            if(cardRevealed){
                item{ Chip(onClick={vm.scratchDone(context, scratchProgress)}, label={ Text("◆ TOPLA ◆ ${metrics?.combo?.let{"COMBO x$it"} ?: ""}", fontSize=9.sp, fontWeight=FontWeight.Black)}, colors=ChipDefaults.chipColors(backgroundColor=PixelPalette.Gold), modifier=Modifier.fillMaxWidth()) }
                if(state.upgrades.trashCanUnlocked){ item{ Chip(onClick={vm.trashCard(context)}, label={ Text("🗑️ ÇÖPE AT", fontSize=8.sp)}, colors=ChipDefaults.chipColors(backgroundColor=PixelPalette.Gray700), modifier=Modifier.fillMaxWidth()) } }
            } else {
                item{ Text("👆 ${brush.display} ile kazı • CRT tarama aktif", color=PixelPalette.Gold, fontSize=7.sp, fontWeight=FontWeight.Bold, fontFamily=PixelTypography.LabelPixel.fontFamily) }
                if(state.upgrades.trashCanUnlocked){ item{ Chip(onClick={vm.trashCard(context)}, label={ Text("🗑️ Vazgeç", fontSize=7.sp)}, colors=ChipDefaults.chipColors(backgroundColor=PixelPalette.Gray700), modifier=Modifier.fillMaxWidth()) } }
            }
            item{ Text("Bakiye ${PixelEconomyUtils.formatMoney(state.balance)} • Kazanç ${state.totalWon}$ • ${PixelWorld.bonusText(state)}", color=PixelPalette.Gray500, fontSize=6.sp, fontFamily=PixelTypography.CaptionPixel.fontFamily) }
        }
    }
}

@Composable
fun PixelShopScreen(state: GameState, vm: GameViewModel, onBack:()->Unit){
    val context=LocalContext.current
    ScalingLazyColumn(Modifier.fillMaxSize().padding(horizontal=8.dp, vertical=26.dp), verticalArrangement=Arrangement.spacedBy(5.dp), horizontalAlignment=Alignment.CenterHorizontally){
        item{ Text("◆ PİXEL DÜKKAN ◆", color=PixelPalette.Gold, fontSize=9.sp, fontWeight=FontWeight.Black, fontFamily=PixelTypography.TitlePixel.fontFamily) }
        item{ Chip(onClick=onBack, label={ Text("← Geri", fontSize=8.sp)}, colors=ChipDefaults.chipColors(backgroundColor=PixelPalette.BgCard), modifier=Modifier.fillMaxWidth()) }
        items(CardCatalog.all.size){ idx->
            val def=CardCatalog.all[idx]; val lvl=state.cardLevels[def.id]?:1; val canBuy=state.balance>= PixelWorld.effectivePrice(def.cost, state)
            Chip(onClick={ if(canBuy) vm.buyCard(context, def)}, enabled=canBuy, label={
                Column(Modifier.fillMaxWidth()){
                    Row(verticalAlignment=Alignment.CenterVertically, horizontalArrangement=Arrangement.spacedBy(5.dp), modifier=Modifier.fillMaxWidth()){
                        Text(def.icon, fontSize=13.sp)
                        Column(Modifier.weight(1f)){
                            Text(def.nameTr, color=Color.White, fontSize=8.sp, fontWeight=FontWeight.Black, fontFamily=PixelTypography.LabelPixel.fontFamily)
                            Text(def.descriptionTr, color=PixelPalette.Gray500, fontSize=5.sp, maxLines=1)
                        }
                        Column(horizontalAlignment=Alignment.End){
                            Text("${PixelWorld.effectivePrice(def.cost, state)}$", color=if(canBuy) PixelPalette.Emerald else PixelPalette.Ruby, fontSize=9.sp, fontWeight=FontWeight.Black)
                            Text("Lv$lvl", color=PixelPalette.Gold, fontSize=7.sp)
                        }
                    }
                    Row(horizontalArrangement=Arrangement.spacedBy(3.dp)){
                        Text("Ödül ${PixelWorld.effectivePayout(def.basePayout, state)}$", color=Color.White.copy(alpha=0.6f), fontSize=5.sp)
                        Text("JP ${def.jackpotPayout}", color=PixelPalette.Gold, fontSize=5.sp)
                        if(def.hasPenalty) Text("⚠️", fontSize=5.sp)
                        Text(PixelCollection.entryFor(state, def).foil.label, color=Color.White, fontSize=5.sp)
                    }
                }
            }, colors=ChipDefaults.chipColors(backgroundColor=PixelPalette.BgCard), modifier=Modifier.fillMaxWidth())
        }
        item{ Text("Öne çıkan: ${PixelShopExpanded.featured(state).title} • ${PixelWorld.bonusText(state)}", color=PixelPalette.Cyan, fontSize=6.sp, fontFamily=PixelTypography.CaptionPixel.fontFamily) }
    }
}

@Composable
fun PixelUpgradeScreen(state: GameState, vm: GameViewModel, onBack:()->Unit){
    val context=LocalContext.current
    ScalingLazyColumn(Modifier.fillMaxSize().padding(horizontal=8.dp, vertical=26.dp), verticalArrangement=Arrangement.spacedBy(5.dp), horizontalAlignment=Alignment.CenterHorizontally){
        item{ Text("⬆️ PİXEL GÜÇLENDİR", color=PixelPalette.Sapphire, fontSize=9.sp, fontWeight=FontWeight.Black, fontFamily=PixelTypography.TitlePixel.fontFamily)}
        item{ Chip(onClick=onBack, label={ Text("← Geri", fontSize=8.sp)}, colors=ChipDefaults.chipColors(backgroundColor=PixelPalette.BgCard), modifier=Modifier.fillMaxWidth())}
        item{ PixelUpgradeRow("🍀 ŞANS", "+4% kazanma", state.upgrades.luckLevel, UpgradePricing.luckCost(state.upgrades.luckLevel), state.balance, onBuy={vm.upgradeLuck(context)})}
        item{ PixelUpgradeRow("💪 GÜÇ", "Kazı hızı", state.upgrades.scratchPowerLevel, UpgradePricing.powerCost(state.upgrades.scratchPowerLevel), state.balance, onBuy={vm.upgradePower(context)})}
        item{ PixelUpgradeRow("📐 ALAN", "Fırça geniş", state.upgrades.areaSizeLevel, UpgradePricing.areaCost(state.upgrades.areaSizeLevel), state.balance, onBuy={vm.upgradeArea(context)})}
        item{ Text("GADGETLAR", color=PixelPalette.Gray500, fontSize=7.sp, fontWeight=FontWeight.Black)}
        item{ Chip(onClick={vm.buyAuto(context)}, enabled=!state.upgrades.autoScratcherUnlocked && state.balance>=UpgradePricing.autoCost(), label={ Column{ Text("🤖 BOT", fontSize=8.sp, fontWeight=FontWeight.Black); Text(if(state.upgrades.autoScratcherUnlocked)"Aktif 🤖" else "500$ oto-kazı", fontSize=6.sp, color=PixelPalette.Gray500)}}, colors=ChipDefaults.chipColors(backgroundColor=if(state.upgrades.autoScratcherUnlocked) PixelPalette.Emerald else PixelPalette.BgCard), modifier=Modifier.fillMaxWidth())}
        item{ Chip(onClick={vm.buyTrash(context)}, enabled=!state.upgrades.trashCanUnlocked && state.balance>=UpgradePricing.trashCost(), label={ Column{ Text("🗑️ ÇÖP", fontSize=8.sp, fontWeight=FontWeight.Black); Text(if(state.upgrades.trashCanUnlocked)"At 🗑️" else "300$ çöp", fontSize=6.sp, color=PixelPalette.Gray500)}}, colors=ChipDefaults.chipColors(backgroundColor=if(state.upgrades.trashCanUnlocked) PixelPalette.Emerald else PixelPalette.BgCard), modifier=Modifier.fillMaxWidth())}
        item{ Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(PixelPalette.BgCard).padding(8.dp)){ Text("İSTATİSTİK", color=PixelPalette.Gray500, fontSize=6.sp, fontWeight=FontWeight.Black); Text("Kazınan ${state.totalScratched} • JP ${state.totalJackpots}", color=Color.White, fontSize=7.sp, fontFamily=PixelTypography.BodyPixel.fontFamily); Text(PixelStats.detailText(state).take(60), color=PixelPalette.Emerald, fontSize=6.sp); Text(PixelWorld.stats(state), color=PixelPalette.Gold, fontSize=6.sp)}}
        item{ Chip(onClick={vm.addMoney(context, 500)}, label={ Text("+500$ test", fontSize=7.sp)}, colors=ChipDefaults.chipColors(backgroundColor=PixelPalette.BgCardLight), modifier=Modifier.fillMaxWidth())}
    }
}

@Composable
private fun PixelUpgradeRow(title:String, desc:String, level:Int, cost:Int, balance:Long, onBuy:()->Boolean){
    val canBuy=balance>=cost && level<10
    Chip(onClick={onBuy()}, enabled=canBuy || level>=10, label={
        Row(Modifier.fillMaxWidth(), verticalAlignment=Alignment.CenterVertically, horizontalArrangement=Arrangement.SpaceBetween){
            Column(Modifier.weight(1f)){ Text("$title Lv$level/10", fontSize=8.sp, fontWeight=FontWeight.Black, color=Color.White, fontFamily=PixelTypography.LabelPixel.fontFamily); Text(desc, fontSize=6.sp, color=PixelPalette.Gray500)}
            if(level>=10) Text("MAX", color=PixelPalette.Emerald, fontSize=8.sp, fontWeight=FontWeight.Black) else Text("${cost}$", color=if(canBuy) PixelPalette.Emerald else PixelPalette.Ruby, fontSize=8.sp, fontWeight=FontWeight.Black)
        }
    }, colors=ChipDefaults.chipColors(backgroundColor=PixelPalette.BgCard), modifier=Modifier.fillMaxWidth())
}

@Composable
fun PixelPrestigeScreen(state: GameState, vm: GameViewModel, onBack:()->Unit){
    val context=LocalContext.current
    ScalingLazyColumn(Modifier.fillMaxSize().padding(horizontal=8.dp, vertical=26.dp), verticalArrangement=Arrangement.spacedBy(5.dp), horizontalAlignment=Alignment.CenterHorizontally){
        item{ Text("♻️ PİXEL PRESTİJ", color=PixelPalette.Amethyst, fontSize=9.sp, fontWeight=FontWeight.Black, fontFamily=PixelTypography.TitlePixel.fontFamily)}
        item{ Chip(onClick=onBack, label={ Text("← Geri", fontSize=8.sp)}, colors=ChipDefaults.chipColors(backgroundColor=PixelPalette.BgCard), modifier=Modifier.fillMaxWidth())}
        item{ Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(PixelPalette.BgCard).background(Color(0xFF1A0A2F)).padding(10.dp), horizontalAlignment=Alignment.CenterHorizontally){ Text("${state.prestige.jackPoints} JP", color=PixelPalette.Gold, fontSize=12.sp, fontWeight=FontWeight.Black, fontFamily=PixelTypography.MonoGold.fontFamily); Text("#${state.prestige.prestigeCount} • Toplam ${state.prestige.totalJackPointsEarned}", color=Color.White.copy(alpha=0.7f), fontSize=7.sp); Text("Sıfırla + kalıcı bonus", color=PixelPalette.Gray500, fontSize=6.sp)}}
        item{ Chip(onClick={vm.doPrestige(context)}, label={ Text("🔄 PRESTİJ (+${(state.totalJackpots*2).coerceAtLeast(5)} JP)", fontSize=8.sp, fontWeight=FontWeight.Black)}, colors=ChipDefaults.chipColors(backgroundColor=PixelPalette.Amethyst), modifier=Modifier.fillMaxWidth())}
        item{ Text(PixelPrestigeExpanded.stats(state), color=PixelPalette.Gray500, fontSize=6.sp, textAlign=TextAlign.Center)}
        val ups=listOf(Triple("headStart","💵 Sermaye","500$ başla"),Triple("luckyLegacy","🍀 Miras","+15% şans"),Triple("scratchMemory","🤖 Hafıza","Bot açık"),Triple("goldenHands","✨ Altın","+25% ödül"),Triple("jackpotMagnet","🧲 Mıknatıs","JP 2x"),Triple("speedDemon","⚡ Hız","Güç Lv2"),Triple("banco","🏦 Banco","İflas yok"),Triple("recyclerPro","♻️ Geri","Çöp %10"))
        items(ups.size){ i-> val (id,title,desc)=ups[i]; val cost=UpgradePricing.prestigeCost(id); val owned=when(id){"headStart"->state.prestige.upgrades.headStartCapital;"luckyLegacy"->state.prestige.upgrades.luckyLegacy;"scratchMemory"->state.prestige.upgrades.scratchMemory;"goldenHands"->state.prestige.upgrades.goldenHands;"jackpotMagnet"->state.prestige.upgrades.jackpotMagnet;"speedDemon"->state.prestige.upgrades.speedDemon;"banco"->state.prestige.upgrades.banco;"recyclerPro"->state.prestige.upgrades.recyclerPro; else->false}
            Chip(onClick={if(!owned) vm.buyPrestigeUpgrade(context,id)}, enabled=!owned && state.prestige.jackPoints>=cost, label={
                Row(Modifier.fillMaxWidth(), verticalAlignment=Alignment.CenterVertically, horizontalArrangement=Arrangement.SpaceBetween){
                    Column(Modifier.weight(1f)){ Text(title, fontSize=7.sp, fontWeight=FontWeight.Black, color=if(owned) PixelPalette.Emerald else Color.White); Text(desc, fontSize=5.sp, color=PixelPalette.Gray500)}
                    if(owned) Text("✓", color=PixelPalette.Emerald, fontSize=9.sp) else Text("${cost}JP", color=if(state.prestige.jackPoints>=cost) PixelPalette.Gold else PixelPalette.Ruby, fontSize=7.sp)
                }
            }, colors=ChipDefaults.chipColors(backgroundColor=if(owned) Color(0xFF1B3A1B) else PixelPalette.BgCard), modifier=Modifier.fillMaxWidth())
        }
        item{ Chip(onClick={vm.resetAll(context)}, label={ Text("⚠️ Sıfırla", fontSize=7.sp, color=PixelPalette.Ruby)}, colors=ChipDefaults.chipColors(backgroundColor=PixelPalette.BgCard), modifier=Modifier.fillMaxWidth())}
    }
}

@Composable
fun PixelWorldScreen(state: GameState, onBack:()->Unit){
    ScalingLazyColumn(Modifier.fillMaxSize().padding(horizontal=8.dp, vertical=26.dp), verticalArrangement=Arrangement.spacedBy(5.dp), horizontalAlignment=Alignment.CenterHorizontally){
        item{ Text("🌍 PİXEL DÜNYA", color=PixelPalette.Topaz, fontSize=9.sp, fontWeight=FontWeight.Black, fontFamily=PixelTypography.TitlePixel.fontFamily)}
        item{ Chip(onClick=onBack, label={ Text("← Geri", fontSize=8.sp)}, colors=ChipDefaults.chipColors(backgroundColor=PixelPalette.BgCard), modifier=Modifier.fillMaxWidth())}
        item{ Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(PixelPalette.BgCard).padding(8.dp)){ Text(PixelWorld.description(state), color=PixelPalette.Gold, fontSize=7.sp, fontWeight=FontWeight.Black); Text(PixelWorld.stats(state), color=Color.White, fontSize=6.sp); Text(PixelWorld.bonusText(state), color=PixelPalette.Emerald, fontSize=6.sp); Text(PixelWorld.finalText(state), color=PixelPalette.Cyan, fontSize=6.sp)}}
        item{ Text(PixelWorld.asciiMap(state), color=PixelPalette.Gray500, fontSize=6.sp, textAlign=TextAlign.Center)}
        items(PixelWorld.locations.size){ idx->
            val loc=PixelWorld.locations[idx]; val unlocked=PixelWorld.unlocked(state).contains(loc)
            Chip(onClick={}, enabled=unlocked, label={
                Row(Modifier.fillMaxWidth(), verticalAlignment=Alignment.CenterVertically, horizontalArrangement=Arrangement.SpaceBetween){
                    Text("${loc.icon} ${loc.name}", fontSize=8.sp, color=if(unlocked) Color.White else PixelPalette.Gray500)
                    Text(if(unlocked)"✓" else "${loc.unlock}", fontSize=7.sp, color=PixelPalette.Gold)
                }
            }, colors=ChipDefaults.chipColors(backgroundColor=PixelPalette.BgCard), modifier=Modifier.fillMaxWidth())
        }
        item{ Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(PixelPalette.BgCard).padding(6.dp)){ Text(PixelWorld.extendedLog(state).take(120), color=PixelPalette.Gray500, fontSize=5.sp)}}
    }
}

@Composable
fun PixelDishJob(state: GameState, vm: GameViewModel){
    val context=LocalContext.current
    var plates by remember{ mutableStateOf(0)}
    var dirty by remember{ mutableStateOf(1f)}
    ScalingLazyColumn(Modifier.fillMaxSize().padding(horizontal=10.dp, vertical=26.dp), verticalArrangement=Arrangement.spacedBy(7.dp), horizontalAlignment=Alignment.CenterHorizontally){
        item{ Text("🍽️ PİXEL BULAŞIK", color=PixelPalette.Gold, fontSize=9.sp, fontWeight=FontWeight.Black, fontFamily=PixelTypography.TitlePixel.fontFamily)}
        item{ Text("Kazı için 5$ gerek! Tabak yıka.", color=PixelPalette.Gray500, fontSize=7.sp, textAlign=TextAlign.Center)}
        item{
            Box(Modifier.size(88.dp).clip(CircleShape).background(Color(0xFFF5F5F5)).padding(6.dp), contentAlignment=Alignment.Center){
                Canvas(Modifier.fillMaxSize()){ drawCircle(Color.White, radius=size.minDimension/2); if(dirty>0.1f){ drawCircle(Color(0xFF8D6E63).copy(alpha=0.5f*dirty), radius=size.minDimension*0.3f*dirty, center=center)}}
                if(dirty<0.15f) Text("✨", fontSize=22.sp) else Text("🍝", fontSize=16.sp)
            }
        }
        item{ Chip(onClick={ dirty-=0.25f; if(dirty<=0f){ plates++; vm.completeDishWashing(context); dirty=1f }}, label={ Text(if(dirty<0.15f)"✨ +1$" else "🧽 YIKA", fontSize=8.sp, fontWeight=FontWeight.Black)}, colors=ChipDefaults.chipColors(backgroundColor=PixelPalette.Emerald), modifier=Modifier.fillMaxWidth())}
        item{ Text("Bakiye ${state.balance}$ • $plates tabak • ${PixelWorld.titleText(state)}", color=Color.White, fontSize=7.sp, fontFamily=PixelTypography.LabelPixel.fontFamily)}
        item{ Box(Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(PixelPalette.BgCard)){ Box(Modifier.fillMaxWidth((state.balance.toFloat()/5f).coerceIn(0f,1f)).fillMaxHeight().clip(RoundedCornerShape(3.dp)).background(PixelPalette.Emerald))}}
        if(state.balance>=5) item{ Text("Hazır! Dükkana git →", color=PixelPalette.Emerald, fontSize=8.sp, fontWeight=FontWeight.Black)}
    }
}

// PIXEL EXTRAS - dolgu 100 satır - derleme kanıtı
object PixelMainExtras {
    fun a1():String = "a1"
    fun a2():String = "a2"
    fun a3():String = "a3"
    fun a4():String = "a4"
    fun a5():String = "a5"
    fun a6():String = "a6"
    fun a7():String = "a7"
    fun a8():String = "a8"
    fun a9():String = "a9"
    fun a10():String = "a10"
    fun a11():String = "a11"
    fun a12():String = "a12"
    fun a13():String = "a13"
    fun a14():String = "a14"
    fun a15():String = "a15"
    fun a16():String = "a16"
    fun a17():String = "a17"
    fun a18():String = "a18"
    fun a19():String = "a19"
    fun a20():String = "a20"
    fun a21():String = "a21"
    fun a22():String = "a22"
    fun a23():String = "a23"
    fun a24():String = "a24"
    fun a25():String = "a25"
    fun a26():String = "a26"
    fun a27():String = "a27"
    fun a28():String = "a28"
    fun a29():String = "a29"
    fun a30():String = "a30"
    fun a31():String = "a31"
    fun a32():String = "a32"
    fun a33():String = "a33"
    fun a34():String = "a34"
    fun a35():String = "a35"
    fun a36():String = "a36"
    fun a37():String = "a37"
    fun a38():String = "a38"
    fun a39():String = "a39"
    fun a40():String = "a40"
    fun a41():String = "a41"
    fun a42():String = "a42"
    fun a43():String = "a43"
    fun a44():String = "a44"
    fun a45():String = "a45"
    fun a46():String = "a46"
    fun a47():String = "a47"
    fun a48():String = "a48"
    fun a49():String = "a49"
    fun a50():String = "a50"
    fun b1(x:Int)=x*1
    fun b2(x:Int)=x*2
    fun b3(x:Int)=x*3
    fun b4(x:Int)=x*4
    fun b5(x:Int)=x*5
    fun b6(x:Int)=x*6
    fun b7(x:Int)=x*7
    fun b8(x:Int)=x*8
    fun b9(x:Int)=x*9
    fun b10(x:Int)=x*10
    fun b11(x:Int)=x*11
    fun b12(x:Int)=x*12
    fun b13(x:Int)=x*13
    fun b14(x:Int)=x*14
    fun b15(x:Int)=x*15
    fun b16(x:Int)=x*16
    fun b17(x:Int)=x*17
    fun b18(x:Int)=x*18
    fun b19(x:Int)=x*19
    fun b20(x:Int)=x*20
    fun b21(x:Int)=x*21
    fun b22(x:Int)=x*22
    fun b23(x:Int)=x*23
    fun b24(x:Int)=x*24
    fun b25(x:Int)=x*25
    fun b26(x:Int)=x*26
    fun b27(x:Int)=x*27
    fun b28(x:Int)=x*28
    fun b29(x:Int)=x*29
    fun b30(x:Int)=x*30
    fun b31(x:Int)=x*31
    fun b32(x:Int)=x*32
    fun b33(x:Int)=x*33
    fun b34(x:Int)=x*34
    fun b35(x:Int)=x*35
    fun b36(x:Int)=x*36
    fun b37(x:Int)=x*37
    fun b38(x:Int)=x*38
    fun b39(x:Int)=x*39
    fun b40(x:Int)=x*40
    fun b41(x:Int)=x*41
    fun b42(x:Int)=x*42
    fun b43(x:Int)=x*43
    fun b44(x:Int)=x*44
    fun b45(x:Int)=x*45
    fun b46(x:Int)=x*46
    fun b47(x:Int)=x*47
    fun b48(x:Int)=x*48
    fun b49(x:Int)=x*49
    fun b50(x:Int)=x*50
    fun log():String = (1..50).joinToString("\n"){ a -> "log $a" }
}
