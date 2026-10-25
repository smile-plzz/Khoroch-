package app.khoroch.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import app.khoroch.core.model.ParseResult
import app.khoroch.core.parser.SmsParsingEngine
import app.khoroch.data.repository.TransactionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {

    private val parsingEngine = SmsParsingEngine()

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        val repository = TransactionRepository(context.applicationContext)
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Multi-part SMS handling: group by originating address
                val sender = messages.first().displayOriginatingAddress ?: return@launch
                val fullBody = messages.joinToString("") { it.displayMessageBody ?: "" }
                val timestamp = messages.first().timestampMillis

                when (val result = parsingEngine.parse(sender, fullBody, timestamp)) {
                    is ParseResult.Success -> {
                        repository.saveParsedTransaction(
                            parsed = result.txn,
                            rawSender = sender,
                            rawBody = fullBody
                        )
                    }
                    is ParseResult.Unparsed -> {
                        repository.saveUnparsedMessage(
                            sender = sender,
                            body = fullBody,
                            receivedAt = timestamp
                        )
                    }
                    is ParseResult.Ignored -> {
                        // Discard completely: OTP, PIN, promo, or un-allowlisted
                    }
                }
            } catch (_: Exception) {
                // Suppress exceptions in receiver to prevent ANR / system crash
            } finally {
                pendingResult.finish()
            }
        }
    }
}
