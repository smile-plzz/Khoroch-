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

**Rule for scope changes.** Any new MVP request must name the Must feature it replaces. The MVP list above stays at 14 Musts.

## 4. Functional specification

Each feature lists its behavior, rules and edge cases. "Read" and "write" name the data the feature touches, using the entities in section 7.

### F1. SMS permission and history import

- On first launch, after onboarding screen 2, show a rationale card explaining that messages are read on the device only and never leave it. Then request `READ_SMS` and `RECEIVE_SMS`.
- If granted: query the SMS inbox for messages from known sender IDs (section 5) received in the last 90 days, oldest first, in batches of 200. Show a progress bar with a running count ("Found 143 transactions").
- Run the import in a foreground `WorkManager` job so it survives the screen turning off. Target: 1,000 messages in under 15 seconds.
- If denied once: show the manual-only Home with a persistent banner "Turn on automatic tracking". If denied twice (permanent): the banner opens the system app settings.
- Read: SMS inbox. Write: `raw_message`, `transaction`, `import_run`.
- Edge case: import is resumable. Store the last processed SMS id in `import_run` so a killed job continues instead of restarting.

### F2. Live SMS capture

- A manifest-declared `BroadcastReceiver` for `SMS_RECEIVED` checks the sender against the allowlist. If it does not match, return immediately and store nothing. This keeps unrelated personal messages out of the app.
- A match is handed to a `WorkManager` expedited job that parses, dedupes, categorizes and writes the entry, then posts an optional notification (F14 settings).
- Target: entry visible in the list within 5 seconds of arrival.
- Edge case: if the receiver is killed by battery optimization, the next app open runs a catch-up scan from the last stored SMS id.

### F3. Provider parsers

- Full rules in section 5. Output of a parser is a `ParsedTxn` (amount, currency, direction, provider, counterparty, reference id, balance after, timestamp, confidence).
- A message that matches no parser but comes from an allowlisted sender is saved as an **Unparsed** item in a review queue, so no money movement is silently lost.

### F4. Transaction list and detail

- List: grouped by day ("Today", "Yesterday", then "Mon 20 Oct"), newest first, each day header showing the day's net total. Row shows category icon, counterparty or note, provider chip, time, signed amount.
- Swipe left to delete (with undo snackbar for 5 seconds). Swipe right to open the category picker.
- Detail screen shows every field, the original SMS text (collapsed, selectable), and actions: change category, edit amount or note, add tags, mark as transfer, split into two entries, delete.
- Paging: load 50 rows at a time using Room `PagingSource`.
- Deleting a parsed entry keeps its `raw_message` hash in a `suppressed` table so a re-import never brings it back.

### F5. Manual entry

- A floating button on Home and List opens a bottom sheet: segmented control (Expense, Income, Transfer), large amount keypad, category chip row (last 5 used first), date chip (defaults to now), optional note, Save.
- Amount accepts up to 2 decimals, max 99,99,99,999.99. Zero or empty disables Save.
- Default account for manual entry is "Cash". The user can add accounts in Settings.
- Save target: 3 taps for a repeat expense (open, pick category chip, Save after keying amount).

### F6. Categorization

- Full rules in section 6. Every entry has a `category_id` and a `category_source`: `RULE`, `MERCHANT_MEMORY`, `USER` or `DEFAULT`. A `USER` source is never overwritten by automation.
- When the user changes a category, show a snackbar "Apply to all past and future entries from {counterparty}?" with Yes (default) and Just this one.

### F7. Dashboard

- Shows the selected month. Left and right arrows move months. Long press on the month title opens a month and year picker.
- Blocks, top to bottom: month total spent (large), income and net, budget bar, category donut with top 5 and "Other", daily spending bars with average line, 3 most recent entries, Unparsed items count if above zero.
- Tapping a donut slice or category row opens List pre-filtered to that category and month.
- Excludes transfers from all totals. Includes refunds as negative expense.

### F8. Budgets

- Monthly budget per category plus an optional overall budget. Amounts are in whole taka. A budget applies to every month until changed.
- Alert at 80% and again at 100% of any budget, once per month each, as a local notification and an in-app banner. Notifications are off by default and requested at the moment the user creates their first budget.
- Daily safe-to-spend = (overall budget minus spent) divided by days remaining, floored at 0, shown on Home.

### F9. Search and filters

- Search matches counterparty, note, tags and reference id, case-insensitive, with Bangla and English input. Debounce 250 ms.
- Filters: category (multi), provider (multi), direction, date range, amount range, tag, has-note. Active filters show as removable chips above the list. A "Save this filter" action is deferred to v1.1.
- Results header shows count and net total of the filtered set.

### F10 to F12. Language, theme, lock

- Language: Bangla or English, switched in Settings with immediate effect and no restart, using per-app language APIs (`AppCompatDelegate.setApplicationLocales`). Details in section 12.
- Theme: Light, Dark, System (default). Dynamic color is optional and off by default so brand colors stay consistent.
- App lock: off by default, offered at the end of onboarding. Uses `BiometricPrompt` with device credential fallback, plus an in-app 4 to 6 digit PIN when no biometrics exist. Locks after 30 seconds in background (options: immediately, 30 s, 5 min). Hides content in the recents screen (`FLAG_SECURE`).

### F13. Export, backup, restore

- CSV export: all entries or the current filter, UTF-8 with BOM so Excel shows Bangla text, columns defined in section 13.
- Backup: one `.khoroch` file (zipped database plus settings, AES-256-GCM encrypted with a key derived from a user passphrase via PBKDF2-SHA256, 310,000 iterations). Saved through the system file picker (Storage Access Framework), so the user chooses the location, for example Google Drive or a USB drive.
- Restore: pick the file, enter the passphrase, choose Replace or Merge. Merge dedupes on the transaction hash. A schema version in the file header blocks restores from newer app versions with a clear message.

### F14. Onboarding

- Four screens: promise, privacy, SMS permission with rationale, quick setup (language, theme, optional budget and app lock). Skip is allowed on screens 1, 2 and 4, never silently on 3. Screen order and copy are in section 14.

### F15. Tags

- Free-text tags, lowercase-normalized, up to 5 per entry, autocomplete from existing tags. Tags appear as small chips on the detail screen and are searchable. Renaming or deleting a tag in Settings updates all entries in one transaction.

## 5. SMS and notification parsing spec

Parsing is the heart of the product, so it is a pure Kotlin module with no Android dependencies, fully unit-testable on a laptop. It turns one message string plus its sender and timestamp into zero or one `ParsedTxn`.

**Important caveat for the builder.** The message samples below are synthetic and only model the common structure of wallet and bank SMS. Before writing any regex, collect at least 30 real messages per provider from your own phone and friends' phones (with permission, numbers masked), and treat those as the source of truth. Wording changes over time, so parsers must be easy to patch.

**Pipeline**

1. Receive `(sender, body, receivedAtMillis)`.
2. Sender check against the allowlist table below. No match: discard, store nothing.
3. Normalize the body: trim, collapse whitespace, convert Bangla digits to ASCII digits, unify "Tk", "Tk.", "BDT", "৳" to one token.
4. Pick the parser by sender. Run its ordered list of templates; the first match wins.
5. Build `ParsedTxn` and a confidence score (1.0 for a full template match, 0.6 for a generic fallback match).
6. Compute the dedupe key and check the database. Duplicate: stop.
7. No template matched: run the generic fallback (amount plus direction keyword). Still nothing: save as Unparsed.
8. Categorize (section 6), write to the database in one transaction, emit a UI event.

