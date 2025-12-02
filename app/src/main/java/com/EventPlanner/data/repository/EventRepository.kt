package com.EventPlanner.data.repository

import com.EventPlanner.data.model.Event
import com.EventPlanner.data.auth.AuthRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class EventRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val authRepository = AuthRepository()
    private val eventsCollection = firestore.collection("events")

    /**
     * Obtener todos los eventos del usuario actual en tiempo real
     * Usa snapshot listener para actualizaciones automáticas
     */
    fun getEventsByUser(): Flow<List<Event>> = callbackFlow {
        val userId = authRepository.getCurrentUserId()
        if (userId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listenerRegistration: ListenerRegistration = eventsCollection
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val events = snapshot.documents.mapNotNull { doc ->
                        doc.toEvent(doc.id)
                    }
                    trySend(events)
                } else {
                    trySend(emptyList())
                }
            }

        awaitClose { listenerRegistration.remove() }
    }

    /**
     * Crear un nuevo evento
     */
    suspend fun createEvent(event: Event): Result<String> {
        return try {
            val userId = authRepository.getCurrentUserId()
            if (userId == null) {
                return Result.failure(Exception("Usuario no autenticado"))
            }

            val eventData = hashMapOf(
                "userId" to userId,
                "title" to event.title,
                "date" to event.date,
                "description" to event.description
            )

            val docRef = eventsCollection.add(eventData).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Actualizar un evento existente
     */
    suspend fun updateEvent(eventId: String, event: Event): Result<Boolean> {
        return try {
            val userId = authRepository.getCurrentUserId()
            if (userId == null) {
                return Result.failure(Exception("Usuario no autenticado"))
            }

            val eventData = hashMapOf(
                "title" to event.title,
                "date" to event.date,
                "description" to event.description
            )

            eventsCollection.document(eventId).update(eventData as Map<String, Any>).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Eliminar un evento
     */
    suspend fun deleteEvent(eventId: String): Result<Boolean> {
        return try {
            eventsCollection.document(eventId).delete().await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtener un evento por su ID
     */
    suspend fun getEventById(eventId: String): Result<Event> {
        return try {
            val doc = eventsCollection.document(eventId).get().await()
            if (doc.exists()) {
                val event = doc.toEvent(eventId)
                if (event != null) {
                    Result.success(event)
                } else {
                    Result.failure(Exception("Error al parsear el evento"))
                }
            } else {
                Result.failure(Exception("Evento no encontrado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Extensión para convertir DocumentSnapshot a Event
     */
    private fun com.google.firebase.firestore.DocumentSnapshot.toEvent(id: String): Event? {
        return try {
            Event(
                id = id,
                userId = getString("userId") ?: "",
                title = getString("title") ?: "",
                date = getString("date") ?: "",
                description = getString("description") ?: ""
            )
        } catch (e: Exception) {
            null
        }
    }
}
