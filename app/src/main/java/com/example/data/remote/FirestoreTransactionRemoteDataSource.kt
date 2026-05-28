package com.example.data.remote

import android.util.Log
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
        val db = firestore ?: return
        try {
            val data = hashMapOf(
                "id" to transaction.id,
                "type" to transaction.type,
                "category" to transaction.category,
                "customCategory" to transaction.customCategory,
                "label" to transaction.label,
                "amount" to transaction.amount,
                "dateTimestamp" to transaction.dateTimestamp,
                "associatedGoalId" to transaction.associatedGoalId
            )
            db.collection("users")
                .document(userId)
                .collection("transactions")
                .document(transaction.id.toString())
                .set(data)
                .awaitTask()
            Log.d(tag, "Saved transaction ${transaction.id} to Firestore for user $userId")
        } catch (e: Exception) {
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
            Log.d(tag, "Deleted transaction $transactionId from Firestore for user $userId")
        } catch (e: Exception) {
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
                    val id = doc.getLong("id")?.toInt() ?: doc.id.toIntOrNull() ?: return@mapNotNull null
                    val type = doc.getString("type") ?: ""
                    val category = doc.getString("category") ?: ""
                    val customCategory = doc.getString("customCategory")
                    val label = doc.getString("label") ?: ""
                    val amount = doc.getDouble("amount") ?: 0.0
                    val dateTimestamp = doc.getLong("dateTimestamp") ?: 0L
                    val associatedGoalId = doc.getLong("associatedGoalId")?.toInt()
                    Transaction(
                        id = id,
                        type = type,
                        category = category,
                        customCategory = customCategory,
                        label = label,
                        amount = amount,
                        dateTimestamp = dateTimestamp,
                        associatedGoalId = associatedGoalId
                    )
                } catch (e: Exception) {
                    Log.e(tag, "Error parsing transaction from Firestore: ${doc.id}", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Error getting transactions from database", e)
            emptyList()
        }
    }
}
