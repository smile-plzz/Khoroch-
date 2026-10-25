package app.khoroch.data.local;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class TransactionDao_Impl implements TransactionDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<TransactionEntity> __insertionAdapterOfTransactionEntity;

  private final EntityDeletionOrUpdateAdapter<TransactionEntity> __updateAdapterOfTransactionEntity;

  private final SharedSQLiteStatement __preparedStmtOfSoftDelete;

  private final SharedSQLiteStatement __preparedStmtOfRestore;

  private final SharedSQLiteStatement __preparedStmtOfUpdateCategory;

  private final SharedSQLiteStatement __preparedStmtOfUpdateCategoryForMerchant;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAll;

  public TransactionDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfTransactionEntity = new EntityInsertionAdapter<TransactionEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR IGNORE INTO `transaction_record` (`id`,`accountId`,`direction`,`amountPaisa`,`feePaisa`,`categoryId`,`categorySource`,`counterparty`,`counterpartyKey`,`note`,`occurredAt`,`timeUncertain`,`provider`,`referenceId`,`balanceAfterPaisa`,`dedupeHash`,`rawMessageId`,`linkedTxnId`,`parserVersion`,`confidence`,`createdAt`,`updatedAt`,`deletedAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TransactionEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getAccountId());
        statement.bindLong(3, entity.getDirection());
        statement.bindLong(4, entity.getAmountPaisa());
        statement.bindLong(5, entity.getFeePaisa());
        if (entity.getCategoryId() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getCategoryId());
        }
        statement.bindLong(7, entity.getCategorySource());
        statement.bindString(8, entity.getCounterparty());
        statement.bindString(9, entity.getCounterpartyKey());
        if (entity.getNote() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getNote());
        }
        statement.bindLong(11, entity.getOccurredAt());
        final int _tmp = entity.getTimeUncertain() ? 1 : 0;
        statement.bindLong(12, _tmp);
        statement.bindString(13, entity.getProvider());
        if (entity.getReferenceId() == null) {
          statement.bindNull(14);
        } else {
          statement.bindString(14, entity.getReferenceId());
        }
        if (entity.getBalanceAfterPaisa() == null) {
          statement.bindNull(15);
        } else {
          statement.bindLong(15, entity.getBalanceAfterPaisa());
        }
        statement.bindString(16, entity.getDedupeHash());
        if (entity.getRawMessageId() == null) {
          statement.bindNull(17);
        } else {
          statement.bindLong(17, entity.getRawMessageId());
        }
        if (entity.getLinkedTxnId() == null) {
          statement.bindNull(18);
        } else {
          statement.bindLong(18, entity.getLinkedTxnId());
        }
        statement.bindLong(19, entity.getParserVersion());
        statement.bindDouble(20, entity.getConfidence());
        statement.bindLong(21, entity.getCreatedAt());
        statement.bindLong(22, entity.getUpdatedAt());
        if (entity.getDeletedAt() == null) {
          statement.bindNull(23);
        } else {
          statement.bindLong(23, entity.getDeletedAt());
        }
      }
    };
    this.__updateAdapterOfTransactionEntity = new EntityDeletionOrUpdateAdapter<TransactionEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `transaction_record` SET `id` = ?,`accountId` = ?,`direction` = ?,`amountPaisa` = ?,`feePaisa` = ?,`categoryId` = ?,`categorySource` = ?,`counterparty` = ?,`counterpartyKey` = ?,`note` = ?,`occurredAt` = ?,`timeUncertain` = ?,`provider` = ?,`referenceId` = ?,`balanceAfterPaisa` = ?,`dedupeHash` = ?,`rawMessageId` = ?,`linkedTxnId` = ?,`parserVersion` = ?,`confidence` = ?,`createdAt` = ?,`updatedAt` = ?,`deletedAt` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TransactionEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getAccountId());
        statement.bindLong(3, entity.getDirection());
        statement.bindLong(4, entity.getAmountPaisa());
        statement.bindLong(5, entity.getFeePaisa());
        if (entity.getCategoryId() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getCategoryId());
        }
        statement.bindLong(7, entity.getCategorySource());
        statement.bindString(8, entity.getCounterparty());
        statement.bindString(9, entity.getCounterpartyKey());
        if (entity.getNote() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getNote());
        }
        statement.bindLong(11, entity.getOccurredAt());
        final int _tmp = entity.getTimeUncertain() ? 1 : 0;
        statement.bindLong(12, _tmp);
        statement.bindString(13, entity.getProvider());
        if (entity.getReferenceId() == null) {
          statement.bindNull(14);
        } else {
          statement.bindString(14, entity.getReferenceId());
        }
        if (entity.getBalanceAfterPaisa() == null) {
          statement.bindNull(15);
        } else {
          statement.bindLong(15, entity.getBalanceAfterPaisa());
        }
        statement.bindString(16, entity.getDedupeHash());
        if (entity.getRawMessageId() == null) {
          statement.bindNull(17);
        } else {
          statement.bindLong(17, entity.getRawMessageId());
        }
        if (entity.getLinkedTxnId() == null) {
          statement.bindNull(18);
        } else {
          statement.bindLong(18, entity.getLinkedTxnId());
        }
        statement.bindLong(19, entity.getParserVersion());
        statement.bindDouble(20, entity.getConfidence());
        statement.bindLong(21, entity.getCreatedAt());
        statement.bindLong(22, entity.getUpdatedAt());
        if (entity.getDeletedAt() == null) {
          statement.bindNull(23);
        } else {
          statement.bindLong(23, entity.getDeletedAt());
        }
        statement.bindLong(24, entity.getId());
      }
    };
    this.__preparedStmtOfSoftDelete = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE transaction_record SET deletedAt = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfRestore = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE transaction_record SET deletedAt = NULL WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateCategory = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE transaction_record SET categoryId = ?, categorySource = ?, updatedAt = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateCategoryForMerchant = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE transaction_record SET categoryId = ?, categorySource = 2, updatedAt = ? WHERE counterpartyKey = ? AND deletedAt IS NULL";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM transaction_record";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final TransactionEntity transaction,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfTransactionEntity.insertAndReturnId(transaction);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertAll(final List<TransactionEntity> transactions,
      final Continuation<? super List<Long>> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<List<Long>>() {
      @Override
      @NonNull
      public List<Long> call() throws Exception {
        __db.beginTransaction();
        try {
          final List<Long> _result = __insertionAdapterOfTransactionEntity.insertAndReturnIdsList(transactions);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final TransactionEntity transaction,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfTransactionEntity.handle(transaction);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object softDelete(final long id, final long deletedAt,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfSoftDelete.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, deletedAt);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfSoftDelete.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object restore(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfRestore.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfRestore.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateCategory(final long id, final String categoryId, final int source,
      final long updatedAt, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateCategory.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, categoryId);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, source);
        _argIndex = 3;
        _stmt.bindLong(_argIndex, updatedAt);
        _argIndex = 4;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfUpdateCategory.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateCategoryForMerchant(final String key, final String categoryId,
      final long updatedAt, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateCategoryForMerchant.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, categoryId);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, updatedAt);
        _argIndex = 3;
        _stmt.bindString(_argIndex, key);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfUpdateCategoryForMerchant.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteAll(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAll.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteAll.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<TransactionEntity>> getAllTransactions() {
    final String _sql = "SELECT * FROM transaction_record WHERE deletedAt IS NULL ORDER BY occurredAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"transaction_record"}, new Callable<List<TransactionEntity>>() {
      @Override
      @NonNull
      public List<TransactionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfAccountId = CursorUtil.getColumnIndexOrThrow(_cursor, "accountId");
          final int _cursorIndexOfDirection = CursorUtil.getColumnIndexOrThrow(_cursor, "direction");
          final int _cursorIndexOfAmountPaisa = CursorUtil.getColumnIndexOrThrow(_cursor, "amountPaisa");
          final int _cursorIndexOfFeePaisa = CursorUtil.getColumnIndexOrThrow(_cursor, "feePaisa");
          final int _cursorIndexOfCategoryId = CursorUtil.getColumnIndexOrThrow(_cursor, "categoryId");
          final int _cursorIndexOfCategorySource = CursorUtil.getColumnIndexOrThrow(_cursor, "categorySource");
          final int _cursorIndexOfCounterparty = CursorUtil.getColumnIndexOrThrow(_cursor, "counterparty");
          final int _cursorIndexOfCounterpartyKey = CursorUtil.getColumnIndexOrThrow(_cursor, "counterpartyKey");
          final int _cursorIndexOfNote = CursorUtil.getColumnIndexOrThrow(_cursor, "note");
          final int _cursorIndexOfOccurredAt = CursorUtil.getColumnIndexOrThrow(_cursor, "occurredAt");
          final int _cursorIndexOfTimeUncertain = CursorUtil.getColumnIndexOrThrow(_cursor, "timeUncertain");
          final int _cursorIndexOfProvider = CursorUtil.getColumnIndexOrThrow(_cursor, "provider");
          final int _cursorIndexOfReferenceId = CursorUtil.getColumnIndexOrThrow(_cursor, "referenceId");
          final int _cursorIndexOfBalanceAfterPaisa = CursorUtil.getColumnIndexOrThrow(_cursor, "balanceAfterPaisa");
          final int _cursorIndexOfDedupeHash = CursorUtil.getColumnIndexOrThrow(_cursor, "dedupeHash");
          final int _cursorIndexOfRawMessageId = CursorUtil.getColumnIndexOrThrow(_cursor, "rawMessageId");
          final int _cursorIndexOfLinkedTxnId = CursorUtil.getColumnIndexOrThrow(_cursor, "linkedTxnId");
          final int _cursorIndexOfParserVersion = CursorUtil.getColumnIndexOrThrow(_cursor, "parserVersion");
          final int _cursorIndexOfConfidence = CursorUtil.getColumnIndexOrThrow(_cursor, "confidence");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final List<TransactionEntity> _result = new ArrayList<TransactionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TransactionEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpAccountId;
            _tmpAccountId = _cursor.getLong(_cursorIndexOfAccountId);
            final int _tmpDirection;
            _tmpDirection = _cursor.getInt(_cursorIndexOfDirection);
            final long _tmpAmountPaisa;
            _tmpAmountPaisa = _cursor.getLong(_cursorIndexOfAmountPaisa);
            final long _tmpFeePaisa;
            _tmpFeePaisa = _cursor.getLong(_cursorIndexOfFeePaisa);
            final String _tmpCategoryId;
            if (_cursor.isNull(_cursorIndexOfCategoryId)) {
              _tmpCategoryId = null;
            } else {
              _tmpCategoryId = _cursor.getString(_cursorIndexOfCategoryId);
            }
            final int _tmpCategorySource;
            _tmpCategorySource = _cursor.getInt(_cursorIndexOfCategorySource);
            final String _tmpCounterparty;
            _tmpCounterparty = _cursor.getString(_cursorIndexOfCounterparty);
            final String _tmpCounterpartyKey;
            _tmpCounterpartyKey = _cursor.getString(_cursorIndexOfCounterpartyKey);
            final String _tmpNote;
            if (_cursor.isNull(_cursorIndexOfNote)) {
              _tmpNote = null;
            } else {
              _tmpNote = _cursor.getString(_cursorIndexOfNote);
            }
            final long _tmpOccurredAt;
            _tmpOccurredAt = _cursor.getLong(_cursorIndexOfOccurredAt);
            final boolean _tmpTimeUncertain;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfTimeUncertain);
            _tmpTimeUncertain = _tmp != 0;
            final String _tmpProvider;
            _tmpProvider = _cursor.getString(_cursorIndexOfProvider);
            final String _tmpReferenceId;
            if (_cursor.isNull(_cursorIndexOfReferenceId)) {
              _tmpReferenceId = null;
            } else {
              _tmpReferenceId = _cursor.getString(_cursorIndexOfReferenceId);
            }
            final Long _tmpBalanceAfterPaisa;
            if (_cursor.isNull(_cursorIndexOfBalanceAfterPaisa)) {
              _tmpBalanceAfterPaisa = null;
            } else {
              _tmpBalanceAfterPaisa = _cursor.getLong(_cursorIndexOfBalanceAfterPaisa);
            }
            final String _tmpDedupeHash;
            _tmpDedupeHash = _cursor.getString(_cursorIndexOfDedupeHash);
            final Long _tmpRawMessageId;
            if (_cursor.isNull(_cursorIndexOfRawMessageId)) {
              _tmpRawMessageId = null;
            } else {
              _tmpRawMessageId = _cursor.getLong(_cursorIndexOfRawMessageId);
            }
            final Long _tmpLinkedTxnId;
            if (_cursor.isNull(_cursorIndexOfLinkedTxnId)) {
              _tmpLinkedTxnId = null;
            } else {
              _tmpLinkedTxnId = _cursor.getLong(_cursorIndexOfLinkedTxnId);
            }
            final int _tmpParserVersion;
            _tmpParserVersion = _cursor.getInt(_cursorIndexOfParserVersion);
            final float _tmpConfidence;
            _tmpConfidence = _cursor.getFloat(_cursorIndexOfConfidence);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final Long _tmpDeletedAt;
            if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
              _tmpDeletedAt = null;
            } else {
              _tmpDeletedAt = _cursor.getLong(_cursorIndexOfDeletedAt);
            }
            _item = new TransactionEntity(_tmpId,_tmpAccountId,_tmpDirection,_tmpAmountPaisa,_tmpFeePaisa,_tmpCategoryId,_tmpCategorySource,_tmpCounterparty,_tmpCounterpartyKey,_tmpNote,_tmpOccurredAt,_tmpTimeUncertain,_tmpProvider,_tmpReferenceId,_tmpBalanceAfterPaisa,_tmpDedupeHash,_tmpRawMessageId,_tmpLinkedTxnId,_tmpParserVersion,_tmpConfidence,_tmpCreatedAt,_tmpUpdatedAt,_tmpDeletedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getById(final long id, final Continuation<? super TransactionEntity> $completion) {
    final String _sql = "SELECT * FROM transaction_record WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<TransactionEntity>() {
      @Override
      @Nullable
      public TransactionEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfAccountId = CursorUtil.getColumnIndexOrThrow(_cursor, "accountId");
          final int _cursorIndexOfDirection = CursorUtil.getColumnIndexOrThrow(_cursor, "direction");
          final int _cursorIndexOfAmountPaisa = CursorUtil.getColumnIndexOrThrow(_cursor, "amountPaisa");
          final int _cursorIndexOfFeePaisa = CursorUtil.getColumnIndexOrThrow(_cursor, "feePaisa");
          final int _cursorIndexOfCategoryId = CursorUtil.getColumnIndexOrThrow(_cursor, "categoryId");
          final int _cursorIndexOfCategorySource = CursorUtil.getColumnIndexOrThrow(_cursor, "categorySource");
          final int _cursorIndexOfCounterparty = CursorUtil.getColumnIndexOrThrow(_cursor, "counterparty");
          final int _cursorIndexOfCounterpartyKey = CursorUtil.getColumnIndexOrThrow(_cursor, "counterpartyKey");
          final int _cursorIndexOfNote = CursorUtil.getColumnIndexOrThrow(_cursor, "note");
          final int _cursorIndexOfOccurredAt = CursorUtil.getColumnIndexOrThrow(_cursor, "occurredAt");
          final int _cursorIndexOfTimeUncertain = CursorUtil.getColumnIndexOrThrow(_cursor, "timeUncertain");
          final int _cursorIndexOfProvider = CursorUtil.getColumnIndexOrThrow(_cursor, "provider");
          final int _cursorIndexOfReferenceId = CursorUtil.getColumnIndexOrThrow(_cursor, "referenceId");
          final int _cursorIndexOfBalanceAfterPaisa = CursorUtil.getColumnIndexOrThrow(_cursor, "balanceAfterPaisa");
          final int _cursorIndexOfDedupeHash = CursorUtil.getColumnIndexOrThrow(_cursor, "dedupeHash");
          final int _cursorIndexOfRawMessageId = CursorUtil.getColumnIndexOrThrow(_cursor, "rawMessageId");
          final int _cursorIndexOfLinkedTxnId = CursorUtil.getColumnIndexOrThrow(_cursor, "linkedTxnId");
          final int _cursorIndexOfParserVersion = CursorUtil.getColumnIndexOrThrow(_cursor, "parserVersion");
          final int _cursorIndexOfConfidence = CursorUtil.getColumnIndexOrThrow(_cursor, "confidence");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final TransactionEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpAccountId;
            _tmpAccountId = _cursor.getLong(_cursorIndexOfAccountId);
            final int _tmpDirection;
            _tmpDirection = _cursor.getInt(_cursorIndexOfDirection);
            final long _tmpAmountPaisa;
            _tmpAmountPaisa = _cursor.getLong(_cursorIndexOfAmountPaisa);
            final long _tmpFeePaisa;
            _tmpFeePaisa = _cursor.getLong(_cursorIndexOfFeePaisa);
            final String _tmpCategoryId;
            if (_cursor.isNull(_cursorIndexOfCategoryId)) {
              _tmpCategoryId = null;
            } else {
              _tmpCategoryId = _cursor.getString(_cursorIndexOfCategoryId);
            }
            final int _tmpCategorySource;
            _tmpCategorySource = _cursor.getInt(_cursorIndexOfCategorySource);
            final String _tmpCounterparty;
            _tmpCounterparty = _cursor.getString(_cursorIndexOfCounterparty);
            final String _tmpCounterpartyKey;
            _tmpCounterpartyKey = _cursor.getString(_cursorIndexOfCounterpartyKey);
            final String _tmpNote;
            if (_cursor.isNull(_cursorIndexOfNote)) {
              _tmpNote = null;
            } else {
              _tmpNote = _cursor.getString(_cursorIndexOfNote);
            }
            final long _tmpOccurredAt;
            _tmpOccurredAt = _cursor.getLong(_cursorIndexOfOccurredAt);
            final boolean _tmpTimeUncertain;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfTimeUncertain);
            _tmpTimeUncertain = _tmp != 0;
            final String _tmpProvider;
            _tmpProvider = _cursor.getString(_cursorIndexOfProvider);
            final String _tmpReferenceId;
            if (_cursor.isNull(_cursorIndexOfReferenceId)) {
              _tmpReferenceId = null;
            } else {
              _tmpReferenceId = _cursor.getString(_cursorIndexOfReferenceId);
            }
            final Long _tmpBalanceAfterPaisa;
            if (_cursor.isNull(_cursorIndexOfBalanceAfterPaisa)) {
              _tmpBalanceAfterPaisa = null;
            } else {
              _tmpBalanceAfterPaisa = _cursor.getLong(_cursorIndexOfBalanceAfterPaisa);
            }
            final String _tmpDedupeHash;
            _tmpDedupeHash = _cursor.getString(_cursorIndexOfDedupeHash);
            final Long _tmpRawMessageId;
            if (_cursor.isNull(_cursorIndexOfRawMessageId)) {
              _tmpRawMessageId = null;
            } else {
              _tmpRawMessageId = _cursor.getLong(_cursorIndexOfRawMessageId);
            }
            final Long _tmpLinkedTxnId;
            if (_cursor.isNull(_cursorIndexOfLinkedTxnId)) {
              _tmpLinkedTxnId = null;
            } else {
              _tmpLinkedTxnId = _cursor.getLong(_cursorIndexOfLinkedTxnId);
            }
            final int _tmpParserVersion;
            _tmpParserVersion = _cursor.getInt(_cursorIndexOfParserVersion);
            final float _tmpConfidence;
            _tmpConfidence = _cursor.getFloat(_cursorIndexOfConfidence);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final Long _tmpDeletedAt;
            if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
              _tmpDeletedAt = null;
            } else {
              _tmpDeletedAt = _cursor.getLong(_cursorIndexOfDeletedAt);
            }
            _result = new TransactionEntity(_tmpId,_tmpAccountId,_tmpDirection,_tmpAmountPaisa,_tmpFeePaisa,_tmpCategoryId,_tmpCategorySource,_tmpCounterparty,_tmpCounterpartyKey,_tmpNote,_tmpOccurredAt,_tmpTimeUncertain,_tmpProvider,_tmpReferenceId,_tmpBalanceAfterPaisa,_tmpDedupeHash,_tmpRawMessageId,_tmpLinkedTxnId,_tmpParserVersion,_tmpConfidence,_tmpCreatedAt,_tmpUpdatedAt,_tmpDeletedAt);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<TransactionEntity>> getTransactionsBetween(final long start, final long end) {
    final String _sql = "SELECT * FROM transaction_record WHERE occurredAt BETWEEN ? AND ? AND deletedAt IS NULL ORDER BY occurredAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, start);
    _argIndex = 2;
    _statement.bindLong(_argIndex, end);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"transaction_record"}, new Callable<List<TransactionEntity>>() {
      @Override
      @NonNull
      public List<TransactionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfAccountId = CursorUtil.getColumnIndexOrThrow(_cursor, "accountId");
          final int _cursorIndexOfDirection = CursorUtil.getColumnIndexOrThrow(_cursor, "direction");
          final int _cursorIndexOfAmountPaisa = CursorUtil.getColumnIndexOrThrow(_cursor, "amountPaisa");
          final int _cursorIndexOfFeePaisa = CursorUtil.getColumnIndexOrThrow(_cursor, "feePaisa");
          final int _cursorIndexOfCategoryId = CursorUtil.getColumnIndexOrThrow(_cursor, "categoryId");
          final int _cursorIndexOfCategorySource = CursorUtil.getColumnIndexOrThrow(_cursor, "categorySource");
          final int _cursorIndexOfCounterparty = CursorUtil.getColumnIndexOrThrow(_cursor, "counterparty");
          final int _cursorIndexOfCounterpartyKey = CursorUtil.getColumnIndexOrThrow(_cursor, "counterpartyKey");
          final int _cursorIndexOfNote = CursorUtil.getColumnIndexOrThrow(_cursor, "note");
          final int _cursorIndexOfOccurredAt = CursorUtil.getColumnIndexOrThrow(_cursor, "occurredAt");
          final int _cursorIndexOfTimeUncertain = CursorUtil.getColumnIndexOrThrow(_cursor, "timeUncertain");
          final int _cursorIndexOfProvider = CursorUtil.getColumnIndexOrThrow(_cursor, "provider");
          final int _cursorIndexOfReferenceId = CursorUtil.getColumnIndexOrThrow(_cursor, "referenceId");
          final int _cursorIndexOfBalanceAfterPaisa = CursorUtil.getColumnIndexOrThrow(_cursor, "balanceAfterPaisa");
          final int _cursorIndexOfDedupeHash = CursorUtil.getColumnIndexOrThrow(_cursor, "dedupeHash");
          final int _cursorIndexOfRawMessageId = CursorUtil.getColumnIndexOrThrow(_cursor, "rawMessageId");
          final int _cursorIndexOfLinkedTxnId = CursorUtil.getColumnIndexOrThrow(_cursor, "linkedTxnId");
          final int _cursorIndexOfParserVersion = CursorUtil.getColumnIndexOrThrow(_cursor, "parserVersion");
          final int _cursorIndexOfConfidence = CursorUtil.getColumnIndexOrThrow(_cursor, "confidence");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final List<TransactionEntity> _result = new ArrayList<TransactionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TransactionEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpAccountId;
            _tmpAccountId = _cursor.getLong(_cursorIndexOfAccountId);
            final int _tmpDirection;
            _tmpDirection = _cursor.getInt(_cursorIndexOfDirection);
            final long _tmpAmountPaisa;
            _tmpAmountPaisa = _cursor.getLong(_cursorIndexOfAmountPaisa);
            final long _tmpFeePaisa;
            _tmpFeePaisa = _cursor.getLong(_cursorIndexOfFeePaisa);
            final String _tmpCategoryId;
            if (_cursor.isNull(_cursorIndexOfCategoryId)) {
              _tmpCategoryId = null;
            } else {
              _tmpCategoryId = _cursor.getString(_cursorIndexOfCategoryId);
            }
            final int _tmpCategorySource;
            _tmpCategorySource = _cursor.getInt(_cursorIndexOfCategorySource);
            final String _tmpCounterparty;
            _tmpCounterparty = _cursor.getString(_cursorIndexOfCounterparty);
            final String _tmpCounterpartyKey;
            _tmpCounterpartyKey = _cursor.getString(_cursorIndexOfCounterpartyKey);
            final String _tmpNote;
            if (_cursor.isNull(_cursorIndexOfNote)) {
              _tmpNote = null;
            } else {
              _tmpNote = _cursor.getString(_cursorIndexOfNote);
            }
            final long _tmpOccurredAt;
            _tmpOccurredAt = _cursor.getLong(_cursorIndexOfOccurredAt);
            final boolean _tmpTimeUncertain;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfTimeUncertain);
            _tmpTimeUncertain = _tmp != 0;
            final String _tmpProvider;
            _tmpProvider = _cursor.getString(_cursorIndexOfProvider);
            final String _tmpReferenceId;
            if (_cursor.isNull(_cursorIndexOfReferenceId)) {
              _tmpReferenceId = null;
            } else {
              _tmpReferenceId = _cursor.getString(_cursorIndexOfReferenceId);
            }
            final Long _tmpBalanceAfterPaisa;
            if (_cursor.isNull(_cursorIndexOfBalanceAfterPaisa)) {
              _tmpBalanceAfterPaisa = null;
            } else {
              _tmpBalanceAfterPaisa = _cursor.getLong(_cursorIndexOfBalanceAfterPaisa);
            }
            final String _tmpDedupeHash;
            _tmpDedupeHash = _cursor.getString(_cursorIndexOfDedupeHash);
            final Long _tmpRawMessageId;
            if (_cursor.isNull(_cursorIndexOfRawMessageId)) {
              _tmpRawMessageId = null;
            } else {
              _tmpRawMessageId = _cursor.getLong(_cursorIndexOfRawMessageId);
            }
            final Long _tmpLinkedTxnId;
            if (_cursor.isNull(_cursorIndexOfLinkedTxnId)) {
              _tmpLinkedTxnId = null;
            } else {
              _tmpLinkedTxnId = _cursor.getLong(_cursorIndexOfLinkedTxnId);
            }
            final int _tmpParserVersion;
            _tmpParserVersion = _cursor.getInt(_cursorIndexOfParserVersion);
            final float _tmpConfidence;
            _tmpConfidence = _cursor.getFloat(_cursorIndexOfConfidence);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final Long _tmpDeletedAt;
            if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
              _tmpDeletedAt = null;
            } else {
              _tmpDeletedAt = _cursor.getLong(_cursorIndexOfDeletedAt);
            }
            _item = new TransactionEntity(_tmpId,_tmpAccountId,_tmpDirection,_tmpAmountPaisa,_tmpFeePaisa,_tmpCategoryId,_tmpCategorySource,_tmpCounterparty,_tmpCounterpartyKey,_tmpNote,_tmpOccurredAt,_tmpTimeUncertain,_tmpProvider,_tmpReferenceId,_tmpBalanceAfterPaisa,_tmpDedupeHash,_tmpRawMessageId,_tmpLinkedTxnId,_tmpParserVersion,_tmpConfidence,_tmpCreatedAt,_tmpUpdatedAt,_tmpDeletedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<CategorySpending>> getSpendingByCategory(final long start, final long end) {
    final String _sql = "SELECT categoryId, SUM(amountPaisa + feePaisa) AS totalPaisa, COUNT(*) AS count FROM transaction_record WHERE occurredAt BETWEEN ? AND ? AND deletedAt IS NULL AND direction = 0 GROUP BY categoryId";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, start);
    _argIndex = 2;
    _statement.bindLong(_argIndex, end);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"transaction_record"}, new Callable<List<CategorySpending>>() {
      @Override
      @NonNull
      public List<CategorySpending> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfCategoryId = 0;
          final int _cursorIndexOfTotalPaisa = 1;
          final int _cursorIndexOfCount = 2;
          final List<CategorySpending> _result = new ArrayList<CategorySpending>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CategorySpending _item;
            final String _tmpCategoryId;
            if (_cursor.isNull(_cursorIndexOfCategoryId)) {
              _tmpCategoryId = null;
            } else {
              _tmpCategoryId = _cursor.getString(_cursorIndexOfCategoryId);
            }
            final long _tmpTotalPaisa;
            _tmpTotalPaisa = _cursor.getLong(_cursorIndexOfTotalPaisa);
            final int _tmpCount;
            _tmpCount = _cursor.getInt(_cursorIndexOfCount);
            _item = new CategorySpending(_tmpCategoryId,_tmpTotalPaisa,_tmpCount);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<Long> getTotalExpense(final long start, final long end) {
    final String _sql = "SELECT SUM(amountPaisa + feePaisa) FROM transaction_record WHERE occurredAt BETWEEN ? AND ? AND deletedAt IS NULL AND direction = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, start);
    _argIndex = 2;
    _statement.bindLong(_argIndex, end);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"transaction_record"}, new Callable<Long>() {
      @Override
      @Nullable
      public Long call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Long _result;
          if (_cursor.moveToFirst()) {
            final Long _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getLong(0);
            }
            _result = _tmp;
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<Long> getTotalIncome(final long start, final long end) {
    final String _sql = "SELECT SUM(amountPaisa) FROM transaction_record WHERE occurredAt BETWEEN ? AND ? AND deletedAt IS NULL AND direction = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, start);
    _argIndex = 2;
    _statement.bindLong(_argIndex, end);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"transaction_record"}, new Callable<Long>() {
      @Override
      @Nullable
      public Long call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Long _result;
          if (_cursor.moveToFirst()) {
            final Long _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getLong(0);
            }
            _result = _tmp;
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object hasDedupeHash(final String hash, final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM transaction_record WHERE dedupeHash = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, hash);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
