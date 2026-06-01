// Copyright (c) 2025 Gladstone Joy. Licensed under the MIT License.
package com.example.data.remote

import com.example.data.security.SafeLog as Log
import com.example.domain.model.SavingsGoal
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

// Private local extension function to handle safe async continuation
private suspend fun <T> Task<T>.awaitGoalTask(): T = suspendCancellableCoroutine { continuation ->
    addOnCompleteListener { task ->
        if (task.isSuccessful) {
            continuation.resume(task.result)
        } else {
            continuation.resumeWithException(task.exception ?: Exception("Task failed to complete"))
        }
    }
}

class FirestoreGoalRemoteDataSource {
    private val tag = "FirestoreGoalRemoteDS"
    
    private val firestore: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Throwable) {
            Log.w(tag, "Firestore is not available or google-services.json is missing.", e)
            null
        }
    }

    suspend fun saveGoal(userId: String, goal: SavingsGoal) {
        if (userId.isBlank()) {
            Log.e(tag, "Aborting Firestore action: User is not authenticated.")
            return
        }
        if (goal.targetAmount <= 0.0) {
            Log.e(tag, "Aborting Firestore action: Target amount must be greater than 0.")
            return
        }
        if (goal.name.isBlank()) {
            Log.e(tag, "Aborting Firestore action: Savings goal name is required.")
            return
        }
        val db = firestore ?: return
        try {
            val data = hashMapOf(
                "id" to goal.id,
                "userId" to userId,
                "name" to goal.name,
                "targetAmount" to goal.targetAmount,
                "initialAmount" to goal.initialAmount,
                "targetDateTimestamp" to goal.targetDateTimestamp
            )
            db.collection("users")
                .document(userId)
                .collection("savings_goals")
                .document(goal.id.toString())
                .set(data)
                .awaitGoalTask()
            Log.d(tag, "Saved savings goal ${goal.id} to Firestore")
        } catch (e: Throwable) {
            Log.e(tag, "Error saving savings goal to Firestore", e)
            throw e
        }
    }

    suspend fun deleteGoal(userId: String, goalId: Int) {
        val db = firestore ?: return
        try {
            db.collection("users")
                .document(userId)
                .collection("savings_goals")
                .document(goalId.toString())
                .delete()
                .awaitGoalTask()
            Log.d(tag, "Deleted savings goal $goalId from Firestore")
        } catch (e: Throwable) {
            Log.e(tag, "Error deleting savings goal from Firestore", e)
            throw e
        }
    }

    suspend fun getAllGoals(userId: String): List<SavingsGoal> {
        val db = firestore ?: return emptyList()
        return try {
            val snapshot = db.collection("users")
                .document(userId)
                .collection("savings_goals")
                .get()
                .awaitGoalTask()
            snapshot.documents.mapNotNull { doc ->
                try {
                    val id = when (val v = doc.get("id")) {
                        is Number -> v.toInt()
                        else -> doc.id.toIntOrNull() ?: return@mapNotNull null
                    }
                    val name = doc.getString("name") ?: ""
                    val targetAmount = when (val v = doc.get("targetAmount")) {
                        is Number -> v.toDouble()
                        else -> 0.0
                    }
                    val initialAmount = when (val v = doc.get("initialAmount")) {
                        is Number -> v.toDouble()
                        else -> 0.0
                    }
                    val targetDateTimestamp = when (val v = doc.get("targetDateTimestamp")) {
                        is Number -> v.toLong()
                        else -> null
                    }
                    SavingsGoal(
                        id = id,
                        name = name,
                        targetAmount = targetAmount,
                        initialAmount = initialAmount,
                        targetDateTimestamp = targetDateTimestamp
                    )
                } catch (e: Throwable) {
                    Log.e(tag, "Error parsing savings goal from Firestore: ${doc.id}", e)
                    null
                }
            }
        } catch (e: Throwable) {
            Log.e(tag, "Error getting savings goals from Firestore", e)
            emptyList()
        }
    }
}
