package app.khoroch.data.repository

import android.content.Context
import app.khoroch.core.categorizer.CategorizationEngine
import app.khoroch.core.categorizer.MerchantNormalizer
import app.khoroch.core.common.HashUtils
import app.khoroch.core.model.CategorySource
import app.khoroch.core.model.Direction
import app.khoroch.core.model.ParsedTxn
import app.khoroch.data.local.CategorySpending
import app.khoroch.data.local.DailySpending
import app.khoroch.data.local.KhorochDatabase
import app.khoroch.data.local.MerchantMemoryEntity
import app.khoroch.data.local.RawMessageEntity
import app.khoroch.data.local.SuppressedHashEntity
import app.khoroch.data.local.TagEntity
import app.khoroch.data.local.TransactionEntity
import app.khoroch.data.local.TransactionTagCrossRef
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class TransactionRepository(
    private val context: Context,
    private val database: KhorochDatabase = KhorochDatabase.getInstance(context)
) {
    private val transactionDao = database.transactionDao()
    private val categoryDao = database.categoryDao()
    private val budgetDao = database.budgetDao()
    private val merchantMemoryDao = database.merchantMemoryDao()
    private val rawMessageDao = database.rawMessageDao()
    private val suppressedHashDao = database.suppressedHashDao()
    private val tagDao = database.tagDao()

    private val categorizer: CategorizationEngine by lazy {
        val rulesJson = try {
            context.assets.open("rules.json").use { InputStreamReader(it).readText() }
        } catch (_: Exception) {
            "{}"
        }
        CategorizationEngine(CategorizationEngine.parseRulesFromJson(rulesJson))
    }

    fun getAllTransactions(): Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()

    fun getTransactionsForMonth(startEpoch: Long, endEpoch: Long): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsBetween(startEpoch, endEpoch)

    fun getCategorySpending(startEpoch: Long, endEpoch: Long): Flow<List<CategorySpending>> =
        transactionDao.getSpendingByCategory(startEpoch, endEpoch)

    fun getTotalExpense(startEpoch: Long, endEpoch: Long): Flow<Long?> =
        transactionDao.getTotalExpense(startEpoch, endEpoch)

    fun getTotalIncome(startEpoch: Long, endEpoch: Long): Flow<Long?> =
        transactionDao.getTotalIncome(startEpoch, endEpoch)

    fun getAllCategories() = categoryDao.getAllCategories()

    fun getAllBudgets() = budgetDao.getAllBudgets()

    fun getUnparsedCount() = rawMessageDao.getUnparsedCount()

    fun getUnparsedMessages() = rawMessageDao.getUnparsedMessages()

    suspend fun saveParsedTransaction(
        parsed: ParsedTxn,
        rawSender: String,
        rawBody: String,
        smsId: Long? = null
    ): Long = withContext(Dispatchers.IO) {
        // 1. Calculate Dedupe Hash
        val dedupeHash = if (!parsed.referenceId.isNullOrBlank()) {
            HashUtils.sha256("${parsed.provider.lowercase()}:${parsed.referenceId}")
        } else {
            val minuteBucket = parsed.timestamp / 60000
            HashUtils.sha256("$rawSender:$rawBody:$minuteBucket")
        }

        // 2. Check suppressed or duplicate
        if (suppressedHashDao.isSuppressed(dedupeHash) > 0) return@withContext -1L
        if (transactionDao.hasDedupeHash(dedupeHash) > 0) return@withContext -1L

        // 3. Save Raw Message for audit
        val bodyHash = HashUtils.sha256(rawBody)
        val rawMsgId = rawMessageDao.insert(
            RawMessageEntity(
                sender = rawSender,
                body = rawBody,
                receivedAt = parsed.timestamp,
                smsId = smsId,
                bodyHash = bodyHash,
                status = "PARSED"
            )
        )

        // 4. Categorize
        val counterpartyKey = MerchantNormalizer.normalize(parsed.counterparty)
        val memoryCategory = merchantMemoryDao.getCategoryForKey(counterpartyKey)
        val (assignedCat, catSource) = categorizer.categorize(
            counterparty = parsed.counterparty,
            body = rawBody,
            direction = parsed.direction,
            provider = parsed.provider,
            merchantMemoryCategory = memoryCategory
        )

        // 5. Insert Transaction
        val entity = TransactionEntity(
            direction = parsed.direction.value,
            amountPaisa = parsed.amountPaisa,
            feePaisa = parsed.feePaisa,
            categoryId = assignedCat,
            categorySource = catSource.value,
            counterparty = parsed.counterparty,
            counterpartyKey = counterpartyKey,
            occurredAt = parsed.timestamp,
            provider = parsed.provider,
            referenceId = parsed.referenceId,
            balanceAfterPaisa = parsed.balanceAfterPaisa,
            dedupeHash = dedupeHash,
            rawMessageId = rawMsgId,
            confidence = parsed.confidence
        )

        transactionDao.insert(entity)
    }

    suspend fun logManualTransaction(
        amountPaisa: Long,
        direction: Direction,
        categoryId: String,
        counterparty: String,
        note: String?,
        timestamp: Long = System.currentTimeMillis()
    ): Long = withContext(Dispatchers.IO) {
        val counterpartyKey = MerchantNormalizer.normalize(counterparty)
        val dedupeHash = HashUtils.sha256("manual:${System.nanoTime()}:$amountPaisa")
        val entity = TransactionEntity(
            direction = direction.value,
            amountPaisa = amountPaisa,
            feePaisa = 0L,
            categoryId = categoryId,
            categorySource = CategorySource.USER.value,
            counterparty = counterparty.ifBlank { "Cash Transaction" },
            counterpartyKey = counterpartyKey,
            note = note,
            occurredAt = timestamp,
            provider = "manual",
            dedupeHash = dedupeHash
        )
        transactionDao.insert(entity)
    }

    suspend fun updateCategory(txnId: Long, categoryId: String, applyToAll: Boolean) = withContext(Dispatchers.IO) {
        val txn = transactionDao.getById(txnId) ?: return@withContext
        transactionDao.updateCategory(txnId, categoryId, CategorySource.USER.value)

        if (applyToAll && txn.counterpartyKey.isNotBlank()) {
            merchantMemoryDao.upsert(
                MerchantMemoryEntity(
                    counterpartyKey = txn.counterpartyKey,
                    categoryId = categoryId,
                    updatedAt = System.currentTimeMillis()
                )
            )
            transactionDao.updateCategoryForMerchant(txn.counterpartyKey, categoryId)
        }
    }

    suspend fun deleteTransaction(id: Long) = withContext(Dispatchers.IO) {
        val txn = transactionDao.getById(id)
        if (txn != null) {
            transactionDao.softDelete(id)
            suppressedHashDao.suppress(SuppressedHashEntity(txn.dedupeHash))
        }
    }

    suspend fun restoreTransaction(id: Long) = withContext(Dispatchers.IO) {
        transactionDao.restore(id)
    }

    suspend fun getRawMessage(id: Long): RawMessageEntity? = withContext(Dispatchers.IO) {
        rawMessageDao.getById(id)
    }

    suspend fun splitTransaction(
        originalId: Long,
        firstPartPaisa: Long,
        firstPartCategory: String,
        secondPartPaisa: Long,
        secondPartCategory: String
    ): Boolean = withContext(Dispatchers.IO) {
        val original = transactionDao.getById(originalId) ?: return@withContext false
        if (firstPartPaisa + secondPartPaisa != original.amountPaisa) return@withContext false

        // 1. Update original with first part
        val updatedOriginal = original.copy(
            amountPaisa = firstPartPaisa,
            categoryId = firstPartCategory,
            categorySource = CategorySource.USER.value,
            updatedAt = System.currentTimeMillis()
        )
        transactionDao.update(updatedOriginal)

        // 2. Insert split sibling transaction
        val splitSibling = original.copy(
            id = 0,
            amountPaisa = secondPartPaisa,
            feePaisa = 0L,
            categoryId = secondPartCategory,
            categorySource = CategorySource.USER.value,
            linkedTxnId = originalId,
            dedupeHash = HashUtils.sha256("split:$originalId:${System.nanoTime()}"),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        transactionDao.insert(splitSibling)
        true
    }

    fun getAllTags(): Flow<List<TagEntity>> = tagDao.getAllTags()

    fun getTagsForTransaction(txnId: Long): Flow<List<TagEntity>> = tagDao.getTagsForTransaction(txnId)

    suspend fun addTag(txnId: Long, rawTag: String) = withContext(Dispatchers.IO) {
        val cleanTag = rawTag.trim().lowercase().removePrefix("#")
        if (cleanTag.isBlank()) return@withContext

        var tagId = tagDao.getTagByName(cleanTag)
        if (tagId == null) {
            tagId = tagDao.insertTag(TagEntity(name = cleanTag))
        }
        if (tagId > 0) {
            tagDao.linkTag(TransactionTagCrossRef(transactionId = txnId, tagId = tagId))
        }
    }

    suspend fun removeTag(txnId: Long, tagId: Long) = withContext(Dispatchers.IO) {
        tagDao.unlinkTag(txnId, tagId)
    }

    fun getAllMerchantMemories(): Flow<List<MerchantMemoryEntity>> = merchantMemoryDao.getAll()

    suspend fun deleteMerchantMemory(key: String) = withContext(Dispatchers.IO) {
        merchantMemoryDao.delete(key)
    }

    suspend fun saveUnparsedMessage(sender: String, body: String, receivedAt: Long) = withContext(Dispatchers.IO) {
        val bodyHash = HashUtils.sha256(body)
        rawMessageDao.insert(
            RawMessageEntity(
                sender = sender,
                body = body,
                receivedAt = receivedAt,
                bodyHash = bodyHash,
                status = "UNPARSED"
            )
        )
    }

    suspend fun exportToCsv(transactions: List<TransactionEntity>): String = withContext(Dispatchers.Default) {
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+06:00", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("Asia/Dhaka")
        }
        val sb = StringBuilder()
        // UTF-8 BOM
        sb.append("\uFEFF")
        sb.append("date,type,amount_tk,fee_tk,category,account,provider,counterparty,reference_id,tags,note\n")

        for (t in transactions) {
            val dateStr = isoFormat.format(t.occurredAt)
            val typeStr = when (t.direction) {
                0 -> "expense"
                1 -> "income"
                else -> "transfer"
            }
            val amountTk = String.format(Locale.US, "%.2f", t.amountPaisa / 100.0)
            val feeTk = String.format(Locale.US, "%.2f", t.feePaisa / 100.0)
            val category = t.categoryId ?: "uncategorized"
            val account = "Cash"
            val provider = t.provider
            val counterparty = "\"${t.counterparty.replace("\"", "\"\"")}\""
            val ref = t.referenceId ?: ""
            val tags = ""
            val note = "\"${(t.note ?: "").replace("\"", "\"\"")}\""

            sb.append("$dateStr,$typeStr,$amountTk,$feeTk,$category,$account,$provider,$counterparty,$ref,$tags,$note\n")
        }
        sb.toString()
    }

    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        transactionDao.deleteAll()
        rawMessageDao.clear()
        merchantMemoryDao.clear()
        budgetDao.deleteAll()
    }
}
