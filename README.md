# Scritchy Scratchy — Galaxy Watch 8 Edition ⌚✨

> **Scritchy Scratchy oyununun birebir aynısı, Samsung Galaxy Watch 8 için Wear OS 6 optimize klonu.**

Orijinal Steam oyunu **Lunch Money Games / Funday Games** tarafından geliştirilen *Scritchy Scratchy* incremental scratch-card oyununun **tamamen aynı** döngüsünü Galaxy Watch 8'in **480×480 yuvarlak Super AMOLED** ekranında sunar. Bileğinizde kazı, topla, yükselt, prestij yap!

---

## 🎮 Orijinal ile Birebir Aynı Oynanış

Bu port, Scritchy Scratchy'nin tüm temel sistemlerini **aynen** kopyalar:

| Özellik | Orijinal | Watch Sürümü |
|---|---|---|
| **Bulaşık yıkama işi** (giriş) | Tabak sürükle, 1$ kazan | ✅ Aynı — 92dp yuvarlak tabak, sürt/ov |
| **Kartlar** | 7 kart tipi | ✅ 7 kart tipi tamamen aynı |
| **Kazı mekaniği** | Mouse drag ile kaplamayı kazı | ✅ Parmak/dokunmatik drag + bezel uyumlu, %72 açılınca sonuç |
| **Yükseltmeler** | Şans / Güç / Alan (10 seviye) | ✅ Aynı fiyat eğrisi (1.8x / 1.7x / 1.75x) |
| **Gadget** | Scratch Bot (oto-kazı) + Trash Can | ✅ Aynı — 500$ / 300$ |
| **Prestij** | Jack Points (JP) | ✅ Aynı 8 kalıcı upgrade |
| **Kredi/Kredi borç batağı** | Loan Shark 6000% tek sefer faiz | ✅ Aynı “Kredi al +400$” |
| **Haptik** | Tatmin edici scratch sesi | ✅ Titreşim (VibrationEffect) |
| **Kayıt** | Steam Cloud | ✅ DataStore (kalıcı, saatte saklanır) |

### Kart Tipleri (Aynı)

1. **Hızlı Nakit (Quick Cash)** — 5$ → 10$/50$ — 3 alan, 2 eşleşme kazanır
2. **Yılan Gözü (Snake Eyes)** — 15$ → 30$/150$ — 1'li zar = ceza
3. **Elma Ağacı (Apple Tree)** — 30$ → 60$/300$ — Solucan = ceza
4. **Şanslı Kedi (Lucky Cat)** — 60$ → 120$/600$ — Kara kedi = kayıp
5. **Sırtımı Kaşı (Scratch My Back)** — 150$ → 300$/2500$ — Deniz anası büyük, poşet kayıp
6. **Mega Deste (Mega Stack)** — 500$ → 1000$/10000$ — 9 panel, yüksek risk
7. **Son Şans (Final Chance)** — 2500$ → 0$/50000$ — %1 kazan, Prestij tetikler

**Odds, luck formülü ve jackpot sıklığı** orijinal ile aynı: `baseWinChance + luckBonus` (luck başına +4%, Lucky Legacy +15%, Jackpot Magnet 2x)

### Prestij Ağacı (8 Kalıcı Yükseltme)

- 💵 **Başlangıç Sermayesi** (10 JP) — 500$ ile başla
- 🍀 **Şanslı Miras** (15 JP) — +15% kalıcı şans
- 🤖 **Kazı Hafızası** (20 JP) — Oto-kazı baştan açık
- ✨ **Altın Eller** (25 JP) — Tüm ödüller +25%
- 🧲 **Jackpot Mıknatısı** (30 JP) — Jackpot 2x
- ⚡ **Hız Canavarı** (20 JP) — Kazı gücü Lv2 başla
- 🏦 **Banco** (35 JP) — İflas koruması
- ♻️ **Geri Dönüşüm Pro** (15 JP) — Çöp %10 iade

