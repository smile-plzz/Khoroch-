package app.khoroch.core.parser

import app.khoroch.core.common.MoneyFormatter
import app.khoroch.core.model.Direction
import app.khoroch.core.model.ParseResult
import app.khoroch.core.model.ParsedTxn
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * Normalizes text for consistent parsing.
 */
object TextNormalizer {
    private val WHITESPACE_REGEX = "\\s+".toRegex()
    private val CURRENCY_REGEX = "(?i)(?:Tk\\.|BDT|৳|Tk)".toRegex()

    fun normalize(text: String): String {
        var result = text.trim()
        result = MoneyFormatter.toAsciiNumerals(result)
        result = WHITESPACE_REGEX.replace(result, " ")
        result = CURRENCY_REGEX.replace(result, "Tk")
        return result
    }
}

/**
 * Base interface for provider-specific parsers.
 */
interface ProviderParser {
    val providerId: String
    fun canHandle(sender: String): Boolean
    fun parse(sender: String, normalizedBody: String, receivedAt: Long): ParsedTxn?
}

/**
 * Parser for bKash SMS transactions.
 */
class BkashParser : ProviderParser {
    override val providerId: String = "bkash"

    override fun canHandle(sender: String): Boolean {
        val s = sender.trim().lowercase(Locale.ROOT)
        return s == "bkash"
    }