**Sender allowlist (verify each on a real device)**

| Provider | Expected sender ID | Type | Parser class |
| --- | --- | --- | --- |
| bKash | bKash | Wallet | `BkashParser` |
| Nagad | NAGAD | Wallet | `NagadParser` |
| Rocket | ROCKET | Wallet | `RocketParser` |
| Upay | Upay | Wallet | `UpayParser` |
| Banks (5 at launch) | Chosen from the bank sender IDs your test users actually use | Card and account | One parser per bank, sharing `BankBaseParser` |

Keep the allowlist in a bundled JSON asset (`senders.json`) so a new sender is a data change, not a code change. Match case-insensitively and allow the numeric short-code forms if a provider uses them.

**Direction keywords (English and Bangla)**

| Direction | Example keywords |
| --- | --- |
| Expense | sent, payment, paid, purchase, cash out, withdrawn, debited, bill pay, recharge, পাঠিয়েছেন, পেমেন্ট, ক্যাশ আউট |
| Income | received, cash in, credited, deposit, refund, পেয়েছেন, ক্যাশ ইন |
| Ignore (not money) | OTP, PIN, verification code, offer, promo, campaign, ক্যাম্পেইন |

Any message containing an Ignore keyword with no amount is discarded. OTP and PIN messages are never stored, not even as raw text.

**Synthetic template example (send money)**

```text
You have sent Tk 500.00 to 01XXXXXXXXX. Fee Tk 0.00. Balance Tk 1,250.50. TrxID ABC123XYZ at 25/10/2026 13:45
```

Regex building blocks, all tested with and without commas and with 0 to 2 decimals:

```text
AMOUNT    = (?:Tk|BDT)\s?([\d,]+(?:\.\d{1,2})?)
TRXID     = (?:TrxID|Txn ?ID|Trx ?ID)[:\s]*([A-Z0-9]{6,})
BALANCE   = Balance[:\s]*(?:Tk|BDT)\s?([\d,]+(?:\.\d{1,2})?)
FEE       = Fee[:\s]*(?:Tk|BDT)\s?([\d,]+(?:\.\d{1,2})?)
DATETIME  = (\d{2}/\d{2}/\d{4})\s+(\d{1,2}:\d{2})
PHONE     = (01[3-9]\d{8})
```

**Extraction rules**

- Amount is stored as integer **paisa** (Long) to avoid floating point errors. 500.00 becomes 50000.
- Fee is stored on the same entry as `fee_paisa`. The entry total is amount plus fee for expenses.
- Counterparty: a masked phone number or a merchant name. Phone numbers are stored as given by the SMS, masked as the SMS masked them. Never try to un-mask.
- Timestamp: prefer the time inside the message (device timezone Asia/Dhaka). Fall back to the SMS received time. If they differ by more than 24 hours, use received time and set a `time_uncertain` flag.
- Reference id: the provider transaction id. It is the primary dedupe key.
- Balance after: stored for reconciliation display only, never used in totals.

**Deduplication**

- Primary key: `sha256(provider + ":" + referenceId)`.
- Fallback when there is no reference id: `sha256(sender + ":" + normalizedBody + ":" + receivedAt rounded to the minute)`.
- The hash is stored in a unique-indexed column, and inserts use `OnConflictStrategy.IGNORE`.

**Special cases**

| Case | Handling |
| --- | --- |
| Cash out at agent | Expense, counterparty "Agent {number}", suggested as transfer to cash |
| Add money from bank | Marked as transfer by default, excluded from totals |
| Mobile recharge | Expense, category Bills, counterparty operator name |
| Reversal or refund | Income-direction entry linked by `reference_id`, with `linked_txn_id` set when the original exists |
| Failed transaction message | Discard, store nothing as an entry, count in debug stats only |
| Card swipe with merchant name | Counterparty is the merchant string, title-cased, trailing city and country codes trimmed |
| Multi-part SMS | Android joins parts before delivery; handle bodies up to 1,000 characters |
| Non-Latin text | Handle UTF-16 messages; Bangla text can appear inside any template |

**Notification-listener fallback (v1.1).** Where SMS access is unavailable, a `NotificationListenerService` can read wallet app notifications. It feeds the same pipeline from step 3 onward, with the notification title as the sender hint. It needs its own onboarding explanation because the permission is granted in system settings.

**Versioning.** Each parser template has an id and a `since` date. The app stores `parser_version` on each entry, so after a parser fix, a one-tap "Re-scan messages" action in Settings can re-run affected entries without touching user edits.

## 6. Categorization engine

Categorization runs in a fixed order of precedence, and the first layer that returns a category wins. Everything runs on the device, in under 20 ms per entry.

**Order of precedence**

1. **User override.** An entry the user categorized by hand is never changed by automation.
2. **Merchant memory.** A table mapping a normalized counterparty to the category the user last chose for it. Created when the user answers Yes to "Apply to all".
3. **Rule engine.** Keyword and pattern rules from a bundled `rules.json` asset (below).
4. **Provider defaults.** For example, a mobile recharge message maps to Bills, an agent cash out maps to Transfer.
5. **Default.** Uncategorized, shown with a dotted icon and a prompt chip "Pick a category".

**Default categories (editable by the user)**

| Category | Bangla label | Typical triggers |
| --- | --- | --- |
| Food and dining | খাবার | restaurant, cafe, foodpanda, pathao food, bakery, KFC |
| Groceries | মুদি | agora, shwapno, meena bazar, grocery, bazar |
| Transport | যাতায়াত | uber, pathao, obhai, fuel, CNG, metro rail, rapid pass |
| Bills and utilities | বিল | electricity, DESCO, DPDC, gas, WASA, internet, recharge, broadband |
| Rent and housing | বাসা ভাড়া | rent, landlord, house |
| Shopping | কেনাকাটা | daraz, bata, apex, mall, clothing |
| Health | স্বাস্থ্য | pharmacy, hospital, clinic, lab, doctor |
| Education | শিক্ষা | tuition, school, coaching, course, exam fee |
| Entertainment | বিনোদন | cinema, netflix, spotify, games |
| Family and gifts | পরিবার | send money to saved family number, eid, salami |
| Transfer | ট্রান্সফার | cash out, add money, own account |
| Income | আয় | salary, received, cash in |
| Other | অন্যান্য | fallback for user choice |

The trigger words are starting points. Tune them against the labeled test set in section 15.

**Rule format (`rules.json`)**

```json
{
  "version": 1,
  "rules": [
    { "id": "food-001", "category": "food", "field": "counterparty",
      "match": "contains_any", "values": ["foodpanda", "pathao food", "kfc"],
      "priority": 50 },
    { "id": "bills-010", "category": "bills", "field": "body",
      "match": "regex", "values": ["(?i)recharge|top ?up"], "priority": 60 }
  ]
}
```

- Higher `priority` wins when several rules match. A tie goes to the more specific match (longer matched text).
- Matching is case-insensitive on normalized text: Latin lowercased, Bangla left as is, punctuation stripped.
- Rules ship in the app and are overridden, never deleted, by the user's own rules added in Settings (v1.1).

**Merchant normalization.** Before lookup, lowercase, strip digits except in known brands, drop words like "ltd", "limited", "store", "shop", collapse spaces, and cut at the first branch or city token. Numbers masked as 01XXXXXXXXX stay as the key, so repeated payments to the same person map to the same category.

**Learning behavior**

- One confirmation is enough to create a memory entry. Two conflicting choices for the same merchant: the latest wins and an "undo" snackbar is offered.
- Merchant memory can be viewed, edited and cleared in Settings, in a screen called "Learned merchants".

