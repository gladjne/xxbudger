// Copyright (c) 2025 Gladstone Joy. Licensed under the MIT License.
package com.example.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.repository.FirebaseAuthRepository
import com.example.domain.model.UserSession
import com.example.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

enum class PasswordStrength {
    WEAK, MEDIUM, STRONG
}

sealed interface AuthUiState {
    object Idle : AuthUiState
    object Loading : AuthUiState
    data class Success(val session: UserSession) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _passwordUpgradeRequired = MutableStateFlow(false)
    val passwordUpgradeRequired: StateFlow<Boolean> = _passwordUpgradeRequired.asStateFlow()

    private val _is2faPending = MutableStateFlow(false)
    val is2faPending: StateFlow<Boolean> = _is2faPending.asStateFlow()

    private val _is2faOnboardingPending = MutableStateFlow(false)
    val is2faOnboardingPending: StateFlow<Boolean> = _is2faOnboardingPending.asStateFlow()

    // Observe active user sessions directly from repository to trigger views navigation
    val currentUser: StateFlow<UserSession?> = authRepository.currentUser
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { session ->
                if (session != null) {
                    _passwordUpgradeRequired.value = authRepository.isPasswordUpgradeRequired(session.email)
                    _is2faPending.value = authRepository.is2FAEnabled(session.email)
                    _is2faOnboardingPending.value = !authRepository.is2FAEnabled(session.email) && !authRepository.is2FAPromptDismissed(session.email)
                } else {
                    _passwordUpgradeRequired.value = false
                    _is2faPending.value = false
                    _is2faOnboardingPending.value = false
                }
            }
        }
    }

    fun login(email: String, password: String) {
        if (!validateLoginInputs(email, password)) return

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = authRepository.login(email.trim(), password)
            result.fold(
                onSuccess = { session ->
                    val (_, strength) = getPasswordFeedbackAndStrength(password)
                    if (strength != PasswordStrength.STRONG) {
                        authRepository.setPasswordUpgradeRequired(session.email, true)
                        _passwordUpgradeRequired.value = true
                    } else {
                        authRepository.setPasswordUpgradeRequired(session.email, false)
                        _passwordUpgradeRequired.value = false
                    }
                    _uiState.value = AuthUiState.Success(session)
                },
                onFailure = { throwable ->
                    _uiState.value = AuthUiState.Error("Identifiants ou mot de passe invalides.")
                }
            )
        }
    }

    fun register(email: String, password: String) {
        if (!validateRegisterInputs(email, password)) return

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = authRepository.register(email.trim(), password)
            result.fold(
                onSuccess = { session ->
                    authRepository.setPasswordUpgradeRequired(session.email, false)
                    _passwordUpgradeRequired.value = false
                    _uiState.value = AuthUiState.Success(session)
                },
                onFailure = { throwable ->
                    _uiState.value = AuthUiState.Error("Échec de l'inscription d'utilisateur.")
                }
            )
        }
    }

    fun updatePassword(newPassword: String) {
        val (missing, strength) = getPasswordFeedbackAndStrength(newPassword)
        if (strength != PasswordStrength.STRONG) {
            val missingText = missing.joinToString(", ")
            _uiState.value = AuthUiState.Error("Nouveau mot de passe insuffisant. Manquant : $missingText")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = authRepository.updatePassword(newPassword)
            result.fold(
                onSuccess = {
                    val session = authRepository.currentUser.value
                    if (session != null) {
                        authRepository.setPasswordUpgradeRequired(session.email, false)
                    }
                    _passwordUpgradeRequired.value = false
                    _uiState.value = AuthUiState.Idle // reset layout state
                },
                onFailure = { throwable ->
                    _uiState.value = AuthUiState.Error("Échec de la mise à jour.")
                }
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Idle
            authRepository.logout()
        }
    }

    fun clearError() {
        if (_uiState.value is AuthUiState.Error) {
            _uiState.value = AuthUiState.Idle
        }
    }

    fun sendVerificationEmail() {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = authRepository.sendVerificationEmail()
            result.fold(
                onSuccess = {
                    _uiState.value = AuthUiState.Error("E-mail de vérification envoyé / renvoyé. Veuillez vérifier votre boîte de réception.")
                },
                onFailure = { throwable ->
                    _uiState.value = AuthUiState.Error("Échec de l'envoi de l'e-mail.")
                }
            )
        }
    }

    fun reloadUserStatus() {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = authRepository.reloadUser()
            result.fold(
                onSuccess = { session ->
                    if (session != null) {
                        _uiState.value = AuthUiState.Success(session)
                    } else {
                        _uiState.value = AuthUiState.Idle
                    }
                },
                onFailure = { throwable ->
                    _uiState.value = AuthUiState.Error("Échec du rafraîchissement. Veuillez réessayer.")
                }
            )
        }
    }

    fun getPasswordFeedbackAndStrength(password: String): Pair<List<String>, PasswordStrength> {
        val missingConstraints = mutableListOf<String>()
        if (password.length < 8) {
            missingConstraints.add("Au moins 8 caractères")
        }
        if (!password.any { it.isUpperCase() }) {
            missingConstraints.add("Au moins 1 lettre majuscule")
        }
        if (!password.any { it.isLowerCase() }) {
            missingConstraints.add("Au moins 1 lettre minuscule")
        }
        if (!password.any { it.isDigit() }) {
            missingConstraints.add("Au moins 1 chiffre")
        }
        val specialCharsSet = "@#$%^&+=!_*-+()[]{}?/;:,.<>~\"'|\\`"
        if (!password.any { it in specialCharsSet || (!it.isLetterOrDigit() && !it.isWhitespace()) }) {
            missingConstraints.add("Au moins 1 caractère spécial (ex: @, #, $, !)")
        }

        val criteriaMetCount = 5 - missingConstraints.size
        val strength = when {
            password.isEmpty() -> PasswordStrength.WEAK
            criteriaMetCount <= 2 -> PasswordStrength.WEAK
            criteriaMetCount < 5 -> PasswordStrength.MEDIUM
            else -> PasswordStrength.STRONG
        }
        return Pair(missingConstraints, strength)
    }

    private fun validateLoginInputs(email: String, password: String): Boolean {
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            _uiState.value = AuthUiState.Error("Veuillez saisir une adresse e-mail valide.")
            return false
        }
        if (password.isBlank()) {
            _uiState.value = AuthUiState.Error("Veuillez saisir votre mot de passe.")
            return false
        }
        return true
    }

    private fun validateRegisterInputs(email: String, password: String): Boolean {
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            _uiState.value = AuthUiState.Error("Veuillez saisir une adresse e-mail valide.")
            return false
        }
        val (missing, strength) = getPasswordFeedbackAndStrength(password)
        if (strength != PasswordStrength.STRONG) {
            val missingText = missing.joinToString(", ")
            _uiState.value = AuthUiState.Error("Mot de passe insuffisant. Manquant : $missingText")
            return false
        }
        return true
    }

    // TOTP and 2FA functions
    fun is2FAEnabled(): Boolean {
        val email = currentUser.value?.email ?: return false
        return authRepository.is2FAEnabled(email)
    }

    fun get2FASecret(): String? {
        val email = currentUser.value?.email ?: return null
        return authRepository.get2FASecret(email)
    }

    fun generateAndGet2FASecret(): String {
        val email = currentUser.value?.email ?: throw Exception("Aucun utilisateur connecté")
        val existing = authRepository.get2FASecret(email)
        if (existing != null) return existing
        val newSecret = generateSecretKey()
        authRepository.set2FASecret(email, newSecret)
        return newSecret
    }

    fun verifyAndEnable2FA(code: String): Boolean {
        val email = currentUser.value?.email ?: return false
        val secret = authRepository.get2FASecret(email) ?: return false
        if (verifyTotp(secret, code)) {
            authRepository.set2FAEnabled(email, true)
            authRepository.set2FAPromptDismissed(email, true)
            _is2faOnboardingPending.value = false
            return true
        }
        return false
    }

    fun disable2FA() {
        val email = currentUser.value?.email ?: return
        authRepository.set2FAEnabled(email, false)
        authRepository.set2FASecret(email, null)
        authRepository.set2FAPromptDismissed(email, false)
        _is2faOnboardingPending.value = false
    }

    fun skip2FAOnboarding() {
        val email = currentUser.value?.email ?: return
        authRepository.set2FAPromptDismissed(email, true)
        _is2faOnboardingPending.value = false
    }

    fun verifyLogin2FA(code: String): Boolean {
        val email = currentUser.value?.email ?: return false
        val secret = authRepository.get2FASecret(email) ?: return false
        if (verifyTotp(secret, code)) {
            _is2faPending.value = false
            return true
        }
        return false
    }

    private fun generateSecretKey(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
        return (1..16).map { chars.random() }.joinToString("")
    }

    private fun decodeBase32(base32: String): ByteArray {
        val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
        val cleaned = base32.uppercase().replace(Regex("[^A-Z2-7]"), "")
        var l = 0
        var val1 = 0
        val out = java.io.ByteArrayOutputStream()
        for (i in 0 until cleaned.length) {
            val c = cleaned[i]
            val digit = alphabet.indexOf(c)
            if (digit == -1) continue
            val1 = (val1 shl 5) or digit
            l += 5
            if (l >= 8) {
                val b = ((val1 shr (l - 8)) and 0xFF).toByte()
                out.write(b.toInt())
                l -= 8
            }
        }
        return out.toByteArray()
    }

    private fun getTotpCode(secret: String, timeIndex: Long): String {
        try {
            val key = decodeBase32(secret)
            val data = ByteArray(8)
            var value = timeIndex
            for (i in 7 downTo 0) {
                data[i] = (value and 0xFF).toByte()
                value = value shr 8
            }
            val mac = Mac.getInstance("HmacSHA1")
            val signKey = SecretKeySpec(key, "RAW")
            mac.init(signKey)
            val hash = mac.doFinal(data)
            val offset = (hash[hash.size - 1].toInt() and 0xF)
            val binary = (
                ((hash[offset].toInt() and 0x7F) shl 24) or
                ((hash[offset + 1].toInt() and 0xFF) shl 16) or
                ((hash[offset + 2].toInt() and 0xFF) shl 8) or
                (hash[offset + 3].toInt() and 0xFF)
            )
            val otp = binary % 1000000
            return String.format(Locale.US, "%06d", otp)
        } catch (e: Exception) {
            return "000000"
        }
    }

    private fun verifyTotp(secret: String, code: String): Boolean {
        val cleanedCode = code.replace(Regex("\\s"), "")
        if (cleanedCode.length != 6) return false
        val seconds = System.currentTimeMillis() / 1000
        val timeIndex = seconds / 30
        for (i in -1..1) {
            if (getTotpCode(secret, timeIndex + i) == cleanedCode) {
                return true
            }
        }
        return false
    }
}

class AuthViewModelFactory(
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