    override fun parse(sender: String, normalizedBody: String, receivedAt: Long): ParsedTxn? {
        val body = normalizedBody

        // 1. Send Money: You have sent Tk 500.00 to 017XXXXXXXX. Fee Tk 0.00. Balance Tk 1,250.50. TrxID ABC123XYZ at 25/10/2026 13:45
        val sendMoneyRegex = "(?i)You have sent Tk\\s?([\\d,]+(?:\\.\\d{1,2})?)\\s+to\\s+([^.]+?)\\.\\s*(?:Fee\\s+Tk\\s?([\\d,]+(?:\\.\\d{1,2})?)\\.)?\\s*(?:Balance\\s+Tk\\s?([\\d,]+(?:\\.\\d{1,2})?)\\.)?\\s*(?:TrxID|TxnID)[:\\s]*([A-Z0-9]+)".toRegex()
        sendMoneyRegex.find(body)?.let { match ->
            val amount = MoneyFormatter.parseTakaToPaisa(match.groupValues[1])
            val counterparty = match.groupValues[2].trim()
            val fee = MoneyFormatter.parseTakaToPaisa(match.groupValues.getOrNull(3) ?: "0")
            val balance = match.groupValues.getOrNull(4)?.takeIf { it.isNotBlank() }?.let { MoneyFormatter.parseTakaToPaisa(it) }
            val trxId = match.groupValues.getOrNull(5)?.trim()
            val timestamp = extractTimestamp(body) ?: receivedAt
            return ParsedTxn(
                amountPaisa = amount,
                feePaisa = fee,
                direction = Direction.EXPENSE,
                provider = providerId,
                counterparty = counterparty,
                referenceId = trxId,
                balanceAfterPaisa = balance,
                timestamp = timestamp,
                confidence = 1.0f
            )
        }

        // 2. Payment: Payment Tk 450.00 to Rahim Store successful...
        val paymentRegex = "(?i)Payment\\s+Tk\\s?([\\d,]+(?:\\.\\d{1,2})?)\\s+to\\s+([^.]+?)\\s+(?:successful|successful\\.)(?:.*?Fee\\s+Tk\\s?([\\d,]+(?:\\.\\d{1,2})?)\\.)?(?:.*?Balance\\s+Tk\\s?([\\d,]+(?:\\.\\d{1,2})?)\\.)?(?:.*?(?:TrxID|TxnID)[:\\s]*([A-Z0-9]+))?".toRegex()
        paymentRegex.find(body)?.let { match ->
            val amount = MoneyFormatter.parseTakaToPaisa(match.groupValues[1])
            val counterparty = match.groupValues[2].trim()
            val fee = MoneyFormatter.parseTakaToPaisa(match.groupValues.getOrNull(3) ?: "0")
            val balance = match.groupValues.getOrNull(4)?.takeIf { it.isNotBlank() }?.let { MoneyFormatter.parseTakaToPaisa(it) }
            val trxId = match.groupValues.getOrNull(5)?.trim()
            return ParsedTxn(
                amountPaisa = amount,
                feePaisa = fee,
                direction = Direction.EXPENSE,
                provider = providerId,
                counterparty = counterparty,
                referenceId = trxId,
                balanceAfterPaisa = balance,
                timestamp = extractTimestamp(body) ?: receivedAt,
                confidence = 1.0f
            )
        }

        // 3. Cash Out: Cash Out Tk 1,000.00 to Agent 018XXXXXXXX successful...
        val cashOutRegex = "(?i)Cash Out\\s+Tk\\s?([\\d,]+(?:\\.\\d{1,2})?)\\s+to\\s+([^.]+?)\\s+(?:successful|successful\\.)(?:.*?Fee\\s+Tk\\s?([\\d,]+(?:\\.\\d{1,2})?)\\.)?(?:.*?Balance\\s+Tk\\s?([\\d,]+(?:\\.\\d{1,2})?)\\.)?(?:.*?(?:TrxID|TxnID)[:\\s]*([A-Z0-9]+))?".toRegex()
        cashOutRegex.find(body)?.let { match ->
            val amount = MoneyFormatter.parseTakaToPaisa(match.groupValues[1])
            val counterparty = match.groupValues[2].trim()
            val fee = MoneyFormatter.parseTakaToPaisa(match.groupValues.getOrNull(3) ?: "0")
            val balance = match.groupValues.getOrNull(4)?.takeIf { it.isNotBlank() }?.let { MoneyFormatter.parseTakaToPaisa(it) }
            val trxId = match.groupValues.getOrNull(5)?.trim()
            return ParsedTxn(
                amountPaisa = amount,
                feePaisa = fee,
                direction = Direction.TRANSFER,
                provider = providerId,
                counterparty = counterparty,
                referenceId = trxId,
                balanceAfterPaisa = balance,
                timestamp = extractTimestamp(body) ?: receivedAt,
                confidence = 1.0f
            )
        }

        // 4. Mobile Recharge: Mobile recharge Tk 50.00 to 019XXXXXXXX successful...
        val rechargeRegex = "(?i)(?:Mobile\\s+)?recharge\\s+Tk\\s?([\\d,]+(?:\\.\\d{1,2})?)\\s+to\\s+([^.]+?)\\s+(?:successful|successful\\.)(?:.*?Balance\\s+Tk\\s?([\\d,]+(?:\\.\\d{1,2})?)\\.)?(?:.*?(?:TrxID|TxnID)[:\\s]*([A-Z0-9]+))?".toRegex()
        rechargeRegex.find(body)?.let { match ->
            val amount = MoneyFormatter.parseTakaToPaisa(match.groupValues[1])
            val counterparty = match.groupValues[2].trim()
            val balance = match.groupValues.getOrNull(3)?.takeIf { it.isNotBlank() }?.let { MoneyFormatter.parseTakaToPaisa(it) }
            val trxId = match.groupValues.getOrNull(4)?.trim()
            return ParsedTxn(
                amountPaisa = amount,
                feePaisa = 0L,
                direction = Direction.EXPENSE,
                provider = providerId,
                counterparty = "Mobile Recharge: $counterparty",
                referenceId = trxId,
                balanceAfterPaisa = balance,
                timestamp = extractTimestamp(body) ?: receivedAt,
                confidence = 1.0f
            )
        }

        // 5. Received money / Cash in: You have received Tk 2,000.00 from 017XXXXXXXX...
        val receivedRegex = "(?i)(?:You have received|Cash In)\\s+Tk\\s?([\\d,]+(?:\\.\\d{1,2})?)\\s+from\\s+([^.]+?)\\.(?:.*?Balance\\s+Tk\\s?([\\d,]+(?:\\.\\d{1,2})?)\\.)?(?:.*?(?:TrxID|TxnID)[:\\s]*([A-Z0-9]+))?".toRegex()
        receivedRegex.find(body)?.let { match ->
            val amount = MoneyFormatter.parseTakaToPaisa(match.groupValues[1])
            val counterparty = match.groupValues[2].trim()
            val balance = match.groupValues.getOrNull(3)?.takeIf { it.isNotBlank() }?.let { MoneyFormatter.parseTakaToPaisa(it) }
            val trxId = match.groupValues.getOrNull(4)?.trim()
            return ParsedTxn(
                amountPaisa = amount,
                feePaisa = 0L,
                direction = Direction.INCOME,
                provider = providerId,
                counterparty = counterparty,
                referenceId = trxId,
                balanceAfterPaisa = balance,
                timestamp = extractTimestamp(body) ?: receivedAt,
                confidence = 1.0f
            )
        }

        // 6. Bangla patterns: আপনি 017XXXXXXXX নম্বরে Tk 500 পাঠিয়েছেন / পেয়েছেন
        val banglaSentRegex = "আপনি\\s+([^\\s]+)\\s+(?:নম্বরে)?\\s*Tk\\s?([\\d,]+(?:\\.\\d{1,2})?)\\s+পাঠিয়েছেন(?:.*?TrxID[:\\s]*([A-Z0-9]+))?".toRegex()
        banglaSentRegex.find(body)?.let { match ->
            return ParsedTxn(
                amountPaisa = MoneyFormatter.parseTakaToPaisa(match.groupValues[2]),
                feePaisa = 0L,
                direction = Direction.EXPENSE,
                provider = providerId,
                counterparty = match.groupValues[1],
                referenceId = match.groupValues.getOrNull(3)?.trim(),
                timestamp = extractTimestamp(body) ?: receivedAt,
                confidence = 0.95f
            )
        }

        return null
    }
}

