package app.khoroch.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.khoroch.core.common.MoneyFormatter
import app.khoroch.data.local.TransactionEntity
import app.khoroch.ui.components.TransactionRow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ActivityScreen(
    transactions: List<TransactionEntity>,
    useBanglaNumerals: Boolean = false,
    currencySymbol: String = "Tk",
    onTransactionClick: (Long) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterIndex by remember { mutableIntStateOf(0) } // 0=All, 1=Expense, 2=Income, 3=Transfer

    // 1. Filter by query and direction
    val filteredTransactions = remember(transactions, searchQuery, selectedFilterIndex) {
        transactions.filter { txn ->
            val matchesQuery = searchQuery.isBlank() ||
                txn.counterparty.contains(searchQuery, ignoreCase = true) ||
                (txn.note?.contains(searchQuery, ignoreCase = true) == true) ||
                (txn.referenceId?.contains(searchQuery, ignoreCase = true) == true)

            val matchesDirection = when (selectedFilterIndex) {
                1 -> txn.direction == 0 // Expense
                2 -> txn.direction == 1 // Income
                3 -> txn.direction == 2 // Transfer
                else -> true
            }

            matchesQuery && matchesDirection
        }
    }

    // 2. Group by day
    val dayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val groupedByDay = remember(filteredTransactions) {
        filteredTransactions.groupBy { txn ->
            dayFormat.format(Date(txn.occurredAt))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 8.dp)
    ) {
        // Search TextField
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by merchant, note, or TrxID...") },
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

        // Filter Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filters = listOf("All", "Expense", "Income", "Transfer")
            filters.forEachIndexed { index, title ->
                FilterChip(
                    selected = index == selectedFilterIndex,
                    onClick = { selectedFilterIndex = index },
                    label = { Text(title) }
                )
            }
        }

        // Transactions Grouped by Day
        if (groupedByDay.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (searchQuery.isNotBlank()) "No transactions matching \"$searchQuery\"" else "No transactions logged yet.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 15.sp
                )
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                groupedByDay.forEach { (dayKey, dayTxns) ->
                    // Sticky / Group Day Header
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
                        TransactionRow(
                            transaction = txn,
                            useBanglaNumerals = useBanglaNumerals,
                            currencySymbol = currencySymbol,
                            onClick = { onTransactionClick(txn.id) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
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
