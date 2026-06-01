// Copyright (c) 2025 Gladstone Joy. Licensed under the MIT License.
package com.example.data.remote

import com.example.data.security.SafeLog as Log
import com.example.domain.model.Transaction
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

// Custom await task extension to safely use coroutines with custom tasks
suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { continuation ->
    addOnCompleteListener { task ->
        if (task.isSuccessful) {
            continuation.resume(task.result)
        } else {
            continuation.resumeWithException(task.exception ?: Exception("Task failed to complete"))
        }
    }
}

class FirestoreTransactionRemoteDataSource {
    private val tag = "FirestoreTxRemoteDS"
    
    private val firestore: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Throwable) {
            Log.w(tag, "Firestore is not available or google-services.json is missing.", e)
            null
        }
    }

    suspend fun saveTransaction(userId: String, transaction: Transaction) {
        if (userId.isBlank()) {
            Log.e(tag, "Aborting Firestore action: User is not authenticated.")
            return
        }
        if (transaction.amount <= 0.0) {
            Log.e(tag, "Aborting Firestore action: Amount must be greater than 0.")
            return
        }
        if (transaction.type.isBlank() || transaction.category.isBlank()) {
            Log.e(tag, "Aborting Firestore action: Transaction type and category are required.")
            return
        }
        val db = firestore ?: return
        try {
            val data = hashMapOf(
                "id" to transaction.id,
                "userId" to userId,
                "type" to transaction.type,
                "category" to transaction.category,
                "customCategory" to transaction.customCategory,
                "label" to transaction.label,
                "amount" to transaction.amount,
                "dateTimestamp" to transaction.dateTimestamp,
                "associatedGoalId" to transaction.associatedGoalId,
                "associatedDebtId" to transaction.associatedDebtId
            )
            db.collection("users")
                .document(userId)
                .collection("transactions")
                .document(transaction.id.toString())
                .set(data)
                .awaitTask()
            Log.d(tag, "Saved transaction ${transaction.id} to Firestore")
        } catch (e: Throwable) {
            Log.e(tag, "Error saving transaction to Firestore", e)
            throw e
        }
    }

    suspend fun deleteTransaction(userId: String, transactionId: Int) {
        val db = firestore ?: return
        try {
            db.collection("users")
                .document(userId)
                .collection("transactions")
                .document(transactionId.toString())
                .delete()
                .awaitTask()
            Log.d(tag, "Deleted transaction $transactionId from Firestore")
        } catch (e: Throwable) {
            Log.e(tag, "Error deleting transaction from Firestore", e)
            throw e
        }
    }

    suspend fun getAllTransactions(userId: String): List<Transaction> {
        val db = firestore ?: return emptyList()
        return try {
            val snapshot = db.collection("users")
                .document(userId)
                .collection("transactions")
                .get()
                .awaitTask()
            snapshot.documents.mapNotNull { doc ->
                try {
                    val id = when (val v = doc.get("id")) {
                        is Number -> v.toInt()
                        else -> doc.id.toIntOrNull() ?: return@mapNotNull null
                    }
                    val type = doc.getString("type") ?: ""
                    val category = doc.getString("category") ?: ""
                    val customCategory = doc.getString("customCategory")
                    val label = doc.getString("label") ?: ""
                    val amount = when (val v = doc.get("amount")) {
                        is Number -> v.toDouble()
                        else -> 0.0
                    }
                    val dateTimestamp = when (val v = doc.get("dateTimestamp")) {
                        is Number -> v.toLong()
                        else -> 0L
                    }
                    val associatedGoalId = when (val v = doc.get("associatedGoalId")) {
                        is Number -> v.toInt()
                        else -> null
                    }
                    val associatedDebtId = when (val v = doc.get("associatedDebtId")) {
                        is Number -> v.toInt()
                        else -> null
                    }
                    Transaction(
                        id = id,
                        type = type,
                        category = category,
                        customCategory = customCategory,
                        label = label,
                        amount = amount,
                        dateTimestamp = dateTimestamp,
                        associatedGoalId = associatedGoalId,
                        associatedDebtId = associatedDebtId
                    )
                } catch (e: Throwable) {
                    Log.e(tag, "Error parsing transaction from Firestore: ${doc.id}", e)
                    null
                }
            }
        } catch (e: Throwable) {
            Log.e(tag, "Error getting transactions from database", e)
            emptyList()
        }
    }
}
