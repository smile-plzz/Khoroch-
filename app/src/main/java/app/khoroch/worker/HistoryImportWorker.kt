package app.khoroch.worker

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.Telephony
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import app.khoroch.core.model.ParseResult
import app.khoroch.core.parser.SmsParsingEngine
import app.khoroch.data.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HistoryImportWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    private val repository = TransactionRepository(appContext)
    private val parsingEngine = SmsParsingEngine()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val context = applicationContext
        val ninetyDaysAgo = System.currentTimeMillis() - (90L * 24 * 60 * 60 * 1000)

        val uri: Uri = Telephony.Sms.Inbox.CONTENT_URI
        val projection = arrayOf(
            Telephony.Sms.Inbox._ID,
            Telephony.Sms.Inbox.ADDRESS,
            Telephony.Sms.Inbox.BODY,
            Telephony.Sms.Inbox.DATE
        )
        val selection = "${Telephony.Sms.Inbox.DATE} >= ?"
        val selectionArgs = arrayOf(ninetyDaysAgo.toString())
        val sortOrder = "${Telephony.Sms.Inbox.DATE} ASC"

        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)
            if (cursor == null) return@withContext Result.failure()

            val idCol = cursor.getColumnIndexOrThrow(Telephony.Sms.Inbox._ID)
            val addrCol = cursor.getColumnIndexOrThrow(Telephony.Sms.Inbox.ADDRESS)
            val bodyCol = cursor.getColumnIndexOrThrow(Telephony.Sms.Inbox.BODY)
            val dateCol = cursor.getColumnIndexOrThrow(Telephony.Sms.Inbox.DATE)

            while (cursor.moveToNext()) {
                val smsId = cursor.getLong(idCol)
                val sender = cursor.getString(addrCol) ?: continue
                val body = cursor.getString(bodyCol) ?: continue
                val timestamp = cursor.getLong(dateCol)

                when (val result = parsingEngine.parse(sender, body, timestamp)) {
                    is ParseResult.Success -> {
                        repository.saveParsedTransaction(
                            parsed = result.txn,
                            rawSender = sender,
                            rawBody = body,
                            smsId = smsId
                        )
                    }
                    is ParseResult.Unparsed -> {
                        repository.saveUnparsedMessage(sender, body, timestamp)
                    }
                    is ParseResult.Ignored -> {
                        // Skip
                    }
                }
            }
            Result.success()
        } catch (_: SecurityException) {
            Result.failure()
        } catch (_: Exception) {
            Result.retry()
        } finally {
            cursor?.close()
        }
    }
}