/**
 * Parser for Nagad SMS transactions.
 */
class NagadParser : ProviderParser {
    override val providerId: String = "nagad"

    override fun canHandle(sender: String): Boolean {
        val s = sender.trim().lowercase(Locale.ROOT)
        return s == "nagad" || s == "16167"
    }

    override fun parse(sender: String, normalizedBody: String, receivedAt: Long): ParsedTxn? {
        val body = normalizedBody

        // 1. Send Money: Tk 500.00 sent to 017XXXXXXXX. Fee: Tk 0.00. Balance: Tk 1,200.00. TxnID: 71ABCDEF at 25/10/2026
        val sendRegex = "(?i)Tk\\s?([\\d,]+(?:\\.\\d{1,2})?)\\s+sent to\\s+([^.]+?)\\.(?:.*?Fee[:\\s]*Tk\\s?([\\d,]+(?:\\.\\d{1,2})?)\\.)?(?:.*?Balance[:\\s]*Tk\\s?([\\d,]+(?:\\.\\d{1,2})?)\\.)?(?:.*?TxnID[:\\s]*([A-Z0-9]+))?".toRegex()
        sendRegex.find(body)?.let { match ->
            return ParsedTxn(
                amountPaisa = MoneyFormatter.parseTakaToPaisa(match.groupValues[1]),
                feePaisa = MoneyFormatter.parseTakaToPaisa(match.groupValues.getOrNull(3) ?: "0"),
                direction = Direction.EXPENSE,
                provider = providerId,
                counterparty = match.groupValues[2].trim(),
                referenceId = match.groupValues.getOrNull(5)?.trim(),
                balanceAfterPaisa = match.groupValues.getOrNull(4)?.takeIf { it.isNotBlank() }?.let { MoneyFormatter.parseTakaToPaisa(it) },
                timestamp = extractTimestamp(body) ?: receivedAt,
                confidence = 1.0f
            )
        }

        // 2. Merchant Payment: Payment of Tk 350.00 to Shwapno is successful...
        val payRegex = "(?i)Payment of Tk\\s?([\\d,]+(?:\\.\\d{1,2})?)\\s+to\\s+([^.]+?)\\s+is successful(?:.*?Balance[:\\s]*Tk\\s?([\\d,]+(?:\\.\\d{1,2})?)\\.)?(?:.*?TxnID[:\\s]*([A-Z0-9]+))?".toRegex()
        payRegex.find(body)?.let { match ->
            return ParsedTxn(
                amountPaisa = MoneyFormatter.parseTakaToPaisa(match.groupValues[1]),
                direction = Direction.EXPENSE,
                provider = providerId,
                counterparty = match.groupValues[2].trim(),
                referenceId = match.groupValues.getOrNull(4)?.trim(),
                balanceAfterPaisa = match.groupValues.getOrNull(3)?.takeIf { it.isNotBlank() }?.let { MoneyFormatter.parseTakaToPaisa(it) },
                timestamp = extractTimestamp(body) ?: receivedAt,
                confidence = 1.0f
            )
        }

        // 3. Cash Out: Cash Out Tk 1,000.00 from Agent 019XXXXXXXX...
        val cashOutRegex = "(?i)Cash Out\\s+(?:of\\s+)?Tk\\s?([\\d,]+(?:\\.\\d{1,2})?)\\s+(?:to|from)\\s+([^.]+?)\\s+(?:is\\s+)?successful(?:.*?Fee[:\\s]*Tk\\s?([\\d,]+(?:\\.\\d{1,2})?)\\.)?(?:.*?Balance[:\\s]*Tk\\s?([\\d,]+(?:\\.\\d{1,2})?)\\.)?(?:.*?TxnID[:\\s]*([A-Z0-9]+))?".toRegex()
        cashOutRegex.find(body)?.let { match ->
            return ParsedTxn(
                amountPaisa = MoneyFormatter.parseTakaToPaisa(match.groupValues[1]),
                feePaisa = MoneyFormatter.parseTakaToPaisa(match.groupValues.getOrNull(3) ?: "0"),
                direction = Direction.TRANSFER,
                provider = providerId,
                counterparty = match.groupValues[2].trim(),
                referenceId = match.groupValues.getOrNull(5)?.trim(),
                balanceAfterPaisa = match.groupValues.getOrNull(4)?.takeIf { it.isNotBlank() }?.let { MoneyFormatter.parseTakaToPaisa(it) },
                timestamp = extractTimestamp(body) ?: receivedAt,
                confidence = 1.0f
            )
        }

        // 4. Money Received / Cash In: Tk 1,500.00 received from 018XXXXXXXX...
        val receivedRegex = "(?i)Tk\\s?([\\d,]+(?:\\.\\d{1,2})?)\\s+received from\\s+([^.]+?)\\.(?:.*?Balance[:\\s]*Tk\\s?([\\d,]+(?:\\.\\d{1,2})?)\\.)?(?:.*?TxnID[:\\s]*([A-Z0-9]+))?".toRegex()
        receivedRegex.find(body)?.let { match ->
            return ParsedTxn(
                amountPaisa = MoneyFormatter.parseTakaToPaisa(match.groupValues[1]),
                direction = Direction.INCOME,
                provider = providerId,
                counterparty = match.groupValues[2].trim(),
                referenceId = match.groupValues.getOrNull(4)?.trim(),
                balanceAfterPaisa = match.groupValues.getOrNull(3)?.takeIf { it.isNotBlank() }?.let { MoneyFormatter.parseTakaToPaisa(it) },
                timestamp = extractTimestamp(body) ?: receivedAt,
                confidence = 1.0f
            )
        }

        return null
    }
}

