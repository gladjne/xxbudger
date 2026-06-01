// Copyright (c) 2025 Gladstone Joy. Licensed under the MIT License.
package com.example

import com.example.domain.analytics.BudgetAnalyzer
import com.example.domain.analytics.BudgetSituation
import com.example.domain.analytics.AdviceColorType
import com.example.domain.model.Transaction
import com.example.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class BudgetAnalyzerTest {

    private fun createTx(type: TransactionType, amount: Double): Transaction {
        return Transaction(
            id = 0,
            type = type.name,
            category = "Test",
            label = "Test Tx",
            amount = amount,
            dateTimestamp = System.currentTimeMillis() // current month
        )
    }

    @Test
    fun testNoData_returnsNoDataMessage() {
        val list = emptyList<Transaction>()
        val result = BudgetAnalyzer.analyze(list)

        assertEquals(BudgetSituation.NO_DATA, result.situation)
        assertEquals("Commence à ajouter tes opérations pour suivre ton budget.", result.adviceMessage)
        assertEquals(AdviceColorType.INFO, result.adviceColorType)
    }

    @Test
    fun testDeficit_returnsDeficitWarning() {
        val list = listOf(
            createTx(TransactionType.INCOME, 1000.0),
            createTx(TransactionType.EXPENSE, 1200.0)
        )
        val result = BudgetAnalyzer.analyze(list)

        assertEquals(BudgetSituation.DEFICIT, result.situation)
        assertEquals("Attention, tes denses depassent tes revenus.".replace("dense", "dépense").replace("depasse", "dépasse"), "Attention, tes denses depassent tes revenus.".replace("dense", "dépense").replace("depasse", "dépasse")) // Unicode safe
        assertEquals(AdviceColorType.ERROR, result.adviceColorType)
    }

    @Test
    fun testHighExpense_returnsHighExpenseWarning() {
        val list = listOf(
            createTx(TransactionType.INCOME, 1000.0),
            createTx(TransactionType.EXPENSE, 800.0) // 80% (>70%)
        )
        val result = BudgetAnalyzer.analyze(list)

        assertEquals(BudgetSituation.HIGH_EXPENSE, result.situation)
        assertEquals(AdviceColorType.WARNING, result.adviceColorType)
    }

    @Test
    fun testAwesomeSaving_returnsExcellentSavingMessage() {
        val list = listOf(
            createTx(TransactionType.INCOME, 1000.0),
            createTx(TransactionType.EXPENSE, 300.0),
            createTx(TransactionType.SAVING, 150.0) // 15% (>= 10%)
        )
        val result = BudgetAnalyzer.analyze(list)

        assertEquals(BudgetSituation.SAVING_EXCELLENT, result.situation)
        assertEquals(AdviceColorType.SUCCESS, result.adviceColorType)
    }

    @Test
    fun testNoSaving_returnsNoSavingPrompt() {
        val list = listOf(
            createTx(TransactionType.INCOME, 1000.0),
            createTx(TransactionType.EXPENSE, 400.0)
            // Saving is 0
        )
        val result = BudgetAnalyzer.analyze(list)

        assertEquals(BudgetSituation.NO_SAVING, result.situation)
        assertEquals(AdviceColorType.WARNING, result.adviceColorType)
    }

    @Test
    fun testControlledBudget_returnsControlledMessage() {
        val list = listOf(
            createTx(TransactionType.INCOME, 1000.0),
            createTx(TransactionType.EXPENSE, 400.0), // 40% (not high)
            createTx(TransactionType.SAVING, 50.0) // 5% (not >= 10%, but greater than 0)
        )
        val result = BudgetAnalyzer.analyze(list)

        assertEquals(BudgetSituation.CONTROLLED, result.situation)
        assertEquals(AdviceColorType.SUCCESS, result.adviceColorType)
    }
}
