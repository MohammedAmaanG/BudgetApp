package com.prog7313.budgetapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prog7313.budgetapp.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "AuthViewModel"

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val error: String? = null,
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = ""
)

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        // Restore any in-memory session that survived a config change
        _uiState.value = _uiState.value.copy(isLoggedIn = repository.restoreSession())
    }

    fun onEmailChange(value: String)           { _uiState.value = _uiState.value.copy(email = value, error = null) }
    fun onPasswordChange(value: String)        { _uiState.value = _uiState.value.copy(password = value, error = null) }
    fun onConfirmPasswordChange(value: String) { _uiState.value = _uiState.value.copy(confirmPassword = value, error = null) }
    fun clearError()                           { _uiState.value = _uiState.value.copy(error = null) }

    // ── Login ─────────────────────────────────────────────────────────────────

    fun login() {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.value = state.copy(error = "Please enter email and password"); return
        }
        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, error = null)
            val result = repository.login(state.email.trim(), state.password)
            _uiState.value = if (result.isSuccess) {
                _uiState.value.copy(isLoading = false, isLoggedIn = true)
            } else {
                _uiState.value.copy(isLoading = false, error = result.exceptionOrNull()?.message ?: "Login failed")
            }
        }
    }

    // ── Register ─────────────────────────────────────────────────────────────

    fun register() {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.value = state.copy(error = "Please enter email and password"); return
        }
        if (state.password != state.confirmPassword) {
            _uiState.value = state.copy(error = "Passwords do not match"); return
        }
        if (state.password.length < 6) {
            _uiState.value = state.copy(error = "Password must be at least 6 characters"); return
        }
        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, error = null)
            val result = repository.register(state.email.trim(), state.password)
            _uiState.value = if (result.isSuccess) {
                val userId = result.getOrNull()
                if (userId == "pending_confirmation") {
                    // Supabase requires email confirmation
                    _uiState.value.copy(isLoading = false, error = "Check your email to confirm your account, then log in.")
                } else {
                    _uiState.value.copy(isLoading = false, isLoggedIn = true)
                }
            } else {
                _uiState.value.copy(isLoading = false, error = result.exceptionOrNull()?.message ?: "Registration failed")
            }
        }
    }

    // ── Logout ────────────────────────────────────────────────────────────────

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _uiState.value = AuthUiState()
        }
    }
}
