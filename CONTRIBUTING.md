# Contributing to Calculate ⚡

Thank you for your interest in contributing to **Calculate** by CodeStitcher! We welcome pull requests, bug reports, and suggestions from developers worldwide.

---

## 🛠️ Development Setup

1. **Prerequisites**:
   - Android Studio Iguana / Jellyfish / Koala or newer.
   - JDK 21 (Temurin or JetBrains Runtime recommended).
   - Android SDK Platform 34 (Android 14+).

2. **Clone the Repository**:
   ```bash
   git clone https://github.com/MustafijRatul/Calculate.git
   cd Calculate
   ```

3. **Build the Project**:
   ```bash
   ./gradlew assembleDebug
   ```

---

## 📐 Code Style & Architecture
- **Language**: Kotlin 2.0+
- **UI Toolkit**: Jetpack Compose with Material 3 design system.
- **State Management**: Reactive state holding via Compose `remember`, `mutableStateOf`, and Coroutines.
- **Architecture**: Clean modular separation (`ui`, `data`, `calculation`, `navigation`).
- **Formatting**: Keep code clean, idiomatic, and follow standard Kotlin naming conventions.

---

## 🚀 Submitting a Pull Request
1. Fork the repository.
2. Create your feature branch (`git checkout -b feature/AmazingFeature`).
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`).
4. Push to the branch (`git push origin feature/AmazingFeature`).
5. Open a Pull Request with a clear description of your improvements.
