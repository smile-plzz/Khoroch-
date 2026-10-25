package app.khoroch.core.categorizer

import app.khoroch.core.model.Direction
import java.util.Calendar

data class CategorySuggestion(
    val categoryId: String,
    val categoryName: String,
    val reason: String,
    val confidence: Float
)

/**
 * Generates user-tailored interactive category suggestions based on
 * merchant memory, time of day, direction of money flow, and user habits.
 */
object SmartSuggestionEngine {

    /**
     * Recommends tailored categories for manual entry or review.
     */
    fun getSuggestions(
        counterparty: String = "",
        direction: Direction = Direction.EXPENSE,
        merchantMemoryCategory: String? = null,
        recentCategories: List<String> = emptyList(),
        currentHour: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    ): List<CategorySuggestion> {
        val suggestions = mutableListOf<CategorySuggestion>()

        // 1. Merchant Memory has highest confidence
        if (!merchantMemoryCategory.isNullOrBlank()) {
            suggestions.add(
                CategorySuggestion(
                    categoryId = merchantMemoryCategory,
                    categoryName = merchantMemoryCategory.replaceFirstChar { it.uppercase() },
                    reason = "Learned from past transactions",
                    confidence = 0.95f
                )
            )
        }

        // 2. Flow Direction Considerations
        if (direction == Direction.INCOME) {
            suggestions.add(CategorySuggestion("income", "Income", "Direction is incoming", 0.9f))
            suggestions.add(CategorySuggestion("family", "Family & Gifts", "Gift or remittance", 0.7f))
            return suggestions
        } else if (direction == Direction.TRANSFER) {
            suggestions.add(CategorySuggestion("transfer", "Transfer", "Account or Cash transfer", 0.9f))
            return suggestions
        }

        // 3. Time-of-day contextual suggestions for Expenses
        when (currentHour) {
            in 7..10 -> {
                suggestions.add(CategorySuggestion("transport", "Transport", "Morning commute hours", 0.8f))
                suggestions.add(CategorySuggestion("food", "Breakfast & Coffee", "Morning meal time", 0.75f))
            }
            in 12..15 -> {
                suggestions.add(CategorySuggestion("food", "Food & Dining", "Typical lunch hour", 0.85f))
            }
            in 17..20 -> {
                suggestions.add(CategorySuggestion("groceries", "Groceries", "Evening shopping / bazar", 0.8f))
                suggestions.add(CategorySuggestion("transport", "Transport", "Evening return commute", 0.75f))
            }
            in 20..23 -> {
                suggestions.add(CategorySuggestion("food", "Dinner & Dining", "Dinner hours", 0.8f))
                suggestions.add(CategorySuggestion("entertainment", "Entertainment", "Night leisure / streaming", 0.7f))
            }
        }

        // 4. Recently used user categories
        for (recent in recentCategories.distinct()) {
            if (suggestions.none { it.categoryId == recent }) {
                suggestions.add(
                    CategorySuggestion(
                        categoryId = recent,
                        categoryName = recent.replaceFirstChar { it.uppercase() },
                        reason = "Recently used",
                        confidence = 0.65f
                    )
                )
            }
        }

        // 5. Default popular categories fallback
        val defaultFallbacks = listOf("food", "groceries", "transport", "bills", "shopping")
        for (cat in defaultFallbacks) {
            if (suggestions.none { it.categoryId == cat }) {
                suggestions.add(
                    CategorySuggestion(
                        categoryId = cat,
                        categoryName = cat.replaceFirstChar { it.uppercase() },
                        reason = "Popular category",
                        confidence = 0.5f
                    )
                )
            }
        }

        return suggestions.distinctBy { it.categoryId }.take(6)
    }
}
