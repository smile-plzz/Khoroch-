package app.khoroch.core.parser

import app.khoroch.core.model.Direction
import app.khoroch.core.model.ParseResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SmsParsingEngineTest {

    private val engine = SmsParsingEngine()
    private val testTimestamp = 1729845900000L

    @Test
    fun testBkashSendMoney() {
        val sms = "You have sent Tk 500.00 to 01711223344. Fee Tk 0.00. Balance Tk 1,250.50. TrxID ABC123XYZ at 25/10/2026 13:45"
        val result = engine.parse("bKash", sms, testTimestamp)

        assertTrue(result is ParseResult.Success)
        val txn = (result as ParseResult.Success).txn
        assertEquals(50000L, txn.amountPaisa)
        assertEquals(0L, txn.feePaisa)
        assertEquals(Direction.EXPENSE, txn.direction)
        assertEquals("bkash", txn.provider)
        assertEquals("01711223344", txn.counterparty)
        assertEquals("ABC123XYZ", txn.referenceId)
        assertEquals(125050L, txn.balanceAfterPaisa)
    }

    @Test
    fun testBkashPayment() {
        val sms = "Payment Tk 450.00 to Rahim Store successful. Fee Tk 0.00. Balance Tk 800.50. TrxID PAY8899 at 25/10/2026 14:00"
        val result = engine.parse("bKash", sms, testTimestamp)

        assertTrue(result is ParseResult.Success)
        val txn = (result as ParseResult.Success).txn
        assertEquals(45000L, txn.amountPaisa)
        assertEquals(Direction.EXPENSE, txn.direction)
        assertEquals("Rahim Store", txn.counterparty)
        assertEquals("PAY8899", txn.referenceId)
    }

    @Test
    fun testBkashCashOut() {
        val sms = "Cash Out Tk 1,000.00 to Agent 01811223344 successful. Fee Tk 18.50. Balance Tk 2,500.00. TrxID CASH9911"
        val result = engine.parse("bKash", sms, testTimestamp)

        assertTrue(result is ParseResult.Success)
        val txn = (result as ParseResult.Success).txn
        assertEquals(100000L, txn.amountPaisa)
        assertEquals(1850L, txn.feePaisa)
        assertEquals(Direction.TRANSFER, txn.direction)
        assertEquals("Agent 01811223344", txn.counterparty)
    }

    @Test
    fun testNagadPayment() {
        val sms = "Payment of Tk 350.00 to Shwapno is successful. Balance: Tk 1,500.00. TxnID: 71ABCDEF"
        val result = engine.parse("NAGAD", sms, testTimestamp)

        assertTrue(result is ParseResult.Success)
        val txn = (result as ParseResult.Success).txn
        assertEquals(35000L, txn.amountPaisa)
        assertEquals(Direction.EXPENSE, txn.direction)
        assertEquals("Shwapno", txn.counterparty)
        assertEquals("71ABCDEF", txn.referenceId)
    }

    @Test
    fun testRocketBillPay() {
        val sms = "Bill Pay Tk 1,200.00 to DESCO successful. TxnId: 88776655"
        val result = engine.parse("ROCKET", sms, testTimestamp)

        assertTrue(result is ParseResult.Success)
        val txn = (result as ParseResult.Success).txn
        assertEquals(120000L, txn.amountPaisa)
        assertEquals(Direction.EXPENSE, txn.direction)
        assertEquals("DESCO", txn.counterparty)
    }

    @Test
    fun testBankCardSwipe() {
        val sms = "Your Card ending with 1234 has been debited for BDT 1,500.00 at MEENA BAZAR on 25-OCT-26"
        val result = engine.parse("CityBank", sms, testTimestamp)

        assertTrue(result is ParseResult.Success)
        val txn = (result as ParseResult.Success).txn
        assertEquals(150000L, txn.amountPaisa)
        assertEquals(Direction.EXPENSE, txn.direction)
        assertEquals("Meena Bazar", txn.counterparty)
    }

    @Test
    fun testOtpMessageIsIgnored() {
        val sms = "Your bKash verification code is 492011. Do not share your OTP or PIN with anyone."
        val result = engine.parse("bKash", sms, testTimestamp)

        assertTrue(result is ParseResult.Ignored)
    }

    @Test
    fun testUnallowlistedSenderIsIgnored() {
        val sms = "Hey, let's meet for lunch today at 1pm."
        val result = engine.parse("01711000000", sms, testTimestamp)

        assertTrue(result is ParseResult.Ignored)
    }
}
