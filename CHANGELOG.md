# Changelog - Khoroch (খরচ)

All notable changes to this project will be documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]
### Added
- Project documentation: README, Architecture & Context, Dev Log, and Changelog.
- Pure Kotlin SMS parsing engine supporting bKash, Nagad, Rocket, Upay, and Bank formats with synthetic test verification.
- South Asian currency formatting (`MoneyFormatter`) with lakh/crore grouping and English/Bangla numeral conversion.
- Rule-based categorization engine with merchant memory and normalization.
- SQLite Room database layer (`khoroch.db`) with entities for transactions, categories, budgets, merchant memories, and raw messages.
- Jetpack Compose Material 3 UI design system with custom Canvas Donut & Bar charts.
- Full localization strings in English (`values/strings.xml`) and Bangla (`values-bn/strings.xml`).