> Prestij: `+2 JP / jackpot`, en az 5 JP, Final Chance bonus 20 JP — orijinal ile aynı.

---

## ⌚ Galaxy Watch 8 Optimizasyonu

**Hedef cihaz:** Samsung Galaxy Watch 8 (44mm & 40mm)

- **Ekran:** 1.47" 480×480 (327 ppi) + 1.34" 438×438 — yuvarlak `CircleShape` + `ScalingLazyColumn`
- **OS:** Wear OS 6 / One UI 8 Watch (API 34, `targetSdk 34`, `minSdk 26`)
- **İşlemci:** Exynos W1000 (3nm, 5 çekirdek) — 2 GB RAM, 32 GB depolama
- **Giriş:** Dokunmatik sürtme + döner bezel (ScalingLazyColumn rotary desteği), tek parmak kazı
- **Haptik:** Her kazı stroke + kazanç/kayıp titreşimi
- **Pil:** 435 mAh (44mm) / 325 mAh (40mm) — düşük güçte `delay(2200)` oto-kazı döngüsü
- **AOD:** Koyu casino teması (#121212) ile yanma önleme

UI özellikle **yuvarlak** için tasarlandı:
- Üstte sabit bakiye kapsülü (JP + oto-bot göstergesi)
- Altta 4 nokta sekme göstergesi (Scratch / Shop / Upgrades / Prestige)
- Kartlar `RoundedCornerShape(16dp)` + metalik kazı kaplaması (`BlendMode.Clear` + `CompositingStrategy.Offscreen`)

---

## 🔄 Workflow ile Otomatik APK Derleme

Her `push` / `pull_request` / `workflow_dispatch` tetiklediğinde **GitHub Actions** otomatik APK derler.

**Workflow dosyaları:**
- `.github/workflows/build.yml` — ana derleme (debug + release, artifact + release)
- `.github/workflows/build-apk.yml` — kısa isim, aynı iş

### Ne yapar?

```yaml
on: [push (main, arena/**), pull_request, workflow_dispatch]

jobs:
  build:
    - JDK 17 (Temurin) + Android SDK 34
    - Gradle 8.7 (wrapper yoksa otomatik oluştur)
    - ./gradlew assembleDebug + assembleRelease
    - APK'leri artifact olarak yükle (30 gün saklama)
    - Tag push'ta (v*) otomatik GitHub Release oluştur
```

### APK'leri indirme

1. GitHub repo → **Actions** sekmesi
2. En son **Build Galaxy Watch 8 APK** çalışmasına tıkla
3. **Artifacts** → `ScritchyScratchy-Watch-Debug-APK` (veya `GalaxyWatch8-APK`) indir
4. Zip içindeki `app-debug.apk` dosyası

### APK'yi saate yükleme (ADB)

```bash
# Saatte: Ayarlar → Saat hakkında → Yazılım → Derleme numarasına 7 kez dokun (Geliştirici modu)
# Saatte: Geliştirici seçenekleri → ADB hata ayıklama + Kablosuz hata ayıklama → Açık

# Bilgisayarda (aynı Wi-Fi):
adb pair <SAAT_IP>:<PAIR_PORT>  # Saatteki eşleşme kodu ile
adb connect <SAAT_IP>:<PORT>

# Yükle:
adb install app/build/outputs/apk/debug/app-debug.apk
# veya indirdiğin artifact'ten:
adb install app-debug.apk

# Log:
adb logcat | grep scritchy
```

> **Mağaza yok, direkt APK** — Galaxy Watch 8'e yan yükleme (sideload) ile çalışır. Wear OS standalone (`com.google.android.wearable.standalone=true`).

---

## 🛠️ Yerelde Derleme

```bash
git clone https://github.com/StrongBomber/ScratchCards.git
cd ScratchCards

# Android SDK 34 + JDK 17 gerekli
./gradlew assembleDebug
# veya wrapper jar yoksa:
gradle wrapper --gradle-version 8.7
./gradlew assembleDebug

# APK:
# app/build/outputs/apk/debug/app-debug.apk
# app/build/outputs/apk/release/app-release-unsigned.apk
```

**Gereksinimler:**
- Android Studio Hedgehog+ / SDK Platform 34
- JDK 17
- Gradle 8.7, AGP 8.4.2, Kotlin 1.9.22, Compose BOM 2024.02.00
- Wear OS Compose 1.3.1

---

## 📁 Proje Yapısı

```
ScratchCards/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml          # watch feature, vibrate, standalone
│   │   ├── java/com/scritchyscratchy/watch/
│   │   │   ├── MainActivity.kt          # 4 sekmeli Wear UI (ScalingLazyColumn)
│   │   │   ├── GameModels.kt            # 7 kart, 8 prestij, odds/luck formülü
│   │   │   ├── GameViewModel.kt         # DataStore kayıt, shop, prestige, oto-kazı
│   │   │   └── ScratchCardView.kt       # Canvas kazı (BlendMode.Clear, haptik)
│   │   └── res/
│   │       ├── mipmap-*/ic_launcher.png # 480x480 optimize ikon (altın kazı)
│   │       └── values/strings.xml
│   ├── build.gradle.kts                 # Wear OS 6, 480x480, minSdk 26
│   └── proguard-rules.pro
├── gradle/wrapper/
├── .github/workflows/
│   ├── build.yml                        # Tam workflow (debug+release+release)
│   └── build-apk.yml                    # Kısa APK workflow
└── README.md
```

---

## 🔁 Oyun Döngüsü (Orijinal ile birebir)

```
Bulaşık yıka (1$ / tabak) → 5$ → Dükkan → Kart al (5-2500$)
    ↓ drag ile kazı (%72 açılınca sonuç)
    ↓ +payout / -ceza / JACKPOT (+5 JP)
    ↓ Seviye atla (+25% ödül / seviye)
    → Yükselt: Şans (1.8x), Güç (1.7x), Alan (1.75x)
    → Gadget: Scratch Bot (oto 2.2sn döngü) / Çöp
    → Prestij: Jackpot sayısına göre JP → kalıcı ağaç → sıfırlama
    → Sonsuz döngü, her koşu daha hızlı
```

**Tıpkı Steam açıklamasındaki gibi:** *“Boosted odds and payouts • Faster and easier scratching • New scratch card variants with unique rules • Prestige to reset, gather Jack Points”*

---

## 🌐 Dil

- **Varsayılan Türkçe**, İngilizce kart isimleri de korunuyor
- Saatin dili fark etmeksizin oynanabilir (emoji + $ + JP evrensel)

---

## 📄 Lisans & Teşekkür

- Orijinal oyun: **© Lunch Money Games / Funday Games** — Steam `3948120`
- Bu port **hayran yapımı**, kar amacı gütmeyen, eğitim amaçlı bir Wear OS uyarlamasıdır. Orijinal fikre saygı duyulur.
- Kod: MIT (bu repo)
- İkon: özel altın kazı (Pillow ile üretildi, ticari marka değil)

---

## 🚀 Hızlı Başlangıç

1. Actions → APK indir → `adb install`
2. Saatte uygulamayı aç → **Bulaşık yıka** (4 tabak ov)
3. **Dükkan** → Hızlı Nakit 5$ al
4. **Kazımak için sürt** → %72 → **TOPLA**
5. **Yükselt** → Şans Lv4'e kadar öncelik
6. **Oto-kazı** 500$ → izlerken kazan
7. **Prestij** → Jack Points ile kalıcı bonus → tekrar daha hızlı

**İyi kazımalar! 🍀💰**

> Galaxy Watch 8'in bezelini çevirerek listelerde gezin, parmağınla kazı — Scritchy Scratchy artık bileğinde, tamamen aynı!
