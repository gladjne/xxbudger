package com.example.domain.analytics

import com.example.domain.model.Transaction
import com.example.domain.model.TransactionType
import java.util.Calendar
import java.util.Locale

data class CategorizedExpense(
    val category: String,
    val totalAmount: Double,
    val percentage: Double // 0.0 to 100.0
)

data class MonthlySnapshot(
    val year: Int,
    val month: Int, // 0-indexed (0 = Jan, 11 = Dec)
    val monthLabel: String, // e.g., "Mai" or "Mai 26"
    val totalIncome: Double,
    val totalExpense: Double,
    val totalSaving: Double
)

data class SavingDetail(
    val totalSaving: Double,
    val savingRatio: Double, // 0.0 to 100.0
    val ratingMessage: String,
    val isGood: Boolean
)

data class AdvancedAnalysisResult(
    val totalIncomeMonth: Double,
    val totalExpenseMonth: Double,
    val totalSavingMonth: Double,
    val expenseIncomeRatio: Double, // 0.0 to 100.0
    val savingIncomeRatio: Double,  // 0.0 to 100.0
    val categorizedExpenses: List<CategorizedExpense>,
    val monthlyHistory: List<MonthlySnapshot>,
    val dominantCategory: String?,
    val dominantCategoryAmount: Double,
    val savingDetail: SavingDetail
)

object AdvancedFinancialAnalyzer {

    private val FRENCH_MONTH_NAMES = listOf(
        "Jan", "Fév", "Mar", "Avr", "Mai", "Jun",
        "Jul", "Aoû", "Sep", "Oct", "Nov", "Déc"
    )

    /**
     * Complete and robust analytic calculator.
     */
    fun analyze(allTransactions: List<Transaction>): AdvancedAnalysisResult {
        // 1. Month-Specific Calculations
        // To find the month specific, we filter transactions of the current calendar month.
        val calNow = Calendar.getInstance()
        val currentYear = calNow.get(Calendar.YEAR)
        val currentMonth = calNow.get(Calendar.MONTH)

        val monthTxs = allTransactions.filter {
            val calTx = Calendar.getInstance().apply { timeInMillis = it.dateTimestamp }
            calTx.get(Calendar.YEAR) == currentYear && calTx.get(Calendar.MONTH) == currentMonth
        }

        // If current month is empty but there are old operations, fall back to analyzing all transactions
        // so that the screen doesn't look empty and builds fine.
        val activeTxs = if (monthTxs.isNotEmpty()) monthTxs else allTransactions

        val totalIncome = activeTxs.filter { it.type == TransactionType.INCOME.name }.sumOf { it.amount }
        val totalExpense = activeTxs.filter { it.type == TransactionType.EXPENSE.name }.sumOf { it.amount }
        val totalSaving = activeTxs.filter { it.type == TransactionType.SAVING.name }.sumOf { it.amount }

        val expenseRatio = if (totalIncome > 0.0) (totalExpense / totalIncome) * 100.0 else 0.0
        val savingRatio = if (totalIncome > 0.0) (totalSaving / totalIncome) * 100.0 else 0.0

        // 2. Expenses Categorization
        val expenseTxs = activeTxs.filter { it.type == TransactionType.EXPENSE.name }
        val totalAllExpense = expenseTxs.sumOf { it.amount }

        val categorizedExpenses = expenseTxs
            .groupBy { it.displayCategory }
            .map { (cat, txs) ->
                val amount = txs.sumOf { it.amount }
                val pct = if (totalAllExpense > 0.0) (amount / totalAllExpense) * 100.0 else 0.0
                CategorizedExpense(category = cat, totalAmount = amount, percentage = pct)
            }
            .sortedByDescending { it.totalAmount }

        // 3. Dominant Category
        val dominant = categorizedExpenses.firstOrNull()
        val dominantCategory = dominant?.category
        val dominantAmount = dominant?.totalAmount ?: 0.0

        // 4. Monthly History (Aggregated evolution)
        // Let's analyze all transactions, partition by Calendar Year and Month, and extract values
        val historySnapshots = allTransactions
            .groupBy {
                val cal = Calendar.getInstance().apply { timeInMillis = it.dateTimestamp }
                Pair(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH))
            }
            .map { (key, txs) ->
                val year = key.first
                val month = key.second
                val monthStr = if (month in 0..11) FRENCH_MONTH_NAMES[month] else "M$month"
                val label = if (year != currentYear) "$monthStr ${year % 100}" else monthStr

                val inc = txs.filter { it.type == TransactionType.INCOME.name }.sumOf { it.amount }
                val exp = txs.filter { it.type == TransactionType.EXPENSE.name }.sumOf { it.amount }
                val sav = txs.filter { it.type == TransactionType.SAVING.name }.sumOf { it.amount }

                MonthlySnapshot(
                    year = year,
                    month = month,
                    monthLabel = label,
                    totalIncome = inc,
                    totalExpense = exp,
                    totalSaving = sav
                )
            }
            // Sort chronologically
            .sortedWith(compareBy<MonthlySnapshot> { it.year }.thenBy { it.month })
            .takeLast(6) // Take the last 6 months for a clean chart evolution

        // 5. Saving Detail Logic
        val isGoodSaving = savingRatio >= 10.0
        val ratingMessage = if (isGoodSaving) "Très bonne épargne" else "Tu peux améliorer ton épargne"
        val savingDetail = SavingDetail(
            totalSaving = totalSaving,
            savingRatio = savingRatio,
            ratingMessage = ratingMessage,
            isGood = isGoodSaving
        )

        return AdvancedAnalysisResult(
            totalIncomeMonth = totalIncome,
            totalExpenseMonth = totalExpense,
            totalSavingMonth = totalSaving,
            expenseIncomeRatio = expenseRatio,
            savingIncomeRatio = savingRatio,
            categorizedExpenses = categorizedExpenses,
            monthlyHistory = historySnapshots,
            dominantCategory = dominantCategory,
            dominantCategoryAmount = dominantAmount,
            savingDetail = savingDetail
        )
    }
}