**Optional on-device ML (v1.2).** A small text classifier (TensorFlow Lite, under 5 MB) trained on counterparty text, used only when rules return Default. It must beat the rule engine's category accuracy on the held-out test set by at least 5 percentage points before it ships, and its output carries `category_source = MODEL` so the UI can show a subtle "suggested" badge.

## 7. Data model

Storage is a single Room (SQLite) database named `khoroch.db`, with schema export on and every schema change covered by a tested migration. Money is always an integer number of paisa (1 taka = 100 paisa), time is always epoch milliseconds in UTC, and display conversion to Asia/Dhaka happens only in the UI layer.

**Entities**

`transaction` is the main table.

| Column | Type | Notes |
| --- | --- | --- |
| id | Long, PK, auto |  |
| account\_id | Long, FK | Wallet, bank or Cash |
| direction | Int | 0 expense, 1 income, 2 transfer |
| amount\_paisa | Long | Always positive |
| fee\_paisa | Long | Default 0 |
| category\_id | Long, FK | Nullable until categorized |
| category\_source | Int | 0 DEFAULT, 1 RULE, 2 MERCHANT\_MEMORY, 3 USER, 4 MODEL |
| counterparty | Text | Display name or masked number |
| counterparty\_key | Text | Normalized key for merchant memory |
| note | Text | User note, nullable |
| occurred\_at | Long | Epoch ms |
| time\_uncertain | Boolean | See section 5 |
| provider | Text | bkash, nagad, rocket, upay, bank id, manual |
| reference\_id | Text | Provider transaction id, nullable |
| balance\_after\_paisa | Long | Nullable, display only |
| dedupe\_hash | Text | Unique index |
| raw\_message\_id | Long, FK | Nullable for manual entries |
| linked\_txn\_id | Long, FK | For reversals, nullable |
| parser\_version | Int | 0 for manual |
| confidence | Real | 0.0 to 1.0 |
| created\_at, updated\_at | Long | Epoch ms |
| deleted\_at | Long | Soft delete marker, nullable |

Supporting tables:

| Table | Key columns | Purpose |
| --- | --- | --- |
| account | id, name, type (WALLET, BANK, CASH), provider, opening\_balance\_paisa, archived | Where money lives |
| category | id, key, name\_en, name\_bn, icon, color, sort\_order, is\_system, archived | Editable category list |
| raw\_message | id, sender, body, received\_at, sms\_id, body\_hash, status (PARSED, UNPARSED, IGNORED) | Original text for audit and re-scan |
| merchant\_memory | counterparty\_key (PK), category\_id, updated\_at, hits | Learned categories |
| budget | id, category\_id (null = overall), amount\_paisa, effective\_from | Monthly limits |
| budget\_alert | budget\_id, month (yyyyMM), threshold (80 or 100), fired\_at | Prevents repeat alerts |
| tag | id, name (unique, lowercase) | Free-text labels |
| transaction\_tag | transaction\_id, tag\_id (composite PK) | Many to many |
| suppressed\_hash | dedupe\_hash (PK), suppressed\_at | Stops deleted entries returning on re-import |
| import\_run | id, started\_at, finished\_at, last\_sms\_id, found, saved, failed | Resumable import and diagnostics |

**Indexes.** `transaction(occurred_at DESC)`, `transaction(category_id, occurred_at)`, `transaction(account_id, occurred_at)`, unique `transaction(dedupe_hash)`, `raw_message(body_hash)`, `merchant_memory(counterparty_key)`.

**Read rules**

- All reads go through DAOs returning `Flow` so the UI updates the moment a write lands.
- Month totals use a single SQL aggregate (`SUM` grouped by direction and category) with `deleted_at IS NULL` and `direction != 2`. The UI never loops over rows to total.
- The list screen uses `PagingSource` with a page size of 50.
- Search uses an FTS4 virtual table over counterparty, note and tags, kept in sync by Room triggers.

**Write rules**

- Every multi-row change (import batch, tag rename, category merge, restore) runs inside one `@Transaction`.
- Only the repository layer writes. ViewModels call repository functions, never DAOs.
- Deletes are soft (`deleted_at`) for 30 days to power undo and restore, then purged by a daily maintenance worker, which also writes the `suppressed_hash` row.
- Automation never overwrites a row where `category_source = 3 (USER)` or where the user edited amount or note.
- Inserts use `IGNORE` on the unique dedupe hash. A conflict is a normal outcome, not an error.

**Preferences (not in Room).** Language, theme, lock settings, notification toggles, onboarding state and the last-seen app version live in Jetpack DataStore (Preferences). The PIN is never stored in plain form: store a salted PBKDF2 hash, with the salt and a keystore-wrapped key in `EncryptedSharedPreferences` replacement (Android Keystore plus DataStore).

**Encryption at rest.** v1.0 relies on Android's file-based encryption and the app sandbox, and stores no data on external storage. Optional SQLCipher database encryption is a v1.1 item, because it adds size and complicates backup.

**Migrations.** Start at version 1. Each version bump adds a `Migration(from, to)` and an instrumented test that opens a database built from the previous exported schema. Destructive fallback is forbidden.

## 8. Architecture and tech stack

Data flows one way: inputs pass through pure parsing logic into the repository, the database is the single source of truth, and the screens only observe it.

&#91;embedded content: architecture · 4 layers, 12 parts\]

SMS and manual entries share one write path through the repository, and every screen reads from the database through Flow, so a new transaction appears everywhere without any manual refresh.

**Tech stack**

| Area | Choice | Why |
| --- | --- | --- |
| Language | Kotlin (latest stable) | Native Android, coroutines, null safety |
| UI | Jetpack Compose with Material 3 | Declarative UI, easy theming, good Bangla text support |
| Navigation | Navigation Compose with type-safe routes | One back stack, deep links from notifications |
| Dependency injection | Hilt | Standard, testable, works with WorkManager |
| Async | Kotlin coroutines and Flow | Reactive reads from Room |
| Database | Room with KSP, FTS4 for search | Compile-time checked SQL, migrations, offline |
| Settings | Jetpack DataStore (Preferences) | Replaces SharedPreferences, safe with coroutines |
| Background work | WorkManager | Survives process death and reboots |
| Serialization | kotlinx.serialization | `rules.json`, `senders.json`, `settings.json` in backups |
| Charts | Custom Compose Canvas (donut and bars) | Avoids heavy libraries, full control over Bangla labels |
| Biometrics | `androidx.biometric` | Fingerprint and face with device credential fallback |
| Logging | Timber, debug builds only | No logs in release |
| Build | Gradle Kotlin DSL, version catalog, R8 | Reproducible, small APK |
| SDK levels | minSdk 26, compileSdk and targetSdk latest stable | Android 8.0 and above covers nearly all active devices |

**Layer rules**

- `core:parser` and `core:categorizer` depend on Kotlin only. They never import Android classes, so they test in milliseconds on a laptop.
- Only `core:data` repositories write to the database. ViewModels never touch DAOs.
- Screens are stateless functions of a `UiState`. Events go up as a sealed `UiEvent`, state comes down through `StateFlow`, collected with `collectAsStateWithLifecycle`.
- One-off effects (snackbars, navigation) use a `Channel`, never a state field.
- The parser never throws. It returns a sealed result: `Parsed`, `Ignored` or `Unparsed`.
- The repository converts database failures into `UiState.Error` with a retry action.
- A global uncaught-exception handler writes a short log file locally, with no message bodies or amounts in it.

**Background jobs**

