# Calculate (Native Android Edition) ⚡

A high-performance, ultra-smooth, native Android calculator by **CodeStitcher**, built with **Kotlin** and **Jetpack Compose (Material 3)**.

Designed for peak elegance, minimal aesthetic, instant cold startup, 120 FPS hardware acceleration, spring physics, tactile haptic feedback, and a cinematic studio brand intro.

---

## 🌟 Key Features

### 1. 🎬 CodeStitcher Cinematic Studio Intro
- **Zero Cold-Start Flicker**: Android OS window and splash themes configured in pure pitch-black (`#000000`).
- **100% Native Compose GPU Canvas**: 120 FPS hardware-accelerated vector drawing of the CodeStitcher capsule mark and typography.
- **Game-Studio Polish**: 1-loop (3.2s) outline tracing and fill with tap-to-skip support and seamless 500ms crossfade transition into the calculator.

### 2. 🧮 Professional Scientific & Standard Calculator
- **Minimalist, Elegant Top Bar**: Clean utility icons for quick access to history, units, currency, and scientific mode without distracting branding or blinking dots.
- **Natural Bottom-Anchored Keypad**: Perfectly proportioned tactile buttons brought down to the lower screen area for effortless, comfortable one-handed thumb reach.
- **Smart Adaptive Display Card**: Expands smoothly to fill vertical space with crisp, auto-scrolling single-line expressions and instant live result preview.
- **Casio fx-991EX Scientific Keypad**: Expandable scientific mode with SHIFT/2nd toggle supporting:
  - Trigonometry & Inverse Trig (`sin`, `cos`, `tan`, `sin⁻¹`, `cos⁻¹`, `tan⁻¹`)
  - Logarithms & Exponentials (`ln`, `log`, `eˣ`, `10ˣ`, `xʸ`, `√`, `³√`, `ⁿ√`)
  - Permutations & Combinations (`nPr`, `nCr`, `MOD`)
  - Factorials, Absolute values, Powers (`x!`, `|x|`, `x²`, `x³`, `x⁻¹`)
  - Constants & Answer Memory (`π`, `e`, `ANS`)
- **Calculation History Sheet**: Persistently saves calculations so you can recall past equations with a single tap.

### 3. 💱 Real-Time World Currency Converter
- **70+ World Countries & Currencies**: Includes official Unicode high-resolution country flag emojis (`🇧🇩`, `🇺🇸`, `🇪🇺`, `🇬🇧`, `🇸🇦`, `🇮🇳`, `🇯🇵`, `🇦🇪`, etc.).
- **Alphabetical A–Z Grouping**: Countries cleanly grouped with minimal, elegant circular badge headers and hairline dividers.
- **Daily Auto-Syncing Exchange Rates**: Automatically fetches live market rates once a day via open exchange API, with local caching for full offline operation.
- **Instant Search**: Real-time filtering by country name, currency name, or currency code.

### 4. 📏 Multi-Category Unit Converter
- Supports Length, Weight, Temperature, and Speed with precise formula conversions.

### 5. 🎨 Minimalist Dual Theme System
- One-tap toggle between **Deep OLED Dark Mode** and **Clean Minimalist Light Mode**.
- Custom spring-physics tactile buttons with responsive haptic feedback.

---

## 📱 How to Run & Build
1. Open **Android Studio**.
2. Select **File -> Open** and choose `CalcPro`.
3. Press **Run** (`Shift + F10`) to launch on your Android device or emulator!
4. Or build an APK via terminal:
   ```powershell
   ./gradlew assembleDebug
   ```
   The APK will be generated at `app/build/outputs/apk/debug/app-debug.apk`.
