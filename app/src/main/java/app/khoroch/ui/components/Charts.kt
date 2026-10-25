package app.khoroch.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.khoroch.core.common.MoneyFormatter
import app.khoroch.data.local.CategorySpending
import app.khoroch.ui.theme.CategoryColors

data class DonutSlice(
    val categoryId: String,
    val name: String,
    val paisa: Long,
    val color: Color
)

@Composable
fun DonutChart(
    categorySpendings: List<CategorySpending>,
    totalSpentPaisa: Long,
    useBanglaNumerals: Boolean = false,
    currencySymbol: String = "Tk",
    modifier: Modifier = Modifier
) {
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(categorySpendings) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(1f, animationSpec = tween(durationMillis = 600))
    }

    val slices = remember(categorySpendings, totalSpentPaisa) {
        if (totalSpentPaisa <= 0) return@remember emptyList()
        val sorted = categorySpendings.sortedByDescending { it.totalPaisa }
        val top = sorted.take(5)
        val remainingPaisa = sorted.drop(5).sumOf { it.totalPaisa }

        val list = top.map { item ->
            val catId = item.categoryId ?: "other"
            DonutSlice(
                categoryId = catId,
                name = catId.replaceFirstChar { it.uppercase() },
                paisa = item.totalPaisa,
                color = CategoryColors[catId] ?: Color(0xFF8A949B)
            )
        }.toMutableList()

        if (remainingPaisa > 0) {
            list.add(
                DonutSlice(
                    categoryId = "other",
                    name = "Other",
                    paisa = remainingPaisa,
                    color = CategoryColors["other"] ?: Color(0xFF8A949B)
                )
            )
        }
        list
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(160.dp)
    ) {
        Canvas(modifier = Modifier.size(160.dp)) {
            val strokeWidth = 20.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val radius = diameter / 2f
            val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
            val arcSize = Size(diameter, diameter)

            if (slices.isEmpty()) {
                drawCircle(
                    color = Color.LightGray.copy(alpha = 0.3f),
                    radius = radius,
                    style = Stroke(width = strokeWidth)
                )
                return@Canvas
            }

            var startAngle = -90f
            val gapAngle = 2f // 2 degree gap

            for (slice in slices) {
                val sweepRaw = (slice.paisa.toFloat() / totalSpentPaisa.toFloat()) * 360f
                val sweep = (sweepRaw - gapAngle).coerceAtLeast(0.5f) * animationProgress.value

                drawArc(
                    color = slice.color,
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                )

                startAngle += sweepRaw
            }
        }

        // Center Total Text
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Total",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = MoneyFormatter.formatPaisa(
                    paisa = totalSpentPaisa,
                    useBanglaNumerals = useBanglaNumerals,
                    includeDecimals = false,
                    currencySymbol = currencySymbol
                ),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun DailyBarChart(
    dailySpendings: List<Long>, // List of amounts in paisa for each day in month (1 to 28..31)
    currentDayIndex: Int, // 0-based index of today
    averagePaisa: Long,
    useBanglaNumerals: Boolean = false,
    modifier: Modifier = Modifier
) {
    if (dailySpendings.isEmpty()) return

    val maxDailyPaisa = (dailySpendings.maxOrNull() ?: 1L).coerceAtLeast(100L)
    val primaryColor = MaterialTheme.colorScheme.primary
    val variantColor = MaterialTheme.colorScheme.primaryContainer
    val avgLineColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(80.dp)) {
                val width = size.width
                val height = size.height
                val count = dailySpendings.size
                val barWidth = (width / (count * 1.5f)).coerceIn(4f, 16f)
                val spacing = (width - (count * barWidth)) / (count + 1)

                // Average line
                if (averagePaisa > 0 && maxDailyPaisa > 0) {
                    val avgY = height - (height * (averagePaisa.toFloat() / maxDailyPaisa.toFloat()))
                    drawLine(
                        color = avgLineColor,
                        start = Offset(0f, avgY),
                        end = Offset(width, avgY),
                        strokeWidth = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                }

                // Bars
                for (i in dailySpendings.indices) {
                    val paisa = dailySpendings[i]
                    val barHeight = ((paisa.toFloat() / maxDailyPaisa.toFloat()) * height).coerceAtLeast(4f)
                    val x = spacing + i * (barWidth + spacing)
                    val y = height - barHeight

                    val color = if (i == currentDayIndex) primaryColor else variantColor
                    drawRoundRect(
                        color = color,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight)
                    )
                }
            }
        }
    }
}
