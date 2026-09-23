package com.scritchyscratchy.watch

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

private val Context.dataStore by preferencesDataStore(name = "scritchy_watch_save")

class GameViewModel : ViewModel() {
    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    private var dataStoreRef: androidx.datastore.core.DataStore<Preferences>? = null

    fun attachContext(context: Context) {
        dataStoreRef = context.dataStore
        viewModelScope.launch { load(context) }
        // Auto scratcher loop
        viewModelScope.launch {
            while(true) {
                delay(2200)
                tryAutoScratch(context)
            }
        }
    }

    private suspend fun load(context: Context) {
        try {
            val prefs = context.dataStore.data.first()
            if(!prefs.contains(stringPreferencesKey("save_json")) && !prefs.contains(longPreferencesKey("balance"))) return
            val bal = prefs[longPreferencesKey("balance")] ?: 50L
            val scratched = prefs[intPreferencesKey("totalScratched")] ?: 0
            val won = prefs[longPreferencesKey("totalWon")] ?: 0L
            val jps = prefs[intPreferencesKey("jackPoints")] ?: 0
            val prestigeCount = prefs[intPreferencesKey("prestigeCount")] ?: 0
            val luck = prefs[intPreferencesKey("luckLevel")] ?: 0
            val power = prefs[intPreferencesKey("powerLevel")] ?: 0
            val area = prefs[intPreferencesKey("areaLevel")] ?: 0
            val auto = prefs[booleanPreferencesKey("autoUnlocked")] ?: false
            val trash = prefs[booleanPreferencesKey("trashUnlocked")] ?: false
            val dishDone = prefs[booleanPreferencesKey("dishDone")] ?: false
            // prestige upgrades
            val upgrades = PrestigeUpgrades(
                headStartCapital = prefs[booleanPreferencesKey("p_headStart")] ?: false,
                luckyLegacy = prefs[booleanPreferencesKey("p_lucky")] ?: false,
                scratchMemory = prefs[booleanPreferencesKey("p_memory")] ?: false,
                goldenHands = prefs[booleanPreferencesKey("p_golden")] ?: false,
                jackpotMagnet = prefs[booleanPreferencesKey("p_magnet")] ?: false,
                speedDemon = prefs[booleanPreferencesKey("p_speed")] ?: false,
                banco = prefs[booleanPreferencesKey("p_banco")] ?: false,
                recyclerPro = prefs[booleanPreferencesKey("p_recycler")] ?: false,
            )
            _state.value = _state.value.copy(
                balance = bal,
                totalScratched = scratched,
                totalWon = won,
                prestige = PrestigeState(jackPoints = jps, prestigeCount = prestigeCount, upgrades = upgrades),
                upgrades = PlayerUpgrades(luck, power, area, auto, trash),
                hasCompletedDishJob = dishDone
            )
            // start bonus
            applyPrestigeStartBonuses()
        } catch (e: Exception) {
            // ignore
        }
    }

    private suspend fun save(context: Context) {
        try {
            val s = _state.value
            context.dataStore.edit { prefs ->
                prefs[longPreferencesKey("balance")] = s.balance
                prefs[intPreferencesKey("totalScratched")] = s.totalScratched
                prefs[longPreferencesKey("totalWon")] = s.totalWon
                prefs[intPreferencesKey("jackPoints")] = s.prestige.jackPoints
                prefs[intPreferencesKey("prestigeCount")] = s.prestige.prestigeCount
                prefs[intPreferencesKey("luckLevel")] = s.upgrades.luckLevel
                prefs[intPreferencesKey("powerLevel")] = s.upgrades.scratchPowerLevel
                prefs[intPreferencesKey("areaLevel")] = s.upgrades.areaSizeLevel
                prefs[booleanPreferencesKey("autoUnlocked")] = s.upgrades.autoScratcherUnlocked
                prefs[booleanPreferencesKey("trashUnlocked")] = s.upgrades.trashCanUnlocked
                prefs[booleanPreferencesKey("dishDone")] = s.hasCompletedDishJob
                prefs[booleanPreferencesKey("p_headStart")] = s.prestige.upgrades.headStartCapital
                prefs[booleanPreferencesKey("p_lucky")] = s.prestige.upgrades.luckyLegacy
                prefs[booleanPreferencesKey("p_memory")] = s.prestige.upgrades.scratchMemory
                prefs[booleanPreferencesKey("p_golden")] = s.prestige.upgrades.goldenHands
                prefs[booleanPreferencesKey("p_magnet")] = s.prestige.upgrades.jackpotMagnet
                prefs[booleanPreferencesKey("p_speed")] = s.prestige.upgrades.speedDemon
                prefs[booleanPreferencesKey("p_banco")] = s.prestige.upgrades.banco
                prefs[booleanPreferencesKey("p_recycler")] = s.prestige.upgrades.recyclerPro
                prefs[stringPreferencesKey("save_json")] = "v1"
            }
        } catch (_: Exception) {}
    }

