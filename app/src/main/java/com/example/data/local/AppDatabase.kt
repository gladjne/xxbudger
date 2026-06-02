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

                var instance = builder
                    .fallbackToDestructiveMigration()
                    .build()

                try {
                    // Force-trigger safe database opening/creation check
                    instance.openHelper.writableDatabase
                } catch (t: Throwable) {
                    android.util.Log.e("DATABASE_ROBUSTNESS", "Database opening generated errors. Corrupted, old, or mismatched key? Recreating cleanly...", t)
                    try {
                        instance.close()
                    } catch (closeEx: Throwable) {
                        closeEx.printStackTrace()
                    }
                    try {
                        val dbFile = context.applicationContext.getDatabasePath("budget_joy_database")
                        if (dbFile.exists()) {
                            dbFile.delete()
                        }
                        val shmFile = context.applicationContext.getDatabasePath("budget_joy_database-shm")
                        if (shmFile.exists()) {
                            shmFile.delete()
                        }
                        val walFile = context.applicationContext.getDatabasePath("budget_joy_database-wal")
                        if (walFile.exists()) {
                            walFile.delete()
                        }
                    } catch (delEx: Throwable) {
                        delEx.printStackTrace()
                    }
                    // Rebuild a fresh, empty database instance
                    instance = builder
                        .fallbackToDestructiveMigration()
                        .build()
                }

                INSTANCE = instance
                instance
            }
        }
    }
}
