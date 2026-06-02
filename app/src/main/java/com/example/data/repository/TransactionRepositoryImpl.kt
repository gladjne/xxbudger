// Copyright (c) 2025 Gladstone Joy. Licensed under the MIT License.
package com.example.data.repository

import com.example.data.security.SafeLog as Log
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.example.data.local.TransactionDao
import com.example.data.local.SavingsGoalDao
import com.example.data.remote.FirestoreGoalRemoteDataSource
import com.example.data.remote.FirestoreTransactionRemoteDataSource
import com.example.data.sync.SyncManager
import com.example.data.sync.SyncState
import com.example.domain.model.Transaction
import com.example.domain.model.SavingsGoal
import com.example.domain.model.Debt
import com.example.domain.repository.AuthRepository
import com.example.domain.repository.TransactionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class TransactionRepositoryImpl(
    private val transactionDao: TransactionDao,
    private val savingsGoalDao: SavingsGoalDao,
    private val debtDao: com.example.data.local.DebtDao,
    private val categoryLimitDao: com.example.data.local.CategoryLimitDao,
    private val authRepository: AuthRepository,
    private val context: android.content.Context
) : TransactionRepository {

    private val tag = "TransactionRepository"
    private val firestoreTxDataSource = FirestoreTransactionRemoteDataSource()
    private val firestoreGoalDataSource = FirestoreGoalRemoteDataSource()
    private val firestoreDebtDataSource = com.example.data.remote.FirestoreDebtRemoteDataSource()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val sharedPrefs = com.example.data.security.SecureStorageManager.getEncryptedSharedPreferences(context)
    private val connectivityManager = context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    init {
        // Automatically synchronize when the active user changes
        coroutineScope.launch {
            authRepository.currentUser.collect { user ->
                if (user != null) {
                    Log.d(tag, "Auth session detected. Restoring cloud synchronization...")
                    syncWithCloud()
                } else {
                    Log.d(tag, "No active user. Clearing all cached user data.")
                    clearAllLocalData()
                }
            }
        }

        // Auto sync when network is restored
        try {
            val networkRequest = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            connectivityManager?.registerNetworkCallback(networkRequest, object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    Log.d(tag, "Internet is back online. Syncing data...")
                    coroutineScope.launch {
                        if (getUserId() != null) {
                            syncWithCloud()
                        }
                    }
                }
                override fun onLost(network: Network) {
                    Log.d(tag, "Network connection lost.")
                }
            })
        } catch (e: Exception) {
            Log.e(tag, "Failed to register network callback service", e)
        }
    }

    private fun isCurrentlyOnline(): Boolean {
        return try {
            val activeNetwork = connectivityManager?.activeNetwork ?: return false
            val caps = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (e: Exception) {
            try {
                @Suppress("DEPRECATION")
                connectivityManager?.activeNetworkInfo?.isConnected == true
            } catch (ex: Exception) {
                false
            }
        }
    }

    private fun getUserId(): String? {
        return authRepository.currentUser.value?.uid
    }

    override fun getAllTransactions(): Flow<List<Transaction>> {
        return transactionDao.getAllTransactions()
    }

    override suspend fun insertTransaction(transaction: Transaction) {
        // Step 1: Write locally to Room and retrieve the actual local primary key ID
        val localId = transactionDao.insertTransaction(transaction)
        val finalTransaction = if (transaction.id == 0) {
            transaction.copy(id = localId.toInt())
        } else {
            transaction
        }

        // Recalculate debt if linked
        if (finalTransaction.associatedDebtId != null) {
            recalculateDebtReimbursement(finalTransaction.associatedDebtId)
        }

        // Step 2: Write asynchronously to Firestore if user session exists
        val userId = getUserId()
        if (userId != null) {
            coroutineScope.launch {
                SyncManager.updateState(SyncState.SYNCING)
                try {
                    firestoreTxDataSource.saveTransaction(userId, finalTransaction)
                    SyncManager.updateState(SyncState.SYNCED)
                } catch (e: Exception) {
                    Log.e(tag, "Failed to upload transaction to Firestore", e)
                    SyncManager.updateState(SyncState.OFFLINE)
                }
            }
        }
    }

    override suspend fun deleteTransaction(transaction: Transaction) {
        // Step 1: Remove locally
        transactionDao.deleteTransaction(transaction)

        // Recalculate debt if linked
        if (transaction.associatedDebtId != null) {
            recalculateDebtReimbursement(transaction.associatedDebtId)
        }

        // Step 2: Delete from Firestore if user session exists
        val userId = getUserId()
        if (userId != null) {
            coroutineScope.launch {
                SyncManager.updateState(SyncState.SYNCING)
                try {
                    firestoreTxDataSource.deleteTransaction(userId, transaction.id)
                    SyncManager.updateState(SyncState.SYNCED)
                } catch (e: Exception) {
                    Log.e(tag, "Failed to delete transaction from Firestore", e)
                    SyncManager.updateState(SyncState.OFFLINE)
                }
            }
        }
    }

    private suspend fun recalculateDebtReimbursement(debtId: Int) {
        val debt = debtDao.getDebtById(debtId) ?: return
        val transactions = transactionDao.getTransactionsForDebt(debtId)
        val reimbursed = transactions.sumOf { it.amount }
        val updatedDebt = debt.copy(reimbursedAmount = reimbursed)
        debtDao.insertDebt(updatedDebt)
        
        // Also update remote Firestore
        val userId = getUserId()
        if (userId != null) {
            try {
                firestoreDebtDataSource.saveDebt(userId, updatedDebt)
            } catch (e: Exception) {
                Log.e(tag, "Failed to sync recalculated debt", e)
            }
        }
    }

    override fun getAllGoals(): Flow<List<SavingsGoal>> {
        return savingsGoalDao.getAllGoals()
    }

    override suspend fun insertGoal(goal: SavingsGoal): Long {
        // Step 1: Write locally
        val localId = savingsGoalDao.insertGoal(goal)
        val finalGoal = if (goal.id == 0) {
            goal.copy(id = localId.toInt())
        } else {
            goal
        }

        // Step 2: Sync to Firestore
        val userId = getUserId()
        if (userId != null) {
            coroutineScope.launch {
                SyncManager.updateState(SyncState.SYNCING)
                try {
                    firestoreGoalDataSource.saveGoal(userId, finalGoal)
                    SyncManager.updateState(SyncState.SYNCED)
                } catch (e: Exception) {
                    Log.e(tag, "Failed to upload savings goal to Firestore", e)
                    SyncManager.updateState(SyncState.OFFLINE)
                }
            }
        }
        return localId
    }

    override suspend fun deleteGoal(goal: SavingsGoal) {
        // Remove locally
        savingsGoalDao.deleteGoal(goal)

        // Delete from Firestore
        val userId = getUserId()
        if (userId != null) {
            coroutineScope.launch {
                SyncManager.updateState(SyncState.SYNCING)
                try {
                    firestoreGoalDataSource.deleteGoal(userId, goal.id)
                    SyncManager.updateState(SyncState.SYNCED)
                } catch (e: Exception) {
                    Log.e(tag, "Failed to delete savings goal from Firestore", e)
                    SyncManager.updateState(SyncState.OFFLINE)
                }
            }
        }
    }

    override suspend fun deleteGoalById(id: Int) {
        // Remove locally
        savingsGoalDao.deleteGoalById(id)

        // Delete from Firestore
        val userId = getUserId()
        if (userId != null) {
            coroutineScope.launch {
                SyncManager.updateState(SyncState.SYNCING)
                try {
                    firestoreGoalDataSource.deleteGoal(userId, id)
                    SyncManager.updateState(SyncState.SYNCED)
                } catch (e: Exception) {
                    Log.e(tag, "Failed to delete savings goal by id from Firestore", e)
                    SyncManager.updateState(SyncState.OFFLINE)
                }
            }
        }
    }

    // Manual cloud integration mapping down to Room
    override suspend fun syncWithCloud() {
        val userId = getUserId() ?: return
        Log.d(tag, "Initiating bidirectional cloud sync")
        SyncManager.updateState(SyncState.SYNCING)

        // Check if there's a user shift to clear out any stale cache
        val lastSyncUserId = sharedPrefs.getString("last_sync_user_id", null)
        if (lastSyncUserId != null && lastSyncUserId != userId) {
            val isPrevLocal = lastSyncUserId.startsWith("local_")
            val isCurrentLocal = userId.startsWith("local_")
            if (!isPrevLocal && !isCurrentLocal) {
                Log.d(tag, "Detecting real user shift between two online accounts! Forcing clear of local cache.")
                clearAllLocalData()
            } else if (isPrevLocal && !isCurrentLocal) {
                Log.d(tag, "Migrating local offline user data to online user account: $userId. Local database is preserved.")
            }
        }
        sharedPrefs.edit().putString("last_sync_user_id", userId).apply()

        try {
            // STEP 1: Upload step (push local changes/additions to remote if they aren't there yet)
            val localTxList = transactionDao.getLocalTransactionsList()
            for (tx in localTxList) {
                try {
                    firestoreTxDataSource.saveTransaction(userId, tx)
                } catch (e: Throwable) {
                    Log.w(tag, "Could not upload transaction ${tx.id} due to network/rules constraint, skipping item for now", e)
                }
            }

            val localGoalsList = savingsGoalDao.getLocalGoalsList()
            for (goal in localGoalsList) {
                try {
                    firestoreGoalDataSource.saveGoal(userId, goal)
                } catch (e: Throwable) {
                    Log.w(tag, "Could not upload savings goal ${goal.id} due to network/rules constraint, skipping item for now", e)
                }
            }

            val localDebtsList = debtDao.getLocalDebtsList()
            for (debt in localDebtsList) {
                try {
                    firestoreDebtDataSource.saveDebt(userId, debt)
                } catch (e: Throwable) {
                    Log.w(tag, "Could not upload debt ${debt.id} due to network/rules constraint, skipping item for now", e)
                }
            }

            // STEP 2: Download step (pull latest remote snapshot)
            // 2a. Recover Transactions
            val remoteTransactions = firestoreTxDataSource.getAllTransactions(userId)
            remoteTransactions.forEach { tx ->
                transactionDao.insertTransaction(tx)
            }

            // 2b. Recover Savings goals
            val remoteGoals = firestoreGoalDataSource.getAllGoals(userId)
            remoteGoals.forEach { goal ->
                savingsGoalDao.insertGoal(goal)
            }

            // 2c. Recover Debts
            val remoteDebts = firestoreDebtDataSource.getAllDebts(userId)
            remoteDebts.forEach { debt ->
                debtDao.insertDebt(debt)
            }

            Log.d(tag, "Bidirectional cloud sync complete. Synced: ${remoteTransactions.size} transactions, ${remoteGoals.size} savings goals, ${remoteDebts.size} debts.")
            SyncManager.updateState(SyncState.SYNCED)
        } catch (e: Throwable) {
            Log.e(tag, "Failed to synchronize downloads, transitioning state to OFFLINE", e)
            SyncManager.updateState(SyncState.OFFLINE)
            try {
                kotlinx.coroutines.withContext(Dispatchers.Main) {
                    val errorMsg = e.localizedMessage ?: e.message ?: "Mise à jour requise ou problème réseau"
                    android.widget.Toast.makeText(
                        context,
                        "Erreur de synchronisation : $errorMsg",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }
    }

    override suspend fun clearAllLocalData() {
        try {
            transactionDao.clearAllTransactions()
            savingsGoalDao.clearAllGoals()
            debtDao.clearAllDebts()
            categoryLimitDao.clearAllCategoryLimits()
            Log.d(tag, "Successfully cleared all cached local tables plus category limits.")
        } catch (e: Throwable) {
            Log.e(tag, "Error clearing local data tables", e)
        }
    }

    override suspend fun clearAllTransactions() {
        try {
            transactionDao.clearAllTransactions()
            // If logged in, we can also delete in Firestore. But for client-side user control, local clearing is safe.
            val userId = getUserId()
            if (userId != null) {
                coroutineScope.launch {
                    try {
                        // Delete remote transactions
                        val list = firestoreTxDataSource.getAllTransactions(userId)
                        list.forEach { tx ->
                            firestoreTxDataSource.deleteTransaction(userId, tx.id)
                        }
                    } catch (e: Exception) {
                        Log.e(tag, "Failed to clear remote transactions", e)
                    }
                }
            }
            Log.d(tag, "Cleared all transactions locally.")
        } catch (e: Exception) {
            Log.e(tag, "Error clearing transaction data", e)
        }
    }

    override suspend fun clearAllGoals() {
        try {
            savingsGoalDao.clearAllGoals()
            val userId = getUserId()
            if (userId != null) {
                coroutineScope.launch {
                    try {
                        // Delete remote goals
                        val list = firestoreGoalDataSource.getAllGoals(userId)
                        list.forEach { goal ->
                            firestoreGoalDataSource.deleteGoal(userId, goal.id)
                        }
                    } catch (e: Exception) {
                        Log.e(tag, "Failed to clear remote goals", e)
                    }
                }
            }
            Log.d(tag, "Cleared all goals locally.")
        } catch (e: Exception) {
            Log.e(tag, "Error clearing goals data", e)
        }
    }

    override fun getAllDebts(): Flow<List<Debt>> {
        return debtDao.getAllDebts()
    }

    override suspend fun insertDebt(debt: Debt) {
        val localId = debtDao.insertDebt(debt)
        val finalDebt = if (debt.id == 0) {
            debt.copy(id = localId.toInt())
        } else {
            debt
        }

        val userId = getUserId()
        if (userId != null) {
            coroutineScope.launch {
                SyncManager.updateState(SyncState.SYNCING)
                try {
                    firestoreDebtDataSource.saveDebt(userId, finalDebt)
                    SyncManager.updateState(SyncState.SYNCED)
                } catch (e: Exception) {
                    Log.e(tag, "Failed to upload debt to Firestore", e)
                    SyncManager.updateState(SyncState.OFFLINE)
                }
            }
        }
    }

    override suspend fun deleteDebt(debt: Debt) {
        debtDao.deleteDebt(debt)

        val userId = getUserId()
        if (userId != null) {
            coroutineScope.launch {
                SyncManager.updateState(SyncState.SYNCING)
                try {
                    firestoreDebtDataSource.deleteDebt(userId, debt.id)
                    SyncManager.updateState(SyncState.SYNCED)
                } catch (e: Exception) {
                    Log.e(tag, "Failed to delete debt from Firestore", e)
                    SyncManager.updateState(SyncState.OFFLINE)
                }
            }
        }
    }

    override suspend fun deleteDebtById(id: Int) {
        debtDao.deleteDebtById(id)

        val userId = getUserId()
        if (userId != null) {
            coroutineScope.launch {
                SyncManager.updateState(SyncState.SYNCING)
                try {
                    firestoreDebtDataSource.deleteDebt(userId, id)
                    SyncManager.updateState(SyncState.SYNCED)
                } catch (e: Exception) {
                    Log.e(tag, "Failed to delete debt by id from Firestore", e)
                    SyncManager.updateState(SyncState.OFFLINE)
                }
            }
        }
    }

    override suspend fun clearAllDebts() {
        try {
            debtDao.clearAllDebts()
            val userId = getUserId()
            if (userId != null) {
                coroutineScope.launch {
                    try {
                        val list = firestoreDebtDataSource.getAllDebts(userId)
                        list.forEach { debt ->
                            firestoreDebtDataSource.deleteDebt(userId, debt.id)
                        }
                    } catch (e: Exception) {
                        Log.e(tag, "Failed to clear remote debts", e)
                    }
                }
            }
            Log.d(tag, "Cleared all debts locally.")
        } catch (e: Exception) {
            Log.e(tag, "Error clearing debts data", e)
        }
    }

    override fun getAllCategoryLimits(): Flow<List<com.example.domain.model.CategoryLimit>> {
        return categoryLimitDao.getAllCategoryLimits()
    }

    override suspend fun insertCategoryLimit(limit: com.example.domain.model.CategoryLimit) {
        categoryLimitDao.insertCategoryLimit(limit)
    }

    override suspend fun deleteCategoryLimit(limit: com.example.domain.model.CategoryLimit) {
        categoryLimitDao.deleteCategoryLimit(limit)
    }

    override suspend fun clearAllCategoryLimits() {
        categoryLimitDao.clearAllCategoryLimits()
    }
}
