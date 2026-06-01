// Copyright (c) 2025 Gladstone Joy. Licensed under the MIT License.
package com.example.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "INCOME", "EXPENSE", "SAVING"
    val category: String, // E.g., "Salaire job étudiant", "Autre dépense" etc.
    val customCategory: String? = null, // The custom text entered if category is "Autre ..."
    val label: String, // Libellé
    val amount: Double, // Montant
    val dateTimestamp: Long, // Simple timestamp representing date
    val associatedGoalId: Int? = null, // Optional goal assignment to track savings goals
    val associatedDebtId: Int? = null // Optional debt assignment to track repayments
) {
    /**
     * Helper to get the displayable category name.
     * If there's a custom category, return it, otherwise return the base category.
     */
    val displayCategory: String
        get() = if (!customCategory.isNullOrBlank()) customCategory else category
}
