package com.EventPlanner.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.EventPlanner.data.auth.AuthRepository
import com.EventPlanner.util.ValidationUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    // Estado observable de la UI (Carga, Errores, Navegación)
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        // 1. Validaciones previas
        if (!ValidationUtils.isValidEmail(email)) {
            _uiState.update { it.copy(error = "Formato de correo inválido") }
            return
        }
        if (ValidationUtils.isFieldEmpty(password)) {
            _uiState.update { it.copy(error = "La contraseña no puede estar vacía") }
            return
        }

        // 2. Iniciar carga
        _uiState.update { it.copy(isLoading = true, error = null) }

        // 3. Llamada al repositorio (Firebase)
        viewModelScope.launch {
            val result = repository.login(email, password)
            if (result.isSuccess) {
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } else {
                _uiState.update {
                    it.copy(isLoading = false, error = result.exceptionOrNull()?.message ?: "Error desconocido")
                }
            }
        }
    }

    fun register(email: String, password: String, confirmPass: String) {
        if (!ValidationUtils.isValidEmail(email)) {
            _uiState.update { it.copy(error = "Correo inválido") }
            return
        }
        if (!ValidationUtils.isValidPassword(password)) {
            _uiState.update { it.copy(error = "La contraseña es muy corta (min 6 caracteres)") }
            return
        }
        if (!ValidationUtils.passwordsMatch(password, confirmPass)) {
            _uiState.update { it.copy(error = "Las contraseñas no coinciden") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            val result = repository.register(email, password)
            if (result.isSuccess) {
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } else {
                _uiState.update {
                    it.copy(isLoading = false, error = result.exceptionOrNull()?.message ?: "Error al registrar")
                }
            }
        }
    }

    // Función para limpiar errores después de mostrarlos
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

// Clase de datos que representa CÓMO se ve la pantalla en cada momento
data class AuthUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)