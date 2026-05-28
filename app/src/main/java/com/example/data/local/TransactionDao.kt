package com.example.data.local

import androidx.room.*
import com.example.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY dateTimestamp DESC, id DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction): Long

    @Query("SELECT * FROM transactions")
    suspend fun getLocalTransactionsList(): List<Transaction>

    @Query("SELECT * FROM transactions WHERE associatedDebtId = :debtId")
    suspend fun getTransactionsForDebt(debtId: Int): List<Transaction>

    @Query("SELECT COUNT(*) FROM transactions WHERE dateTimestamp >= :startOfDay")
    suspend fun getTransactionsCountForToday(startOfDay: Long): Int

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Int)

    @Query("DELETE FROM transactions")
    suspend fun clearAllTransactions()
}
