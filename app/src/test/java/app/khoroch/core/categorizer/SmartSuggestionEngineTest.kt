package app.khoroch.core.categorizer

import app.khoroch.core.model.Direction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SmartSuggestionEngineTest {

    @Test
    fun testMerchantMemoryPriorityInSuggestions() {
        val suggestions = SmartSuggestionEngine.getSuggestions(
            counterparty = "Star Cineplex",
            direction = Direction.EXPENSE,
            merchantMemoryCategory = "entertainment"
        )
        assertTrue(suggestions.isNotEmpty())
        assertEquals("entertainment", suggestions.first().categoryId)
        assertTrue(suggestions.first().confidence >= 0.9f)
    }

    @Test
    fun testIncomeDirectionSuggestions() {
        val suggestions = SmartSuggestionEngine.getSuggestions(
            direction = Direction.INCOME
        )
        assertEquals("income", suggestions.first().categoryId)
        assertTrue(suggestions.any { it.categoryId == "family" })
    }

    @Test
    fun testTransferDirectionSuggestions() {
        val suggestions = SmartSuggestionEngine.getSuggestions(
            direction = Direction.TRANSFER
        )
        assertEquals("transfer", suggestions.first().categoryId)
    }

    @Test
    fun testTimeOfDayContextualSuggestions() {
        // Morning commute: 8 AM
        val morningSuggestions = SmartSuggestionEngine.getSuggestions(
            direction = Direction.EXPENSE,
            currentHour = 8
        )
        assertTrue(morningSuggestions.any { it.categoryId == "transport" })

        // Lunch: 1 PM (13)
        val lunchSuggestions = SmartSuggestionEngine.getSuggestions(
            direction = Direction.EXPENSE,
            currentHour = 13
        )
        assertTrue(lunchSuggestions.any { it.categoryId == "food" })

        // Evening groceries: 6 PM (18)
        val eveningSuggestions = SmartSuggestionEngine.getSuggestions(
            direction = Direction.EXPENSE,
            currentHour = 18
        )
        assertTrue(eveningSuggestions.any { it.categoryId == "groceries" })
    }
}
