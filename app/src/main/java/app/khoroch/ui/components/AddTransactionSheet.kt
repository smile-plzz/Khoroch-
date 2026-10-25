package app.khoroch.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.khoroch.core.common.MoneyFormatter
import app.khoroch.core.model.Direction
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

    val categories = listOf(
        Pair("food", "Food"),
        Pair("groceries", "Groceries"),
        Pair("transport", "Transport"),
        Pair("bills", "Bills"),
        Pair("shopping", "Shopping"),
        Pair("health", "Health"),
        Pair("rent", "Rent"),
        Pair("entertainment", "Entertainment"),
        Pair("family", "Family"),
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
            // Segmented Control (Expense / Income / Transfer)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                val options = listOf("Expense", "Income", "Transfer")
                options.forEachIndexed { index, label ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                        onClick = { selectedDirectionIndex = index },
                        selected = index == selectedDirectionIndex
                    ) {
                        Text(label, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = when (selectedDirectionIndex) {
                    0 -> LightExpense
                    1 -> LightIncome
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Category Chips Row (Horizontal Scroll)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { (catId, catName) ->
                    FilterChip(
                        selected = catId == selectedCategoryId,
                        onClick = { selectedCategoryId = catId },
                        label = { Text(catName) },
                        leadingIcon = {
                            Icon(
                                imageVector = getCategoryIcon(catId),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Note Text Field
            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                label = { Text("Note / Description") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

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

            Spacer(modifier = Modifier.height(12.dp))

            // Save Button
            val direction = when (selectedDirectionIndex) {
                1 -> Direction.INCOME
                2 -> Direction.TRANSFER
                else -> Direction.EXPENSE
            }

            Button(
                onClick = {
                    if (amountPaisa > 0) {
                        onSave(amountPaisa, direction, selectedCategoryId, noteText.ifBlank { null })
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

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
