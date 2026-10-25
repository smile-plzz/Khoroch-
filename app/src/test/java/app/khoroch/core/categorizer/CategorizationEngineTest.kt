package app.khoroch.core.categorizer

import app.khoroch.core.model.CategorySource
import app.khoroch.core.model.Direction
import org.junit.Assert.assertEquals
import org.junit.Test

class CategorizationEngineTest {

    private val rulesJson = """
        {
          "version": 1,
          "rules": [
            {
              "id": "food-001",
              "category": "food",
              "field": "counterparty",
              "match": "contains_any",
              "values": ["foodpanda", "kfc", "restaurant"],
              "priority": 50
            },
            {
              "id": "groceries-001",
              "category": "groceries",
              "field": "counterparty",
              "match": "contains_any",
              "values": ["shwapno", "agora", "meena bazar"],
              "priority": 50
            },
            {
              "id": "transport-001",
              "category": "transport",
              "field": "counterparty",
              "match": "contains_any",
              "values": ["uber", "pathao"],
              "priority": 50
            },
            {
              "id": "bills-001",
              "category": "bills",
              "field": "body",
              "match": "regex",
              "values": ["(?i)recharge|top ?up"],
              "priority": 70
            }
          ]
        }
    """.trimIndent()

    private val engine = CategorizationEngine(CategorizationEngine.parseRulesFromJson(rulesJson))

    @Test
    fun testMerchantMemoryTakesPrecedence() {
        val (category, source) = engine.categorize(
            counterparty = "Random Merchant Ltd",
            body = "Payment Tk 500",
            direction = Direction.EXPENSE,
            provider = "bkash",
            merchantMemoryCategory = "entertainment"
        )
        assertEquals("entertainment", category)
        assertEquals(CategorySource.MERCHANT_MEMORY, source)
    }

    @Test
    fun testRuleMatching() {
        val (foodCat, foodSource) = engine.categorize(
            counterparty = "Foodpanda BD",
            body = "Payment Tk 350",
            direction = Direction.EXPENSE,
            provider = "bkash"
        )
        assertEquals("food", foodCat)
        assertEquals(CategorySource.RULE, foodSource)

        val (groceryCat, _) = engine.categorize(
            counterparty = "Shwapno Super Shop",
            body = "Payment Tk 1200",
            direction = Direction.EXPENSE,
            provider = "nagad"
        )
        assertEquals("groceries", groceryCat)

        val (transportCat, _) = engine.categorize(
            counterparty = "Uber Trip",
            body = "Payment Tk 250",
            direction = Direction.EXPENSE,
            provider = "citybank"
        )
        assertEquals("transport", transportCat)
    }

    @Test
    fun testDirectionDefaults() {
        val (transferCat, source) = engine.categorize(
            counterparty = "Agent 01711223344",
            body = "Cash Out Tk 1000",
            direction = Direction.TRANSFER,
            provider = "bkash"
        )
        assertEquals("transfer", transferCat)
        assertEquals(CategorySource.DEFAULT, source)

        val (incomeCat, _) = engine.categorize(
            counterparty = "Employer",
            body = "Salary Tk 50000",
            direction = Direction.INCOME,
            provider = "bank"
        )
        assertEquals("income", incomeCat)
    }
}
