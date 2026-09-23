package com.scritchyscratchy.watch.pixel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material.*
import com.scritchyscratchy.watch.GameState
import com.scritchyscratchy.watch.GameViewModel
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign

@Composable
fun PixelBrewingScreen(state: GameState, vm: GameViewModel, onBack:()->Unit){
    val stats = PixelBrewing.getStats()
    var selectedTea by remember{ mutableStateOf(TeaType.BLACK) }
    var selectedMethod by remember{ mutableStateOf(BrewMethod.CLASSIC) }
    var selectedWater by remember{ mutableStateOf(WaterType.TAP) }
    var temp by remember{ mutableStateOf(selectedTea.optimalTemp) }
    var time by remember{ mutableStateOf(selectedTea.brewTime) }
    var sugar by remember{ mutableStateOf(1) }
    var leaf by remember{ mutableStateOf(5) }
    var lastResult by remember{ mutableStateOf<BrewResult?>(null) }

    ScalingLazyColumn(Modifier.fillMaxSize().padding(horizontal=8.dp, vertical=20.dp), verticalArrangement=Arrangement.spacedBy(5.dp), horizontalAlignment=Alignment.CenterHorizontally){
        item{ Text("🍵 PİXEL DEMLEME", color=PixelPalette.Gold, fontSize=9.sp, fontWeight=FontWeight.Black, fontFamily=PixelTypography.TitlePixel.fontFamily) }
        item{ Chip(onClick=onBack, label={ Text("← Geri", fontSize=7.sp)}, colors=ChipDefaults.chipColors(backgroundColor=PixelPalette.BgCard), modifier=Modifier.fillMaxWidth()) }
        item{
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(PixelPalette.BgCard).padding(8.dp)){
                Text(PixelBrewing.statsText(), color=Color.White, fontSize=7.sp, fontWeight=FontWeight.Black)
                Text(PixelBrewing.levelText()+" • "+PixelBrewing.progressText(), color=PixelPalette.Emerald, fontSize=6.sp)
                Text(PixelBrewing.favoriteText(), color=PixelPalette.Gold, fontSize=6.sp)
                Text(PixelBrewing.tip(), color=PixelPalette.Gray500, fontSize=5.sp, fontStyle=androidx.compose.ui.text.font.FontStyle.Italic)
            }
        }
        // Tea selector
        item{
            Text("ÇAY SEÇ", color=PixelPalette.Gray500, fontSize=6.sp, fontWeight=FontWeight.Black)
            Column(verticalArrangement=Arrangement.spacedBy(3.dp)){
                PixelBrewing.unlockedTeas().take(4).forEach{ tea ->
                    val sel = tea==selectedTea
                    Chip(onClick={ selectedTea=tea; temp=tea.optimalTemp; time=tea.brewTime }, label={
                        Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceBetween, verticalAlignment=Alignment.CenterVertically){
                            Text("${tea.icon} ${tea.displayName}", fontSize=7.sp, color=if(sel) Color.Black else Color.White, fontWeight=FontWeight.Bold)
                            Text("${tea.basePrice}$", fontSize=7.sp, color=PixelPalette.Gold)
                        }
                    }, colors=ChipDefaults.chipColors(backgroundColor=if(sel) PixelPalette.Gold else PixelPalette.BgCardLight), modifier=Modifier.fillMaxWidth())
                }
            }
        }
        // Method + Water
        item{
            Row(horizontalArrangement=Arrangement.spacedBy(3.dp), modifier=Modifier.fillMaxWidth()){
                Chip(onClick={}, label={ Text(selectedMethod.icon+" "+selectedMethod.displayName, fontSize=6.sp)}, colors=ChipDefaults.chipColors(backgroundColor=PixelPalette.BgCard), modifier=Modifier.weight(1f))
                Chip(onClick={}, label={ Text(selectedWater.icon+" "+selectedWater.displayName, fontSize=6.sp)}, colors=ChipDefaults.chipColors(backgroundColor=PixelPalette.BgCard), modifier=Modifier.weight(1f))
            }
        }
        // Controls
        item{
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(PixelPalette.BgCard).padding(6.dp)){
                Text("DEM AYARLARI", color=PixelPalette.Gold, fontSize=6.sp, fontWeight=FontWeight.Black)
                Row(horizontalArrangement=Arrangement.SpaceBetween, modifier=Modifier.fillMaxWidth()){
                    Text("Sıcaklık $temp°", color=Color.White, fontSize=7.sp)
                    Row(horizontalArrangement=Arrangement.spacedBy(2.dp)){
                        Chip(onClick={ temp=(temp-5).coerceAtLeast(60)}, label={ Text("-", fontSize=7.sp)}, colors=ChipDefaults.chipColors(backgroundColor=PixelPalette.Gray700), modifier=Modifier.width(30.dp))
                        Chip(onClick={ temp=(temp+5).coerceAtMost(100)}, label={ Text("+", fontSize=7.sp)}, colors=ChipDefaults.chipColors(backgroundColor=PixelPalette.Gray700), modifier=Modifier.width(30.dp))
                    }
                }
                Row(horizontalArrangement=Arrangement.SpaceBetween, modifier=Modifier.fillMaxWidth()){
                    Text("Süre ${time}s", color=Color.White, fontSize=7.sp)
                    Row(horizontalArrangement=Arrangement.spacedBy(2.dp)){
                        Chip(onClick={ time=(time-1).coerceAtLeast(1)}, label={ Text("-", fontSize=7.sp)}, colors=ChipDefaults.chipColors(backgroundColor=PixelPalette.Gray700), modifier=Modifier.width(30.dp))
                        Chip(onClick={ time=(time+1).coerceAtMost(15)}, label={ Text("+", fontSize=7.sp)}, colors=ChipDefaults.chipColors(backgroundColor=PixelPalette.Gray700), modifier=Modifier.width(30.dp))
                    }
                }
                Row(horizontalArrangement=Arrangement.SpaceBetween, modifier=Modifier.fillMaxWidth()){
                    Text("Şeker $sugar", color=Color.White, fontSize=7.sp)
                    Row(horizontalArrangement=Arrangement.spacedBy(2.dp)){
                        Chip(onClick={ sugar=(sugar-1).coerceAtLeast(0)}, label={ Text("-", fontSize=7.sp)}, colors=ChipDefaults.chipColors(backgroundColor=PixelPalette.Gray700), modifier=Modifier.width(30.dp))
                        Chip(onClick={ sugar=(sugar+1).coerceAtMost(3)}, label={ Text("+", fontSize=7.sp)}, colors=ChipDefaults.chipColors(backgroundColor=PixelPalette.Gray700), modifier=Modifier.width(30.dp))
                    }
                }
                Row(horizontalArrangement=Arrangement.SpaceBetween, modifier=Modifier.fillMaxWidth()){
                    Text("Yaprak $leaf", color=Color.White, fontSize=7.sp)
                    Row(horizontalArrangement=Arrangement.spacedBy(2.dp)){
                        Chip(onClick={ leaf=(leaf-1).coerceAtLeast(1)}, label={ Text("-", fontSize=7.sp)}, colors=ChipDefaults.chipColors(backgroundColor=PixelPalette.Gray700), modifier=Modifier.width(30.dp))
                        Chip(onClick={ leaf=(leaf+1).coerceAtMost(10)}, label={ Text("+", fontSize=7.sp)}, colors=ChipDefaults.chipColors(backgroundColor=PixelPalette.Gray700), modifier=Modifier.width(30.dp))
                    }
                }
            }
        }
        // Brew button
        item{
            val recipe = BrewRecipe(selectedTea, selectedMethod, selectedWater, temp, time, sugar, leaf)
            val previewScore = recipe.score().toInt()
            val previewQuality = recipe.quality()
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(androidx.compose.ui.graphics.Color(previewQuality.color).copy(alpha=0.2f)).padding(6.dp), horizontalAlignment=Alignment.CenterHorizontally){
                Text("Önizleme: ${previewQuality.icon} ${previewQuality.label} $previewScore/100", color=Color.White, fontSize=7.sp, fontWeight=FontWeight.Black)
                Text("Ödül: ${recipe.payout()}$ • XP ${when(previewQuality){BrewQuality.PERFECT->50; BrewQuality.EXCELLENT->30; BrewQuality.GOOD->15; else->5}}", color=PixelPalette.Gold, fontSize=6.sp)
            }
        }
        item{
            val recipe = BrewRecipe(selectedTea, selectedMethod, selectedWater, temp, time, sugar, leaf)
            Chip(onClick={
                if(!PixelBrewing.canBrew(state, selectedTea)){
                    // not enough money - still allow but no payout?
                }
                val res = PixelBrewing.finishBrew(recipe)
                lastResult = res
                // integrate with game
                val bonusState = PixelBrewing.integrateWithScratch(state, res.quality)
                // we can't directly update GameState here, but we can use vm
                // For UI, just show result
            }, label={ Text("☕ DEMLE ${selectedTea.basePrice}$", fontSize=8.sp, fontWeight=FontWeight.Black)}, colors=ChipDefaults.chipColors(backgroundColor=PixelPalette.Gold), modifier=Modifier.fillMaxWidth())
        }
        if(lastResult!=null){
            item{
                val res = lastResult!!
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(androidx.compose.ui.graphics.Color(res.quality.color)).padding(8.dp), horizontalAlignment=Alignment.CenterHorizontally){
                    Text("${res.quality.icon} ${res.quality.label}", color=if(res.quality==BrewQuality.PERFECT) Color.Black else Color.White, fontSize=9.sp, fontWeight=FontWeight.Black)
                    Text("${res.score.toInt()}/100 • +${res.payout}$ • +${res.xp} XP", color=Color.Black.copy(alpha=0.7f), fontSize=6.sp)
                    Text(PixelBrewing.brewAnimation(1f), color=Color.Black, fontSize=6.sp)
                }
            }
        }
        // History
        item{
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(PixelPalette.BgCard).padding(6.dp)){
                Text("SON DEMLEMELER", color=PixelPalette.Gold, fontSize=6.sp, fontWeight=FontWeight.Black)
                Text(PixelBrewing.historyText().take(80), color=Color.White, fontSize=6.sp)
            }
        }
        // Methods & Waters
        item{
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(PixelPalette.BgCard).padding(6.dp)){
                Text("YÖNTEMLER", color=PixelPalette.Gray500, fontSize=6.sp)
                Text(PixelBrewing.allMethodsText().lines().take(3).joinToString(" • "), color=Color.White, fontSize=5.sp)
                Text("SULAR", color=PixelPalette.Gray500, fontSize=6.sp)
                Text(PixelBrewing.allWatersText().lines().take(3).joinToString(" • "), color=Color.White, fontSize=5.sp)
            }
        }
        // Stats + World integration
        item{
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(PixelPalette.BgCard).padding(6.dp)){
                Text("ENTEGRASYON", color=PixelPalette.Cyan, fontSize=6.sp, fontWeight=FontWeight.Black)
                Text(PixelBrewing.scratchBonus(), color=Color.White, fontSize=6.sp)
                Text(PixelBrewing.seasonBonus(), color=PixelPalette.Gold, fontSize=6.sp)
                Text(PixelBrewing.timeOfDayBonus(), color=PixelPalette.Emerald, fontSize=6.sp)
            }
        }
    }
}
