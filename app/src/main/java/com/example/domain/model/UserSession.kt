// Copyright (c) 2025 Gladstone Joy. Licensed under the MIT License.
package com.example.domain.model

data class UserSession(
    val uid: String,
    val email: String,
    val displayName: String? = null,
    val isEmailVerified: Boolean = true
)
