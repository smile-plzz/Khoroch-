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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import app.khoroch.core.common.MoneyFormatter
import app.khoroch.data.local.CategorySpending
import app.khoroch.data.local.TransactionEntity
import app.khoroch.ui.components.AddTransactionSheet
import app.khoroch.ui.components.DailyBarChart
import app.khoroch.ui.components.DonutChart
import app.khoroch.ui.components.TransactionRow
import app.khoroch.ui.theme.LightExpense
import app.khoroch.ui.theme.LightIncome
import app.khoroch.ui.theme.LightWarning
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun HomeScreen(
    transactions: List<TransactionEntity>,
    categorySpendings: List<CategorySpending>,
    totalExpensePaisa: Long,
    totalIncomePaisa: Long,
    unparsedCount: Int,
    useBanglaNumerals: Boolean = false,
    currencySymbol: String = "Tk",
    onSeeAllClick: () -> Unit = {},
    onUnparsedClick: () -> Unit = {},
    onTransactionClick: (Long) -> Unit = {},
    onAddTransaction: (Long, app.khoroch.core.model.Direction, String, String?) -> Unit
) {
    var showAddSheet by remember { mutableStateOf(false) }
    var calendarMonthOffset by remember { mutableStateOf(0) } // 0 is current month

    val calendar = remember(calendarMonthOffset) {
        Calendar.getInstance().apply {
            add(Calendar.MONTH, calendarMonthOffset)
        }
    }

    val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.US)
    val monthTitle = monthYearFormat.format(calendar.time)

    // Calculate budget & safe to spend
    val monthlyBudgetPaisa = 3000000L // 30,000 Tk default or calculated
    val remainingBudgetPaisa = (monthlyBudgetPaisa - totalExpensePaisa).coerceAtLeast(0L)
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val dayOfMonth = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    val daysRemaining = (daysInMonth - dayOfMonth + 1).coerceAtLeast(1)
    val safeToSpendTodayPaisa = remainingBudgetPaisa / daysRemaining

    val budgetPercent = if (monthlyBudgetPaisa > 0) {
        ((totalExpensePaisa.toFloat() / monthlyBudgetPaisa.toFloat()) * 100).toInt()
    } else 0

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Transaction")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Month Selector Bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = monthTitle,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row {
                        IconButton(onClick = { calendarMonthOffset-- }) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Month")
                        }
                        IconButton(onClick = { calendarMonthOffset++ }) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Next Month")
                        }
                    }
                }
            }

            // Unparsed Messages Banner
            if (unparsedCount > 0) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = LightWarning.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .clickable { onUnparsedClick() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = LightWarning,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "$unparsedCount messages need a look",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Spend & Income Summary Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Spent this month",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = MoneyFormatter.formatPaisa(
                                paisa = totalExpensePaisa,
                                useBanglaNumerals = useBanglaNumerals,
                                currencySymbol = currencySymbol
                            ),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Income and Net
                        val netPaisa = totalIncomePaisa - totalExpensePaisa
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                text = "Income: ${MoneyFormatter.formatPaisa(totalIncomePaisa, useBanglaNumerals, currencySymbol = currencySymbol)}",
                                fontSize = 13.sp,
                                color = LightIncome,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Net: ${if (netPaisa >= 0) "+" else ""}${MoneyFormatter.formatPaisa(netPaisa, useBanglaNumerals, currencySymbol = currencySymbol)}",
                                fontSize = 13.sp,
                                color = if (netPaisa >= 0) LightIncome else LightExpense,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Budget Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "$budgetPercent% of ${MoneyFormatter.formatPaisa(monthlyBudgetPaisa, useBanglaNumerals, currencySymbol = currencySymbol)}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Safe to spend today: ${MoneyFormatter.formatPaisa(safeToSpendTodayPaisa, useBanglaNumerals, currencySymbol = currencySymbol)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { (budgetPercent / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (budgetPercent > 100) LightExpense else if (budgetPercent >= 80) LightWarning else MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }

            // User-Tailored Smart Insight Card
            if (categorySpendings.isNotEmpty() && totalExpensePaisa > 0) {
                val topCat = categorySpendings.maxByOrNull { it.totalPaisa }
                if (topCat != null) {
                    val catName = (topCat.categoryId ?: "other").replaceFirstChar { it.uppercase() }
                    val percent = ((topCat.totalPaisa.toFloat() / totalExpensePaisa.toFloat()) * 100).toInt()
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(14.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Top Expense: $catName ($percent%)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${MoneyFormatter.formatPaisa(topCat.totalPaisa, useBanglaNumerals, currencySymbol = currencySymbol)} spent across ${topCat.count} payments",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Donut & Breakdown Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Category Breakdown",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        DonutChart(
                            categorySpendings = categorySpendings,
                            totalSpentPaisa = totalExpensePaisa,
                            useBanglaNumerals = useBanglaNumerals,
                            currencySymbol = currencySymbol
                        )
                    }
                }
            }

            // Recent Transactions Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Transactions",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "See all >",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { onSeeAllClick() }
                    )
                }
            }

            // Recent Items
            val recentTransactions = transactions.take(5)
            if (recentTransactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No transactions yet this month.\nTap (+) to log an expense or receive SMS.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                items(recentTransactions) { txn ->
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

        if (showAddSheet) {
            AddTransactionSheet(
                onDismiss = { showAddSheet = false },
                onSave = onAddTransaction,
                useBanglaNumerals = useBanglaNumerals,
                currencySymbol = currencySymbol
            )
        }
    }
}
