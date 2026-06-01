// Copyright (c) 2025 Gladstone Joy. Licensed under the MIT License.
package com.example.data.local

import androidx.room.*
import com.example.domain.model.SavingsGoal
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingsGoalDao {
    @Query("SELECT * FROM savings_goals ORDER BY id DESC")
    fun getAllGoals(): Flow<List<SavingsGoal>>

    @Query("SELECT * FROM savings_goals WHERE id = :id LIMIT 1")
    suspend fun getGoalById(id: Int): SavingsGoal?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: SavingsGoal): Long

    @Query("SELECT * FROM savings_goals")
    suspend fun getLocalGoalsList(): List<SavingsGoal>

    @Delete
    suspend fun deleteGoal(goal: SavingsGoal)

    @Query("DELETE FROM savings_goals WHERE id = :id")
    suspend fun deleteGoalById(id: Int)

    @Query("DELETE FROM savings_goals")
    suspend fun clearAllGoals()
}
