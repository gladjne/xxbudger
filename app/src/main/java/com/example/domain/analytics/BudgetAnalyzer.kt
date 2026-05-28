package com.example.domain.analytics

import com.example.domain.model.Transaction
import com.example.domain.model.TransactionType
import java.util.Calendar

enum class BudgetSituation {
    NO_DATA,
    DEFICIT,
    HIGH_EXPENSE,
    SAVING_EXCELLENT,
    NO_SAVING,
    CONTROLLED
}

data class BudgetAnalysisResult(
    val totalIncomeMonth: Double,
    val totalExpenseMonth: Double,
    val totalSavingMonth: Double,
    val expenseIncomeRatio: Double, // ratio dépenses / revenus
    val savingIncomeRatio: Double,  // ratio épargne / revenus
    val situation: BudgetSituation,
    val adviceMessage: String,
    val adviceDescription: String,
    val adviceColorType: AdviceColorType // Color category to align with Bento Design palette
)

enum class AdviceColorType {
    INFO,    // Indigo / Muted Blue (Case 1)
    ERROR,   // Warm Rose / Red (Case 2)
    WARNING, // Soft Amber / Sunset (Case 4, 6)
    SUCCESS  // Soft Forest Green (Case 3, 5)
}

object BudgetAnalyzer {

    /**
     * Determines whether a given timestamp belongs to the current calendar month.
     */
    fun isTimestampInCurrentMonth(timestamp: Long): Boolean {
        val calTx = Calendar.getInstance().apply { timeInMillis = timestamp }
        val calNow = Calendar.getInstance() // System current clock
        return calTx.get(Calendar.YEAR) == calNow.get(Calendar.YEAR) &&
               calTx.get(Calendar.MONTH) == calNow.get(Calendar.MONTH)
    }

    /**
     * Filters transactions matching the current month.
     */
    fun filterCurrentMonthTransactions(transactions: List<Transaction>): List<Transaction> {
        return transactions.filter { isTimestampInCurrentMonth(it.dateTimestamp) }
    }

    /**
     * Core function to automatically analyze transactions list and compute 5 key metrics.
     */
    fun analyze(allTransactions: List<Transaction>): BudgetAnalysisResult {
        // Find transactions of the current calendar month
        val monthTxs = filterCurrentMonthTransactions(allTransactions)

        // Calculate totals for the month
        val totalIncome = monthTxs.filter { it.type == TransactionType.INCOME.name }.sumOf { it.amount }
        val totalExpense = monthTxs.filter { it.type == TransactionType.EXPENSE.name }.sumOf { it.amount }
        val totalSaving = monthTxs.filter { it.type == TransactionType.SAVING.name }.sumOf { it.amount }

        // Calculate ratios
        val expenseRatio = if (totalIncome > 0.0) totalExpense / totalIncome else 0.0
        val savingRatio = if (totalIncome > 0.0) totalSaving / totalIncome else 0.0

        // Handle case 1: If there is no transaction of any type
        if (allTransactions.isEmpty()) {
            return BudgetAnalysisResult(
                totalIncomeMonth = 0.0,
                totalExpenseMonth = 0.0,
                totalSavingMonth = 0.0,
                expenseIncomeRatio = 0.0,
                savingIncomeRatio = 0.0,
                situation = BudgetSituation.NO_DATA,
                adviceMessage = "Commence à ajouter tes opérations pour suivre ton budget.",
                adviceDescription = "Ton coach budget Joy se tient prêt à analyser tes habitudes !",
                adviceColorType = AdviceColorType.INFO
            )
        }

        // What if there is no transactions in the *current calendar month* specifically, but there are old ones?
        // Let's degrade gracefully so that users always have analysis based on all transactions or show a nice message.
        val activeTxs = if (monthTxs.isEmpty()) allTransactions else monthTxs
        val targetIncome = if (monthTxs.isEmpty()) allTransactions.filter { it.type == TransactionType.INCOME.name }.sumOf { it.amount } else totalIncome
        val targetExpense = if (monthTxs.isEmpty()) allTransactions.filter { it.type == TransactionType.EXPENSE.name }.sumOf { it.amount } else totalExpense
        val targetSaving = if (monthTxs.isEmpty()) allTransactions.filter { it.type == TransactionType.SAVING.name }.sumOf { it.amount } else totalSaving

        val currentExpenseRatio = if (targetIncome > 0.0) targetExpense / targetIncome else 0.0
        val currentSavingRatio = if (targetIncome > 0.0) targetSaving / targetIncome else 0.0

        // Situation Check prioritization
        val situation: BudgetSituation
        val message: String
        val colorType: AdviceColorType
        val description: String

        when {
            targetExpense > targetIncome -> {
                situation = BudgetSituation.DEFICIT
                message = "Attention, tes dépenses dépassent tes revenus."
                colorType = AdviceColorType.ERROR
                description = "Tu es en déficit de ${com.example.presentation.ui.UiUtils.formatCurrency(targetExpense - targetIncome)}"
            }
            targetExpense > 0.7 * targetIncome && targetIncome > 0.0 -> {
                situation = BudgetSituation.HIGH_EXPENSE
                message = "Tes dépenses sont élevées, vérifie les postes importants."
                colorType = AdviceColorType.WARNING
                description = "Tes dépenses absorbent ${(currentExpenseRatio * 100).toInt()}% de tes entrées."
            }
            targetSaving >= 0.1 * targetIncome && targetIncome > 0.0 -> {
                situation = BudgetSituation.SAVING_EXCELLENT
                message = "Excellent, tu épargnes une partie de tes revenus."
                colorType = AdviceColorType.SUCCESS
                description = "Tu mets de côté ${(currentSavingRatio * 100).toInt()}% de tes sous ce mois-ci !"
            }
            targetSaving == 0.0 && targetIncome > 0.0 -> {
                situation = BudgetSituation.NO_SAVING
                message = "Pense à mettre de côté même un petit montant."
                colorType = AdviceColorType.WARNING
                description = "Épargner aide à financer tes futurs projets étudiants."
            }
            else -> {
                // Expenses under control and saving might be positive but under 10%, or no income but positive/balanced
                situation = BudgetSituation.CONTROLLED
                message = "Tes dépenses sont bien sous contrôle."
                colorType = AdviceColorType.SUCCESS
                description = "Bonne gestion ! Continue de suivre tes comptes régulièrement."
            }
        }

        return BudgetAnalysisResult(
            totalIncomeMonth = totalIncome,
            totalExpenseMonth = totalExpense,
            totalSavingMonth = totalSaving,
            expenseIncomeRatio = expenseRatio,
            savingIncomeRatio = savingRatio,
            situation = situation,
            adviceMessage = message,
            adviceDescription = description,
            adviceColorType = colorType
        )
    }
}
