package com.EventPlanner.ui.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.EventPlanner.data.model.Event
import com.EventPlanner.data.repository.EventRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class EventUiState(
    val events: List<Event> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isCreating: Boolean = false,
    val isUpdating: Boolean = false,
    val isDeleting: Boolean = false
)

class EventViewModel(
    private val eventRepository: EventRepository = EventRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(EventUiState())
    val uiState: StateFlow<EventUiState> = _uiState.asStateFlow()

    private val auth = FirebaseAuth.getInstance()

    init {
        loadEvents()
    }

    /**
     * Carga los eventos del usuario actual en tiempo real
     */
    private fun loadEvents() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Usuario no autenticado"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            eventRepository.getEventsByUserId(currentUser.uid)
                .catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Error al cargar eventos"
                    )
                }
                .collect { events ->
                    _uiState.value = _uiState.value.copy(
                        events = events,
                        isLoading = false,
                        errorMessage = null
                    )
                }
        }
    }

    /**
     * Crea un nuevo evento
     * @param title Título del evento (obligatorio)
     * @param date Fecha del evento
     * @param description Descripción del evento (opcional)
     */
    fun createEvent(title: String, date: String, description: String) {
        // Validaciones
        if (title.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "El título es obligatorio"
            )
            return
        }

        if (date.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "La fecha es obligatoria"
            )
            return
        }

        val currentUser = auth.currentUser
        if (currentUser == null) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Usuario no autenticado"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isCreating = true,
                errorMessage = null
            )

            val newEvent = Event(
                userId = currentUser.uid,
                title = title.trim(),
                date = date.trim(),
                description = description.trim()
            )

            eventRepository.createEvent(newEvent)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isCreating = false,
                        errorMessage = null
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isCreating = false,
                        errorMessage = exception.message ?: "Error al crear el evento"
                    )
                }
        }
    }

    /**
     * Actualiza un evento existente
     * @param eventId ID del evento a actualizar
     * @param title Nuevo título
     * @param date Nueva fecha
     * @param description Nueva descripción
     */
    fun updateEvent(eventId: String, title: String, date: String, description: String) {
        // Validaciones
        if (title.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "El título es obligatorio"
            )
            return
        }

        if (date.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "La fecha es obligatoria"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isUpdating = true,
                errorMessage = null
            )

            val updatedEvent = Event(
                id = eventId,
                title = title.trim(),
                date = date.trim(),
                description = description.trim()
            )

            eventRepository.updateEvent(eventId, updatedEvent)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isUpdating = false,
                        errorMessage = null
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isUpdating = false,
                        errorMessage = exception.message ?: "Error al actualizar el evento"
                    )
                }
        }
    }

    /**
     * Elimina un evento
     * @param eventId ID del evento a eliminar
     */
    fun deleteEvent(eventId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isDeleting = true,
                errorMessage = null
            )

            eventRepository.deleteEvent(eventId)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isDeleting = false,
                        errorMessage = null
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isDeleting = false,
                        errorMessage = exception.message ?: "Error al eliminar el evento"
                    )
                }
        }
    }

    /**
     * Limpia el mensaje de error
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    /**
     * Obtiene el ID del usuario actual
     */
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }
}
