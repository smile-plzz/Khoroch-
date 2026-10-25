package app.khoroch.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.khoroch.core.categorizer.SmartSuggestionEngine
import app.khoroch.core.common.MoneyFormatter
import app.khoroch.core.model.Direction
import app.khoroch.ui.theme.CategoryColors
import app.khoroch.ui.theme.LightExpense
import app.khoroch.ui.theme.LightIncome

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionSheet(
    onDismiss: () -> Unit,
    onSave: (amountPaisa: Long, direction: Direction, categoryId: String, note: String?) -> Unit,
    useBanglaNumerals: Boolean = false,
    currencySymbol: String = "Tk"
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedDirectionIndex by remember { mutableIntStateOf(0) } // 0=Expense, 1=Income, 2=Transfer
    var amountString by remember { mutableStateOf("0") }
    var selectedCategoryId by remember { mutableStateOf("food") }
    var noteText by remember { mutableStateOf("") }

    val currentDirection = when (selectedDirectionIndex) {
        1 -> Direction.INCOME
        2 -> Direction.TRANSFER
        else -> Direction.EXPENSE
    }

    // Dynamic, user-tailored suggestions based on time of day and money direction
    val smartSuggestions = remember(currentDirection) {
        SmartSuggestionEngine.getSuggestions(direction = currentDirection)
    }

    val allCategories = listOf(
        Pair("food", "Food"),
        Pair("groceries", "Groceries"),
        Pair("transport", "Transport"),
        Pair("bills", "Bills"),
        Pair("shopping", "Shopping"),
        Pair("health", "Health"),
        Pair("rent", "Rent"),
        Pair("entertainment", "Entertainment"),
        Pair("family", "Family"),
        Pair("transfer", "Transfer"),
        Pair("income", "Income"),
        Pair("other", "Other")
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Modern Segmented Control
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                val options = listOf("Expense", "Income", "Transfer")
                options.forEachIndexed { index, label ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                        onClick = {
                            selectedDirectionIndex = index
                            val newDir = when (index) {
                                1 -> Direction.INCOME
                                2 -> Direction.TRANSFER
                                else -> Direction.EXPENSE
                            }
                            if (newDir == Direction.INCOME) selectedCategoryId = "income"
                            else if (newDir == Direction.TRANSFER) selectedCategoryId = "transfer"
                        },
                        selected = index == selectedDirectionIndex
                    ) {
                        Text(label, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Large Formatted Amount Display
            val amountPaisa = MoneyFormatter.parseTakaToPaisa(amountString)
            val formattedDisplay = MoneyFormatter.formatPaisa(
                paisa = amountPaisa,
                useBanglaNumerals = useBanglaNumerals,
                includeDecimals = amountString.contains("."),
                currencySymbol = currencySymbol
            )

            Text(
                text = formattedDisplay,
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold,
                color = when (selectedDirectionIndex) {
                    0 -> LightExpense
                    1 -> LightIncome
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // User-Tailored Interactive Suggestion Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Tailored Suggestions",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Interactive Smart Suggestions Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                smartSuggestions.forEach { suggestion ->
                    val isSelected = suggestion.categoryId == selectedCategoryId
                    val catColor = CategoryColors[suggestion.categoryId] ?: MaterialTheme.colorScheme.primary

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                        modifier = Modifier
                            .clickable { selectedCategoryId = suggestion.categoryId }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = getCategoryIcon(suggestion.categoryId),
                                contentDescription = null,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else catColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = suggestion.categoryName,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = suggestion.reason,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Secondary All Categories Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                allCategories.forEach { (catId, catName) ->
                    val selected = catId == selectedCategoryId
                    FilterChip(
                        selected = selected,
                        onClick = { selectedCategoryId = catId },
                        label = { Text(catName, fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = getCategoryIcon(catId),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Minimal Note Field
            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                placeholder = { Text("Add note (optional)") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Numerical Keypad
            AmountKeypad(
                currentAmountString = amountString,
                onDigitClick = { digit ->
                    if (amountString == "0") {
                        amountString = digit
                    } else if (amountString.length < 10) {
                        amountString += digit
                    }
                },
                onBackspaceClick = {
                    amountString = if (amountString.length > 1) {
                        amountString.dropLast(1)
                    } else {
                        "0"
                    }
                },
                onDecimalClick = {
                    if (!amountString.contains(".")) {
                        amountString += "."
                    }
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Save Button
            Button(
                onClick = {
                    if (amountPaisa > 0) {
                        onSave(amountPaisa, currentDirection, selectedCategoryId, noteText.ifBlank { null })
                        onDismiss()
                    }
                },
                enabled = amountPaisa > 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Save", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
