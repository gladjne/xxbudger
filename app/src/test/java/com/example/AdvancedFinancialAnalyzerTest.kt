package com.example

import com.example.domain.analytics.AdvancedFinancialAnalyzer
import com.example.domain.analytics.MonthlySnapshot
import com.example.domain.model.Transaction
import com.example.domain.model.TransactionType
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar

class AdvancedFinancialAnalyzerTest {

    private fun createTxAtTime(type: TransactionType, amount: Double, category: String, timestamp: Long): Transaction {
        return Transaction(
            id = 0,
            type = type.name,
            category = category,
            label = "Transaction test",
            amount = amount,
            dateTimestamp = timestamp
        )
    }

    @Test
    fun testEmptyTransactions_returnsZeroAndEmptyStats() {
        val result = AdvancedFinancialAnalyzer.analyze(emptyList())

        assertEquals(0.0, result.totalIncomeMonth, 0.001)
        assertEquals(0.0, result.totalExpenseMonth, 0.001)
        assertEquals(0.0, result.totalSavingMonth, 0.001)
        assertTrue(result.categorizedExpenses.isEmpty())
        assertTrue(result.monthlyHistory.isEmpty())
        assertNull(result.dominantCategory)
        assertEquals(0.0, result.dominantCategoryAmount, 0.01)
        assertFalse(result.savingDetail.isGood)
        assertEquals("Tu peux améliorer ton épargne", result.savingDetail.ratingMessage)
    }

    @Test
    fun testCategorizedExpensesGrouping() {
        // Set all in the current calendar month
        val now = System.currentTimeMillis()
        val txs = listOf(
            createTxAtTime(TransactionType.EXPENSE, 500.0, "Logement", now),
            createTxAtTime(TransactionType.EXPENSE, 200.0, "Courses", now),
            createTxAtTime(TransactionType.EXPENSE, 300.0, "Logement", now),
            createTxAtTime(TransactionType.INCOME, 2000.0, "Salaire", now)
        )

        val result = AdvancedFinancialAnalyzer.analyze(txs)

        assertEquals("Logement", result.dominantCategory)
        assertEquals(800.0, result.dominantCategoryAmount, 0.01)
        
        // Expense sum = 1000.0. Logement is 800.0 (80%), Courses is 200.0 (20%)
        val logementGroup = result.categorizedExpenses.first { it.category == "Logement" }
        assertEquals(80.0, logementGroup.percentage, 0.1)

        val coursesGroup = result.categorizedExpenses.first { it.category == "Courses" }
        assertEquals(20.0, coursesGroup.percentage, 0.1)
    }

    @Test
    fun testSavingQualityDetails() {
        val now = System.currentTimeMillis()
        
        // Good saving >= 10%
        val txsGood = listOf(
            createTxAtTime(TransactionType.INCOME, 1000.0, "Bourse", now),
            createTxAtTime(TransactionType.SAVING, 120.0, "Voyage", now) // 12%
        )
        val resultGood = AdvancedFinancialAnalyzer.analyze(txsGood)
        assertTrue(resultGood.savingDetail.isGood)
        assertEquals("Très bonne épargne", resultGood.savingDetail.ratingMessage)

        // Poor saving < 10%
        val txsPoor = listOf(
            createTxAtTime(TransactionType.INCOME, 1000.0, "Bourse", now),
            createTxAtTime(TransactionType.SAVING, 50.0, "Voyage", now) // 5%
        )
        val resultPoor = AdvancedFinancialAnalyzer.analyze(txsPoor)
        assertFalse(resultPoor.savingDetail.isGood)
        assertEquals("Tu peux améliorer ton épargne", resultPoor.savingDetail.ratingMessage)
    }

    @Test
    fun testMonthlyEvolutionHistory() {
        val cal1 = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2026)
            set(Calendar.MONTH, Calendar.JANUARY)
            set(Calendar.DAY_OF_MONTH, 15)
        }
        val cal2 = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2026)
            set(Calendar.MONTH, Calendar.FEBRUARY)
            set(Calendar.DAY_OF_MONTH, 20)
        }

        val txs = listOf(
            createTxAtTime(TransactionType.INCOME, 1500.0, "Salaire", cal1.timeInMillis),
            createTxAtTime(TransactionType.EXPENSE, 600.0, "Loisirs", cal1.timeInMillis),
            createTxAtTime(TransactionType.INCOME, 1800.0, "Salaire", cal2.timeInMillis),
            createTxAtTime(TransactionType.EXPENSE, 800.0, "Courses", cal2.timeInMillis),
            createTxAtTime(TransactionType.SAVING, 200.0, "Urgence", cal2.timeInMillis)
        )

        val result = AdvancedFinancialAnalyzer.analyze(txs)
        
        // Should have 2 months grouped
        assertEquals(2, result.monthlyHistory.size)

        val janSnap = result.monthlyHistory.first { it.year == 2026 && it.month == Calendar.JANUARY }
        assertEquals(1500.0, janSnap.totalIncome, 0.01)
        assertEquals(600.0, janSnap.totalExpense, 0.01)
        assertEquals(0.0, janSnap.totalSaving, 0.01)

        val febSnap = result.monthlyHistory.first { it.year == 2026 && it.month == Calendar.FEBRUARY }
        assertEquals(1800.0, febSnap.totalIncome, 0.01)
        assertEquals(800.0, febSnap.totalExpense, 0.01)
        assertEquals(200.0, febSnap.totalSaving, 0.01)
    }
}
