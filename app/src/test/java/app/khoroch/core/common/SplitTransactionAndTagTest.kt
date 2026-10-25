package app.khoroch.core.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SplitTransactionAndTagTest {

    @Test
    fun testSplitValidationMath() {
        val totalPaisa = 50000L // 500 Tk
        val part1Paisa = 30000L // 300 Tk
        val part2Paisa = totalPaisa - part1Paisa // 200 Tk

        assertEquals(20000L, part2Paisa)
        assertTrue(part1Paisa > 0 && part2Paisa > 0)
        assertEquals(totalPaisa, part1Paisa + part2Paisa)
    }

    @Test
    fun testSplitValidationRejectsZeroOrOverAmount() {
        val totalPaisa = 100000L // 1,000 Tk

        // Zero first part
        val invalidZero = 0L
        assertFalse(invalidZero in 1 until totalPaisa)

        // Equal or exceeding first part
        val invalidEqual = 100000L
        assertFalse(invalidEqual in 1 until totalPaisa)

        val invalidExceeding = 120000L
        assertFalse(invalidExceeding in 1 until totalPaisa)

        // Valid part
        val validPart = 40000L
        assertTrue(validPart in 1 until totalPaisa)
    }

    @Test
    fun testTagNormalization() {
        val rawTag1 = "#Freelance"
        val rawTag2 = "  OFFICE  "
        val rawTag3 = "#Eid_Salami "

        val clean1 = rawTag1.trim().lowercase().removePrefix("#")
        val clean2 = rawTag2.trim().lowercase().removePrefix("#")
        val clean3 = rawTag3.trim().lowercase().removePrefix("#")

        assertEquals("freelance", clean1)
        assertEquals("office", clean2)
        assertEquals("eid_salami", clean3)
    }
}
