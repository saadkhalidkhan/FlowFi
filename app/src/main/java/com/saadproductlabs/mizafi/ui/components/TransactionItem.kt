package com.saadproductlabs.mizafi.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.saadproductlabs.mizafi.R
import com.saadproductlabs.mizafi.data.entity.TransactionEntity
import com.saadproductlabs.mizafi.data.entity.TransactionType
import com.saadproductlabs.mizafi.ui.theme.ExpenseRed
import com.saadproductlabs.mizafi.ui.theme.ExpenseRedContainer
import com.saadproductlabs.mizafi.ui.theme.IncomeGreen
import com.saadproductlabs.mizafi.ui.theme.IncomeGreenContainer
import com.saadproductlabs.mizafi.ui.util.categoryDisplayName
import com.saadproductlabs.mizafi.ui.util.formatCurrency

@Composable
fun TransactionItem(
    transaction: TransactionEntity,
    formattedDate: String? = null,
    onClick: () -> Unit
) {
    val isIncome = transaction.type == TransactionType.INCOME
    val typeLabel = stringResource(
        if (isIncome) R.string.label_income else R.string.label_expense
    )
    val categoryLabel = categoryDisplayName(transaction.category)
    val signedAmount = (if (isIncome) "+" else "-") + formatCurrency(transaction.amount)
    val accessibilityLabel = buildString {
        append("$typeLabel, $categoryLabel, $signedAmount")
        if (transaction.note.isNotEmpty()) append(", ${transaction.note}")
        if (formattedDate != null) append(", $formattedDate")
    }

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                contentDescription = accessibilityLabel
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                color = if (isIncome) IncomeGreenContainer else ExpenseRedContainer,
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isIncome) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                            contentDescription = typeLabel,
                            tint = if (isIncome) IncomeGreen else ExpenseRed,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            categoryLabel,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        if (transaction.note.isNotEmpty()) {
                            Text(
                                transaction.note,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Text(
                    text = signedAmount,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isIncome) IncomeGreen else ExpenseRed
                )
            }
            if (formattedDate != null) {
                Spacer(modifier = Modifier.size(6.dp))
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
