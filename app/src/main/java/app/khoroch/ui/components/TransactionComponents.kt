package app.khoroch.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.khoroch.core.common.MoneyFormatter
import app.khoroch.data.local.TransactionEntity
import app.khoroch.ui.theme.CategoryColors
import app.khoroch.ui.theme.LightExpense
import app.khoroch.ui.theme.LightIncome
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun getCategoryIcon(categoryId: String?): ImageVector {
    return when (categoryId) {
        "food" -> Icons.Default.Fastfood
        "groceries" -> Icons.Default.ShoppingCart
        "transport" -> Icons.Default.DirectionsCar
        "bills" -> Icons.Default.ReceiptLong
        "rent" -> Icons.Default.Home
        "shopping" -> Icons.Default.ShoppingBag
        "health" -> Icons.Default.LocalHospital
        "education" -> Icons.Default.School
        "transfer" -> Icons.Default.SwapHoriz
        "income" -> Icons.Default.AccountBalanceWallet
        else -> Icons.Default.MoreHoriz
    }
}

@Composable
fun TransactionRow(
    transaction: TransactionEntity,
    useBanglaNumerals: Boolean = false,
    currencySymbol: String = "Tk",
    onClick: () -> Unit = {}
) {
    val categoryId = transaction.categoryId ?: "other"
    val categoryColor = CategoryColors[categoryId] ?: Color(0xFF8A949B)
    val isExpense = transaction.direction == 0
    val isTransfer = transaction.direction == 2

    val timeFormat = SimpleDateFormat("h:mm a", Locale.US)
    val timeStr = timeFormat.format(Date(transaction.occurredAt))

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clickable { onClick() }
            .padding(horizontal = 16.dp)
    ) {
        // Leading 40dp Circular Icon
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(categoryColor.copy(alpha = 0.15f))
        ) {
            Icon(
                imageVector = getCategoryIcon(categoryId),
                contentDescription = null,
                tint = categoryColor,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Title and Provider/Time
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.counterparty,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Provider badge
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.padding(end = 6.dp)
                ) {
                    Text(
                        text = transaction.provider.uppercase(Locale.ROOT).take(6),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
                Text(
                    text = timeStr,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Signed Amount
        val amountColor = when {
            isTransfer -> MaterialTheme.colorScheme.onSurface
            isExpense -> LightExpense
            else -> LightIncome
        }

        val formattedAmount = if (isTransfer) {
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
            text = formattedAmount,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = amountColor
        )
    }
}

@Composable
fun AmountKeypad(
    currentAmountString: String,
    onDigitClick: (String) -> Unit,
    onBackspaceClick: () -> Unit,
    onDecimalClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf(".", "0", "DEL")
    )

    Column(modifier = modifier.fillMaxWidth()) {
        for (row in rows) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (key in row) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier
                            .size(width = 80.dp, height = 54.dp)
                            .clickable {
                                when (key) {
                                    "DEL" -> onBackspaceClick()
                                    "." -> onDecimalClick()
                                    else -> onDigitClick(key)
                                }
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (key == "DEL") {
                                Icon(
                                    imageVector = Icons.Default.Backspace,
                                    contentDescription = "Backspace",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            } else {
                                Text(
                                    text = key,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
