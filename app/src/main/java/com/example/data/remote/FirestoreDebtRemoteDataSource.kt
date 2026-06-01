// Copyright (c) 2025 Gladstone Joy. Licensed under the MIT License.
package com.example.data.remote

import com.example.data.security.SafeLog as Log
import com.example.domain.model.Debt
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private suspend fun <T> Task<T>.awaitDebtTask(): T = suspendCancellableCoroutine { continuation ->
    addOnCompleteListener { task ->
        if (task.isSuccessful) {
            continuation.resume(task.result)
        } else {
            continuation.resumeWithException(task.exception ?: Exception("Task failed to complete"))
        }
    }
}

class FirestoreDebtRemoteDataSource {
    private val tag = "FirestoreDebtRemoteDS"
    
    private val firestore: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Throwable) {
            Log.w(tag, "Firestore is not available or google-services.json is missing.", e)
            null
        }
    }

    suspend fun saveDebt(userId: String, debt: Debt) {
        if (userId.isBlank()) {
            Log.e(tag, "Aborting Firestore action: User is not authenticated.")
            return
        }
        if (debt.totalAmount <= 0.0) {
            Log.e(tag, "Aborting Firestore action: Total debt amount must be greater than 0.")
            return
        }
        if (debt.name.isBlank()) {
            Log.e(tag, "Aborting Firestore action: Creditor name is required.")
            return
        }
        val db = firestore ?: return
        try {
            val data = hashMapOf(
                "id" to debt.id,
                "userId" to userId,
                "name" to debt.name,
                "totalAmount" to debt.totalAmount,
                "reimbursedAmount" to debt.reimbursedAmount,
                "dateTimestamp" to debt.dateTimestamp
            )
            db.collection("users")
                .document(userId)
                .collection("debts")
                .document(debt.id.toString())
                .set(data)
                .awaitDebtTask()
            Log.d(tag, "Saved debt ${debt.id} to Firestore")
        } catch (e: Throwable) {
            Log.e(tag, "Error saving debt to Firestore", e)
            throw e
        }
    }

    suspend fun deleteDebt(userId: String, debtId: Int) {
        val db = firestore ?: return
        try {
            db.collection("users")
                .document(userId)
                .collection("debts")
                .document(debtId.toString())
                .delete()
                .awaitDebtTask()
            Log.d(tag, "Deleted debt $debtId from Firestore")
        } catch (e: Throwable) {
            Log.e(tag, "Error deleting debt from Firestore", e)
            throw e
        }
    }

    suspend fun getAllDebts(userId: String): List<Debt> {
        val db = firestore ?: return emptyList()
        return try {
            val snapshot = db.collection("users")
                .document(userId)
                .collection("debts")
                .get()
                .awaitDebtTask()
            snapshot.documents.mapNotNull { doc ->
                try {
                    val id = when (val v = doc.get("id")) {
                        is Number -> v.toInt()
                        else -> doc.id.toIntOrNull() ?: return@mapNotNull null
                    }
                    val name = doc.getString("name") ?: ""
                    val totalAmount = when (val v = doc.get("totalAmount")) {
                        is Number -> v.toDouble()
                        else -> 0.0
                    }
                    val reimbursedAmount = when (val v = doc.get("reimbursedAmount")) {
                        is Number -> v.toDouble()
                        else -> 0.0
                    }
                    val dateTimestamp = when (val v = doc.get("dateTimestamp")) {
                        is Number -> v.toLong()
                        else -> null
                    }
                    Debt(
                        id = id,
                        name = name,
                        totalAmount = totalAmount,
                        reimbursedAmount = reimbursedAmount,
                        dateTimestamp = dateTimestamp
                    )
                } catch (e: Throwable) {
                    Log.e(tag, "Error parsing debt from Firestore: ${doc.id}", e)
                    null
                }
            }
        } catch (e: Throwable) {
            Log.e(tag, "Error getting debts from Firestore", e)
            emptyList()
        }
    }
}
