package com.example.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "category_limits")
data class CategoryLimit(
    @PrimaryKey val category: String, // e.g. "Courses", "Loisirs", etc.
    val limitAmount: Double,
    val period: String = "MONTHLY" // "mensuelle" as requested
)