/**
 * Parser for Rocket (DBBL) SMS transactions.
 */
class RocketParser : ProviderParser {
    override val providerId: String = "rocket"

    override fun canHandle(sender: String): Boolean {
        val s = sender.trim().lowercase(Locale.ROOT)
        return s == "rocket" || s == "16216"
    }

    override fun parse(sender: String, normalizedBody: String, receivedAt: Long): ParsedTxn? {
        val body = normalizedBody

        // 1. Send Money: You have sent Tk 500.00 to A/C: 017XXXXXXXXX. Fee Tk 4.50. Balance Tk 850. TxnId: 12345678
        val sendRegex = "(?i)You have sent Tk\\s?([\\d,]+(?:\\.\\d{1,2})?)\\s+to\\s+([^.]+?)\\.(?:.*?Fee\\s+Tk\\s?([\\d,]+(?:\\.\\d{1,2})?)\\.)?(?:.*?Balance\\s+Tk\\s?([\\d,]+(?:\\.\\d{1,2})?)\\.)?(?:.*?TxnId[:\\s]*([A-Z0-9]+))?".toRegex()
        sendRegex.find(body)?.let { match ->
            return ParsedTxn(
                amountPaisa = MoneyFormatter.parseTakaToPaisa(match.groupValues[1]),
                feePaisa = MoneyFormatter.parseTakaToPaisa(match.groupValues.getOrNull(3) ?: "0"),
                direction = Direction.EXPENSE,
                provider = providerId,
                counterparty = match.groupValues[2].trim(),
                referenceId = match.groupValues.getOrNull(5)?.trim(),
                balanceAfterPaisa = match.groupValues.getOrNull(4)?.takeIf { it.isNotBlank() }?.let { MoneyFormatter.parseTakaToPaisa(it) },
                timestamp = extractTimestamp(body) ?: receivedAt,
                confidence = 1.0f
            )
        }

        // 2. Bill Pay: Bill Pay Tk 1,200.00 to DESCO successful. TxnId: 88776655
        val billRegex = "(?i)Bill Pay\\s+Tk\\s?([\\d,]+(?:\\.\\d{1,2})?)\\s+to\\s+([^.]+?)\\s+successful(?:.*?TxnId[:\\s]*([A-Z0-9]+))?".toRegex()
        billRegex.find(body)?.let { match ->
            return ParsedTxn(
                amountPaisa = MoneyFormatter.parseTakaToPaisa(match.groupValues[1]),
                direction = Direction.EXPENSE,
                provider = providerId,
                counterparty = match.groupValues[2].trim(),
                referenceId = match.groupValues.getOrNull(3)?.trim(),
                timestamp = extractTimestamp(body) ?: receivedAt,
                confidence = 1.0f
            )
        }

        return null
    }
}

