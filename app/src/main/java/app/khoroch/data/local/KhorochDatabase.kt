package app.khoroch.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        AccountEntity::class,
        BudgetEntity::class,
        BudgetAlertEntity::class,
        MerchantMemoryEntity::class,
        RawMessageEntity::class,
        SuppressedHashEntity::class,
        TagEntity::class,
        TransactionTagCrossRef::class,
        ImportRunEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class KhorochDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao
    abstract fun merchantMemoryDao(): MerchantMemoryDao
    abstract fun rawMessageDao(): RawMessageDao
    abstract fun suppressedHashDao(): SuppressedHashDao

    companion object {
        @Volatile
        private var INSTANCE: KhorochDatabase? = null

        fun getInstance(context: Context): KhorochDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KhorochDatabase::class.java,
                    "khoroch.db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Seed default categories and default cash account
                        CoroutineScope(Dispatchers.IO).launch {
                            val database = getInstance(context)
                            seedDefaultData(database)
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun seedDefaultData(db: KhorochDatabase) {
            val categories = listOf(
                CategoryEntity("food", "Food & Dining", "খাবার", "restaurant", "#0F766E", 1),
                CategoryEntity("groceries", "Groceries", "মুদি", "shopping_cart", "#E69F00", 2),
                CategoryEntity("transport", "Transport", "যাতায়াত", "directions_car", "#56B4E9", 3),
                CategoryEntity("bills", "Bills & Utilities", "বিল", "receipt_long", "#009E73", 4),
                CategoryEntity("rent", "Rent & Housing", "বাসা ভাড়া", "home", "#0072B2", 5),
                CategoryEntity("shopping", "Shopping", "কেনাকাটা", "shopping_bag", "#D55E00", 6),
                CategoryEntity("health", "Health & Medical", "স্বাস্থ্য", "local_hospital", "#CC79A7", 7),
                CategoryEntity("education", "Education", "শিক্ষা", "school", "#F0E442", 8),
                CategoryEntity("entertainment", "Entertainment", "বিনোদন", "movie", "#9C27B0", 9),
                CategoryEntity("family", "Family & Gifts", "পরিবার", "card_giftcard", "#E91E63", 10),
                CategoryEntity("transfer", "Transfer", "ট্রান্সফার", "swap_horiz", "#607D8B", 11),
                CategoryEntity("income", "Income", "আয়", "account_balance_wallet", "#1B7F3B", 12),
                CategoryEntity("other", "Other", "অন্যান্য", "more_horiz", "#8A949B", 13)
            )
            db.categoryDao().insertAll(categories)
        }
    }
}
