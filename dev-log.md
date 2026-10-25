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

### [2026-10-25 14:35:40 +06:00] - Pushed to GitHub Remote
- **Remote**: `git@github.com:smile-plzz/Khoroch-.git` (branch `main`).
- **Commits Synced**:
  - `f43d723..9db8177`: Handoff documentation merged, full Android MVP codebase, tests, assets, and configs pushed.
- **Verification**: Remote branch `origin/main` synchronized.

### [2026-10-25 14:55:00 +06:00] - Interactive Suggestions & Feature Test Suite Expansion
- **Action**:
  - Implemented `SmartSuggestionEngine` for dynamic, context-aware category recommendations (morning commute, lunch, evening bazar, merchant memory, money flow direction).
  - Modernized `AddTransactionSheet` with interactive tailored suggestion chips displaying category icons and contextual reasoning.
  - Added modern, minimal Smart Financial Insight card to `HomeScreen` summarizing top spend category and proportion.
  - Expanded unit test suite to 33 tests across all features (`BankParsersTest`, `SmartSuggestionEngineTest`, `BudgetFeatureTest`, `DeduplicationAndSecurityTest`, `SmsParsingEngineTest`, `MoneyFormatterTest`, `CategorizationEngineTest`).
- **Verification**:
  - `.\gradlew.bat testDebugUnitTest`: **33/33 tests PASSED**.
  - `.\gradlew.bat assembleDebug`: **BUILD SUCCESSFUL** -> `app-debug.apk` built.
