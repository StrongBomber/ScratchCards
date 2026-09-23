package com.scritchyscratchy.watch.pixel

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.Build
import kotlin.random.Random

// ============= PIXEL SOUND & HAPTICS - RETRO 8-BIT =============

enum class PixelSound(val tone: Int, val duration: Int) {
    SCRATCH(15, 30), // ToneGenerator tone
    REVEAL(20, 80),
    WIN(25, 120),
    JACKPOT(30, 300),
    PENALTY(35, 200),
    COIN(40, 60),
    PRESTIGE(45, 400),
    CLICK(50, 30),
    HOVER(55, 20),
    LEVEL_UP(60, 150),
    COMBO(65, 100),
    ERROR(70, 60)
}

object PixelSoundManager {
    private var toneGen: ToneGenerator? = null

    fun init() {
        try {
            toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 60)
        } catch (_: Exception) {}
    }

    fun play(sound: PixelSound) {
        try {
            toneGen?.startTone(sound.tone, sound.duration)
        } catch (_: Exception) {}
    }

    fun playScratch(velocity: Float) {
        val tone = when {
            velocity > 15 -> PixelSound.SCRATCH
            velocity > 8 -> PixelSound.COIN
            else -> PixelSound.HOVER
        }
        play(tone)
    }

    fun playWin(amount: Int) {
        when {
            amount >= 10000 -> play(PixelSound.JACKPOT)
            amount >= 1000 -> play(PixelSound.WIN)
            amount > 0 -> play(PixelSound.COIN)
            else -> play(PixelSound.PENALTY)
        }
    }

    fun playJackpot() = play(PixelSound.JACKPOT)
    fun playPenalty() = play(PixelSound.PENALTY)
    fun playPrestige() = play(PixelSound.PRESTIGE)
    fun playClick() = play(PixelSound.CLICK)
    fun playLevelUp() = play(PixelSound.LEVEL_UP)
    fun playCombo(combo: Int) {
        repeat(combo.coerceAtMost(3)) {
            play(PixelSound.COMBO)
        }
    }

    fun release() {
        try { toneGen?.release() } catch (_: Exception) {}
        toneGen = null
    }

    // Pixel chiptune sequence
    fun playChiptuneWin() {
        // simple arpeggio
        val seq = listOf(PixelSound.COIN, PixelSound.WIN, PixelSound.JACKPOT)
        for (s in seq) {
            play(s)
            Thread.sleep(80)
        }
    }

    fun playChiptuneLose() {
        play(PixelSound.PENALTY)
        Thread.sleep(60)
        play(PixelSound.ERROR)
    }

    fun playPrestigeFanfare() {
        val seq = listOf(PixelSound.LEVEL_UP, PixelSound.WIN, PixelSound.JACKPOT, PixelSound.PRESTIGE)
        for (s in seq) {
            play(s)
            Thread.sleep(100)
        }
    }

    // Volume control
    var enabled = true
    var volume = 0.6f

    fun setVolumeLevel(v: Float) {
        volume = v.coerceIn(0f, 1f)
        try {
            toneGen?.release()
            toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, (volume * 100).toInt())
        } catch (_: Exception) {}
    }

    // Retro sound names
    fun nameFor(sound: PixelSound): String = when (sound) {
        PixelSound.SCRATCH -> "SCRITCH"
        PixelSound.REVEAL -> "REVEAL"
        PixelSound.WIN -> "WIN"
        PixelSound.JACKPOT -> "JACKPOT!"
        PixelSound.PENALTY -> "BUZZ"
        PixelSound.COIN -> "COIN"
        PixelSound.PRESTIGE -> "PRESTIGE"
        PixelSound.CLICK -> "CLICK"
        PixelSound.HOVER -> "HOVER"
        PixelSound.LEVEL_UP -> "LEVEL UP"
        PixelSound.COMBO -> "COMBO"
        PixelSound.ERROR -> "ERROR"
    }

    fun allSounds(): List<PixelSound> = PixelSound.values().toList()
}

