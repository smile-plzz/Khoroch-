# Khoroch (খরচ) - Offline-First Expense Tracker for Android

> **Track expenses automatically from mobile wallet and bank SMS without accounts, internet connection, or manual data entry.**

Khoroch is an offline-first Android application specifically engineered for Bangladesh. It reads structured SMS notifications from bKash, Nagad, Rocket, Upay, and major Bangladeshi banks, turning incoming transactions into categorized expense and income records with zero typing and strict privacy guarantees (zero internet permissions declared in MVP).

---

## 🌟 Key Features

- **Automatic SMS Capture & Parsing**: Live broadcast receiver and 90-day history batch importer for bKash, Nagad, Rocket, Upay, and bank SMS.
- **Strictly Offline & Privacy-First**: No internet permission (`android.permission.INTERNET` is never declared). Zero telemetry, zero external trackers, 100% on-device SQLite database.
- **Smart Rule & Memory Categorization**: Precedence-based categorizer (User override > Merchant memory > Keyword rules > Provider defaults > Default uncategorized).
- **Bangla-First Experience**: Complete bilingual UI (Bangla and English), optional Bangla numeral rendering (`০, ১, ২, ...`), and South Asian number formatting (lakh/crore: `১,২৫,০০০`).
- **Comprehensive Dashboard**: Monthly spending totals, category breakdown donut chart, daily spending trends with average thresholds, safe-to-spend daily calculations.
- **Monthly Budgets**: Category-level and overall limits with 80% and 100% warnings.
- **Manual Logging with Fast Keypad**: Custom numerical keypad for rapid cash logging in under 10 seconds.
- **Biometric Security & App Lock**: `BiometricPrompt` with PIN fallback and `FLAG_SECURE` screen shielding.
- **Safe Export & Backup**: UTF-8 BOM CSV export and AES-256-GCM encrypted backup (`.khoroch`).

---

## 🛠️ Tech Stack & Architecture

- **OS / Min SDK**: Android 8.0 (API 26) up to Android 15 (API 35+)
- **Language**: Kotlin 2.0.21
- **UI Framework**: Jetpack Compose with Material 3, Custom Canvas Charts
- **Architecture**: Clean Architecture & MVI/Unidirectional Data Flow (UDF)
  - `core:model`: Pure Kotlin data models
  - `core:parser`: Pure Kotlin SMS parsing engine (100% JVM unit-testable, no Android framework dependencies)
  - `core:categorizer`: Rule engine & merchant memory
  - `core:database`: Room SQLite with FTS4 full-text search
  - `core:data`: Single write-path repositories emitting reactive `Flow`
  - `feature:*`: Compose UI screens & ViewModels
- **Storage**: Room Database (`khoroch.db`), Jetpack DataStore (Preferences)
- **Background Processing**: AndroidX WorkManager (Expedited jobs & Periodic maintenance)
- **Serialization**: `kotlinx.serialization` (JSON parsing for `rules.json`, `senders.json`)
- **Security**: Android Biometrics API & PBKDF2/AES-GCM encryption

---

## 🚀 Setup & Build Instructions

### Prerequisites
- JDK 21 (Eclipse Temurin 21 recommended)
- Android SDK (API 35, Build-Tools 35.0.0+)
- Gradle 8.11+ (bundled with `gradlew`)

### Build & Run
```powershell
# Clone or navigate to the directory
cd C:\Users\ismai\.gemini\antigravity\scratch\khoroch

# Build debug APK
.\gradlew.bat assembleDebug

# Run Unit Tests (Parser, Categorizer, Formatter)
.\gradlew.bat testDebugUnitTest
```

---

## 📂 Project Structure

```text
khoroch/
├── app/                      # Android Application module & Compose UI
│   ├── src/main/
│   │   ├── java/app/khoroch/
│   │   │   ├── data/         # Repositories & Room DAOs
│   │   │   ├── parser/       # SMS Parsers (bKash, Nagad, Rocket, Bank)
│   │   │   ├── categorizer/  # Rule engine & normalization
│   │   │   ├── ui/           # Compose theme, screens, components & charts
│   │   │   ├── receiver/     # SMS BroadcastReceiver
│   │   │   └── worker/       # WorkManager background workers
│   │   ├── assets/           # senders.json, rules.json
│   │   └── res/              # Strings (en, bn), icons, themes
├── docs/                     # Product specs, architecture diagrams
├── README.md                 # Project overview and setup
├── context.md                # System goals, constraints, tech choices
├── dev-log.md                # Development log with timestamps
└── CHANGELOG.md              # Versioned history
```
