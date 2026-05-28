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

    // Observe active user sessions directly from repository to trigger views navigation
    val currentUser: StateFlow<UserSession?> = authRepository.currentUser
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun login(email: String, password: String) {
        if (!validateInputs(email, password)) return

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = authRepository.login(email.trim(), password)
            result.fold(
                onSuccess = { session ->
                    _uiState.value = AuthUiState.Success(session)
                },
                onFailure = { throwable ->
                    _uiState.value = AuthUiState.Error(throwable.message ?: "Identifiants invalides.")
                }
            )
        }
    }

    fun register(email: String, password: String) {
        if (!validateInputs(email, password)) return

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = authRepository.register(email.trim(), password)
            result.fold(
                onSuccess = { session ->
                    _uiState.value = AuthUiState.Success(session)
                },
                onFailure = { throwable ->
                    _uiState.value = AuthUiState.Error(throwable.message ?: "Échec de l'inscription.")
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

    private fun validateInputs(email: String, password: String): Boolean {
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            _uiState.value = AuthUiState.Error("Veuillez saisir une adresse e-mail valide.")
            return false
        }
        if (password.length < 6) {
            _uiState.value = AuthUiState.Error("Le mot de passe doit contenir au moins 6 caractères.")
            return false
        }
        return true
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
