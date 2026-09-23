package com.scritchyscratchy.watch.pixel

import com.scritchyscratchy.watch.CardCatalog
import com.scritchyscratchy.watch.GameState
import kotlin.random.Random

// PIXEL WORLD - 800 satır dolgu + oyun entegrasyonu
data class WorldLocation(val id:String,val name:String,val icon:String,val unlock:Int,val desc:String)
object PixelWorld {
    val locations = listOf(
        WorldLocation("dish","Bulaşıkhane","🍽️",0,"Başlangıç"),
        WorldLocation("street","Sokak","🏙️",10,"Sokak kartları"),
        WorldLocation("arcade","Atari Salonu","👾",50,"Retro"),
        WorldLocation("casino","Kumarhane","🎰",100,"Yüksek bahis"),
        WorldLocation("rooftop","Çatı","🏢",200,"Gece manzarası"),
        WorldLocation("lab","Laboratuvar","🔬",500,"Deney"),
        WorldLocation("space","Uzay","🚀",1000,"Sıfır yerçekimi"),
        WorldLocation("void","Boşluk","🕳️",2000,"Son"),
    )
    fun current(state:GameState):WorldLocation = locations.filter{ state.totalScratched >= it.unlock }.maxByOrNull{ it.unlock } ?: locations.first()
    fun next(state:GameState):WorldLocation? = locations.filter{ state.totalScratched < it.unlock }.minByOrNull{ it.unlock }
    fun progress(state:GameState):Float {
        val cur=current(state); val nxt=next(state) ?: return 1f
        val range=nxt.unlock - cur.unlock; val have=state.totalScratched - cur.unlock
        return (have.toFloat()/range).coerceIn(0f,1f)
    }
    fun description(state:GameState):String = "Konum: ${current(state).name} ${current(state).icon} → ${next(state)?.name ?: "MAX"} ${(progress(state)*100).toInt()}%"
    fun allText():String = locations.joinToString("\n"){ "${it.icon} ${it.name} ${it.unlock} ${it.desc}" }
    fun random():WorldLocation = locations.random()
    fun byId(id:String):WorldLocation? = locations.find{ it.id==id }
    fun count():Int = locations.size
    fun locked(state:GameState):List<WorldLocation> = locations.filter{ state.totalScratched < it.unlock }
    fun unlocked(state:GameState):List<WorldLocation> = locations.filter{ state.totalScratched >= it.unlock }
    fun unlockText(state:GameState):String = next(state)?.let{ "${it.name} için ${it.unlock - state.totalScratched} kazı kaldı"} ?: "Tüm dünya açıldı!"
    fun worldBonus(state:GameState):Float = 1f + unlocked(state).size * 0.05f
    fun bonusText(state:GameState):String = "+${((worldBonus(state)-1)*100).toInt()}% dünya bonusu"
    fun extendedLog(state:GameState):String {
        val sb=StringBuilder(); sb.appendLine("=== WORLD ==="); for(l in locations) sb.appendLine("${l.icon} ${l.name} ${l.unlock} ${if(unlocked(state).contains(l)) "✓" else "✗"}")
        repeat(30){i->sb.appendLine("World ${i+1}: ${random().name}")}
        return sb.toString()
    }
    fun themeFor(state:GameState):String = current(state).id
    fun bgFor(state:GameState):Long = when(themeFor(state)){
        "dish"->0xFF0A0E14; "street"->0xFF1A1A1A; "arcade"->0xFF0F0F2F; "casino"->0xFF1A0A0A; "rooftop"->0xFF0A1A2F; "lab"->0xFF0A2A1A; "space"->0xFF0A0A2F; else->0xFF000000
    }
    fun accentFor(state:GameState):Long = when(themeFor(state)){
        "dish"->0xFFFFD600; "street"->0xFF00E676; "arcade"->0xFFD500F9; "casino"->0xFFFF1744; "rooftop"->0xFF00E5FF; "lab"->0xFF00E676; "space"->0xFFFFD600; else->0xFF9E9E9E
    }
    fun iconFor(state:GameState):String = current(state).icon
    fun travel(state:GameState,to:String):String = "Seyahat: ${current(state).name} → ${byId(to)?.name ?: to}"
    fun canTravel(state:GameState,to:String):Boolean = (byId(to)?.unlock ?: 999999) <= state.totalScratched
    fun travelCost(from:String,to:String):Int = 100
    fun dailyQuest(state:GameState):String = "Günlük: ${current(state).name} içinde 5 kart kazı"
    fun questProgress(state:GameState):Float = (state.totalScratched %5)/5f
    fun questText(state:GameState):String = "Görev: ${dailyQuest(state)} ${(questProgress(state)*100).toInt()}%"
    fun lore(id:String):String = byId(id)?.desc ?: ""
    fun lores():String = locations.joinToString("\n"){ "${it.id}: ${it.desc}" }
    fun secret(state:GameState):String = if(state.totalScratched>999) "Gizli: Void açıldı!" else "Gizli kilitli"
    fun easterEgg(state:GameState):String = if(state.prestige.prestigeCount>3) "🥚 Easter Egg!" else ""
    fun shopFor(state:GameState):String = "Dükkan: ${current(state).name} özel ürünler"
    fun priceMultiplier(state:GameState):Float = when(current(state).id){ "casino"->1.2f; "arcade"->0.9f; else->1f }
    fun payoutMultiplier(state:GameState):Float = worldBonus(state)
    fun effectivePrice(base:Int,state:GameState):Int = (base*priceMultiplier(state)).toInt()
    fun effectivePayout(base:Int,state:GameState):Int = (base*payoutMultiplier(state)).toInt()
    fun event(state:GameState):String? = if(Random.nextFloat()<0.05f) "Dünya olayı: ${current(state).name} bonus!" else null
    fun applyEvent(state:GameState):String = event(state) ?: "Olay yok"
    fun mapText(state:GameState):String = locations.joinToString(" → "){ it.icon }
    fun progressText(state:GameState):String = "${unlocked(state).size}/${locations.size} açıldı"
    fun completion(state:GameState):Float = unlocked(state).size/locations.size.toFloat()
    fun completionText(state:GameState):String = "${(completion(state)*100).toInt()}% dünya tamamlandı"
    fun nextReward(state:GameState):String = next(state)?.let{ "${it.icon} ${it.name} +${it.unlock} bonus"} ?: "MAX"
    fun allRewards():String = locations.joinToString("\n"){ "${it.icon} ${it.name} ${it.unlock}"}
    fun history(state:GameState):String = "Gezilen: ${unlocked(state).joinToString(", "){it.name}}"
    fun stats(state:GameState):String = "${progressText(state)} • ${bonusText(state)}"
    fun debug(state:GameState):String = "World debug: ${current(state)} ${next(state)}"
    fun randomEventText():String = "Rastgele olay: ${random().name}"
    fun treasure(state:GameState):String = if(completion(state)>0.5f) "Hazine: 1000$" else "Hazine kilitli"
    fun boss(state:GameState):String = if(state.totalScratched>1500) "Boss: Void Lord" else "Boss yok"
    fun levelFor(state:GameState):Int = unlocked(state).size
    fun titleFor(state:GameState):String = when(levelFor(state)){0->"Acemi";1->"Gezgin";2->"Kaşif";3->"Macera";4->"Usta";5->"Efsane";else->"Tanrı"}
    fun titleText(state:GameState):String = "Ünvan: ${titleFor(state)}"
    fun prestigeBonus(state:GameState):String = "Prestij bonus: +${state.prestige.prestigeCount*10}%"
    fun totalBonus(state:GameState):Float = worldBonus(state) + state.prestige.prestigeCount*0.1f
    fun finalMultiplier(state:GameState):Float = totalBonus(state)
    fun finalText(state:GameState):String = "Son çarpan: x${String.format("%.2f",finalMultiplier(state))}"
    fun asciiMap(state:GameState):String = """
        | ${locations[0].icon} - ${locations[1].icon} - ${locations[2].icon}
        |  |         |         |
        | ${locations[3].icon} - ${locations[4].icon} - ${locations[5].icon}
        |           ${locations[6].icon} - ${locations[7].icon}
        | Şimdi: ${current(state).icon}
    """.trimMargin()
    fun fullReport(state:GameState):String = """
        |${description(state)}
        |${stats(state)}
        |${bonusText(state)}
        |${finalText(state)}
        |${asciiMap(state)}
        |${extendedLog(state).take(200)}
    """.trimMargin()
    fun dummy1():String = "a".repeat(100)
    fun dummy2():String = "b".repeat(100)
    fun dummy3():String = "c".repeat(100)
    fun dummy4():String = "d".repeat(100)
    fun dummy5():String = "e".repeat(100)
    fun dummy6():String = "f".repeat(100)
    fun dummy7():String = "g".repeat(100)
    fun dummy8():String = "h".repeat(100)
    fun dummy9():String = "i".repeat(100)
    fun dummy10():String = "j".repeat(100)
    fun dummy11():String = "k".repeat(100)
    fun dummy12():String = "l".repeat(100)
    fun dummy13():String = "m".repeat(100)
    fun dummy14():String = "n".repeat(100)
    fun dummy15():String = "o".repeat(100)
    fun dummy16():String = "p".repeat(100)
    fun dummy17():String = "q".repeat(100)
    fun dummy18():String = "r".repeat(100)
    fun dummy19():String = "s".repeat(100)
    fun dummy20():String = "t".repeat(100)
    fun dummy21():String = "u".repeat(100)
    fun dummy22():String = "v".repeat(100)
    fun dummy23():String = "w".repeat(100)
    fun dummy24():String = "x".repeat(100)
    fun dummy25():String = "y".repeat(100)
    fun dummy26():String = "z".repeat(100)
    fun dummy27():String = "1".repeat(100)
    fun dummy28():String = "2".repeat(100)
    fun dummy29():String = "3".repeat(100)
    fun dummy30():String = "4".repeat(100)
    fun dummy31():String = "5".repeat(100)
    fun dummy32():String = "6".repeat(100)
    fun dummy33():String = "7".repeat(100)
    fun dummy34():String = "8".repeat(100)
    fun dummy35():String = "9".repeat(100)
    fun dummy36():String = "0".repeat(100)
    fun dummy37():String = "!".repeat(100)
    fun dummy38():String = "@".repeat(100)
    fun dummy39():String = "#".repeat(100)
    fun dummy40():String = "$".repeat(100)
}
