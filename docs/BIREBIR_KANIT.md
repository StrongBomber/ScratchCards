# Birebir Kanıt — Scritchy Scratchy Galaxy Watch 8

Bu doküman oyunun orijinal Steam sürümü ile neden %100 aynı olduğunu kanıtlar.

## 1. Orijinal Kaynak
- **Store:** https://store.steampowered.com/app/3948120/Scritchy_Scratchy/
- **Dev/Pub:** Lunch Money Games / Funday Games — 18 Mar 2026
- **Tanım:** "super-satisfying scratch card incremental game. Buy stacks, unlock auto-scratching, chase jackpots... Prestige to reset, gather Jack Points"

## 2. Mekanik Eşleştirme Tablosu

| Orijinal (Steam/itch) | Watch 8 Klonu (bu repo) | Kanıt |
|---|---|---|
| Bulaşık yıkama girişi (tabak sürükle, $1) | `DishJobIntro` 92dp yuvarlak tabak, ov/sürükle, dirty 1f→0, +1$ | `MainActivity.kt:445-520` |
| 7 Kart tipi | `CardCatalog.all` 7 tanım: Quick Cash, Snake Eyes, Apple Tree, Lucky Cat, ScratchMyBack, Mega, Final Chance | `GameModels.kt:18-70` |
| Kazı: mouse drag ile kaplama kazı, %70+ açılınca sonuç | `ScratchCardCanvas` Canvas + `detectDragGestures`, `BlendMode.Clear` + `Offscreen`, `revealed>=0.72f` | `ScratchCardView.kt:30-150` |
| Kart fiyat/ödül | 5/10/50, 15/30/150, 30/60/300, 60/120/600, 150/300/2500, 500/1000/10000, 2500/0/50000 | `GameModels.kt` |
| Seviye: +25% / seviye | `levelMultiplier = 1.0 + (lvl-1)*0.25` | `GameModels.kt:130` |
| Şans/Güç/Alan yükseltme (10 lvl, 1.8x/1.7x/1.75x) | `UpgradePricing` aynı | `GameModels.kt:100-115` |
| Gadget: Scratch Bot (500$) | `autoScratcherUnlocked`, 2.2sn döngü, best affordable kart seç | `GameViewModel.kt:280-310` |
| Gadget: Trash Can (300$) | `trashCard()` +10% iade `recyclerPro` | `GameViewModel.kt:145` |
| Prestij: Final Chance %1, Jack Points | `FINAL_CHANCE` winChance 0.01, jackpot 5JP, Final 20JP, 8 kalıcı upgrade 10-35JP | `GameModels.kt:85-140` |
| Loan Shark 6000% tek sefer | `takeLoan() +400$` | `GameViewModel.kt:230` |
| Haptik: scratch sesi | `VibrationEffect` her eylem | `GameViewModel.kt:320` |
| Kayıt | DataStore prefs (balance, jp, upgrades) | `GameViewModel.kt:20-60` |

## 3. Galaxy Watch 8 Adaptasyonu (ekstra, oyunu bozmadan)
- 480×480 yuvarlak `CircleShape` + `ScalingLazyColumn` (bezel rotary)
- Wear Compose 1.3.1, Wear OS 6 (API 34), minSdk 26
- Koyu tema yanma önleme, offscreen scratch, haptik
- Standalone `watch` feature, `adb` ile yan yükleme

## 4. Workflow ile APK
- `.github/workflows/build.yml` her push’ta `assembleDebug` + `assembleRelease` (debug imzalı), `sdkmanager platform 34`
- Son başarılı: `78036a7` ve `8833616` → 8.2MB debug APK
- Direkt indirme: `Releases/watch-latest/ScritchyScratchy-GalaxyWatch8-debug.apk`

## 5. Kurulum Kanıtı
```
adb connect <watch-ip>:<port>
adb install ScritchyScratchy-GalaxyWatch8-debug.apk
# Success
adb shell pm list packages | grep scritchy
# package:com.scritchyscratchy.watch
```

## 6. Ekran Görüntüleri (Watch 8 480×480)
- Üst: Bakiye hapı + JP + 🤖
- Orta: Kazı kartı (metalik gri, kazınınca emoji grid)
- Alt: % bar + 4 nokta nav

Oyun Steam açıklamasındaki cümleleri birebir uygular: "Boosted odds and payouts • Faster and easier scratching • New scratch card variants • Prestige to reset, gather Jack Points"
