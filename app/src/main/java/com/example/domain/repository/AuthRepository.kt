// Copyright (c) 2025 Gladstone Joy. Licensed under the MIT License.
package com.example.domain.repository

import com.example.domain.model.UserSession
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val currentUser: StateFlow<UserSession?>
    
    suspend fun register(email: String, password: String): Result<UserSession>
    suspend fun login(email: String, password: String): Result<UserSession>
    suspend fun logout(): Result<Unit>
    fun isSessionActive(): Boolean
    suspend fun sendVerificationEmail(): Result<Unit>
    suspend fun reloadUser(): Result<UserSession?>
    fun isPasswordUpgradeRequired(email: String): Boolean
    fun setPasswordUpgradeRequired(email: String, required: Boolean)
    suspend fun updatePassword(newPassword: String): Result<Unit>
    fun is2FAEnabled(email: String): Boolean
    fun set2FAEnabled(email: String, enabled: Boolean)
    fun get2FASecret(email: String): String?
    fun set2FASecret(email: String, secret: String?)
    fun is2FAPromptDismissed(email: String): Boolean
    fun set2FAPromptDismissed(email: String, dismissed: Boolean)
}
