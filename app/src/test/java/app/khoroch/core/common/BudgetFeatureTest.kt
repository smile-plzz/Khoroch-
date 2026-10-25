package app.khoroch.core.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BudgetFeatureTest {

    @Test
    fun testSafeToSpendCalculation() {
        val totalBudgetPaisa = 3000000L // 30,000 Tk
        val spentPaisa = 1500000L       // 15,000 Tk
        val remainingPaisa = totalBudgetPaisa - spentPaisa
        val daysRemaining = 15

        val safeToSpendDaily = remainingPaisa / daysRemaining
        assertEquals(100000L, safeToSpendDaily) // 1,000 Tk daily
        assertEquals("Tk 1,000", MoneyFormatter.formatPaisa(safeToSpendDaily, currencySymbol = "Tk"))
    }

    @Test
    fun testSafeToSpendWhenBudgetExceededFloorsAtZero() {
        val totalBudgetPaisa = 2000000L
        val spentPaisa = 2500000L // Exceeded by 5,000 Tk
        val remainingPaisa = (totalBudgetPaisa - spentPaisa).coerceAtLeast(0L)
        val daysRemaining = 10

        val safeToSpendDaily = remainingPaisa / daysRemaining
        assertEquals(0L, safeToSpendDaily)
    }

    @Test
    fun testBudgetThresholdTriggers() {
        val budgetPaisa = 1000000L // 10,000 Tk

        // Under 80%
        val spent70Percent = 700000L
        val percent70 = ((spent70Percent.toFloat() / budgetPaisa.toFloat()) * 100).toInt()
        assertEquals(70, percent70)
        assertFalse(percent70 >= 80)

        // At 80% (Warning condition)
        val spent80Percent = 800000L
        val percent80 = ((spent80Percent.toFloat() / budgetPaisa.toFloat()) * 100).toInt()
        assertEquals(80, percent80)
        assertTrue(percent80 >= 80)
        assertFalse(percent80 >= 100)

        // At 100% (Exceeded alert condition)
        val spent100Percent = 1050000L
        val percent100 = ((spent100Percent.toFloat() / budgetPaisa.toFloat()) * 100).toInt()
        assertTrue(percent100 >= 100)
    }
}
