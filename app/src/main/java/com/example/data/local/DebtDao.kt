// Copyright (c) 2025 Gladstone Joy. Licensed under the MIT License.
package com.example.data.local

import androidx.room.*
import com.example.domain.model.Debt
import kotlinx.coroutines.flow.Flow

@Dao
interface DebtDao {
    @Query("SELECT * FROM debts ORDER BY id DESC")
    fun getAllDebts(): Flow<List<Debt>>

    @Query("SELECT * FROM debts WHERE id = :id LIMIT 1")
    suspend fun getDebtById(id: Int): Debt?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebt(debt: Debt): Long

    @Query("SELECT * FROM debts")
    suspend fun getLocalDebtsList(): List<Debt>

    @Delete
    suspend fun deleteDebt(debt: Debt)

    @Query("DELETE FROM debts WHERE id = :id")
    suspend fun deleteDebtById(id: Int)

    @Query("DELETE FROM debts")
    suspend fun clearAllDebts()
}
