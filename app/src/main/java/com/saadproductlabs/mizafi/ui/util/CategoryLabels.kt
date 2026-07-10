package com.saadproductlabs.mizafi.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.saadproductlabs.mizafi.R

/**
 * Maps stable category values stored in Room to localized display labels.
 * Unknown legacy values are preserved verbatim.
 */
@Composable
fun categoryDisplayName(category: String): String = when (category) {
    "Food" -> stringResource(R.string.category_food)
    "Transport" -> stringResource(R.string.category_transport)
    "Bills" -> stringResource(R.string.category_bills)
    "Shopping" -> stringResource(R.string.category_shopping)
    "Salary" -> stringResource(R.string.category_salary)
    "Other" -> stringResource(R.string.category_other)
    else -> category
}