    fun persist(context: Context) { viewModelScope.launch { save(context) } }

    private fun applyPrestigeStartBonuses() {
        var s = _state.value
        var bal = s.balance
        var upgrades = s.upgrades
        if(s.prestige.upgrades.headStartCapital) bal = maxOf(bal, 500)
        if(s.prestige.upgrades.scratchMemory) upgrades = upgrades.copy(autoScratcherUnlocked = true)
        if(s.prestige.upgrades.speedDemon && upgrades.scratchPowerLevel < 2) upgrades = upgrades.copy(scratchPowerLevel = 2)
        _state.value = s.copy(balance = bal, upgrades = upgrades)
    }

    // --- Actions ---

    fun completeDishWashing(context: Context) {
        // Simulate plate washing: +1 per plate, - risk
        val cur = _state.value
        _state.value = cur.copy(balance = cur.balance + 1, hasCompletedDishJob = cur.balance+1 >= 5)
        vibrate(context, 10)
        persist(context)
    }

    fun buyCard(context: Context, def: CardDefinition): Boolean {
        val s = _state.value
        if(s.balance < def.cost) return false
        val lvl = s.cardLevels[def.id] ?: 1
        val luck = s.upgrades.luckBonus
        val card = CardGenerator.generate(def, lvl, luck, s.prestige)
        _state.value = s.copy(
            balance = s.balance - def.cost,
            currentCard = card,
            history = (listOf("Satın alındı: ${def.nameTr} ${def.cost}$") + s.history).take(20)
        )
        vibrate(context, 20)
        persist(context)
        return true
    }

    fun scratchDone(context: Context, revealedPercent: Float) {
        // Called when user finished scratching (>=70% revealed)
        val s = _state.value
        val card = s.currentCard ?: return
        // payout logic already generated
        val payout = card.payout
        var newBalance = s.balance + payout
        var won = s.totalWon
        var jackpots = s.totalJackpots
        var jpEarned = 0
        var msg = ""
        if(card.isPenalty) {
            msg = "Ceza! ${payout}$"
            if(s.prestige.upgrades.banco && newBalance < 0) {
                newBalance = 0
                msg += " (Banco korudu)"
            }
        } else if(card.isJackpot) {
            won += payout
            jackpots += 1
            jpEarned = 5
            msg = "JACKPOT! +${payout}$ (+${jpEarned} JP)"
        } else if(payout > 0) {
            won += payout
            msg = "Kazandın +${payout}$"
            if(payout > card.definition.basePayout * 2) jackpots +=1
        } else {
            msg = "Kaybettin"
        }

        // Level up card slightly chance
        val newLevels = s.cardLevels.toMutableMap()
        if(payout > 0) {
            // 30% chance to level up on win, or forced every 5 wins count simplified
            if(card.level < 10 && kotlin.random.Random.nextDouble() < 0.35) {
                newLevels[card.definition.id] = card.level + 1
                msg += " | Lv Up!"
            }
        }

        // Prestige check: Final Chance win triggers prestige option
        var newPrestige = s.prestige
        if(card.definition.type == CardType.FINAL_CHANCE && payout > 0) {
            // big bonus
            jpEarned = 20
            newPrestige = newPrestige.copy(jackPoints = newPrestige.jackPoints + jpEarned, totalJackPointsEarned = newPrestige.totalJackPointsEarned + jpEarned)
            msg = "SON ŞANS KAZANDI! PRESTİJ! +${jpEarned} JP"
            // auto prestige? keep balance but enable prestige screen
        } else if(jpEarned > 0) {
            newPrestige = newPrestige.copy(jackPoints = newPrestige.jackPoints + jpEarned, totalJackPointsEarned = newPrestige.totalJackPointsEarned + jpEarned)
        }

        _state.value = s.copy(
            balance = newBalance,
            totalScratched = s.totalScratched + 1,
            totalWon = won,
            totalJackpots = jackpots,
            cardLevels = newLevels,
            currentCard = null,
            prestige = newPrestige,
            history = (listOf(msg) + s.history).take(20)
        )
        vibrate(context, if(payout>0) 80 else 30)
        persist(context)

        // bankruptcy handling
        if(newBalance < 0) {
            handleBankruptcy(context)
        }
    }

