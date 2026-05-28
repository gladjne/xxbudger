package com.example.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "debts")
data class Debt(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val totalAmount: Double,
    val reimbursedAmount: Double = 0.0,
    val dateTimestamp: Long? = null
) {
    val remainingAmount: Double
        get() = (totalAmount - reimbursedAmount).coerceAtLeast(0.0)

    val progressPercent: Double
        get() = if (totalAmount > 0) {
            ((reimbursedAmount / totalAmount) * 100.0).coerceIn(0.0, 100.0)
        } else {
            0.0
        }
}
