package app.khoroch.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transaction_record",
    indices = [
        Index(value = ["occurredAt"]),
        Index(value = ["categoryId", "occurredAt"]),
        Index(value = ["accountId", "occurredAt"]),
        Index(value = ["dedupeHash"], unique = true)
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val accountId: Long = 1, // 1 is default Cash account
    val direction: Int, // 0 = EXPENSE, 1 = INCOME, 2 = TRANSFER
    val amountPaisa: Long,
    val feePaisa: Long = 0,
    val categoryId: String? = null,
    val categorySource: Int = 0, // 0 = DEFAULT, 1 = RULE, 2 = MERCHANT_MEMORY, 3 = USER, 4 = MODEL
    val counterparty: String,
    val counterpartyKey: String,
    val note: String? = null,
    val occurredAt: Long,
    val timeUncertain: Boolean = false,
    val provider: String,
    val referenceId: String? = null,
    val balanceAfterPaisa: Long? = null,
    val dedupeHash: String,
    val rawMessageId: Long? = null,
    val linkedTxnId: Long? = null,
    val parserVersion: Int = 1,
    val confidence: Float = 1.0f,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val deletedAt: Long? = null
)

@Entity(tableName = "category")
data class CategoryEntity(
    @PrimaryKey
    val id: String, // e.g. "food", "groceries", "transport"
    val nameEn: String,
    val nameBn: String,
    val icon: String,
    val colorHex: String,
    val sortOrder: Int = 0,
    val isSystem: Boolean = true,
    val archived: Boolean = false
)

@Entity(tableName = "account")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: String, // WALLET, BANK, CASH
    val provider: String,
    val openingBalancePaisa: Long = 0,
    val archived: Boolean = false
)

@Entity(tableName = "merchant_memory")
data class MerchantMemoryEntity(
    @PrimaryKey
    val counterpartyKey: String,
    val categoryId: String,
    val updatedAt: Long = System.currentTimeMillis(),
    val hits: Int = 1
)

@Entity(tableName = "budget")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val categoryId: String? = null, // null indicates overall monthly budget
    val amountPaisa: Long,
    val effectiveFrom: Long = System.currentTimeMillis()
)

@Entity(tableName = "budget_alert")
data class BudgetAlertEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val budgetId: Long,
    val month: String, // yyyyMM
    val threshold: Int, // 80 or 100
    val firedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "raw_message",
    indices = [Index(value = ["bodyHash"])]
)
data class RawMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sender: String,
    val body: String,
    val receivedAt: Long,
    val smsId: Long? = null,
    val bodyHash: String,
    val status: String // PARSED, UNPARSED, IGNORED
)

@Entity(tableName = "suppressed_hash")
data class SuppressedHashEntity(
    @PrimaryKey
    val dedupeHash: String,
    val suppressedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "tag")
data class TagEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String
)

@Entity(
    tableName = "transaction_tag",
    primaryKeys = ["transactionId", "tagId"]
)
data class TransactionTagCrossRef(
    val transactionId: Long,
    val tagId: Long
)

@Entity(tableName = "import_run")
data class ImportRunEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startedAt: Long,
    val finishedAt: Long? = null,
    val lastSmsId: Long = 0,
    val found: Int = 0,
    val saved: Int = 0,
    val failed: Int = 0
)

data class CategorySpending(
    val categoryId: String?,
    val totalPaisa: Long,
    val count: Int
)

data class DailySpending(
    val dayTimestamp: Long,
    val totalPaisa: Long
)