enum class PixelHaptic(val duration: Long, val amplitude: Int) {
    LIGHT(20, 40),
    MEDIUM(40, 80),
    HEAVY(60, 120),
    SUCCESS(80, 100),
    JACKPOT_LONG(120, 150),
    PENALTY_BUZZ(100, 60),
    TICK(15, 30),
    COMBO_PULSE(30, 90)
}

object PixelHaptics {
    fun vibrate(context: Context, haptic: PixelHaptic) {
        try {
            val vib = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= 26) {
                val effect = VibrationEffect.createOneShot(haptic.duration, haptic.amplitude)
                vib.vibrate(effect)
            } else {
                vib.vibrate(haptic.duration)
            }
        } catch (_: Exception) {}
    }

    fun vibratePattern(context: Context, pattern: LongArray, amplitudes: IntArray? = null) {
        try {
            val vib = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= 26) {
                val effect = if (amplitudes != null) VibrationEffect.createWaveform(pattern, amplitudes, -1)
                else VibrationEffect.createWaveform(pattern, -1)
                vib.vibrate(effect)
            } else {
                vib.vibrate(pattern, -1)
            }
        } catch (_: Exception) {}
    }

    fun scratchTick(context: Context, velocity: Float) {
        val h = when {
            velocity > 20 -> PixelHaptic.HEAVY
            velocity > 10 -> PixelHaptic.MEDIUM
            else -> PixelHaptic.LIGHT
        }
        vibrate(context, h)
    }

    fun win(context: Context, amount: Int) {
        when {
            amount >= 10000 -> jackpot(context)
            amount >= 1000 -> vibrate(context, PixelHaptic.SUCCESS)
            amount > 0 -> vibrate(context, PixelHaptic.MEDIUM)
            amount < 0 -> penalty(context)
        }
    }

    fun jackpot(context: Context) {
        vibratePattern(context, longArrayOf(0, 60, 40, 60, 40, 120), intArrayOf(0, 120, 0, 120, 0, 150))
    }

    fun penalty(context: Context) {
        vibratePattern(context, longArrayOf(0, 80, 40, 80), intArrayOf(0, 60, 0, 60))
    }

    fun prestige(context: Context) {
        vibratePattern(context, longArrayOf(0, 40, 30, 40, 30, 40, 30, 120), intArrayOf(0, 80, 0, 80, 0, 80, 0, 150))
    }

    fun combo(context: Context, combo: Int) {
        repeat(combo.coerceAtMost(4)) {
            vibrate(context, PixelHaptic.COMBO_PULSE)
            Thread.sleep(60)
        }
    }

    fun click(context: Context) = vibrate(context, PixelHaptic.TICK)
    fun levelUp(context: Context) = vibrate(context, PixelHaptic.SUCCESS)
    fun error(context: Context) = vibrate(context, PixelHaptic.LIGHT)

    // Intensity
    var enabled = true
    var intensity = 1f

    fun setHapticIntensity(v: Float) { intensity = v.coerceIn(0f, 1f) }

    // Haptic names
    fun nameFor(h: PixelHaptic): String = when (h) {
        PixelHaptic.LIGHT -> "Hafif"
        PixelHaptic.MEDIUM -> "Orta"
        PixelHaptic.HEAVY -> "Sert"
        PixelHaptic.SUCCESS -> "Başarı"
        PixelHaptic.JACKPOT_LONG -> "Jackpot"
        PixelHaptic.PENALTY_BUZZ -> "Ceza"
        PixelHaptic.TICK -> "Tık"
        PixelHaptic.COMBO_PULSE -> "Kombo"
    }

    fun allHaptics(): List<PixelHaptic> = PixelHaptic.values().toList()

    // Pattern builder
    fun patternForWin(amount: Int): LongArray = when {
        amount >= 10000 -> longArrayOf(0, 60, 40, 60, 40, 120)
        amount >= 1000 -> longArrayOf(0, 40, 30, 80)
        amount > 0 -> longArrayOf(0, 30, 20, 40)
        else -> longArrayOf(0, 80, 40, 80)
    }

    fun randomHaptic(): PixelHaptic = PixelHaptic.values().random()

    fun testAll(context: Context) {
        for (h in PixelHaptic.values()) {
            vibrate(context, h)
            Thread.sleep(300)
        }
    }
}