| Job | Trigger | Type | Notes |
| --- | --- | --- | --- |
| ParseAndSaveWorker | SMS received from an allowlisted sender | Expedited one-time | Receiver stores the raw message first, then passes only its id to the worker |
| HistoryImportWorker | Onboarding permission grant, or Re-scan in Settings | One-time, runs in the foreground with a notification | Declare the foreground service type required by the target SDK. Resumable via `import_run` |
| CatchUpScanWorker | App open, and every 12 hours | Periodic | Scans from the last processed SMS id |
| MaintenanceWorker | Daily, device idle and charging preferred | Periodic | Purges entries soft-deleted over 30 days ago, optimizes the FTS index, vacuums monthly |

Budget checks run inside the repository transaction after every write, not in a worker, so alerts are never late.

**Manifest notes**

- The SMS receiver is exported and protected with `android:permission="android.permission.BROADCAST_SMS"`, so only the system can deliver to it.
- Every other component is `exported="false"`, except the launcher activity.
- `usesCleartextTraffic="false"`, and no INTERNET permission in v1.0.

**Performance budgets**

| Measure | Budget |
| --- | --- |
| Cold start to first Home frame | Under 1.5 seconds on a 3 GB RAM device |
| List scrolling | 60 frames per second with 50,000 entries |
| History import | 1,000 messages in under 15 seconds |
| Memory | Under 150 MB resident during normal use |
| APK size | Under 15 MB per ABI |

Ship a Baseline Profile and verify with Macrobenchmark before each release.

## 9. UX principles and navigation

Design for a glance: the user opens the app for two seconds, learns how the month is going, and leaves.

**Principles**

1. **Numbers first.** The biggest, clearest thing on every screen is the number the user came for. Decoration never competes with it.
2. **Zero typing by default.** Automatic capture does the work. Manual entry exists for cash and must take under 10 seconds.
3. **Fix, don't configure.** The user improves the app by correcting entries (which teaches merchant memory), not by editing settings.
4. **Two taps to correct.** Any wrong category, amount or note is fixable in two taps from the list.
5. **Honest automation.** Every parsed entry can show its original SMS. Entries with parser confidence below 0.8 carry a small "check" badge so the user knows which ones to glance at.
6. **Calm, never scolding.** Budget warnings are informative and neutral in tone. No red alarms for a normal month, no shaming copy.
7. **Everything is reversible.** Delete, split and category changes all offer Undo, and deleted entries stay recoverable for 30 days.
8. **Thumb-friendly.** Primary actions sit in the lower half of the screen. Prefer bottom sheets to centered dialogs.
9. **Bangla and English are equals.** Layouts are designed against the longer Bangla strings from the start.

**Navigation map**

&#91;embedded content: navigation map · 4 tabs, 2 entry screens\]

The add button appears on Home and Activity. The lock screen shows before the tabs on every cold start, but only when app lock is on.

**Navigation rules**

- Four bottom tabs, each keeping its own back stack and scroll position (save and restore state when switching).
- Back from a tab root goes to Home, and back from Home exits the app.
- Sheets (Add, Category picker, Budget, Filter) are modal layers over a screen, not separate destinations, so back closes the sheet first.
- Notifications deep link straight to Transaction detail or Budgets, with Home as the parent in the back stack.
- Use predictive back animations on Android 14 and above.
- Width of 600 dp or more: replace the bottom bar with a navigation rail. Width of 840 dp or more: show Activity and Transaction detail side by side.

**Gestures**

| Gesture | Where | Result |
| --- | --- | --- |
| Swipe left | Activity row | Delete, with Undo |
| Swipe right | Activity row | Open category picker |
| Long press | Activity row | Multi-select mode |
| Pull down | Home, Activity | Run a catch-up SMS scan |
| Long press | Home month title | Month and year picker |
| Tap and hold | Launcher icon | Shortcuts: Add expense, Add income |

**Key flows**

1. *New payment arrives.* SMS received, entry created and categorized, optional notification, entry visible at the top of Home within 5 seconds. If the category is wrong, tap the notification, tap Change category, pick, answer Apply to all.
2. *Add a cash expense.* Plus button, type amount on the keypad, tap a category chip, Save. Three taps after the amount.
3. *Month check-in.* Open Home, read total, budget bar and safe-to-spend, tap the biggest category slice to see its entries.
4. *Clean up Unparsed.* Tap the banner, review each message, create an entry or ignore it. The banner disappears when the queue is empty.

## 10. Visual design system

The look is calm and trustworthy: lots of white space, one confident teal, and numbers that are always the largest, clearest thing on screen. Build it with Material 3 in Jetpack Compose, with a custom `KhorochTheme` that overrides the tokens below. Dynamic color is optional and off by default.

**Color tokens**

| Token | Light | Dark | Use |
| --- | --- | --- | --- |
| background | #F8FAFB | #0E1316 | Screen background |
| surface | #FFFFFF | #161D21 | Cards, sheets |
| surfaceVariant | #EEF2F4 | #1F292E | Chips, inputs, rows on press |
| onSurface | #11181C | #E6ECEF | Primary text |
| onSurfaceVariant | #5B6770 | #9AA7AF | Secondary text |
| primary | #0F766E | #4DD0C4 | Buttons, active nav, links |
| onPrimary | #FFFFFF | #00302C | Text on primary |
| primaryContainer | #CCEBE7 | #0B4F49 | Selected chips, highlights |
| expense | #C62828 | #FF8A80 | Expense amounts |
| income | #1B7F3B | #6FDB93 | Income amounts |
| warning | #B45309 | #FFB74D | 80% budget, unparsed banner |
| outline | #D5DCE0 | #34434A | Dividers, input borders |

All text and amount colors must reach a 4.5:1 contrast ratio against their background in both themes. Check each pair with a contrast tool before release. Color never carries meaning alone: expense and income also differ by sign (minus or plus) and by position, and categories always pair a color with an icon.

**Category palette (colorblind-safe, paired with icons)**

Teal #0F766E, orange #E69F00, sky #56B4E9, green #009E73, blue #0072B2, vermilion #D55E00, pink #CC79A7, yellow #F0E442, and grey #8A949B for Other. Assign in the order of the category table in section 6 and repeat from the start after nine.

**Typography**

- Latin: Inter. Bangla: Noto Sans Bengali (bundle both as variable fonts, regular 400, medium 500, bold 700 only, to keep the APK small). Set the font family per locale so Bangla glyphs never fall back to a system font with different metrics.
- Bangla text needs about 10% more line height than Latin, so use 1.5 for Bangla and 1.4 for Latin.
- Amounts use tabular figures where the font supports them, so digits align in columns.

| Style | Size and weight | Used for |
| --- | --- | --- |
| Display | 34 sp, bold | Month total on Home |
| Headline | 24 sp, bold | Screen titles, daily net on detail |
| Title | 18 sp, medium | Card titles, row amounts |
| Body | 16 sp, regular | Rows, notes, forms |
| Label | 14 sp, medium | Chips, buttons, tabs |
| Caption | 12 sp, regular | Timestamps, helper text |

Respect the system font scale up to 200%. At large scales, rows grow in height and amounts wrap below the title instead of truncating.

**Spacing, shape, elevation**

- 4 dp base grid. Steps: 4, 8, 12, 16, 24, 32, 48. Screen side padding 16 dp, card inner padding 16 dp, gap between cards 12 dp.
- Corner radius: chips 8 dp, cards 16 dp, bottom sheets 28 dp on top corners, buttons fully rounded (height 48 dp).
- Elevation is almost flat: cards use a 1 dp outline instead of shadow in light theme and a tonal surface in dark theme. Sheets and the floating button are the only elements with a shadow.
- Minimum touch target 48 x 48 dp everywhere.