/**
 * Parser for Upay SMS transactions.
 */
class UpayParser : ProviderParser {
    override val providerId: String = "upay"

    override fun canHandle(sender: String): Boolean {
        val s = sender.trim().lowercase(Locale.ROOT)
        return s == "upay"
    }

    override fun parse(sender: String, normalizedBody: String, receivedAt: Long): ParsedTxn? {
        val body = normalizedBody
        // Payment / Send Money
        val regex = "(?i)(?:Payment|Sent|Cash Out)\\s+Tk\\s?([\\d,]+(?:\\.\\d{1,2})?)\\s+to\\s+([^.]+?)\\s+successful(?:.*?TrxID[:\\s]*([A-Z0-9]+))?".toRegex()
        regex.find(body)?.let { match ->
            val isCashOut = body.contains("Cash Out", ignoreCase = true)
            return ParsedTxn(
                amountPaisa = MoneyFormatter.parseTakaToPaisa(match.groupValues[1]),
                direction = if (isCashOut) Direction.TRANSFER else Direction.EXPENSE,
                provider = providerId,
                counterparty = match.groupValues[2].trim(),
                referenceId = match.groupValues.getOrNull(3)?.trim(),
                timestamp = extractTimestamp(body) ?: receivedAt,
                confidence = 1.0f
            )
        }
        return null
    }
}

/**
 * Parser for Bank Card / Account transactions (City Bank, BRAC Bank, EBL, DBBL, Standard Chartered).
 */
class BankParser : ProviderParser {
    override val providerId: String = "bank"

    private val BANK_SENDERS = setOf(
        "citybank", "city bank", "city gem",
        "bracbank", "brac bank",
        "ebl", "easternbank",
        "dbbl", "nexuspay", "dbblcard",
        "scb", "stanchart", "scbl"
    )

    override fun canHandle(sender: String): Boolean {
        val s = sender.trim().lowercase(Locale.ROOT)
        return BANK_SENDERS.any { s.contains(it) }
    }