    fun trashCard(context: Context) {
        val s = _state.value
        val card = s.currentCard ?: return
        var refund = 0
        if(s.prestige.upgrades.recyclerPro) refund = (card.definition.cost * 0.1).toInt()
        _state.value = s.copy(
            currentCard = null,
            balance = s.balance + refund,
            history = (listOf("Çöpe atıldı ${if(refund>0) "+${refund}$ iade" else ""}") + s.history).take(20)
        )
        vibrate(context, 15)
        persist(context)
    }

    fun upgradeLuck(context: Context): Boolean {
        val s = _state.value
        if(s.upgrades.luckLevel >= 10) return false
        val cost = UpgradePricing.luckCost(s.upgrades.luckLevel)
        if(s.balance < cost) return false
        _state.value = s.copy(balance = s.balance - cost, upgrades = s.upgrades.copy(luckLevel = s.upgrades.luckLevel+1))
        vibrate(context, 40)
        persist(context)
        return true
    }
    fun upgradePower(context: Context): Boolean {
        val s = _state.value
        if(s.upgrades.scratchPowerLevel >= 10) return false
        val cost = UpgradePricing.powerCost(s.upgrades.scratchPowerLevel)
        if(s.balance < cost) return false
        _state.value = s.copy(balance = s.balance - cost, upgrades = s.upgrades.copy(scratchPowerLevel = s.upgrades.scratchPowerLevel+1))
        vibrate(context, 40)
        persist(context)
        return true
    }
    fun upgradeArea(context: Context): Boolean {
        val s = _state.value
        if(s.upgrades.areaSizeLevel >= 10) return false
        val cost = UpgradePricing.areaCost(s.upgrades.areaSizeLevel)
        if(s.balance < cost) return false
        _state.value = s.copy(balance = s.balance - cost, upgrades = s.upgrades.copy(areaSizeLevel = s.upgrades.areaSizeLevel+1))
        vibrate(context, 40)
        persist(context)
        return true
    }
    fun buyAuto(context: Context): Boolean {
        val s = _state.value
        if(s.upgrades.autoScratcherUnlocked) return false
        val cost = UpgradePricing.autoCost()
        if(s.balance < cost) return false
        _state.value = s.copy(balance = s.balance - cost, upgrades = s.upgrades.copy(autoScratcherUnlocked = true))
        vibrate(context, 60)
        persist(context)
        return true
    }
    fun buyTrash(context: Context): Boolean {
        val s = _state.value
        if(s.upgrades.trashCanUnlocked) return false
        val cost = UpgradePricing.trashCost()
        if(s.balance < cost) return false
        _state.value = s.copy(balance = s.balance - cost, upgrades = s.upgrades.copy(trashCanUnlocked = true))
        persist(context)
        return true
    }

    fun doPrestige(context: Context) {
        val s = _state.value
        // need at least 1 jackpot or balance threshold? allow anytime but bonus JP based on jackpots
        val jpBonus = (s.totalJackpots * 2).coerceAtLeast(5)
        val newPrestigeCount = s.prestige.prestigeCount + 1
        val newJP = s.prestige.jackPoints + jpBonus
        _state.value = GameState(
            balance = if(s.prestige.upgrades.headStartCapital) 500 else 50,
            prestige = PrestigeState(
                jackPoints = newJP,
                totalJackPointsEarned = s.prestige.totalJackPointsEarned + jpBonus,
                prestigeCount = newPrestigeCount,
                upgrades = s.prestige.upgrades
            ),
            // keep prestige upgrades, reset other upgrades but keep some if purchased prestige
            upgrades = PlayerUpgrades(
                luckLevel = 0,
                scratchPowerLevel = if(s.prestige.upgrades.speedDemon) 2 else 0,
                areaSizeLevel = 0,
                autoScratcherUnlocked = s.prestige.upgrades.scratchMemory,
                trashCanUnlocked = false
            ),
            hasCompletedDishJob = false,
            history = listOf("PRESTİJ #${newPrestigeCount} +${jpBonus} JP")
        )
        vibrate(context, 120)
        persist(context)
    }

