package com.example.domain.repository

import com.example.domain.model.Transaction
import com.example.domain.model.SavingsGoal
import com.example.domain.model.Debt
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getAllTransactions(): Flow<List<Transaction>>
    suspend fun insertTransaction(transaction: Transaction)
    suspend fun deleteTransaction(transaction: Transaction)

    fun getAllGoals(): Flow<List<SavingsGoal>>
    suspend fun insertGoal(goal: SavingsGoal): Long
    suspend fun deleteGoal(goal: SavingsGoal)
    suspend fun deleteGoalById(id: Int)

    fun getAllDebts(): Flow<List<Debt>>
    suspend fun insertDebt(debt: Debt)
    suspend fun deleteDebt(debt: Debt)
    suspend fun deleteDebtById(id: Int)

    fun getAllCategoryLimits(): Flow<List<com.example.domain.model.CategoryLimit>>
    suspend fun insertCategoryLimit(limit: com.example.domain.model.CategoryLimit)
    suspend fun deleteCategoryLimit(limit: com.example.domain.model.CategoryLimit)
    suspend fun clearAllCategoryLimits()

    suspend fun syncWithCloud()
    suspend fun clearAllLocalData()
    suspend fun clearAllTransactions()
    suspend fun clearAllGoals()
    suspend fun clearAllDebts()
}
