// Copyright (c) 2025 Gladstone Joy. Licensed under the MIT License.
package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.domain.model.Transaction
import com.example.domain.model.SavingsGoal
import com.example.domain.model.Debt
import com.example.domain.model.CategoryLimit

@Database(entities = [Transaction::class, SavingsGoal::class, Debt::class, CategoryLimit::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun savingsGoalDao(): SavingsGoalDao
    abstract fun debtDao(): DebtDao
    abstract fun categoryLimitDao(): com.example.data.local.CategoryLimitDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val factory = com.example.data.security.SecureDatabaseHelper.getOpenHelperFactory(context)

                val builder = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "budget_joy_database"
                )
                
                if (factory != null) {
                    builder.openHelperFactory(factory)
                }

                val instance = builder
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
