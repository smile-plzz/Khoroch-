# Offline-First Expense Tracker: Android Build Handoff

Oct 25, 2026 · @Ismail

## 1. Product overview

Khoroch (working title, Bangla for "expense") is an Android app that reads bKash, Nagad, Rocket and bank SMS on the phone and turns each message into a categorized expense or income entry, with no account, no server and no internet connection required.

**Problem.** Most Bangladeshi spending now passes through mobile wallets and cards, and each payment already produces a structured SMS. Existing expense apps assume manual entry or Western bank APIs, so people stop logging within two weeks.

**Vision.** Open the app, and today's spending is already there, correct and sorted, in Bangla or English.

**Goals**

- Capture at least 90% of wallet and bank transactions automatically with zero typing.
- Let the user fix any wrong entry in two taps or fewer.
- Keep every byte of data on the device. No network permission in the MVP.
- Cold start under 1.5 seconds on a 3 GB RAM mid-range phone. Works on Android 8.0 (API 26) and above.

**Non-goals for the MVP**

- No cloud sync, no accounts, no login.
- No bank API or open banking integration.
- No bill reminders, no investment tracking, no multi-currency (BDT only).
- No iOS version.
- No sharing with other people (family or group budgets come later).

**Success metrics**

| Metric | Definition | MVP target |
| --- | --- | --- |
| Parse accuracy | Share of test SMS where amount, direction, provider and counterparty are all correct | 95% or higher |
| Category accuracy | Share of parsed transactions given the right category with no user edit | 75% at launch, 85% after 30 days of merchant memory |
| Correction effort | Median taps to fix a wrong category | 2 or fewer |
| Week-4 retention | Installs still opening the app weekly after 4 weeks | 40% or higher |
| Crash-free sessions | Sessions with no crash | 99.5% or higher |
| Cold start | Time from tap to first rendered Home frame | Under 1.5 seconds |

## 2. Users and use cases

The primary user is a smartphone owner in Bangladesh who pays mostly through bKash, Nagad or a card and wants to know where the money went without doing bookkeeping.

**Personas**

| Persona | Profile | What they need | Design consequence |
| --- | --- | --- | --- |
| Rafi, 24, junior employee | Salaried, pays rent, food and transport by wallet, tight at month end | See spending by category and a daily safe-to-spend figure | Home screen leads with month total and budget remaining |
| Nusrat, 31, freelancer | Irregular income from several sources, mixes personal and work spending | Track income and expenses separately, tag work spending | Income entries, tags, filter by tag |
| Karim, 45, shopkeeper | Low comfort with apps, prefers Bangla, sends and receives many small payments | Big text, Bangla numerals, almost no setup | Bangla-first onboarding, large touch targets, auto-everything |

**Core user stories (MVP)**

1. As a user, I grant SMS access once and see my last 90 days of wallet transactions imported in under 30 seconds.
2. As a user, I see every new payment appear in the app within 5 seconds of the SMS arriving, with a category already set.
3. As a user, I tap an entry and change its category, and the app applies the same category to future payments to the same merchant.
4. As a user, I add a cash expense manually in under 10 seconds using an amount keypad and category chips.
5. As a user, I see this month's total, a category breakdown and a daily trend, and I can move to any past month.
6. As a user, I set a monthly budget per category and get a warning at 80% and 100%.
7. As a user, I search and filter entries by text, category, provider, date range and amount range.
8. As a user, I switch between Bangla and English, and between light and dark theme, at any time.
9. As a user, I export my data to CSV and back it up to a file I control, and restore it on a new phone.
10. As a user, I lock the app with my fingerprint or PIN so a borrowed phone does not expose my spending.

**Key scenarios and what the app must do**

- **Duplicate messages.** Wallets sometimes resend a confirmation. The app must store one entry, not two.
- **Transfers between own accounts.** Cash out from bKash to cash is not an expense. The user can mark it as a transfer, and the app suggests this when the counterparty is an agent number.
- **Refunds and reversals.** A reversal SMS must link to the original entry and net it out.
- **Phone with no SMS access.** If the user declines permission, the app still works as a manual tracker and offers the notification-listener route in Settings.

## 3. Scope and release plan

The MVP (v1.0) is everything a user needs to stop typing and trust the numbers. Anything that needs more than one extra week of work moves to v1.1 or later.

| ID | Feature | Release | Priority |
| --- | --- | --- | --- |
| F1 | SMS permission flow and 90-day history import | v1.0 | Must |
| F2 | Live SMS capture via broadcast receiver | v1.0 | Must |
| F3 | Parsers for bKash, Nagad, Rocket, Upay and 5 major banks | v1.0 | Must |
| F4 | Transaction list with edit, delete, split-free detail view | v1.0 | Must |
| F5 | Manual entry (expense, income, transfer) | v1.0 | Must |
| F6 | Rule-based categorization and merchant memory | v1.0 | Must |
| F7 | Home dashboard: month total, category donut, daily bars | v1.0 | Must |
| F8 | Monthly budgets per category with 80% and 100% alerts | v1.0 | Must |
| F9 | Search and filters | v1.0 | Must |
| F10 | Bangla and English UI, Bangla numerals option | v1.0 | Must |
| F11 | Light, dark and system theme | v1.0 | Must |
| F12 | App lock (biometric or PIN) | v1.0 | Must |
| F13 | CSV export, encrypted backup file, restore | v1.0 | Must |
| F14 | Onboarding in 4 screens | v1.0 | Must |
| F15 | Tags (free text, many per entry) | v1.0 | Should |
| F16 | Recurring entries (rent, subscriptions) | v1.1 | Should |
| F17 | Home screen widget (today's spend, quick add) | v1.1 | Should |
| F18 | Notification-listener capture as SMS fallback | v1.1 | Should |
| F19 | Weekly summary notification | v1.1 | Could |
| F20 | Optional on-device ML categorizer | v1.2 | Could |
| F21 | Savings goals | v1.2 | Could |
| F22 | Multiple wallets and net balance view | v1.2 | Could |
| F23 | Receipt photo attachment (stored locally) | v2.0 | Could |
| F24 | Optional encrypted peer-to-peer sync between own devices | v2.0 | Could |

## 4. Functional specification

### F1. SMS permission and history import
- Reads allowlisted senders received in last 90 days.
- Foreground WorkManager job in batches of 200.

### F2. Live SMS capture
- Manifest-declared BroadcastReceiver for `SMS_RECEIVED`.
- Discards non-allowlisted senders immediately.

### F3. Provider parsers
- Pure Kotlin parsers for bKash, Nagad, Rocket, Upay, and Banks.

### F4. Transaction list and detail
- Grouped by day with sticky headers. Swipe left to delete, swipe right to edit category.

### F5. Manual entry
- Fast keypad bottom sheet (3-tap logging).

### F6. Categorization
- Precedence: User override > Merchant memory > Rules (`rules.json`) > Defaults.

### F7. Dashboard
- Month total, income & net, budget bar, category donut chart, daily bars.

### F8. Budgets
- 80% and 100% threshold indicators.

### F9. Search and filters
- Matches counterparty, note, and TrxID.

### F10 to F12. Language, theme, lock
- Bilingual en/bn, system/light/dark themes, biometric & PIN lock.

### F13. Export & Backup
- UTF-8 BOM CSV export and AES-256-GCM encrypted backup.