**Core components**

| Component | Specification |
| --- | --- |
| Transaction row | 64 dp tall. Leading 40 dp circular category icon on a 12% tint of the category color. Title (counterparty) and caption (provider chip, time) on the left, signed amount right-aligned in Title style. Pressed state uses surfaceVariant. |
| Primary button | 48 dp high, fills width on forms, primary color, Label text. Disabled at 38% opacity. |
| Category chip | 36 dp high, 8 dp radius, icon plus label. Selected: primaryContainer fill plus 2 dp primary outline. |
| Amount keypad | 3 by 4 grid of 64 dp keys, large 28 sp digits, backspace key, decimal key. Amount shown above in Display style, auto-shrinking to fit up to 10 digits. |
| Bottom sheet | Drag handle, 28 dp top radius, max height 90% of screen, scrim at 32%. Dismiss by drag, back or scrim tap. |
| Snackbar | Bottom, 5 seconds, one action (Undo, Apply to all). Never stacks: a new one replaces the old. |
| Banner | Full width card at top of a screen, warning tint, icon, one sentence, one text button. Dismissible when it is informational only. |
| Empty state | Simple line illustration (generic, original artwork), one sentence, one button. Never a blank screen. |
| Skeleton | Grey rows with a 1.2 second shimmer, shown only if loading exceeds 300 ms. |
| Donut chart | 160 dp, 20 dp ring, top 5 categories plus Other, total in the center, 2 dp gaps between slices. |
| Daily bars | 7 to 31 bars, month average as a dashed line, current day highlighted in primary, tap a bar to see that day. |

**Iconography.** Material Symbols Rounded, 24 dp, weight 400. One icon per category, chosen so it is recognizable without color. Provider chips use text initials in a small rounded badge (b, N, R, U), not provider logos, to avoid trademark issues.

**Motion**

- Standard durations: 150 ms for taps and toggles, 250 ms for screen and sheet transitions, 400 ms for chart draw-in on first load only.
- Easing: Material emphasized decelerate for entering, accelerate for leaving.
- New entries from live SMS fade and slide in over 200 ms with a brief 600 ms primaryContainer highlight.
- Respect the system "Remove animations" setting: replace all motion with instant changes or simple fades.

**Dark mode.** Defined by the token table, not by inverting colors. Check that charts stay legible, that category tints use 24% opacity instead of 12% on dark surfaces, and that the status bar icons switch correctly.

**Accessibility**

- TalkBack: every row reads as one phrase, for example "Food, bKash, 1 pm, minus 450 taka". Charts expose a text alternative listing the top categories with amounts.
- Content descriptions on every icon-only button. Logical focus order top to bottom.
- Bangla screen readers read Bangla numerals correctly, so keep ASCII digits in the semantics description when the visible text uses Bangla numerals.

## 11. Screen-by-screen specification

Every screen must define four states: **loading**, **empty**, **populated** and **error**. Unless stated otherwise, the top app bar is 56 dp, titles are left aligned, and back navigation uses the system back gesture. Bottom navigation has four tabs: Home, Activity, Budgets, Settings.

### S1. Splash

Android 12 splash screen API with the app icon on the background color. No custom splash beyond that. Hold until the database and DataStore are ready, with a hard cap of 1 second.

### S2. Onboarding (4 screens, shown once)

| Screen | Content | Primary action | Other actions |
| --- | --- | --- | --- |
| 1. Promise | Headline "Your spending, tracked for you". Three lines: reads wallet and bank SMS, sorts them, works offline. | Next | Skip |
| 2. Privacy | Headline "Stays on your phone". Points: no account, no internet access, you can delete everything anytime. | Next | Back |
| 3. Permission | Explains why SMS access is needed and what is read (only allowlisted senders, nothing else is saved). Shows a short list of providers. | Allow SMS access | "Maybe later" (goes to manual-only mode) |
| 4. Quick setup | Language toggle (Bangla, English), theme choice, optional monthly budget field, optional app lock switch. | Start tracking | Skip |

After screen 3 with permission granted, show the import progress inline on screen 4's background and let the user continue setup while it runs.

### S3. Home

Purpose: answer "how am I doing this month?" in two seconds.

```text
+--------------------------------------+
| October 2026              <   >   (i) |
|                                      |
|  Spent this month                    |
|  Tk 18,450                  (Display)|
|  Income Tk 42,000   Net +23,550      |
|                                      |
|  [=========-----]  62% of Tk 30,000  |
|  Safe to spend today: Tk 520         |
|                                      |
|  +--------------------------------+  |
|  |  (donut)   Food        Tk 5,200 |  |
|  |            Transport   Tk 3,100 |  |
|  |            Bills       Tk 2,900 |  |
|  +--------------------------------+  |
|  |  daily bars ||||| |||| ||||| |  |  |
|  +--------------------------------+  |
|  Recent                  See all >   |
|  [row] [row] [row]                   |
|                                  (+) |
| Home  Activity  Budgets  Settings    |
+--------------------------------------+
```

- Banner slot above the month total, in priority order: SMS permission missing, Unparsed items waiting ("3 messages need a look"), budget exceeded.
- Empty state (no entries this month): illustration, "Nothing here yet", buttons Add expense and, if permission is missing, Turn on automatic tracking.
- The (i) icon opens a sheet explaining how totals are computed (transfers excluded, refunds netted).
- Pull to refresh runs a catch-up SMS scan.

### S4. Activity (transaction list)

- App bar: title, search icon, filter icon with a badge showing the active filter count.
- Month selector chips at the top (current month, previous month, "Pick month"). Selecting changes the list scope.
- Day groups with sticky headers. Swipe actions as in F4. Long press enters multi-select mode with a contextual app bar: Change category, Add tag, Mark as transfer, Delete.
- Empty state per filter result: "No entries match" with a Clear filters button.
- Error state (database read fails): inline card with Retry and a link to export diagnostics.

### S5. Transaction detail (full screen)

- Top: large signed amount, counterparty, date and time. Below: fields in a card (Category, Account, Provider, Reference id, Fee, Balance after, Tags, Note).
- Each field is tappable to edit inline except Provider and Reference id.
- "Original message" expandable card showing the SMS text, with a Copy button. Hidden for manual entries.
- Overflow menu: Mark as transfer, Split, Duplicate, Delete.
- Split: opens a sheet to divide the amount into two parts with separate categories, requiring the parts to sum exactly to the original.

### S6. Add or edit sheet

```text
+--------------------------------------+
| ----  (drag handle)                  |
| ( Expense | Income | Transfer )       |
|                                      |
|              Tk 450                  |
|                                      |
| [Food][Groceries][Transport][Bills]> |
| [ Today, 1:45 pm ]  [ Account: Cash ]|
| [ Add a note...                    ] |
|                                      |
| [ 1 ][ 2 ][ 3 ]                      |
| [ 4 ][ 5 ][ 6 ]                      |
| [ 7 ][ 8 ][ 9 ]                      |
| [ . ][ 0 ][ del ]                    |
|                                      |
| [            Save                  ] |
+--------------------------------------+
```

- Keypad is always visible, the system keyboard appears only when the note field is focused.
- Category row shows the 5 most recently used first, then the rest, ending with a "More" chip that opens S7.
- Save is disabled until an amount above zero is entered. After Save, a snackbar offers Undo and the sheet closes.

### S7. Category picker

