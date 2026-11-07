<div align="center">

# Calculate ⚡
### *The Ultra-Sleek, 120 FPS Scientific Calculator & World Currency Converter for Android*

[![Android CI](https://github.com/MustafijRatul/Calculate/actions/workflows/android.yml/badge.svg)](https://github.com/MustafijRatul/Calculate/actions/workflows/android.yml)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.0-7F52FF?style=flat&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20M3-4285F4?style=flat&logo=android&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Platform](https://img.shields.io/badge/Platform-Android%208.0%2B%20(API%2026%2B)-3DDC84?style=flat&logo=android&logoColor=white)](https://developer.android.com)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Engineered by](https://img.shields.io/badge/Engineered%20by-CodeStitcher-000000?style=flat&logo=github)](https://github.com/MustafijRatul)

<p align="center">
  <b>A production-grade, ultra-fluid Android application combining precision scientific mathematics, real-time daily currency exchange, unit conversion, and a cinematic studio brand intro.</b>
</p>

</div>

---

## 📑 Table of Contents
- [Key Highlights](#-key-highlights)
- [Deep Dive Features](#-deep-dive-features)
  - [1. Cinematic Studio Intro](#1--cinematic-studio-intro)
  - [2. Minimalist Scientific & Standard Calculator](#2--minimalist-scientific--standard-calculator)
  - [3. Real-Time World Currency Converter](#3--real-time-world-currency-converter)
  - [4. Multi-Category Unit Converter](#4--multi-category-unit-converter)
  - [5. Dual Theme System](#5--dual-theme-system)
- [Project Architecture](#-project-architecture)
- [Tech Stack](#-tech-stack)
- [Getting Started & Build Instructions](#-getting-started--build-instructions)
- [Contributing](#-contributing)
- [License](#-license)

---

## ⚡ Key Highlights
- **100% Native Kotlin & Jetpack Compose (Material 3)**: Zero legacy hybrid overhead; guaranteed 120 FPS / 60 FPS silky smooth interactions.
- **Zero Cold-Start Lag**: OS-level pitch-black theme configuration eliminating any gray/ash screen flash.
- **Casio fx-991EX ClassWiz Mathematical Engine**: Full scientific capabilities with robust operator precedence and multi-digit precision.
- **Offline-First & Daily Live Currency Sync**: Seamless daily exchange rates fetched from open market APIs with persistent local caching.

---

## 🚀 Deep Dive Features

### 1. 🎬 Cinematic Studio Intro
- **Vector Outline Tracing**: Custom hardware-accelerated Compose GPU Canvas dynamically traces the CodeStitcher mark and typography.
- **1-Loop Polish (3.2s)**: Traces capsule contours, applies crisp white fill, and cuts the inverted black stitch before seamlessly transitioning into the calculator.
- **Tap-To-Skip**: Instant bypass available at any moment for immediate calculations.

### 2. 🧮 Minimalist Scientific & Standard Calculator
- **Bottom-Anchored Ergonomics**: Keypad naturally aligned to the lower screen edge for comfortable one-handed thumb reach with zero floating black gaps.
- **Adaptive OLED Display**: Expands vertically to absorb available space with live preview evaluations and single-line expression formatting.
- **Casio fx-991EX Scientific Keypad**:
  - **Trigonometric Functions**: $\sin$, $\cos$, $\tan$, $\sin^{-1}$, $\cos^{-1}$, $\tan^{-1}$ (supporting Degree/Radian modes).
  - **Exponentials & Logarithms**: $\ln$, $\log_{10}$, $e^x$, $10^x$, $x^y$, $\sqrt{x}$, $\sqrt[3]{x}$, $\sqrt[n]{x}$.
  - **Discrete Mathematics**: Permutations ($nPr$), Combinations ($nCr$), Modulo ($MOD$), Factorials ($x!$).
  - **Constants & History**: $\pi$, $e$, Answer recall ($ANS$), and full persistent calculation history bottom sheet.

### 3. 💱 Real-Time World Currency Converter
- **70+ Global Currencies**: Features high-resolution official Unicode country flags (e.g., 🇧🇩 Bangladesh, 🇺🇸 United States, 🇪🇺 European Union, 🇬🇧 United Kingdom, 🇸🇦 Saudi Arabia, 🇮🇳 India, 🇯🇵 Japan, 🇦🇪 UAE).
- **Alphabetical A–Z Grouping**: Clean circular letter badges and hairline dividers for rapid visual scanning.
- **Real-Time Live Search**: Instant filtering by country name, currency name, or 3-letter currency code.
- **Daily Auto-Sync Engine**: Auto-checks and updates rates once every 24 hours via `open.er-api.com` with zero impact on startup time.

### 4. 📏 Multi-Category Unit Converter
- Comprehensive unit conversions covering **Length**, **Weight**, **Temperature**, and **Speed** with instant two-way synchronization.

### 5. 🎨 Dual Theme System
- **Deep OLED Dark Mode**: Power-efficient true blacks (`#000000` / `#0F1216`) with glowing cyan accents.
- **Studio Light Mode**: Clean, high-contrast minimalist white interface.
- **Tactile Haptic Feedback**: Customized subtle haptic vibrations on every keypress.

---

## 🏛 Project Architecture

```
Calculate/
├── .github/workflows/           # Automated GitHub Actions CI/CD pipeline
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml  # Permissions and black splash theme declarations
│   │   ├── java/codestitcher/calcpro/
│   │   │   ├── MainActivity.kt                  # Compose root with crossfade navigation
│   │   │   ├── calculation/
│   │   │   │   └── ExpressionEvaluator.kt       # Casio fx-991EX math parser & evaluator
│   │   │   ├── data/
│   │   │   │   ├── CurrencyRepository.kt        # 70+ countries & daily auto-sync engine
│   │   │   │   └── HistoryRepository.kt         # Persistent equation history
│   │   │   ├── navigation/
│   │   │   │   └── Screen.kt                    # Type-safe navigation routes
│   │   │   ├── ui/
│   │   │   │   ├── components/                  # Reusable CalculatorButton & TopAppBar
│   │   │   │   ├── screens/                     # Calculator, Currency, Units, Splash
│   │   │   │   ├── theme/                       # Color, Type, and Material3 Theme
│   │   │   │   └── util/                        # Haptic feedback managers
│   │   └── res/                                 # Drawables, mipmaps, and themes
│   └── build.gradle.kts                         # App module configuration
├── build.gradle.kts                             # Top-level build configuration
├── gradle.properties                            # Memory tuning & JVM arguments
└── README.md                                    # Project documentation
```

---

## 🛠 Tech Stack
| Component | Technology |
|---|---|
| **Language** | Kotlin 2.0+ |
| **UI Framework** | Jetpack Compose (Material 3) |
| **Animation Engine** | Compose GPU Canvas & PathMeasure |
| **Asynchronous Programming** | Kotlin Coroutines & Flow |
| **Networking** | Standard Android `HttpURLConnection` + JSON |
| **Storage** | Android `SharedPreferences` |
| **Build Tool** | Gradle 8.7 with Kotlin DSL |
| **Minimum SDK** | API 26 (Android 8.0 Oreo) |
| **Target SDK** | API 34 (Android 14 UpsideDownCake) |

---

## 💻 Getting Started & Build Instructions

### Prerequisites
- Android Studio Iguana (2023.2.1) or newer
- JDK 21 installed

### Build via Command Line
```powershell
# Clone the repository
git clone https://github.com/MustafijRatul/Calculate.git
cd Calculate

# Assemble Debug APK
./gradlew assembleDebug
```
The compiled output will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`

---

## 🤝 Contributing
Contributions are always welcome! Feel free to review [CONTRIBUTING.md](CONTRIBUTING.md), open an issue, or submit a pull request.

---

## 📄 License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

<div align="center">
  <sub>Engineered with precision by <b>CodeStitcher</b></sub>
</div>
