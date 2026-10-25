package app.khoroch.core.categorizer

import app.khoroch.core.model.CategorySource
import app.khoroch.core.model.Direction
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.Locale

@Serializable
data class CategorizationRule(
    val id: String,
    val category: String,
    val field: String, // "counterparty" or "body"
    val match: String, // "contains_any" or "regex"
    val values: List<String>,
    val priority: Int = 50
)

@Serializable
data class RulesConfig(
    val version: Int = 1,
    val rules: List<CategorizationRule> = emptyList()
)

object MerchantNormalizer {
    private val STRIP_WORDS = setOf("ltd", "limited", "store", "shop", "branch", "enterprise", "mart", "dhaka", "bd")

    fun normalize(counterparty: String): String {
        var text = counterparty.trim().lowercase(Locale.ROOT)
        // If it looks like a phone number, keep it as is
        if (text.startsWith("01") && text.length in 11..13) {
            return text
        }
        val words = text.split("\\s+".toRegex()).filter { it !in STRIP_WORDS && it.isNotBlank() }
        return words.joinToString(" ")
    }
}

class CategorizationEngine(
    private val rules: List<CategorizationRule> = emptyList()
) {
    /**
     * Categorizes a transaction based on the strict precedence hierarchy.
     */
    fun categorize(
        counterparty: String,
        body: String,
        direction: Direction,
        provider: String,
        merchantMemoryCategory: String? = null
    ): Pair<String, CategorySource> {
        val counterpartyKey = MerchantNormalizer.normalize(counterparty)

        // 1. Merchant Memory
        if (!merchantMemoryCategory.isNullOrBlank()) {
            return Pair(merchantMemoryCategory, CategorySource.MERCHANT_MEMORY)
        }

        // 2. Rule Engine (sorted by priority descending, then longest matched text)
        val sortedRules = rules.sortedByDescending { it.priority }
        for (rule in sortedRules) {
            val targetField = when (rule.field) {
                "counterparty" -> counterpartyKey
                "body" -> body.lowercase(Locale.ROOT)
                else -> "$counterpartyKey $body".lowercase(Locale.ROOT)
            }

            val matches = when (rule.match) {
                "contains_any" -> rule.values.any { targetField.contains(it.lowercase(Locale.ROOT)) }
                "regex" -> rule.values.any { pattern ->
                    try {
                        pattern.toRegex(RegexOption.IGNORE_CASE).containsMatchIn(targetField)
                    } catch (_: Exception) {
                        false
                    }
                }
                else -> false
            }

            if (matches) {
                return Pair(rule.category, CategorySource.RULE)
            }
        }

        // 3. Provider / Direction Defaults
        if (direction == Direction.TRANSFER) {
            return Pair("transfer", CategorySource.DEFAULT)
        }
        if (direction == Direction.INCOME) {
            return Pair("income", CategorySource.DEFAULT)
        }
        if (counterparty.contains("recharge", ignoreCase = true)) {
            return Pair("bills", CategorySource.DEFAULT)
        }

        // 4. Default Uncategorized
        return Pair("other", CategorySource.DEFAULT)
    }

    companion object {
        fun parseRulesFromJson(jsonString: String): List<CategorizationRule> {
            return try {
                val config = Json { ignoreUnknownKeys = true }.decodeFromString<RulesConfig>(jsonString)
                config.rules
            } catch (_: Exception) {
                emptyList()
            }
        }
    }
}
