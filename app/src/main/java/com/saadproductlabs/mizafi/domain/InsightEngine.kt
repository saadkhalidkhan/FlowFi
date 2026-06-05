package com.saadproductlabs.mizafi.domain

import com.saadproductlabs.mizafi.data.entity.TransactionEntity
import com.saadproductlabs.mizafi.data.entity.TransactionType
import com.saadproductlabs.mizafi.util.DateUtils
import kotlin.math.roundToInt

enum class InsightTone {
    POSITIVE,
    NEUTRAL,
    WARNING
}

data class BehavioralInsight(
    val message: String,
    val tone: InsightTone
)

object InsightEngine {
    private const val FOOD_CATEGORY = "Food"

    fun generate(allTransactions: List<TransactionEntity>): List<BehavioralInsight> {
        if (allTransactions.isEmpty()) {
            return listOf(
                BehavioralInsight(
                    message = "Add your first transaction to unlock personalized guidance.",
                    tone = InsightTone.NEUTRAL
                )
            )
        }

        val insights = mutableListOf<BehavioralInsight>()
        val monthRange = DateUtils.currentMonthRangeMillis()
        val monthly = allTransactions.filter { it.date in monthRange }
        val monthlyExpenses = monthly.filter { it.type == TransactionType.EXPENSE }
        val monthlyIncome = monthly.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val monthlyExpenseTotal = monthlyExpenses.sumOf { it.amount }

        foodWeekOverWeekInsight(allTransactions)?.let { insights.add(it) }
        spendingVsAverageInsight(allTransactions, monthlyExpenseTotal)?.let { insights.add(it) }
        savingsRateInsight(allTransactions, monthlyIncome, monthlyExpenseTotal)?.let { insights.add(it) }

        if (monthly.isEmpty()) {
            insights += BehavioralInsight(
                message = "No activity this month yet. Log spending to stay on track.",
                tone = InsightTone.NEUTRAL
            )
        } else {
            if (monthlyIncome == 0.0 && monthlyExpenseTotal > 0) {
                insights += BehavioralInsight(
                    message = "No income recorded this month — balance expenses with earnings.",
                    tone = InsightTone.WARNING
                )
            }
            if (monthlyIncome > 0 && monthlyExpenseTotal > monthlyIncome) {
                insights += BehavioralInsight(
                    message = "Expenses are outpacing income this month. Review your top categories.",
                    tone = InsightTone.WARNING
                )
            }
            val savingsRate = (monthlyIncome - monthlyExpenseTotal) / monthlyIncome
            if (monthlyIncome > 0 && savingsRate >= 0.20) {
                insights += BehavioralInsight(
                    message = "Strong month — you're saving over 20% of your income.",
                    tone = InsightTone.POSITIVE
                )
            }
        }

        return insights.distinctBy { it.message }.take(5)
    }

    private fun foodWeekOverWeekInsight(all: List<TransactionEntity>): BehavioralInsight? {
        val thisWeek = DateUtils.weekRangeMillis(weeksAgo = 0)
        val lastWeek = DateUtils.weekRangeMillis(weeksAgo = 1)

        fun foodSpend(range: LongRange) = all
            .filter {
                it.type == TransactionType.EXPENSE &&
                    it.category.equals(FOOD_CATEGORY, ignoreCase = true) &&
                    it.date in range
            }
            .sumOf { it.amount }

        val current = foodSpend(thisWeek)
        val previous = foodSpend(lastWeek)
        if (previous <= 0 || current <= previous) return null

        val increasePercent = (((current - previous) / previous) * 100).roundToInt()
        if (increasePercent < 5) return null

        return BehavioralInsight(
            message = "Food spending increased $increasePercent% this week",
            tone = InsightTone.WARNING
        )
    }

    private fun spendingVsAverageInsight(
        all: List<TransactionEntity>,
        currentMonthExpenses: Double
    ): BehavioralInsight? {
        if (currentMonthExpenses <= 0) return null

        val priorTotals = (1..3).map { offset ->
            val range = DateUtils.monthRangeMonthsAgo(offset)
            all.filter { it.type == TransactionType.EXPENSE && it.date in range }
                .sumOf { it.amount }
        }.filter { it > 0 }

        if (priorTotals.isEmpty()) return null

        val average = priorTotals.average()
        if (currentMonthExpenses <= average * 1.05) return null

        return BehavioralInsight(
            message = "You are spending more than your monthly average",
            tone = InsightTone.WARNING
        )
    }

    private fun savingsRateInsight(
        all: List<TransactionEntity>,
        income: Double,
        expenses: Double
    ): BehavioralInsight? {
        if (income <= 0) return null

        val thisRate = (income - expenses) / income
        val lastMonthRange = DateUtils.previousMonthRangeMillis()
        val lastMonth = all.filter { it.date in lastMonthRange }
        val lastIncome = lastMonth.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val lastExpenses = lastMonth.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        if (lastIncome <= 0) return null

        val lastRate = (lastIncome - lastExpenses) / lastIncome
        if (thisRate <= lastRate + 0.02) return null

        val improvement = ((thisRate - lastRate) * 100).roundToInt().coerceAtLeast(1)
        return BehavioralInsight(
            message = "Your savings rate improved by $improvement% compared to last month",
            tone = InsightTone.POSITIVE
        )
    }
}
