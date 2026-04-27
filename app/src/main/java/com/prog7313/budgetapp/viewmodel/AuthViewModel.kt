package com.prog7313.budgetapp.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.prog7313.budgetapp.data.repository.AuthRepository
import com.prog7313.budgetapp.data.repository.LocalAuthRepository
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

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val ctx = application.applicationContext


    private val localRepo  by lazy { LocalAuthRepository(ctx) }
    private val remoteRepo by lazy { AuthRepository() }

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        // Restore any in-memory session surviving a config change
        val loggedIn = if (USE_LOCAL_DB) localRepo.restoreSession()
        else              remoteRepo.restoreSession()
        _uiState.value = _uiState.value.copy(isLoggedIn = loggedIn)
    }

    /*
    Title: AndroidViewModel — Using Application context in ViewModel
    Author(s): Android Developers
    Date: 2024
    Version: Lifecycle 2.8.2
    Type: Documentation
    Availability: https://developer.android.com/reference/androidx/lifecycle/AndroidViewModel
    */


    fun onEmailChange(v: String)          { _uiState.value = _uiState.value.copy(email = v, error = null) }
    fun onPasswordChange(v: String)       { _uiState.value = _uiState.value.copy(password = v, error = null) }
    fun onConfirmPasswordChange(v: String){ _uiState.value = _uiState.value.copy(confirmPassword = v, error = null) }
    fun clearError()                      { _uiState.value = _uiState.value.copy(error = null) }


    fun login() {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.value = state.copy(error = "Please enter email and password")
            return
        }
        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, error = null)
            val result = if (USE_LOCAL_DB)
                localRepo.login(state.email.trim(), state.password)
            else
                remoteRepo.login(state.email.trim(), state.password)

            _uiState.value = if (result.isSuccess) {
                _uiState.value.copy(isLoading = false, isLoggedIn = true)
            } else {
                _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Login failed"
                )
            }
        }
    }

    /*
    Title: Kotlin Result — isSuccess, getOrNull, exceptionOrNull
    Author(s): JetBrains
    Date: 2024
    Version: Kotlin 2.0
    Type: Documentation
    Availability: https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-result/
    */


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
            val result = if (USE_LOCAL_DB)
                localRepo.register(state.email.trim(), state.password)
            else
                remoteRepo.register(state.email.trim(), state.password)

            _uiState.value = if (result.isSuccess) {
                val uid = result.getOrNull()
                if (uid == "pending_confirmation") {
                    _uiState.value.copy(
                        isLoading = false,
                        error = "Check your email to confirm your account, then log in."
                    )
                } else {
                    _uiState.value.copy(isLoading = false, isLoggedIn = true)
                }
            } else {
                _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Registration failed"
                )
            }
        }
    }


    fun logout() {
        viewModelScope.launch {
            if (USE_LOCAL_DB) localRepo.logout()
            else              remoteRepo.logout()
            _uiState.value = AuthUiState()
        }
    }

    /*
    Title: Android — User authentication and session management
    Author(s): Android Developers
    Date: 2024
    Version: N/A
    Type: Documentation
    Availability: https://developer.android.com/training/id-auth
    */
}
