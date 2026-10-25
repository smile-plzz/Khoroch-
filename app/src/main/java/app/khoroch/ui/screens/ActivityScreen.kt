package app.khoroch.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.khoroch.core.common.MoneyFormatter
import app.khoroch.data.local.TransactionEntity
import app.khoroch.ui.components.TransactionRow
import app.khoroch.ui.components.getCategoryIcon
import app.khoroch.ui.theme.LightExpense
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityScreen(
    transactions: List<TransactionEntity>,
    useBanglaNumerals: Boolean = false,
    currencySymbol: String = "Tk",
    onTransactionClick: (Long) -> Unit = {},
    onDeleteTransaction: (Long) -> Unit = {},
    onRestoreTransaction: (Long) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedDirectionIndex by remember { mutableIntStateOf(0) } // 0=All, 1=Expense, 2=Income, 3=Transfer
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) } // null = all

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val categories = listOf("food", "groceries", "transport", "bills", "shopping", "health", "entertainment", "rent")

    // Filter logic
    val filteredTransactions = remember(transactions, searchQuery, selectedDirectionIndex, selectedCategoryFilter) {
        transactions.filter { txn ->
            val matchesQuery = searchQuery.isBlank() ||
                txn.counterparty.contains(searchQuery, ignoreCase = true) ||
                (txn.note?.contains(searchQuery, ignoreCase = true) == true) ||
                (txn.referenceId?.contains(searchQuery, ignoreCase = true) == true)

            val matchesDirection = when (selectedDirectionIndex) {
                1 -> txn.direction == 0 // Expense
                2 -> txn.direction == 1 // Income
                3 -> txn.direction == 2 // Transfer
                else -> true
            }

            val matchesCategory = selectedCategoryFilter == null || txn.categoryId == selectedCategoryFilter

            matchesQuery && matchesDirection && matchesCategory
        }
    }

    // Group by day
    val dayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val groupedByDay = remember(filteredTransactions) {
        filteredTransactions.groupBy { txn ->
            dayFormat.format(Date(txn.occurredAt))
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(top = 8.dp)
        ) {
            // Search TextField
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search merchant, note, or TrxID...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            )

            // Flow Direction Filters
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val directions = listOf("All", "Expense", "Income", "Transfer")
                directions.forEachIndexed { index, title ->
                    FilterChip(
                        selected = index == selectedDirectionIndex,
                        onClick = { selectedDirectionIndex = index },
                        label = { Text(title, fontSize = 12.sp) }
                    )
                }
            }

            // Category Filter Chips Carousel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedCategoryFilter == null,
                    onClick = { selectedCategoryFilter = null },
                    label = { Text("All Categories", fontSize = 11.sp) }
                )
                categories.forEach { cat ->
                    FilterChip(
                        selected = selectedCategoryFilter == cat,
                        onClick = {
                            selectedCategoryFilter = if (selectedCategoryFilter == cat) null else cat
                        },
                        label = { Text(cat.replaceFirstChar { it.uppercase() }, fontSize = 11.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = getCategoryIcon(cat),
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    )
                }
            }

            // Results summary
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${filteredTransactions.size} transactions",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Grouped List with Swipe-to-Delete and Undo
            if (groupedByDay.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isNotBlank() || selectedCategoryFilter != null) {
                            "No transactions matching current filters"
                        } else {
                            "No transactions recorded yet."
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 15.sp
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    groupedByDay.forEach { (dayKey, dayTxns) ->
                        item(key = "header_$dayKey") {
                            val headerLabel = formatDayHeader(dayKey)
                            val dayNetPaisa = dayTxns.sumOf {
                                when (it.direction) {
                                    0 -> -(it.amountPaisa + it.feePaisa)
                                    1 -> it.amountPaisa
                                    else -> 0L
                                }
                            }

                            Surface(
                                color = MaterialTheme.colorScheme.background,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = headerLabel,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = MoneyFormatter.formatPaisa(
                                            paisa = dayNetPaisa,
                                            useBanglaNumerals = useBanglaNumerals,
                                            currencySymbol = currencySymbol
                                        ),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        items(dayTxns, key = { it.id }) { txn ->
                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = { value ->
                                    if (value == SwipeToDismissBoxValue.EndToStart) {
                                        onDeleteTransaction(txn.id)
                                        scope.launch {
                                            val result = snackbarHostState.showSnackbar(
                                                message = "Deleted ${txn.counterparty}",
                                                actionLabel = "Undo",
                                                duration = SnackbarDuration.Short
                                            )
                                            if (result == SnackbarResult.ActionPerformed) {
                                                onRestoreTransaction(txn.id)
                                            }
                                        }
                                        true
                                    } else false
                                }
                            )

                            SwipeToDismissBox(
                                state = dismissState,
                                enableDismissFromStartToEnd = false,
                                backgroundContent = {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(LightExpense)
                                            .padding(end = 20.dp),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = Color.White
                                        )
                                    }
                                }
                            ) {
                                Surface(color = MaterialTheme.colorScheme.background) {
                                    TransactionRow(
                                        transaction = txn,
                                        useBanglaNumerals = useBanglaNumerals,
                                        currencySymbol = currencySymbol,
                                        onClick = { onTransactionClick(txn.id) }
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

private fun formatDayHeader(dayKey: String): String {
    val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val date = format.parse(dayKey) ?: return dayKey
    val cal = Calendar.getInstance().apply { time = date }
    val now = Calendar.getInstance()

    return when {
        cal.get(Calendar.YEAR) == now.get(Calendar.YEAR) && cal.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR) -> "Today"
        cal.get(Calendar.YEAR) == now.get(Calendar.YEAR) && cal.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR) - 1 -> "Yesterday"
        else -> SimpleDateFormat("EEE, d MMM yyyy", Locale.US).format(date)
    }
}