    override fun parse(sender: String, normalizedBody: String, receivedAt: Long): ParsedTxn? {
        val body = normalizedBody
        val s = sender.trim()

        // 1. ATM Withdrawal (checked before general merchant debit)
        val atmRegex = "(?i)(?:debited for|withdrawn).*?Tk\\s?([\\d,]+(?:\\.\\d{1,2})?)\\s+at\\s+(?:ATM|cash)".toRegex()
        atmRegex.find(body)?.let { match ->
            return ParsedTxn(
                amountPaisa = MoneyFormatter.parseTakaToPaisa(match.groupValues[1]),
                direction = Direction.TRANSFER,
                provider = s,
                counterparty = "ATM Cash Withdrawal",
                referenceId = extractTrxId(body),
                timestamp = extractTimestamp(body) ?: receivedAt,
                confidence = 0.9f
            )
        }

        // 2. Card Swipe / POS Purchase:
        // "Your Card ending with 1234 has been debited for Tk 1,500.00 at MEENA BAZAR on 25-OCT-26. Avail Bal: Tk 45,000"
        // "Purchase of BDT 1,200.00 on card ending 8899 at PIZZA HUT on 25-OCT-26"
        val cardDebitedRegex = "(?i)(?:(?:Card|Acct|A/C).*?(?:debited for|spent|purchase of)|Purchase of)\\s+Tk\\s?([\\d,]+(?:\\.\\d{1,2})?)(?:.*?on card.*?)?\\s+at\\s+([^.]+?)\\s+(?:on|avail|balance|ref|txnid|$)".toRegex()
        cardDebitedRegex.find(body)?.let { match ->
            val amount = MoneyFormatter.parseTakaToPaisa(match.groupValues[1])
            var counterparty = match.groupValues[2].trim()
            // clean trailing dates or codes if any
            counterparty = counterparty.replace("(?i)\\s+on\\s+.*".toRegex(), "").trim()
            return ParsedTxn(
                amountPaisa = amount,
                direction = Direction.EXPENSE,
                provider = s,
                counterparty = cleanMerchantName(counterparty),
                referenceId = extractTrxId(body),
                timestamp = extractTimestamp(body) ?: receivedAt,
                confidence = 0.95f
            )
        }

        // 3. Account Credited / Salary:
        // "Your A/C ... has been credited by Tk 50,000.00 from SALARY"
        val creditedRegex = "(?i)(?:Card|Acct|A/C).*?credited\\s+(?:by|for|with)\\s+Tk\\s?([\\d,]+(?:\\.\\d{1,2})?)(?:.*?from\\s+([^.]+))?".toRegex()
        creditedRegex.find(body)?.let { match ->
            val amount = MoneyFormatter.parseTakaToPaisa(match.groupValues[1])
            val counterparty = match.groupValues.getOrNull(2)?.trim()?.ifBlank { "Bank Credit" } ?: "Bank Credit"
            return ParsedTxn(
                amountPaisa = amount,
                direction = Direction.INCOME,
                provider = s,
                counterparty = cleanMerchantName(counterparty),
                referenceId = extractTrxId(body),
                timestamp = extractTimestamp(body) ?: receivedAt,
                confidence = 0.95f
            )
        }

        return null
    }

    private fun cleanMerchantName(name: String): String {
        return name.replace("(?i)\\s+(ltd|limited|dhaka|bd)\\b".toRegex(), "")
            .trim()
            .split("\\s+".toRegex())
            .joinToString(" ") { word ->
                word.lowercase(Locale.ROOT).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
            }
    }
}

/**
 * Fallback parser for allowlisted senders when no specific template matched.
 */
class GenericFallbackParser : ProviderParser {
    override val providerId: String = "generic"

    override fun canHandle(sender: String): Boolean = true