Full-height sheet with a search field and a grid of categories (3 columns, icon above label). Includes a final tile "New category" that opens a small form (name in English and Bangla, icon, color from the palette).

### S8. Search and filter

- Search field with recent searches (last 5, clearable). Filter sheet has sections in this order: Date range, Category, Provider, Direction, Amount range, Tags. Apply button shows the result count live ("Show 37 entries").

### S9. Budgets

- Top card: overall budget with a progress bar and days left in the month.
- List of category budgets, each with a progress bar. Color rules: below 80% primary, 80 to 99% warning, 100% and above expense.
- Floating button "Add budget" opens a sheet with category chooser and amount keypad. Editing a budget uses the same sheet with a Delete action.
- Empty state explains budgets in one sentence and offers the 3 most common categories as one-tap starters.

### S10. Settings

Ordered list of grouped rows:

| Group | Rows |
| --- | --- |
| Tracking | Automatic tracking (SMS) status and toggle, Review unparsed messages (with count), Re-scan messages, Accounts |
| Money | Categories, Learned merchants, Tags, Currency display (Tk or ৳) |
| Appearance | Language, Theme, Bangla numerals |
| Security | App lock, Lock delay, Hide amounts on Home (toggle that masks totals until tapped) |
| Data | Export CSV, Back up, Restore, Delete all data |
| About | Version, Privacy statement, Open-source licenses, Send feedback by email |

"Delete all data" requires typing the word DELETE (or the Bangla equivalent shown on screen), then wipes the database, DataStore and any cached files, and returns to onboarding.

### S11. Unparsed messages queue

A list of messages from allowlisted senders that no parser understood. Each card shows the SMS text. Actions: Create entry (opens S6 prefilled with any number found), Ignore, Ignore all from this sender. Cleared items go to `raw_message.status = IGNORED`. A "Copy for bug report" action copies the text with phone numbers masked.

### S12. Lock screen

Shown on cold start or after the lock delay when App lock is on. App name, a fingerprint prompt launched automatically, a "Use PIN" button. Five wrong PIN attempts add a 30 second wait, doubling each further round. No account recovery exists, so setup warns that a forgotten PIN means clearing app data.

## 12. Bangla and English localization

Bangla is a first-class language, not a translation added at the end. Every user-visible string is written in both languages from the first commit.

**Mechanics**

- Strings live in `res/values/strings.xml` (English) and `res/values-bn/strings.xml` (Bangla). No hard-coded text in Compose code. Use plurals resources where counts appear.
- Language is switched at runtime with `AppCompatDelegate.setApplicationLocales`, plus a `locales_config.xml` entry so the choice also appears in Android's per-app language settings (Android 13 and above).
- Default language: follow the device locale if it is Bangla or English, otherwise English.
- Category names carry both `name_en` and `name_bn` in the database, so user-created categories need both fields (the second may be copied from the first if left blank).

**Numbers, dates, currency**

| Item | English | Bangla |
| --- | --- | --- |
| Digits | 0 1 2 3 4 5 6 7 8 9 | ০ ১ ২ ৩ ৪ ৫ ৬ ৭ ৮ ৯ (a separate "Bangla numerals" toggle, default on when language is Bangla) |
| Amount grouping | Tk 1,25,000 | ৳ ১,২৫,০০০ |
| Date | 25 Oct 2026 | ২৫ অক্টোবর ২০২৬ |
| Time | 1:45 pm | ১:৪৫ অপরাহ্ণ |
| Month names | October | অক্টোবর |
| Week start | Saturday (default) | শনিবার |

- Use the **South Asian digit grouping** (lakh and crore: 1,25,000), not the Western 125,000, in both languages. Build one `MoneyFormatter` class used by every screen, so grouping and digits can never differ between screens.
- Currency symbol: "Tk" in English and "৳" in Bangla by default, switchable in Settings.
- Week starts on Saturday by default with a Settings option for Sunday or Monday.
- Parsing input: the amount keypad shows digits in the chosen numeral style but always converts to ASCII internally before saving.

**Writing style for Bangla UI text**

- Use plain, spoken-level Bangla. Keep widely used English technical terms in English script or transliteration where users already say them, for example SMS, PIN, backup, CSV, Wi-Fi, and keep provider names (bKash, Nagad) in Latin script.
- Keep sentences short. Prefer verbs users know from wallet apps (পাঠানো, ক্যাশ আউট) over formal alternatives.
- Test every screen at the longest Bangla string: Bangla labels are often 20 to 30% longer, so chips must wrap or truncate with an ellipsis, never overlap.

**Sample microcopy (English and Bangla)**

| Key | English | Bangla |
| --- | --- | --- |
| home\_spent | Spent this month | এই মাসে খরচ |
| home\_safe\_today | Safe to spend today | আজ খরচ করা নিরাপদ |
| add\_expense | Add expense | খরচ যোগ করুন |
| save | Save | সংরক্ষণ করুন |
| undo | Undo | আগের অবস্থায় ফেরান |
| unparsed\_banner | {n} messages need a look | {n}টি মেসেজ দেখা দরকার |
| apply\_all | Apply to all from {name}? | {name} থেকে সব এন্ট্রিতে প্রয়োগ করবেন? |
| budget\_80 | You have used 80% of your {category} budget | আপনার {category} বাজেটের ৮০% খরচ হয়ে গেছে |
| permission\_why | Messages are read on this phone only and never leave it | মেসেজ শুধু এই ফোনেই পড়া হয়, কোথাও পাঠানো হয় না |
| empty\_home | Nothing here yet | এখনো কিছু নেই |

The Bangla strings are a starting draft. Have a native Bangla reader review the full set before release, and keep a glossary table in the repository so the same word is always used for the same concept (for example "entry" is always এন্ট্রি).

## 13. Permissions, privacy, security, backup and export

The app handles very sensitive data, so the privacy promise is a feature: nothing leaves the phone, and the manifest proves it.

**Permissions**

| Permission | When requested | Why | If denied |
| --- | --- | --- | --- |
| READ\_SMS | Onboarding screen 3 | Import the last 90 days of wallet and bank messages | Manual-only mode, banner offers to retry |
| RECEIVE\_SMS | Same prompt as READ\_SMS | Capture new messages as they arrive | Same as above |
| POST\_NOTIFICATIONS (Android 13+) | When the user first enables an alert or creates a budget | Budget alerts, weekly summary | Alerts appear in-app only |
| USE\_BIOMETRIC | When the user turns on app lock | Fingerprint or face unlock | PIN only |
| INTERNET | **Never declared in v1.0** | Not needed | Not applicable |

Do not request storage permissions. Use the system file picker (Storage Access Framework) for export, backup and restore.

**Privacy rules (non-negotiable)**

- No analytics, no ads, no crash reporting that sends data off the device in v1.0. Crash logs are written to a local file the user can choose to share by email.
- No third-party SDKs that include network code. Review the dependency tree before each release.
- Only SMS from the allowlist are read into the database. All other SMS are skipped inside the receiver and are never logged.
- OTP, PIN and verification messages are never stored.
- Logs (Timber) are disabled in release builds. Never log message bodies, phone numbers or amounts.
- `android:allowBackup="false"` and `dataExtractionRules` excluding the database, so Google auto-backup cannot copy data. The app's own encrypted backup file is the only backup path.
- `FLAG_SECURE` on all screens when app lock is on, so screenshots and the recents preview show nothing.
- The in-app privacy statement is plain language, available in both languages, and states exactly what is read, what is stored and what is never done.

**CSV export format**

