package app.khoroch.core.common

import java.security.MessageDigest
import kotlin.math.abs

/**
 * Formats monetary amounts according to South Asian grouping (lakh & crore)
 * with support for English and Bangla numerals.
 */
object MoneyFormatter {

    private val BANGLA_DIGITS = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')

    /**
     * Converts a Long paisa value to a formatted Taka string with South Asian digit grouping.
     *
     * @param paisa Amount in integer paisa (1 Taka = 100 Paisa)
     * @param useBanglaNumerals If true, converts Latin digits to Bangla digits
     * @param includeDecimals If true, always shows 2 decimals; if false, shows decimals only when paisa % 100 != 0
     * @param currencySymbol Currency prefix or symbol (e.g. "Tk ", "৳ ", or "")
     */
    fun formatPaisa(
        paisa: Long,
        useBanglaNumerals: Boolean = false,
        includeDecimals: Boolean = false,
        currencySymbol: String = ""
    ): String {
        val isNegative = paisa < 0
        val absPaisa = abs(paisa)
        val takaPart = absPaisa / 100
        val paisaPart = absPaisa % 100

        val groupedTaka = formatSouthAsianNumber(takaPart)
        val formattedNumber = if (includeDecimals || paisaPart > 0) {
            val decimals = paisaPart.toString().padStart(2, '0')
            "$groupedTaka.$decimals"
        } else {
            groupedTaka
        }

        val localized = if (useBanglaNumerals) {
            toBanglaNumerals(formattedNumber)
        } else {
            formattedNumber
        }

        val prefix = if (currencySymbol.isNotBlank()) "$currencySymbol " else ""
        return if (isNegative) "- $prefix$localized" else "$prefix$localized"
    }

    /**
     * Formats with an explicit plus or minus sign for cash flow representation.
     */
    fun formatSigned(
        paisa: Long,
        isExpense: Boolean,
        useBanglaNumerals: Boolean = false,
        currencySymbol: String = "Tk"
    ): String {
        val absAmount = formatPaisa(
            paisa = abs(paisa),
            useBanglaNumerals = useBanglaNumerals,
            includeDecimals = false,
            currencySymbol = currencySymbol
        )
        return if (isExpense) "- $absAmount" else "+ $absAmount"
    }

    /**
     * Converts an integer or long string to South Asian format: 1,25,000 / 12,34,56,789.
     */
    fun formatSouthAsianNumber(number: Long): String {
        val s = number.toString()
        if (s.length <= 3) return s

        val lastThree = s.substring(s.length - 3)
        var remaining = s.substring(0, s.length - 3)
        val parts = mutableListOf<String>()

        while (remaining.length > 2) {
            val chunk = remaining.substring(remaining.length - 2)
            parts.add(0, chunk)
            remaining = remaining.substring(0, remaining.length - 2)
        }
        if (remaining.isNotEmpty()) {
            parts.add(0, remaining)
        }

        return parts.joinToString(",") + "," + lastThree
    }

    /**
     * Converts ASCII digits in any string to Bangla digits.
     */
    fun toBanglaNumerals(input: String): String {
        val sb = StringBuilder(input.length)
        for (ch in input) {
            if (ch in '0'..'9') {
                sb.append(BANGLA_DIGITS[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    /**
     * Converts Bangla digits in any string to ASCII digits.
     */
    fun toAsciiNumerals(input: String): String {
        val sb = StringBuilder(input.length)
        for (ch in input) {
            val banglaIndex = BANGLA_DIGITS.indexOf(ch)
            if (banglaIndex != -1) {
                sb.append('0' + banglaIndex)
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    /**
     * Parses a string representation of Taka (e.g. "500.50", "1,250") to Long paisa.
     */
    fun parseTakaToPaisa(input: String): Long {
        val ascii = toAsciiNumerals(input).replace(",", "").trim()
        if (ascii.isBlank()) return 0L
        return try {
            val parts = ascii.split(".")
            val taka = parts[0].toLongOrNull() ?: 0L
            val paisa = if (parts.size > 1) {
                val dec = parts[1].take(2).padEnd(2, '0')
                dec.toLongOrNull() ?: 0L
            } else {
                0L
            }
            taka * 100 + paisa
        } catch (_: Exception) {
            0L
        }
    }
}

/**
 * SHA-256 helper for deduplication.
 */
object HashUtils {
    fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
