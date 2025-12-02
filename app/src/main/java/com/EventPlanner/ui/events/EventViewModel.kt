package com.EventPlanner.ui.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.EventPlanner.data.model.Event
import com.EventPlanner.data.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EventViewModel : ViewModel() {

    private val repository = EventRepository()

    // Estado de la lista de eventos (se actualiza en tiempo real)
    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events.asStateFlow()

    // Estado de la UI (carga, errores, operaciones)
    private val _uiState = MutableStateFlow(EventUiState())
    val uiState: StateFlow<EventUiState> = _uiState.asStateFlow()

    init {
        // Iniciar la escucha de eventos en tiempo real
        loadEvents()
    }

    /**
     * Cargar eventos del usuario actual en tiempo real
     */
    private fun loadEvents() {
        viewModelScope.launch {
            repository.getEventsByUser().collect { eventsList ->
                _events.update { eventsList }
            }
        }
    }

    /**
     * Crear un nuevo evento
     */
    fun createEvent(title: String, date: String, description: String) {
        if (title.isBlank()) {
            _uiState.update { it.copy(error = "El título es obligatorio") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            val event = Event(
                title = title.trim(),
                date = date.trim(),
                description = description.trim()
            )

            val result = repository.createEvent(event)
            if (result.isSuccess) {
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Error al crear el evento"
                    )
                }
            }
        }
    }

    /**
     * Actualizar un evento existente
     */
    fun updateEvent(eventId: String, title: String, date: String, description: String) {
        if (title.isBlank()) {
            _uiState.update { it.copy(error = "El título es obligatorio") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            val event = Event(
                id = eventId,
                title = title.trim(),
                date = date.trim(),
                description = description.trim()
            )

            val result = repository.updateEvent(eventId, event)
            if (result.isSuccess) {
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Error al actualizar el evento"
                    )
                }
            }
        }
    }

    /**
     * Eliminar un evento
     */
    fun deleteEvent(eventId: String) {
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            val result = repository.deleteEvent(eventId)
            if (result.isFailure) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Error al eliminar el evento"
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    /**
     * Obtener un evento por su ID (para editar)
     */
    fun getEventById(eventId: String, onEventLoaded: (Event) -> Unit) {
        viewModelScope.launch {
            val result = repository.getEventById(eventId)
            if (result.isSuccess) {
                onEventLoaded(result.getOrNull()!!)
            } else {
                _uiState.update {
                    it.copy(error = result.exceptionOrNull()?.message ?: "Error al cargar el evento")
                }
            }
        }
    }

    /**
     * Limpiar el estado de éxito (para resetear después de navegación)
     */
    fun clearSuccess() {
        _uiState.update { it.copy(isSuccess = false) }
    }

    /**
     * Limpiar errores
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

/**
 * Estado de la UI para eventos
 */
data class EventUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)
