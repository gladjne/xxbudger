package com.example.data.remote

import android.util.Log
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
        val db = firestore ?: return
        try {
            val data = hashMapOf(
                "id" to goal.id,
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
            Log.d(tag, "Saved savings goal ${goal.id} to Firestore for user $userId")
        } catch (e: Exception) {
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
            Log.d(tag, "Deleted savings goal $goalId from Firestore for user $userId")
        } catch (e: Exception) {
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
                    val id = doc.getLong("id")?.toInt() ?: doc.id.toIntOrNull() ?: return@mapNotNull null
                    val name = doc.getString("name") ?: ""
                    val targetAmount = doc.getDouble("targetAmount") ?: 0.0
                    val initialAmount = doc.getDouble("initialAmount") ?: 0.0
                    val targetDateTimestamp = doc.getLong("targetDateTimestamp")
                    SavingsGoal(
                        id = id,
                        name = name,
                        targetAmount = targetAmount,
                        initialAmount = initialAmount,
                        targetDateTimestamp = targetDateTimestamp
                    )
                } catch (e: Exception) {
                    Log.e(tag, "Error parsing savings goal from Firestore: ${doc.id}", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Error getting savings goals from Firestore", e)
            emptyList()
        }
    }
}
