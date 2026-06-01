// Copyright (c) 2025 Gladstone Joy. Licensed under the MIT License.
package com.example.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "category_limits")
data class CategoryLimit(
    @PrimaryKey val category: String, // e.g. "Courses", "Loisirs", etc.
    val limitAmount: Double,
    val period: String = "MONTHLY" // "mensuelle" as requested
)