UTF-8 with byte order mark, comma separated, one header row, dates in ISO 8601 with the +06:00 offset, amounts in taka with two decimals.

```csv
date,type,amount_tk,fee_tk,category,account,provider,counterparty,reference_id,tags,note
2026-10-25T13:45:00+06:00,expense,500.00,0.00,Food and dining,bKash,bkash,Rahim Store,ABC123XYZ,lunch,Office lunch
2026-10-25T09:10:00+06:00,income,42000.00,0.00,Income,Bank,bank-a,Employer,,salary,October salary
```

Export runs on a background thread, streams rows to the chosen file, and shows progress for exports above 5,000 rows.

**Encrypted backup format (.khoroch)**

| Part | Detail |
| --- | --- |
| Header | Magic bytes `KHBK`, format version (1 byte), schema version (2 bytes), KDF id, salt (16 bytes), iterations, GCM nonce (12 bytes) |
| Payload | ZIP of `khoroch.db` (checkpointed so the WAL is merged) and `settings.json` |
| Encryption | AES-256-GCM, key from PBKDF2-HMAC-SHA256 over the passphrase and salt, 310,000 iterations |
| Integrity | The GCM authentication tag is checked before any data is read. A wrong passphrase and a corrupted file show the same message: "Could not open this backup". |

- Passphrase rules: at least 8 characters, entered twice, with a clear warning that it cannot be recovered.
- Suggested file name: `khoroch-backup-2026-10-25.khoroch`.
- Restore flow: pick file, enter passphrase, preview (entry count, date range, app version), choose Replace or Merge, confirm, run in one transaction, restart the UI. A failure leaves the existing data untouched because the restore writes to a temporary database first and swaps on success.
- Auto-reminder: after 30 days without a backup, show a gentle banner on Home (dismissible, returns after 30 more days).

**Threat model, plainly**

| Risk | Mitigation |
| --- | --- |
| Someone borrows the unlocked phone | App lock, hide amounts option, FLAG\_SECURE |
| Phone lost | OS screen lock plus file-based encryption, app lock, encrypted backups stored elsewhere |
| Malicious app reads our database | App sandbox, no export of the database through content providers, no exported components except the SMS receiver |
| Tampered backup file | GCM authentication, schema version check, restore into a temporary database first |
| SMS spoofing creates fake entries | Spoofed entries only affect the user's own display. Show the original message on every entry so the user can verify |

## 14. Notifications, widgets, defaults and first-run behavior

**Notification channels**

| Channel id | Name | Importance | Content | Default |
| --- | --- | --- | --- | --- |
| budget\_alerts | Budget alerts | Default | "Food budget at 80%: Tk 4,000 of Tk 5,000 used" | Off until the first budget is created |
| new\_transaction | New transactions | Low, silent | "Tk 450 spent at Rahim Store. Tap to review" with actions Change category and Undo | Off |
| weekly\_summary (v1.1) | Weekly summary | Low | Total, top category, change versus last week | Off |
| import\_progress | Import | Low, ongoing | Progress while importing history | Always on during import |

- All notification text is localized and never shows full phone numbers.
- Tapping a notification deep links to the transaction detail or Budgets screen.
- Channels are created at first launch, but the permission prompt on Android 13 and above appears only when the user turns on a channel that needs it.

**Home screen widget (v1.1)**

- Small (2 by 2): today's spend and a plus button that opens the Add sheet directly.
- Medium (4 by 2): today's spend, month budget bar, three most recent entries.
- Built with Jetpack Glance. Updates when a transaction is written and every 30 minutes. Respects Hide amounts: shows dots until tapped when the setting is on. When app lock is on, the widget shows only a lock icon and the app name.

**App shortcuts.** Long press the launcher icon for "Add expense" and "Add income" static shortcuts, opening the Add sheet preset to that type.

**Default settings on first run**

| Setting | Default |
| --- | --- |
| Language | Device language if Bangla or English, else English |
| Theme | System |
| Bangla numerals | On if language is Bangla, else off |
| Currency display | Tk in English, ৳ in Bangla |
| Week starts on | Saturday |
| App lock | Off |
| Lock delay | 30 seconds |
| Hide amounts on Home | Off |
| Notifications | All off except import progress |
| Default account for manual entry | Cash |
| Default categories | Section 6 list, all enabled |

**Message and error copy rules**

- Say what happened, then what to do, in under 15 words. Example: "Could not read messages. Check SMS access in Settings."
- Never blame the user. Never show raw exception text. Offer a Copy details button for technical errors.
- Destructive actions always say what will be lost ("Delete 238 entries from October?") and use the destructive color for the confirm button.
- Success messages appear as snackbars only when the result is not already obvious on screen.

**First-run edge cases**

- If the user finishes onboarding while the import is still running, the Home screen shows entries as they arrive and a thin progress bar under the app bar.
- If no allowlisted messages are found, show a friendly explanation and an Add expense button, plus a link to Settings, Accounts to add the bank sender ids manually.
- If the device has no SIM or no SMS app (some tablets), skip onboarding screen 3 and go straight to manual mode.
- Back press on onboarding screen 1 exits the app. Back press elsewhere goes one screen back.

## 15. Testing and evaluation plan

The parser and categorizer are measured against a labeled dataset, so every change can be judged by numbers instead of feelings. This is also the strongest evidence of evaluation work in a portfolio.

**Labeled SMS dataset**

- Target size: 400 messages at launch (about 60 per wallet, 30 per bank, 70 negative examples such as OTP, offers and failures), growing to 1,000.
- Sources: your own messages and volunteers' messages, with phone numbers, names and balances masked before they enter the repository. Store the dataset in a private repository or encrypted file, never in the public code repository.
- Split: 70% development, 30% held-out test. The held-out set is only run before a release, never used to tune rules.

One labeled row per message:

```csv
id,sender,body,expected_is_txn,expected_direction,expected_amount_paisa,expected_fee_paisa,expected_provider,expected_counterparty_key,expected_reference_id,expected_category,notes
```

**Metrics**

```latex
\text{Parse accuracy} = \frac{\text{messages with every expected field correct}}{\text{messages labeled as transactions}}
```

```latex
\text{Precision} = \frac{TP}{TP + FP}, \qquad \text{Recall} = \frac{TP}{TP + FN}
```

Here a true positive is a transaction message correctly turned into an entry, a false positive is a non-transaction (such as an OTP) wrongly saved, and a false negative is a real transaction that was missed.

| Metric | Measured on | Release gate |
| --- | --- | --- |
| Parse accuracy, all fields | Held-out transactions | 95% or higher, and 100% on amount and direction |
| Precision of "is a transaction" | Held-out set including negatives | 99% or higher (a wrongly saved OTP is worse than a missed message) |
| Recall of "is a transaction" | Held-out set | 95% or higher |
| Category accuracy | Held-out transactions with no user history | 75% or higher |
| Dedupe correctness | 50 synthetic duplicate and near-duplicate pairs | 100% |
| Money total correctness | 100 generated months compared with an independent calculation | 100% |

Publish a short evaluation report with every release: metric values, confusion matrix for categories, a list of the 10 worst failures and the fixes made.

**Automated tests**

| Level | What | Tooling |
| --- | --- | --- |
| Unit | Parsers, normalizers, `MoneyFormatter`, dedupe, categorizer, budget math, date handling across month boundaries and the Asia/Dhaka timezone | JUnit 5, Kotest or Truth |
| Database | DAOs, aggregates, FTS search, migrations from every old schema | Room testing library, in-memory database |
| Integration | Receiver to database pipeline using fake SMS broadcasts | AndroidX Test |
| UI | Onboarding, add entry, edit category, filter, budget alert, language switch, lock | Compose UI test, Robolectric where possible |
| Screenshot | Home, Activity, Detail and Add sheet in light, dark, English, Bangla, font scale 100% and 200% | Paparazzi or Roborazzi |
| Performance | Import of 5,000 messages, list scroll with 50,000 rows, cold start | Macrobenchmark |

