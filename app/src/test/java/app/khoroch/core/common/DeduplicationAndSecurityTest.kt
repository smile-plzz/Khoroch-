package app.khoroch.core.common

import app.khoroch.core.model.ParseResult
import app.khoroch.core.parser.SmsParsingEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeduplicationAndSecurityTest {

    private val engine = SmsParsingEngine()

    @Test
    fun testPrimaryDedupeHashConsistency() {
        val hash1 = HashUtils.sha256("bkash:TRX998877")
        val hash2 = HashUtils.sha256("bkash:TRX998877")
        val hashDifferentTrx = HashUtils.sha256("bkash:TRX998878")

        assertEquals(hash1, hash2)
        assertNotEquals(hash1, hashDifferentTrx)
        assertEquals(64, hash1.length) // SHA-256 hex string is 64 chars
    }

    @Test
    fun testFallbackDedupeHashBucketRounding() {
        // Two messages arriving within the same minute have identical minuteBucket
        val timestamp1 = 1729845910000L // e.g. 13:45:10
        val timestamp2 = 1729845940000L // e.g. 13:45:40 (30 seconds later, same minute)
        val bucket1 = timestamp1 / 60000
        val bucket2 = timestamp2 / 60000

        assertEquals(bucket1, bucket2)

        val hash1 = HashUtils.sha256("bKash:payment of 500:$bucket1")
        val hash2 = HashUtils.sha256("bKash:payment of 500:$bucket2")
        assertEquals(hash1, hash2)
    }

    @Test
    fun testZeroSecurityLeakageOnOtpAndPin() {
        val otpMessages = listOf(
            "Your OTP is 998822 for online transaction. Do not share.",
            "bKash PIN change verification code: 123456. Valid for 2 mins.",
            "Never share your secret PIN or security code with anyone."
        )

        for (msg in otpMessages) {
            val result = engine.parse("bKash", msg, System.currentTimeMillis())
            assertTrue("Expected message to be ignored: $msg", result is ParseResult.Ignored)
        }
    }
}