    override fun parse(sender: String, normalizedBody: String, receivedAt: Long): ParsedTxn? {
        val amountRegex = "(?:Tk|BDT)\\s?([\\d,]+(?:\\.\\d{1,2})?)".toRegex()
        val match = amountRegex.find(normalizedBody) ?: return null
        val amount = MoneyFormatter.parseTakaToPaisa(match.groupValues[1])
        if (amount <= 0) return null

        val isExpense = normalizedBody.contains("(?i)spent|paid|payment|debited|cash out|send|পাঠিয়েছেন|পেমেন্ট".toRegex())
        val isIncome = normalizedBody.contains("(?i)received|credited|deposit|cash in|পেয়েছেন|ক্যাশ ইন".toRegex())

        val direction = when {
            isExpense -> Direction.EXPENSE
            isIncome -> Direction.INCOME
            else -> Direction.EXPENSE
        }

        return ParsedTxn(
            amountPaisa = amount,
            feePaisa = 0L,
            direction = direction,
            provider = sender,
            counterparty = sender,
            referenceId = extractTrxId(normalizedBody),
            timestamp = extractTimestamp(normalizedBody) ?: receivedAt,
            confidence = 0.6f
        )
    }
}

/**
 * Main SMS Parsing Engine coordinating all provider parsers.
 */
class SmsParsingEngine(
    private val allowedSenderSet: Set<String> = defaultAllowedSenders()
) {
    private val parsers: List<ProviderParser> = listOf(
        BkashParser(),
        NagadParser(),
        RocketParser(),
        UpayParser(),
        BankParser()
    )
    private val fallbackParser = GenericFallbackParser()

    fun parse(sender: String, body: String, receivedAt: Long): ParseResult {
        val cleanSender = sender.trim()
        if (!isAllowlisted(cleanSender)) {
            return ParseResult.Ignored("Sender not in allowlist: $cleanSender")
        }

        val normalized = TextNormalizer.normalize(body)

        // Ignore OTP, PIN, Promo without money transaction
        if (isSecurityOrPromoMessage(normalized)) {
            return ParseResult.Ignored("Security or promo message ignored")
        }

        for (parser in parsers) {
            if (parser.canHandle(cleanSender)) {
                val txn = parser.parse(cleanSender, normalized, receivedAt)
                if (txn != null) {
                    return ParseResult.Success(txn)
                }
            }
        }

        // Try generic fallback
        val fallbackTxn = fallbackParser.parse(cleanSender, normalized, receivedAt)
        if (fallbackTxn != null) {
            return ParseResult.Success(fallbackTxn)
        }

        return ParseResult.Unparsed("No template matched allowlisted SMS")
    }

    private fun isAllowlisted(sender: String): Boolean {
        val lower = sender.lowercase(Locale.ROOT)
        return allowedSenderSet.any { lower.contains(it) }
    }

    private fun isSecurityOrPromoMessage(body: String): Boolean {
        val hasOtpKeyword = body.contains("(?i)\\b(OTP|verification code|PIN|security code)\\b".toRegex())
        val hasIgnoreKeyword = body.contains("(?i)\\b(congratulations|offer|promo|campaign|ক্যাম্পেইন)\\b".toRegex())
        val hasAmount = body.contains("Tk\\s?[\\d,]+".toRegex())

        if (hasOtpKeyword) return true
        if (hasIgnoreKeyword && !hasAmount) return true
        return false
    }

    companion object {
        fun defaultAllowedSenders(): Set<String> = setOf(
            "bkash", "nagad", "16167", "rocket", "16216", "upay",
            "citybank", "city bank", "city gem", "bracbank", "brac bank",
            "ebl", "easternbank", "dbbl", "nexuspay", "dbblcard", "scb", "stanchart", "scbl"
        )
    }
}

// Helpers
private fun extractTrxId(body: String): String? {
    val regex = "(?:TrxID|TxnID|Txn ID|Trx ID|Ref)[:\\s]*([A-Z0-9]{6,})".toRegex(RegexOption.IGNORE_CASE)
    return regex.find(body)?.groupValues?.get(1)?.trim()
}

private fun extractTimestamp(body: String): Long? {
    // Matches formats like 25/10/2026 13:45 or 25-10-2026 13:45
    val regex = "(\\d{2}[/-]\\d{2}[/-]\\d{4})\\s+(\\d{1,2}:\\d{2})".toRegex()
    val match = regex.find(body) ?: return null
    return try {
        val dateStr = match.groupValues[1].replace('-', '/')
        val timeStr = match.groupValues[2]
        val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("Asia/Dhaka")
        }
        format.parse("$dateStr $timeStr")?.time
    } catch (_: Exception) {
        null
    }
}
