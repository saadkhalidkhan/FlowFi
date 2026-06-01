package com.example.flowfi.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.flowfi.domain.CategorySpend
import com.example.flowfi.ui.util.formatCurrency

@Composable
fun CategoryBarChart(
    breakdown: List<CategorySpend>,
    colors: List<Color>,
    modifier: Modifier = Modifier
) {
    if (breakdown.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        breakdown.forEachIndexed { index, item ->
            CategoryBarRow(
                category = item.category,
                amount = item.amount,
                percent = item.percent,
                barColor = colors[index % colors.size]
            )
        }
    }
}

@Composable
private fun CategoryBarRow(
    category: String,
    amount: Double,
    percent: Float,
    barColor: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(category, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(
                text = "${percent.toInt()}% · ${formatCurrency(amount)}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        val trackColor = MaterialTheme.colorScheme.surfaceVariant
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
        ) {
            val barHeight = size.height
            drawRoundRect(
                color = trackColor,
                size = Size(size.width, barHeight),
                cornerRadius = CornerRadius(barHeight / 2f)
            )
            val fillWidth = size.width * (percent / 100f).coerceIn(0f, 1f)
            if (fillWidth > 0f) {
                drawRoundRect(
                    color = barColor,
                    size = Size(fillWidth, barHeight),
                    cornerRadius = CornerRadius(barHeight / 2f)
                )
            }
        }
    }
}
