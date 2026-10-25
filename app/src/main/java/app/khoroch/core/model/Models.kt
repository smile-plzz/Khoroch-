package app.khoroch.core.model

/**
 * Monetary direction of a transaction.
 */
enum class Direction(val value: Int) {
    EXPENSE(0),
    INCOME(1),
    TRANSFER(2);

    companion object {
        fun fromValue(value: Int): Direction = entries.firstOrNull { it.value == value } ?: EXPENSE
    }
}

/**
 * The authority that assigned the category to the transaction.
 */
enum class CategorySource(val value: Int) {
    DEFAULT(0),
    RULE(1),
    MERCHANT_MEMORY(2),
    USER(3),
    MODEL(4);

    companion object {
        fun fromValue(value: Int): CategorySource = entries.firstOrNull { it.value == value } ?: DEFAULT
    }
}

/**
 * Raw SMS parse result from provider parsers.
 */
data class ParsedTxn(
    val amountPaisa: Long,
    val feePaisa: Long = 0L,
    val currency: String = "BDT",
    val direction: Direction,
    val provider: String,
    val counterparty: String,
    val referenceId: String?,
    val balanceAfterPaisa: Long? = null,
    val timestamp: Long,
    val confidence: Float = 1.0f
)

/**
 * Sealed outcome of parsing an SMS.
 */
sealed interface ParseResult {
    data class Success(val txn: ParsedTxn) : ParseResult
    data class Ignored(val reason: String) : ParseResult
    data class Unparsed(val reason: String) : ParseResult
}

/**
 * Account type.
 */
enum class AccountType {
    WALLET,
    BANK,
    CASH
}
