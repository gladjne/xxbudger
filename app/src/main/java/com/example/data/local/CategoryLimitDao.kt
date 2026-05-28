package com.example.data.local

import androidx.room.*
import com.example.domain.model.CategoryLimit
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryLimitDao {
    @Query("SELECT * FROM category_limits")
    fun getAllCategoryLimits(): Flow<List<CategoryLimit>>

    @Query("SELECT * FROM category_limits")
    suspend fun getLocalCategoryLimitsList(): List<CategoryLimit>

    @Query("SELECT * FROM category_limits WHERE category = :category")
    suspend fun getCategoryLimit(category: String): CategoryLimit?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategoryLimit(limit: CategoryLimit)

    @Delete
    suspend fun deleteCategoryLimit(limit: CategoryLimit)

    @Query("DELETE FROM category_limits")
    suspend fun clearAllCategoryLimits()
}