    fun buyPrestigeUpgrade(context: Context, id: String): Boolean {
        val s = _state.value
        val cost = UpgradePricing.prestigeCost(id)
        if(s.prestige.jackPoints < cost) return false
        val cur = s.prestige.upgrades
        val newUpgrades = when(id) {
            "headStart" -> if(cur.headStartCapital) return false else cur.copy(headStartCapital = true)
            "luckyLegacy" -> if(cur.luckyLegacy) return false else cur.copy(luckyLegacy = true)
            "scratchMemory" -> if(cur.scratchMemory) return false else cur.copy(scratchMemory = true)
            "goldenHands" -> if(cur.goldenHands) return false else cur.copy(goldenHands = true)
            "jackpotMagnet" -> if(cur.jackpotMagnet) return false else cur.copy(jackpotMagnet = true)
            "speedDemon" -> if(cur.speedDemon) return false else cur.copy(speedDemon = true)
            "banco" -> if(cur.banco) return false else cur.copy(banco = true)
            "recyclerPro" -> if(cur.recyclerPro) return false else cur.copy(recyclerPro = true)
            else -> return false
        }
        _state.value = s.copy(
            prestige = s.prestige.copy(jackPoints = s.prestige.jackPoints - cost, upgrades = newUpgrades),
            history = (listOf("Prestij Yükseltmesi: $id -$cost JP") + s.history).take(20)
        )
        // apply immediate if needed
        applyPrestigeStartBonuses()
        vibrate(context, 70)
        persist(context)
        return true
    }

    fun takeLoan(context: Context) {
        val s = _state.value
        if(s.balance >= 0) return
        // Loan Shark: 200$ with 6000% interest but only once
        _state.value = s.copy(balance = s.balance + 400, loanTaken = s.loanTaken + 1, history = (listOf("Kredi: +400$ (faiz tek sefer) ") + s.history).take(20))
        persist(context)
    }

    private fun handleBankruptcy(context: Context) {
        // Show loan option via history; user can tap loan button
        // No auto
    }

    private suspend fun tryAutoScratch(context: Context) {
        val s = _state.value
        if(!s.upgrades.autoScratcherUnlocked) return
        if(s.currentCard != null) return // user has card open, don't interfere
        if(s.balance < 5) return
        // Auto picks best affordable card based on level and luck
        // Prefer Quick Cash early, then higher
        val affordable = CardCatalog.all.filter { it.cost <= s.balance && it.type != CardType.FINAL_CHANCE }
        if(affordable.isEmpty()) return
        // Pick highest level card affordable
        val best = affordable.maxByOrNull { (s.cardLevels[it.id]?:1)*100 - it.cost } ?: affordable.first()
        // Auto has small delay, auto-generates and auto scratches with reduced payout variance?
        val luck = s.upgrades.luckBonus
        val card = CardGenerator.generate(best, s.cardLevels[best.id]?:1, luck, s.prestige)
        // Auto immediately resolves with 90% efficiency (slightly worse than manual perfect)
        // We do it via state mutation directly
        withContext(Dispatchers.Main) {
            val cur = _state.value
            if(cur.currentCard != null) return@withContext
            // pay cost
            if(cur.balance < best.cost) return@withContext
            _state.value = cur.copy(balance = cur.balance - best.cost, currentCard = card)
            // delay then resolve
            delay(600)
            scratchDone(context, 1f)
        }
    }

    private fun vibrate(context: Context, ms: Long) {
        try {
            val vib = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if(android.os.Build.VERSION.SDK_INT >= 26) vib.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
            else vib.vibrate(ms)
        } catch (_: Exception) {}
    }

    // Debug/cheat for testing
    fun addMoney(context: Context, amt: Long) {
        _state.value = _state.value.copy(balance = _state.value.balance + amt)
        persist(context)
    }
    fun resetAll(context: Context) {
        viewModelScope.launch {
            context.dataStore.edit { it.clear() }
            _state.value = GameState()
            persist(context)
        }
    }
}
