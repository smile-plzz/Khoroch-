package app.khoroch.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import app.khoroch.core.common.MoneyFormatter
import app.khoroch.data.local.TagEntity
import app.khoroch.data.local.TransactionEntity
import app.khoroch.ui.theme.LightExpense
import app.khoroch.ui.theme.LightIncome
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailDialog(
    transaction: TransactionEntity,
    rawSmsText: String? = null,
    tags: List<TagEntity> = emptyList(),
    useBanglaNumerals: Boolean = false,
    currencySymbol: String = "Tk",
    onDismiss: () -> Unit,
    onCategoryChanged: (newCategory: String, applyToAll: Boolean) -> Unit,
    onAddTag: (String) -> Unit = {},
    onRemoveTag: (Long) -> Unit = {},
    onSplit: ((Long, String, Long, String) -> Unit)? = null,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("dd MMMM yyyy, h:mm a", Locale.US)
    val formattedDate = dateFormat.format(Date(transaction.occurredAt))

    var expandedCategoryMenu by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(transaction.categoryId ?: "other") }
    var applyToAll by remember { mutableStateOf(true) }
    var showSmsBody by remember { mutableStateOf(false) }
    var showTagInputDialog by remember { mutableStateOf(false) }
    var showSplitDialog by remember { mutableStateOf(false) }

    val categories = listOf(
        "food", "groceries", "transport", "bills", "rent",
        "shopping", "health", "education", "entertainment", "family",
        "transfer", "income", "other"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header: Counterparty and Signed Amount
                Text(
                    text = transaction.counterparty,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))

                val isExpense = transaction.direction == 0
                val isTransfer = transaction.direction == 2
                val amountText = if (isTransfer) {
                    MoneyFormatter.formatPaisa(
                        paisa = transaction.amountPaisa,
                        useBanglaNumerals = useBanglaNumerals,
                        currencySymbol = currencySymbol
                    )
                } else {
                    MoneyFormatter.formatSigned(
                        paisa = transaction.amountPaisa,
                        isExpense = isExpense,
                        useBanglaNumerals = useBanglaNumerals,
                        currencySymbol = currencySymbol
                    )
                }

                Text(
                    text = amountText,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        isTransfer -> MaterialTheme.colorScheme.onSurface
                        isExpense -> LightExpense
                        else -> LightIncome
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(14.dp))

                // Detail Fields
                DetailRow(label = "Date & Time", value = formattedDate)
                DetailRow(label = "Provider", value = transaction.provider.uppercase(Locale.ROOT))
                if (!transaction.referenceId.isNullOrBlank()) {
                    DetailRow(label = "TrxID", value = transaction.referenceId)
                }
                if (transaction.feePaisa > 0) {
                    DetailRow(
                        label = "Fee",
                        value = MoneyFormatter.formatPaisa(transaction.feePaisa, useBanglaNumerals, currencySymbol = currencySymbol)
                    )
                }
                if (transaction.balanceAfterPaisa != null) {
                    DetailRow(
                        label = "Balance After",
                        value = MoneyFormatter.formatPaisa(transaction.balanceAfterPaisa, useBanglaNumerals, currencySymbol = currencySymbol)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Category Dropdown
                Text(
                    text = "Category",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                ExposedDropdownMenuBox(
                    expanded = expandedCategoryMenu,
                    onExpandedChange = { expandedCategoryMenu = !expandedCategoryMenu }
                ) {
                    OutlinedTextField(
                        value = selectedCategory.replaceFirstChar { it.uppercase() },
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategoryMenu) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCategoryMenu,
                        onDismissRequest = { expandedCategoryMenu = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.replaceFirstChar { it.uppercase() }) },
                                onClick = {
                                    selectedCategory = cat
                                    expandedCategoryMenu = false
                                    onCategoryChanged(cat, applyToAll)
                                }
                            )
                        }
                    }
                }

                // Apply to future transactions switch
                if (transaction.counterpartyKey.isNotBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Always remember for ${transaction.counterparty}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = applyToAll,
                            onCheckedChange = { applyToAll = it }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tags Section
                Text(
                    text = "Tags",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    tags.forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { onRemoveTag(tag.id) }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("#${tag.name}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(12.dp))
                            }
                        }
                    }
                    OutlinedButton(
                        onClick = { showTagInputDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tag", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Expandable Original SMS Message (Transparency for Karim/Rafi)
                if (!rawSmsText.isNullOrBlank()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showSmsBody = !showSmsBody },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Original SMS",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            clipboard.setPrimaryClip(ClipData.newPlainText("SMS", rawSmsText))
                                            Toast.makeText(context, "Copied SMS", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = if (showSmsBody) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            AnimatedVisibility(visible = showSmsBody) {
                                Text(
                                    text = rawSmsText,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Split Transaction Button (Nusrat persona)
                if (onSplit != null && transaction.amountPaisa > 100) {
                    OutlinedButton(
                        onClick = { showSplitDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CallSplit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Split this transaction into two")
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            onDelete()
                            onDismiss()
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = LightExpense)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                        Text("Delete")
                    }

                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Done")
                    }
                }
            }
        }
    }

    // Tag Input Dialog
    if (showTagInputDialog) {
        var newTagText by remember { mutableStateOf("") }
        Dialog(onDismissRequest = { showTagInputDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Add Tag", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newTagText,
                        onValueChange = { newTagText = it },
                        placeholder = { Text("e.g. freelance, lunch, tax") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showTagInputDialog = false }) {
                            Text("Cancel")
                        }
                        Button(onClick = {
                            if (newTagText.isNotBlank()) {
                                onAddTag(newTagText)
                                showTagInputDialog = false
                            }
                        }) {
                            Text("Add")
                        }
                    }
                }
            }
        }
    }

    // Split Transaction Dialog
    if (showSplitDialog && onSplit != null) {
        var part1AmountText by remember { mutableStateOf("") }
        var part1Category by remember { mutableStateOf(selectedCategory) }
        var part2Category by remember { mutableStateOf("groceries") }

        Dialog(onDismissRequest = { showSplitDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Split Transaction", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Total: ${MoneyFormatter.formatPaisa(transaction.amountPaisa, useBanglaNumerals, currencySymbol = currencySymbol)}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    val part1Paisa = MoneyFormatter.parseTakaToPaisa(part1AmountText)
                    val part2Paisa = (transaction.amountPaisa - part1Paisa).coerceAtLeast(0L)

                    OutlinedTextField(
                        value = part1AmountText,
                        onValueChange = { part1AmountText = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Part 1 Amount") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Part 2 (Remainder): ${MoneyFormatter.formatPaisa(part2Paisa, useBanglaNumerals, currencySymbol = currencySymbol)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showSplitDialog = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (part1Paisa > 0 && part2Paisa > 0 && part1Paisa + part2Paisa == transaction.amountPaisa) {
                                    onSplit(part1Paisa, part1Category, part2Paisa, part2Category)
                                    showSplitDialog = false
                                    onDismiss()
                                }
                            },
                            enabled = part1Paisa in 1 until transaction.amountPaisa
                        ) {
                            Text("Confirm Split")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
