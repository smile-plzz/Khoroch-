# Development Log - Khoroch (খরচ)

### [2026-10-25 14:06:00 +06:00] - Project Kickoff & Workspace Setup
- **Action**: Initialized Git repository and created foundational documentation (`README.md`, `context.md`, `dev-log.md`, `CHANGELOG.md`).
- **Target**: Set up Gradle 8.11 with Kotlin 2.0.21, AGP 8.7.2, and Android SDK 35 configuration.
- **Architectural Scope**:
  - Pure Kotlin parser suite for bKash, Nagad, Rocket, Upay, and major Bangladeshi banks.
  - Room database schema supporting transactions, accounts, categories, budgets, merchant memory, raw SMS messages, and deduplication hashes.
  - MoneyFormatter utility supporting South Asian number grouping (lakh & crore) and bilingual numeral conversion.
  - Jetpack Compose Material 3 UI: Home Dashboard (with Canvas Donut chart & Daily bar chart), Activity List with swipe actions, Transaction Detail with split/edit, Custom Keypad Add Sheet, Budgets Screen, and Settings.
- **Verification**: Local directory initialized, gradlew checked with Java 21.

### [2026-10-25 14:25:00 +06:00] - Android MVP Build & Verification Complete
- **Status**: SUCCESS
- **Components Built**:
  - `core:model`: Pure Kotlin data classes (`ParsedTxn`, `ParseResult`, `Direction`, `CategorySource`).
  - `core:common`: `MoneyFormatter` (South Asian grouping, lakh & crore formatting, Bangla numerals `০-৯`), `HashUtils` (SHA-256 deduplication).
  - `core:parser`: `BkashParser`, `NagadParser`, `RocketParser`, `UpayParser`, `BankParser` (City Bank, BRAC, EBL, DBBL, SCB), `GenericFallbackParser`, `SmsParsingEngine`.
  - `core:categorizer`: Precedence engine (`CategorizationEngine`), `MerchantNormalizer`, bundled `rules.json` & `senders.json`.
  - `data`: SQLite Room database (`KhorochDatabase`), Room DAOs with FTS4 and reactive Kotlin Flow, `TransactionRepository`, DataStore `PreferencesRepository`.
  - `ui`: Material 3 design system tokens, `KhorochTheme`, custom Compose Canvas `DonutChart` & `DailyBarChart`, `AmountKeypad`, `AddTransactionSheet`, `HomeScreen`, `ActivityScreen`, `BudgetsScreen`, `SettingsScreen`, `TransactionDetailDialog`, `UnparsedScreen`, `OnboardingScreen`.
  - `receiver` & `worker`: Manifest-declared `SmsReceiver` (SMS_RECEIVED), `HistoryImportWorker` for 90-day background import.
- **Verification**:
  - `.\gradlew.bat testDebugUnitTest`: 16/16 tests PASSED in 35s.
  - `.\gradlew.bat assembleDebug`: BUILD SUCCESSFUL in 1m 47s -> `app/build/outputs/apk/debug/app-debug.apk` generated.
