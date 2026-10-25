package app.khoroch.core.parser

import app.khoroch.core.model.Direction
import app.khoroch.core.model.ParseResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BankParsersTest {

    private val engine = SmsParsingEngine()
    private val testTimestamp = 1729845900000L

    @Test
    fun testCityBankCardDebit() {
        val sms = "Your Card ending with 4321 has been debited for BDT 2,450.00 at AARONG DHAKA on 25-OCT-26. Ref: CB998811"
        val result = engine.parse("CityBank", sms, testTimestamp)

        assertTrue(result is ParseResult.Success)
        val txn = (result as ParseResult.Success).txn
        assertEquals(245000L, txn.amountPaisa)
        assertEquals(Direction.EXPENSE, txn.direction)
        assertEquals("Aarong", txn.counterparty)
        assertEquals("CityBank", txn.provider)
    }

    @Test
    fun testBracBankDebit() {
        val sms = "Acct ending 5678 debited for Tk 3,500.00 at UNIMART on 25-OCT-26. Avail Bal: Tk 12,000"
        val result = engine.parse("BRACBANK", sms, testTimestamp)

        assertTrue(result is ParseResult.Success)
        val txn = (result as ParseResult.Success).txn
        assertEquals(350000L, txn.amountPaisa)
        assertEquals(Direction.EXPENSE, txn.direction)
        assertEquals("Unimart", txn.counterparty)
    }

    @Test
    fun testEblCardPurchase() {
        val sms = "Purchase of BDT 1,200.00 on card ending 8899 at PIZZA HUT on 25-OCT-26"
        val result = engine.parse("EBL", sms, testTimestamp)

        assertTrue(result is ParseResult.Success)
        val txn = (result as ParseResult.Success).txn
        assertEquals(120000L, txn.amountPaisa)
        assertEquals(Direction.EXPENSE, txn.direction)
        assertEquals("Pizza Hut", txn.counterparty)
    }

    @Test
    fun testDbblDebit() {
        val sms = "Card 1122 debited for Tk 850.00 at BATA SHOE on 25-OCT-26"
        val result = engine.parse("DBBLCard", sms, testTimestamp)

        assertTrue(result is ParseResult.Success)
        val txn = (result as ParseResult.Success).txn
        assertEquals(85000L, txn.amountPaisa)
        assertEquals(Direction.EXPENSE, txn.direction)
        assertEquals("Bata Shoe", txn.counterparty)
    }

    @Test
    fun testStandardCharteredDebit() {
        val sms = "Your Card ending 9900 has been debited for BDT 4,100.00 at SHWAPNO on 25-OCT-26"
        val result = engine.parse("SCB", sms, testTimestamp)

        assertTrue(result is ParseResult.Success)
        val txn = (result as ParseResult.Success).txn
        assertEquals(410000L, txn.amountPaisa)
        assertEquals(Direction.EXPENSE, txn.direction)
        assertEquals("Shwapno", txn.counterparty)
    }

    @Test
    fun testBankSalaryCredit() {
        val sms = "Your Acct ending 1234 credited with Tk 65,000.00 from TECH SOLUTIONS LTD. Avail Bal: Tk 80,000"
        val result = engine.parse("CityBank", sms, testTimestamp)

        assertTrue(result is ParseResult.Success)
        val txn = (result as ParseResult.Success).txn
        assertEquals(6500000L, txn.amountPaisa)
        assertEquals(Direction.INCOME, txn.direction)
    }

    @Test
    fun testAtmCashWithdrawalTransfer() {
        val sms = "Your Acct ending 7788 debited for Tk 5,000.00 at ATM on 25-OCT-26"
        val result = engine.parse("EBL", sms, testTimestamp)

        assertTrue(result is ParseResult.Success)
        val txn = (result as ParseResult.Success).txn
        assertEquals(500000L, txn.amountPaisa)
        assertEquals(Direction.TRANSFER, txn.direction)
        assertEquals("ATM Cash Withdrawal", txn.counterparty)
    }
}
