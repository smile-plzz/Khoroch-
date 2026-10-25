package app.khoroch.data.local;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class KhorochDatabase_Impl extends KhorochDatabase {
  private volatile TransactionDao _transactionDao;

  private volatile CategoryDao _categoryDao;

  private volatile BudgetDao _budgetDao;

  private volatile MerchantMemoryDao _merchantMemoryDao;

  private volatile RawMessageDao _rawMessageDao;

  private volatile SuppressedHashDao _suppressedHashDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `transaction_record` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `accountId` INTEGER NOT NULL, `direction` INTEGER NOT NULL, `amountPaisa` INTEGER NOT NULL, `feePaisa` INTEGER NOT NULL, `categoryId` TEXT, `categorySource` INTEGER NOT NULL, `counterparty` TEXT NOT NULL, `counterpartyKey` TEXT NOT NULL, `note` TEXT, `occurredAt` INTEGER NOT NULL, `timeUncertain` INTEGER NOT NULL, `provider` TEXT NOT NULL, `referenceId` TEXT, `balanceAfterPaisa` INTEGER, `dedupeHash` TEXT NOT NULL, `rawMessageId` INTEGER, `linkedTxnId` INTEGER, `parserVersion` INTEGER NOT NULL, `confidence` REAL NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `deletedAt` INTEGER)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_transaction_record_occurredAt` ON `transaction_record` (`occurredAt`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_transaction_record_categoryId_occurredAt` ON `transaction_record` (`categoryId`, `occurredAt`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_transaction_record_accountId_occurredAt` ON `transaction_record` (`accountId`, `occurredAt`)");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_transaction_record_dedupeHash` ON `transaction_record` (`dedupeHash`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `category` (`id` TEXT NOT NULL, `nameEn` TEXT NOT NULL, `nameBn` TEXT NOT NULL, `icon` TEXT NOT NULL, `colorHex` TEXT NOT NULL, `sortOrder` INTEGER NOT NULL, `isSystem` INTEGER NOT NULL, `archived` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `account` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `type` TEXT NOT NULL, `provider` TEXT NOT NULL, `openingBalancePaisa` INTEGER NOT NULL, `archived` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `budget` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `categoryId` TEXT, `amountPaisa` INTEGER NOT NULL, `effectiveFrom` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `budget_alert` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `budgetId` INTEGER NOT NULL, `month` TEXT NOT NULL, `threshold` INTEGER NOT NULL, `firedAt` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `merchant_memory` (`counterpartyKey` TEXT NOT NULL, `categoryId` TEXT NOT NULL, `updatedAt` INTEGER NOT NULL, `hits` INTEGER NOT NULL, PRIMARY KEY(`counterpartyKey`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `raw_message` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `sender` TEXT NOT NULL, `body` TEXT NOT NULL, `receivedAt` INTEGER NOT NULL, `smsId` INTEGER, `bodyHash` TEXT NOT NULL, `status` TEXT NOT NULL)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_raw_message_bodyHash` ON `raw_message` (`bodyHash`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `suppressed_hash` (`dedupeHash` TEXT NOT NULL, `suppressedAt` INTEGER NOT NULL, PRIMARY KEY(`dedupeHash`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `tag` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `transaction_tag` (`transactionId` INTEGER NOT NULL, `tagId` INTEGER NOT NULL, PRIMARY KEY(`transactionId`, `tagId`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `import_run` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `startedAt` INTEGER NOT NULL, `finishedAt` INTEGER, `lastSmsId` INTEGER NOT NULL, `found` INTEGER NOT NULL, `saved` INTEGER NOT NULL, `failed` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '2359b47d77466389cb4808492527ef4f')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `transaction_record`");
        db.execSQL("DROP TABLE IF EXISTS `category`");
        db.execSQL("DROP TABLE IF EXISTS `account`");
        db.execSQL("DROP TABLE IF EXISTS `budget`");
        db.execSQL("DROP TABLE IF EXISTS `budget_alert`");
        db.execSQL("DROP TABLE IF EXISTS `merchant_memory`");
        db.execSQL("DROP TABLE IF EXISTS `raw_message`");
        db.execSQL("DROP TABLE IF EXISTS `suppressed_hash`");
        db.execSQL("DROP TABLE IF EXISTS `tag`");
        db.execSQL("DROP TABLE IF EXISTS `transaction_tag`");
        db.execSQL("DROP TABLE IF EXISTS `import_run`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsTransactionRecord = new HashMap<String, TableInfo.Column>(23);
        _columnsTransactionRecord.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactionRecord.put("accountId", new TableInfo.Column("accountId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactionRecord.put("direction", new TableInfo.Column("direction", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactionRecord.put("amountPaisa", new TableInfo.Column("amountPaisa", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactionRecord.put("feePaisa", new TableInfo.Column("feePaisa", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactionRecord.put("categoryId", new TableInfo.Column("categoryId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactionRecord.put("categorySource", new TableInfo.Column("categorySource", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactionRecord.put("counterparty", new TableInfo.Column("counterparty", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactionRecord.put("counterpartyKey", new TableInfo.Column("counterpartyKey", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactionRecord.put("note", new TableInfo.Column("note", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactionRecord.put("occurredAt", new TableInfo.Column("occurredAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactionRecord.put("timeUncertain", new TableInfo.Column("timeUncertain", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactionRecord.put("provider", new TableInfo.Column("provider", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactionRecord.put("referenceId", new TableInfo.Column("referenceId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactionRecord.put("balanceAfterPaisa", new TableInfo.Column("balanceAfterPaisa", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactionRecord.put("dedupeHash", new TableInfo.Column("dedupeHash", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactionRecord.put("rawMessageId", new TableInfo.Column("rawMessageId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactionRecord.put("linkedTxnId", new TableInfo.Column("linkedTxnId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactionRecord.put("parserVersion", new TableInfo.Column("parserVersion", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactionRecord.put("confidence", new TableInfo.Column("confidence", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactionRecord.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactionRecord.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactionRecord.put("deletedAt", new TableInfo.Column("deletedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysTransactionRecord = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesTransactionRecord = new HashSet<TableInfo.Index>(4);
        _indicesTransactionRecord.add(new TableInfo.Index("index_transaction_record_occurredAt", false, Arrays.asList("occurredAt"), Arrays.asList("ASC")));
        _indicesTransactionRecord.add(new TableInfo.Index("index_transaction_record_categoryId_occurredAt", false, Arrays.asList("categoryId", "occurredAt"), Arrays.asList("ASC", "ASC")));
        _indicesTransactionRecord.add(new TableInfo.Index("index_transaction_record_accountId_occurredAt", false, Arrays.asList("accountId", "occurredAt"), Arrays.asList("ASC", "ASC")));
        _indicesTransactionRecord.add(new TableInfo.Index("index_transaction_record_dedupeHash", true, Arrays.asList("dedupeHash"), Arrays.asList("ASC")));
        final TableInfo _infoTransactionRecord = new TableInfo("transaction_record", _columnsTransactionRecord, _foreignKeysTransactionRecord, _indicesTransactionRecord);
        final TableInfo _existingTransactionRecord = TableInfo.read(db, "transaction_record");
        if (!_infoTransactionRecord.equals(_existingTransactionRecord)) {
          return new RoomOpenHelper.ValidationResult(false, "transaction_record(app.khoroch.data.local.TransactionEntity).\n"
                  + " Expected:\n" + _infoTransactionRecord + "\n"
                  + " Found:\n" + _existingTransactionRecord);
        }
        final HashMap<String, TableInfo.Column> _columnsCategory = new HashMap<String, TableInfo.Column>(8);
        _columnsCategory.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCategory.put("nameEn", new TableInfo.Column("nameEn", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCategory.put("nameBn", new TableInfo.Column("nameBn", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCategory.put("icon", new TableInfo.Column("icon", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCategory.put("colorHex", new TableInfo.Column("colorHex", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCategory.put("sortOrder", new TableInfo.Column("sortOrder", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCategory.put("isSystem", new TableInfo.Column("isSystem", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCategory.put("archived", new TableInfo.Column("archived", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCategory = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCategory = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoCategory = new TableInfo("category", _columnsCategory, _foreignKeysCategory, _indicesCategory);
        final TableInfo _existingCategory = TableInfo.read(db, "category");
        if (!_infoCategory.equals(_existingCategory)) {
          return new RoomOpenHelper.ValidationResult(false, "category(app.khoroch.data.local.CategoryEntity).\n"
                  + " Expected:\n" + _infoCategory + "\n"
                  + " Found:\n" + _existingCategory);
        }
        final HashMap<String, TableInfo.Column> _columnsAccount = new HashMap<String, TableInfo.Column>(6);
        _columnsAccount.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAccount.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAccount.put("type", new TableInfo.Column("type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAccount.put("provider", new TableInfo.Column("provider", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAccount.put("openingBalancePaisa", new TableInfo.Column("openingBalancePaisa", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAccount.put("archived", new TableInfo.Column("archived", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysAccount = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesAccount = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoAccount = new TableInfo("account", _columnsAccount, _foreignKeysAccount, _indicesAccount);
        final TableInfo _existingAccount = TableInfo.read(db, "account");
        if (!_infoAccount.equals(_existingAccount)) {
          return new RoomOpenHelper.ValidationResult(false, "account(app.khoroch.data.local.AccountEntity).\n"
                  + " Expected:\n" + _infoAccount + "\n"
                  + " Found:\n" + _existingAccount);
        }
        final HashMap<String, TableInfo.Column> _columnsBudget = new HashMap<String, TableInfo.Column>(4);
        _columnsBudget.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBudget.put("categoryId", new TableInfo.Column("categoryId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBudget.put("amountPaisa", new TableInfo.Column("amountPaisa", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBudget.put("effectiveFrom", new TableInfo.Column("effectiveFrom", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysBudget = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesBudget = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoBudget = new TableInfo("budget", _columnsBudget, _foreignKeysBudget, _indicesBudget);
        final TableInfo _existingBudget = TableInfo.read(db, "budget");
        if (!_infoBudget.equals(_existingBudget)) {
          return new RoomOpenHelper.ValidationResult(false, "budget(app.khoroch.data.local.BudgetEntity).\n"
                  + " Expected:\n" + _infoBudget + "\n"
                  + " Found:\n" + _existingBudget);
        }
        final HashMap<String, TableInfo.Column> _columnsBudgetAlert = new HashMap<String, TableInfo.Column>(5);
        _columnsBudgetAlert.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBudgetAlert.put("budgetId", new TableInfo.Column("budgetId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBudgetAlert.put("month", new TableInfo.Column("month", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBudgetAlert.put("threshold", new TableInfo.Column("threshold", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBudgetAlert.put("firedAt", new TableInfo.Column("firedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysBudgetAlert = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesBudgetAlert = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoBudgetAlert = new TableInfo("budget_alert", _columnsBudgetAlert, _foreignKeysBudgetAlert, _indicesBudgetAlert);
        final TableInfo _existingBudgetAlert = TableInfo.read(db, "budget_alert");
        if (!_infoBudgetAlert.equals(_existingBudgetAlert)) {
          return new RoomOpenHelper.ValidationResult(false, "budget_alert(app.khoroch.data.local.BudgetAlertEntity).\n"
                  + " Expected:\n" + _infoBudgetAlert + "\n"
                  + " Found:\n" + _existingBudgetAlert);
        }
        final HashMap<String, TableInfo.Column> _columnsMerchantMemory = new HashMap<String, TableInfo.Column>(4);
        _columnsMerchantMemory.put("counterpartyKey", new TableInfo.Column("counterpartyKey", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMerchantMemory.put("categoryId", new TableInfo.Column("categoryId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMerchantMemory.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMerchantMemory.put("hits", new TableInfo.Column("hits", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysMerchantMemory = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesMerchantMemory = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoMerchantMemory = new TableInfo("merchant_memory", _columnsMerchantMemory, _foreignKeysMerchantMemory, _indicesMerchantMemory);
        final TableInfo _existingMerchantMemory = TableInfo.read(db, "merchant_memory");
        if (!_infoMerchantMemory.equals(_existingMerchantMemory)) {
          return new RoomOpenHelper.ValidationResult(false, "merchant_memory(app.khoroch.data.local.MerchantMemoryEntity).\n"
                  + " Expected:\n" + _infoMerchantMemory + "\n"
                  + " Found:\n" + _existingMerchantMemory);
        }
        final HashMap<String, TableInfo.Column> _columnsRawMessage = new HashMap<String, TableInfo.Column>(7);
        _columnsRawMessage.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRawMessage.put("sender", new TableInfo.Column("sender", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRawMessage.put("body", new TableInfo.Column("body", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRawMessage.put("receivedAt", new TableInfo.Column("receivedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRawMessage.put("smsId", new TableInfo.Column("smsId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRawMessage.put("bodyHash", new TableInfo.Column("bodyHash", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRawMessage.put("status", new TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysRawMessage = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesRawMessage = new HashSet<TableInfo.Index>(1);
        _indicesRawMessage.add(new TableInfo.Index("index_raw_message_bodyHash", false, Arrays.asList("bodyHash"), Arrays.asList("ASC")));
        final TableInfo _infoRawMessage = new TableInfo("raw_message", _columnsRawMessage, _foreignKeysRawMessage, _indicesRawMessage);
        final TableInfo _existingRawMessage = TableInfo.read(db, "raw_message");
        if (!_infoRawMessage.equals(_existingRawMessage)) {
          return new RoomOpenHelper.ValidationResult(false, "raw_message(app.khoroch.data.local.RawMessageEntity).\n"
                  + " Expected:\n" + _infoRawMessage + "\n"
                  + " Found:\n" + _existingRawMessage);
        }
        final HashMap<String, TableInfo.Column> _columnsSuppressedHash = new HashMap<String, TableInfo.Column>(2);
        _columnsSuppressedHash.put("dedupeHash", new TableInfo.Column("dedupeHash", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSuppressedHash.put("suppressedAt", new TableInfo.Column("suppressedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSuppressedHash = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSuppressedHash = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSuppressedHash = new TableInfo("suppressed_hash", _columnsSuppressedHash, _foreignKeysSuppressedHash, _indicesSuppressedHash);
        final TableInfo _existingSuppressedHash = TableInfo.read(db, "suppressed_hash");
        if (!_infoSuppressedHash.equals(_existingSuppressedHash)) {
          return new RoomOpenHelper.ValidationResult(false, "suppressed_hash(app.khoroch.data.local.SuppressedHashEntity).\n"
                  + " Expected:\n" + _infoSuppressedHash + "\n"
                  + " Found:\n" + _existingSuppressedHash);
        }
        final HashMap<String, TableInfo.Column> _columnsTag = new HashMap<String, TableInfo.Column>(2);
        _columnsTag.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTag.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysTag = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesTag = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoTag = new TableInfo("tag", _columnsTag, _foreignKeysTag, _indicesTag);
        final TableInfo _existingTag = TableInfo.read(db, "tag");
        if (!_infoTag.equals(_existingTag)) {
          return new RoomOpenHelper.ValidationResult(false, "tag(app.khoroch.data.local.TagEntity).\n"
                  + " Expected:\n" + _infoTag + "\n"
                  + " Found:\n" + _existingTag);
        }
        final HashMap<String, TableInfo.Column> _columnsTransactionTag = new HashMap<String, TableInfo.Column>(2);
        _columnsTransactionTag.put("transactionId", new TableInfo.Column("transactionId", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactionTag.put("tagId", new TableInfo.Column("tagId", "INTEGER", true, 2, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysTransactionTag = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesTransactionTag = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoTransactionTag = new TableInfo("transaction_tag", _columnsTransactionTag, _foreignKeysTransactionTag, _indicesTransactionTag);
        final TableInfo _existingTransactionTag = TableInfo.read(db, "transaction_tag");
        if (!_infoTransactionTag.equals(_existingTransactionTag)) {
          return new RoomOpenHelper.ValidationResult(false, "transaction_tag(app.khoroch.data.local.TransactionTagCrossRef).\n"
                  + " Expected:\n" + _infoTransactionTag + "\n"
                  + " Found:\n" + _existingTransactionTag);
        }
        final HashMap<String, TableInfo.Column> _columnsImportRun = new HashMap<String, TableInfo.Column>(7);
        _columnsImportRun.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsImportRun.put("startedAt", new TableInfo.Column("startedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsImportRun.put("finishedAt", new TableInfo.Column("finishedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsImportRun.put("lastSmsId", new TableInfo.Column("lastSmsId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsImportRun.put("found", new TableInfo.Column("found", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsImportRun.put("saved", new TableInfo.Column("saved", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsImportRun.put("failed", new TableInfo.Column("failed", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysImportRun = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesImportRun = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoImportRun = new TableInfo("import_run", _columnsImportRun, _foreignKeysImportRun, _indicesImportRun);
        final TableInfo _existingImportRun = TableInfo.read(db, "import_run");
        if (!_infoImportRun.equals(_existingImportRun)) {
          return new RoomOpenHelper.ValidationResult(false, "import_run(app.khoroch.data.local.ImportRunEntity).\n"
                  + " Expected:\n" + _infoImportRun + "\n"
                  + " Found:\n" + _existingImportRun);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "2359b47d77466389cb4808492527ef4f", "51fddd725bf1f44c4ef6e2835780bf26");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "transaction_record","category","account","budget","budget_alert","merchant_memory","raw_message","suppressed_hash","tag","transaction_tag","import_run");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `transaction_record`");
      _db.execSQL("DELETE FROM `category`");
      _db.execSQL("DELETE FROM `account`");
      _db.execSQL("DELETE FROM `budget`");
      _db.execSQL("DELETE FROM `budget_alert`");
      _db.execSQL("DELETE FROM `merchant_memory`");
      _db.execSQL("DELETE FROM `raw_message`");
      _db.execSQL("DELETE FROM `suppressed_hash`");
      _db.execSQL("DELETE FROM `tag`");
      _db.execSQL("DELETE FROM `transaction_tag`");
      _db.execSQL("DELETE FROM `import_run`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(TransactionDao.class, TransactionDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(CategoryDao.class, CategoryDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(BudgetDao.class, BudgetDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(MerchantMemoryDao.class, MerchantMemoryDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(RawMessageDao.class, RawMessageDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(SuppressedHashDao.class, SuppressedHashDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public TransactionDao transactionDao() {
    if (_transactionDao != null) {
      return _transactionDao;
    } else {
      synchronized(this) {
        if(_transactionDao == null) {
          _transactionDao = new TransactionDao_Impl(this);
        }
        return _transactionDao;
      }
    }
  }

  @Override
  public CategoryDao categoryDao() {
    if (_categoryDao != null) {
      return _categoryDao;
    } else {
      synchronized(this) {
        if(_categoryDao == null) {
          _categoryDao = new CategoryDao_Impl(this);
        }
        return _categoryDao;
      }
    }
  }

  @Override
  public BudgetDao budgetDao() {
    if (_budgetDao != null) {
      return _budgetDao;
    } else {
      synchronized(this) {
        if(_budgetDao == null) {
          _budgetDao = new BudgetDao_Impl(this);
        }
        return _budgetDao;
      }
    }
  }

  @Override
  public MerchantMemoryDao merchantMemoryDao() {
    if (_merchantMemoryDao != null) {
      return _merchantMemoryDao;
    } else {
      synchronized(this) {
        if(_merchantMemoryDao == null) {
          _merchantMemoryDao = new MerchantMemoryDao_Impl(this);
        }
        return _merchantMemoryDao;
      }
    }
  }

  @Override
  public RawMessageDao rawMessageDao() {
    if (_rawMessageDao != null) {
      return _rawMessageDao;
    } else {
      synchronized(this) {
        if(_rawMessageDao == null) {
          _rawMessageDao = new RawMessageDao_Impl(this);
        }
        return _rawMessageDao;
      }
    }
  }

  @Override
  public SuppressedHashDao suppressedHashDao() {
    if (_suppressedHashDao != null) {
      return _suppressedHashDao;
    } else {
      synchronized(this) {
        if(_suppressedHashDao == null) {
          _suppressedHashDao = new SuppressedHashDao_Impl(this);
        }
        return _suppressedHashDao;
      }
    }
  }
}
