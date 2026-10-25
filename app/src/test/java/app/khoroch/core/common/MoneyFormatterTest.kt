package app.khoroch.core.common

import org.junit.Assert.assertEquals
import org.junit.Test

class MoneyFormatterTest {

    @Test
    fun testSouthAsianNumberFormatting() {
        assertEquals("500", MoneyFormatter.formatSouthAsianNumber(500))
        assertEquals("1,250", MoneyFormatter.formatSouthAsianNumber(1250))
        assertEquals("18,450", MoneyFormatter.formatSouthAsianNumber(18450))
        assertEquals("1,25,000", MoneyFormatter.formatSouthAsianNumber(125000))
        assertEquals("12,34,567", MoneyFormatter.formatSouthAsianNumber(1234567))
        assertEquals("1,00,00,000", MoneyFormatter.formatSouthAsianNumber(10000000))
    }

    @Test
    fun testFormatPaisa() {
        assertEquals("500", MoneyFormatter.formatPaisa(50000L))
        assertEquals("500.50", MoneyFormatter.formatPaisa(50050L))
        assertEquals("Tk 1,25,000", MoneyFormatter.formatPaisa(12500000L, currencySymbol = "Tk"))
        assertEquals("- Tk 450", MoneyFormatter.formatPaisa(-45000L, currencySymbol = "Tk"))
    }

    @Test
    fun testBanglaNumerals() {
        val bnNumber = MoneyFormatter.formatPaisa(12500000L, useBanglaNumerals = true, currencySymbol = "৳")
        assertEquals("৳ ১,২৫,০০০", bnNumber)

        val convertedAscii = MoneyFormatter.toAsciiNumerals("১,২৫,০০০")
        assertEquals("1,25,000", convertedAscii)
    }

    @Test
    fun testParseTakaToPaisa() {
        assertEquals(50000L, MoneyFormatter.parseTakaToPaisa("500.00"))
        assertEquals(50050L, MoneyFormatter.parseTakaToPaisa("500.50"))
        assertEquals(12500000L, MoneyFormatter.parseTakaToPaisa("1,25,000"))
        assertEquals(12500000L, MoneyFormatter.parseTakaToPaisa("১,২৫,০০০"))
    }

    @Test
    fun testSignedFormatting() {
        assertEquals("- Tk 450", MoneyFormatter.formatSigned(45000L, isExpense = true))
        assertEquals("+ Tk 42,000", MoneyFormatter.formatSigned(4200000L, isExpense = false))
    }
}
