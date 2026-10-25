# Project Context & Constraints: Khoroch (খরচ)

## 1. Product Goals & Vision
- Enable users in Bangladesh to effortlessly track mobile wallet (bKash, Nagad, Rocket, Upay) and bank transactions automatically without manual data entry.
- Preserve 100% data ownership on the device. No servers, no accounts, no analytics, no external APIs.
- Cold start under 1.5s on entry-level/mid-range Android devices (minSdk 26+).
- Parse accuracy >= 95%, category accuracy >= 75% out-of-the-box, 85% with merchant memory.

## 2. Core Constraints & Invariants
- **No Internet Permission**: `android.permission.INTERNET` is strictly forbidden in MVP to provide users an ironclad privacy guarantee.
- **Paisa-Level Precision**: All monetary values are represented as integer `Long` paisa (1 Taka = 100 Paisa) to completely eliminate floating-point rounding errors.
- **Strict Deduplication**: Every incoming transaction is uniquely identified via `sha256(provider + ":" + referenceId)` or fallback hash `sha256(sender + ":" + normalizedBody + ":" + minuteBucket)`.
- **Pure Kotlin Parsers**: The parser module does not depend on any Android SDK classes, allowing high-speed JVM unit testing and evaluation scripts.
- **Two-Tap Correction**: Changing a category prompts with "Apply to all past and future entries from {merchant}?", establishing merchant memory in one tap.
- **Bilingual & South Asian Formatting**: First-class support for Bangla and English. Numbers grouped as lakh and crore (e.g., `১,২৫,০০০` / `1,25,000`).
- **Resilient Background Processing**: Live SMS captured via BroadcastReceiver; parsing & saving scheduled with WorkManager expedited jobs; history import runs as a resumable foreground job.

## 3. Technology Stack Decisions
- **Language**: Kotlin 2.0.21
- **UI**: Jetpack Compose + Material 3, custom Compose Canvas charts (no heavy third-party chart dependencies)
- **Database**: SQLite via AndroidX Room with FTS4 full-text search
- **State Management**: Reactive MVVM / UDF with Kotlin `StateFlow`, `SharedFlow`, and `collectAsStateWithLifecycle`
- **Preferences**: Jetpack DataStore Preferences
- **Security**: Android Biometrics (`androidx.biometric`) + AES-256-GCM / PBKDF2 encryption for `.khoroch` backups
- **Build System**: Gradle 8.11 with Kotlin DSL (`build.gradle.kts`)
