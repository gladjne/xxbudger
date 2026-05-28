package com.example.domain.repository

import com.example.domain.model.UserSession
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val currentUser: StateFlow<UserSession?>
    
    suspend fun register(email: String, password: String): Result<UserSession>
    suspend fun login(email: String, password: String): Result<UserSession>
    suspend fun logout(): Result<Unit>
    fun isSessionActive(): Boolean
}