// ============= COMBINED FEEDBACK =============
object PixelFeedback {
    fun scratch(context: Context, velocity: Float) {
        if (PixelSoundManager.enabled) PixelSoundManager.playScratch(velocity)
        if (PixelHaptics.enabled) PixelHaptics.scratchTick(context, velocity)
    }

    fun reveal(context: Context) {
        if (PixelSoundManager.enabled) PixelSoundManager.play(PixelSound.REVEAL)
        if (PixelHaptics.enabled) PixelHaptics.vibrate(context, PixelHaptic.MEDIUM)
    }

    fun win(context: Context, amount: Int, combo: Int = 0) {
        if (PixelSoundManager.enabled) PixelSoundManager.playWin(amount)
        if (PixelHaptics.enabled) PixelHaptics.win(context, amount)
        if (combo > 1) {
            if (PixelSoundManager.enabled) PixelSoundManager.playCombo(combo)
            if (PixelHaptics.enabled) PixelHaptics.combo(context, combo)
        }
    }

    fun jackpot(context: Context) {
        if (PixelSoundManager.enabled) PixelSoundManager.playJackpot()
        if (PixelHaptics.enabled) PixelHaptics.jackpot(context)
    }

    fun penalty(context: Context) {
        if (PixelSoundManager.enabled) PixelSoundManager.playPenalty()
        if (PixelHaptics.enabled) PixelHaptics.penalty(context)
    }

    fun prestige(context: Context) {
        if (PixelSoundManager.enabled) PixelSoundManager.playPrestige()
        if (PixelHaptics.enabled) PixelHaptics.prestige(context)
    }

    fun click(context: Context) {
        if (PixelSoundManager.enabled) PixelSoundManager.playClick()
        if (PixelHaptics.enabled) PixelHaptics.click(context)
    }

    fun levelUp(context: Context) {
        if (PixelSoundManager.enabled) PixelSoundManager.playLevelUp()
        if (PixelHaptics.enabled) PixelHaptics.levelUp(context)
    }

    // Settings
    fun setSoundEnabled(e: Boolean) { PixelSoundManager.enabled = e }
    fun setHapticEnabled(e: Boolean) { PixelHaptics.enabled = e }
    fun isSoundEnabled(): Boolean = PixelSoundManager.enabled
    fun isHapticEnabled(): Boolean = PixelHaptics.enabled

    fun testFeedback(context: Context) {
        click(context)
        Thread.sleep(200)
        win(context, 100)
        Thread.sleep(300)
        jackpot(context)
    }

    fun randomFeedback(context: Context) {
        when (Random.nextInt(4)) {
            0 -> win(context, Random.nextInt(100, 5000))
            1 -> jackpot(context)
            2 -> penalty(context)
            3 -> prestige(context)
        }
    }

    // Pixel style onomatopoeia
    fun onomatopoeiaFor(amount: Int): String = when {
        amount >= 10000 -> "KA-CHING!!!"
        amount >= 1000 -> "CHA-CHING!"
        amount > 0 -> "DING!"
        amount < 0 -> "BUZZ!"
        else -> "SCRITCH"
    }

    fun comboText(combo: Int): String = when {
        combo >= 10 -> "ULTRA COMBO!!!"
        combo >= 7 -> "MEGA COMBO!!"
        combo >= 5 -> "SUPER COMBO!"
        combo >= 3 -> "COMBO!"
        else -> ""
    }
}
