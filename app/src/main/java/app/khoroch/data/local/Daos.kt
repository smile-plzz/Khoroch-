package app.khoroch.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transaction_record WHERE deletedAt IS NULL ORDER BY occurredAt DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transaction_record WHERE id = :id")
    suspend fun getById(id: Long): TransactionEntity?

    @Query("SELECT * FROM transaction_record WHERE occurredAt BETWEEN :start AND :end AND deletedAt IS NULL ORDER BY occurredAt DESC")
    fun getTransactionsBetween(start: Long, end: Long): Flow<List<TransactionEntity>>

    @Query("SELECT categoryId, SUM(amountPaisa + feePaisa) AS totalPaisa, COUNT(*) AS count FROM transaction_record WHERE occurredAt BETWEEN :start AND :end AND deletedAt IS NULL AND direction = 0 GROUP BY categoryId")
    fun getSpendingByCategory(start: Long, end: Long): Flow<List<CategorySpending>>

    @Query("SELECT SUM(amountPaisa + feePaisa) FROM transaction_record WHERE occurredAt BETWEEN :start AND :end AND deletedAt IS NULL AND direction = 0")
    fun getTotalExpense(start: Long, end: Long): Flow<Long?>

    @Query("SELECT SUM(amountPaisa) FROM transaction_record WHERE occurredAt BETWEEN :start AND :end AND deletedAt IS NULL AND direction = 1")
    fun getTotalIncome(start: Long, end: Long): Flow<Long?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(transactions: List<TransactionEntity>): List<Long>

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Query("UPDATE transaction_record SET deletedAt = :deletedAt WHERE id = :id")
    suspend fun softDelete(id: Long, deletedAt: Long = System.currentTimeMillis())

    @Query("UPDATE transaction_record SET deletedAt = NULL WHERE id = :id")
    suspend fun restore(id: Long)

    @Query("UPDATE transaction_record SET categoryId = :categoryId, categorySource = :source, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateCategory(id: Long, categoryId: String, source: Int, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE transaction_record SET categoryId = :categoryId, categorySource = 2, updatedAt = :updatedAt WHERE counterpartyKey = :key AND deletedAt IS NULL")
    suspend fun updateCategoryForMerchant(key: String, categoryId: String, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM transaction_record WHERE dedupeHash = :hash")
    suspend fun hasDedupeHash(hash: String): Int

    @Query("DELETE FROM transaction_record")
    suspend fun deleteAll()
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM category WHERE archived = 0 ORDER BY sortOrder ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<CategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: CategoryEntity)

    @Query("SELECT COUNT(*) FROM category")
    suspend fun count(): Int
}

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budget")
    fun getAllBudgets(): Flow<List<BudgetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(budget: BudgetEntity)

    @Query("DELETE FROM budget WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM budget_alert WHERE budgetId = :budgetId AND month = :month AND threshold = :threshold LIMIT 1")
    suspend fun getAlert(budgetId: Long, month: String, threshold: Int): BudgetAlertEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun recordAlert(alert: BudgetAlertEntity)

    @Query("DELETE FROM budget")
    suspend fun deleteAll()
}

@Dao
interface MerchantMemoryDao {
    @Query("SELECT categoryId FROM merchant_memory WHERE counterpartyKey = :key")
    suspend fun getCategoryForKey(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: MerchantMemoryEntity)

    @Query("SELECT * FROM merchant_memory ORDER BY hits DESC, updatedAt DESC")
    fun getAll(): Flow<List<MerchantMemoryEntity>>

    @Query("DELETE FROM merchant_memory WHERE counterpartyKey = :key")
    suspend fun delete(key: String)

    @Query("DELETE FROM merchant_memory")
    suspend fun clear()
}

@Dao
interface RawMessageDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: RawMessageEntity): Long

    @Query("SELECT * FROM raw_message WHERE status = 'UNPARSED' ORDER BY receivedAt DESC")
    fun getUnparsedMessages(): Flow<List<RawMessageEntity>>

    @Query("SELECT COUNT(*) FROM raw_message WHERE status = 'UNPARSED'")
    fun getUnparsedCount(): Flow<Int>

    @Query("UPDATE raw_message SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)

    @Query("DELETE FROM raw_message")
    suspend fun clear()
}

@Dao
interface SuppressedHashDao {
    @Query("SELECT COUNT(*) FROM suppressed_hash WHERE dedupeHash = :hash")
    suspend fun isSuppressed(hash: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun suppress(entity: SuppressedHashEntity)
}