**Manual QA checklist (run before every release)**

- Fresh install on a clean device, grant SMS, confirm history import and totals match a manual count of 20 sampled entries.
- Send a real test payment (small amount) on each provider and confirm the entry appears within 5 seconds.
- Deny permission once, then twice, and confirm the manual-only mode and the recovery banner.
- Turn on airplane mode: every feature still works.
- Kill the app with battery saver on, receive an SMS, open the app, confirm catch-up scan finds it.
- Switch language and numerals while on each screen with no crash and no clipped text.
- Rotate the device and fold or resize if the device supports it: no state is lost on the Add sheet.
- Backup, uninstall, reinstall, restore: entry count, categories, budgets and settings match.
- Change the system date to the last day of a month and the first day of the next: month totals roll over correctly.
- TalkBack pass on Home, Activity and Add sheet.

**Device and OS matrix.** Android 8.0, 10, 12, 13 and 14 or newer; one low-end phone (2 to 3 GB RAM), one mid-range phone and one tablet or large-screen device; at least two brands with aggressive battery managers (for example Xiaomi and Samsung).

## 16. Distribution and release

Plan on a direct APK as the primary channel, with F-Droid as the second. Treat a Play Store release as a stretch goal, because store policy on SMS access is strict. **Policy details below are from general knowledge and change often. Re-check the current Google Play and Android rules before each release decision.**

| Channel | Fit | Main constraint | Action |
| --- | --- | --- | --- |
| Direct APK (website or GitHub Releases) | Best for v1.0 | Users must allow installs from unknown sources, and newer Android versions can block sensitive permissions for apps installed outside a store | Ship an in-app and website guide for the "Allow restricted settings" step |
| F-Droid | Good if the app is open source | Needs a fully open-source dependency tree and no proprietary libraries | Keep dependencies FOSS from day one, publish a clean build recipe |
| Google Play | Hard | Play restricts SMS permissions to a short list of approved uses, and an expense tracker may not qualify | Prepare a notification-listener build variant (v1.1) and read the current permission declaration rules before applying |
| Alternative stores | Possible | Varies by store | Optional later |

**Sideloading user guide (must be written before launch)**

1. Download the APK from the official page and verify the published SHA-256 checksum.
2. Allow the browser or file manager to install unknown apps, then install.
3. If the SMS permission is greyed out or blocked, open the system App info for Khoroch, use the three-dot menu, choose "Allow restricted settings", then grant the SMS permission.
4. If the phone shows a security scan warning, explain that the app is not from a store and that its code is open for review.

Show this guide as screenshots in both languages on the website, and as a help screen in the app reachable from the permission banner.

**Signing and build**

- One release keystore, created once, backed up offline in two places, never committed. Losing it means users cannot update in place.
- Use Play-style app signing v2 and v3, enable R8 shrinking and resource shrinking, split APKs by ABI for the website (arm64-v8a first). Target APK size under 15 MB.
- Gradle version catalog for dependencies, a `release` build type with logs removed, and a `debug` type with an applicationId suffix so test and real data never mix.
- Reproducible builds: pin the Gradle, AGP, Kotlin and JDK versions in the repository.

**Versioning**

- Semantic versioning (1.0.0). `versionCode` = major x 10000 + minor x 100 + patch.
- Each release has a changelog with sections Added, Changed, Fixed, and a note on any database migration.
- A "Check for updates" row in Settings opens the release page in the browser, so the app needs no network permission of its own.

**Release checklist**

- [ ] All tests green, held-out evaluation report attached, release gates in section 15 met
- [ ] Manual QA checklist completed on the device matrix
- [ ] Dependency and license review done, notices screen updated
- [ ] Manifest reviewed: no INTERNET permission, no unexpected exported components
- [ ] Privacy statement text matches actual behavior in both languages
- [ ] Migration tested from the previous two released versions
- [ ] APK signed, checksum published, changelog written
- [ ] Tag created in git, release notes published

**Licensing and monetization.** Choose the license before the first public commit. For a portfolio project that also targets F-Droid, Apache 2.0 or GPLv3 are common choices. Revenue, if wanted, can come from optional supporter donations (for example via bKash) and a paid convenience download, never from selling or exposing user data.

## 18. Appendices

**A. Glossary**

| Term | Meaning |
| --- | --- |
| Paisa | 1/100 of a taka. All money is stored as integer paisa. |
| Allowlist | The list of SMS sender ids the app is allowed to read. All other senders are ignored. |
| Counterparty | The person, shop or agent on the other side of a transaction. |
| Counterparty key | Normalized counterparty text used to look up learned categories. |
| Merchant memory | Stored mapping from counterparty key to the category the user chose. |
| Unparsed | A message from an allowlisted sender that no parser could read. |
| Transfer | Movement between the user's own accounts or cash. Excluded from spending totals. |
| Dedupe hash | Fingerprint of a transaction used to stop the same SMS creating two entries. |
| Catch-up scan | A scan of the SMS inbox from the last processed message to now, for messages missed while the app was not running. |

**B. Definition of done (applies to every feature)**

- Works offline and with airplane mode on.
- All strings exist in English and Bangla, and layouts survive the longest Bangla string.
- Loading, empty, populated and error states are built and screenshot-tested.
- Light and dark themes both checked for contrast.
- TalkBack reads each item sensibly.
- Unit tests cover the logic, and a UI test covers the main path.
- No new permission, library or network code without a note in the decision log.
- Works at font scale 200% without clipped or overlapping text.

**C. Decision log (start it on day one)**

| Date | Decision | Reason | Revisit when |
| --- | --- | --- | --- |
| 2026-10-25 | Native Kotlin and Jetpack Compose | Needs SMS receivers and background work, which are simplest natively | If iOS becomes a goal |
| 2026-10-25 | No INTERNET permission in v1.0 | Strongest privacy claim and trust signal | If sync is added |
| 2026-10-25 | Money stored as integer paisa | Avoids floating point rounding errors | Never |
| 2026-10-25 | Rules before ML for categorization | Explainable, small, testable | After the labeled set exceeds 1,000 rows |

**D. Open questions for the product owner**

- Final app name and package id (suggested `app.khoroch`). Check name availability and trademark conflicts before publishing.
- Which five banks to support first? Decide from where the first 20 testers actually bank.
- Open source from day one, or closed until v1.0? This affects F-Droid and portfolio visibility.
- Is the supporter payment option wanted at launch or later?
- Will the first testers include shopkeepers like the Karim persona? If yes, schedule a Bangla-first usability session before the beta.

**E. Suggested repository layout**

```text
khoroch/
  app/                      Android app module, UI and navigation
  core/
    model/                  Pure Kotlin data classes
    parser/                 SMS parsers, no Android dependencies
    categorizer/            Rules and merchant memory logic
    database/               Room entities, DAOs, migrations
    data/                   Repositories
    designsystem/           Theme, components
    common/                 MoneyFormatter, date utils, dispatchers
  feature/
    home/ activity/ detail/ add/ budgets/ settings/ onboarding/ lock/ backup/
  assets/
    senders.json  rules.json
  eval/                     Labeled dataset tooling and report generator
  docs/                     This handoff, decision log, privacy statement
```
