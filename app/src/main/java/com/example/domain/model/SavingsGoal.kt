// Copyright (c) 2025 Gladstone Joy. Licensed under the MIT License.
package com.example.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "savings_goals")
data class SavingsGoal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val targetAmount: Double,
    val initialAmount: Double = 0.0, // Initial user-inputted balance
    val targetDateTimestamp: Long? = null // Optional target finish date
)
