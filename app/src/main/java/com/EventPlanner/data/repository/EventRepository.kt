package com.EventPlanner.data.repository

import com.EventPlanner.data.model.Event
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.awaitClose
import kotlinx.coroutines.tasks.await

class EventRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val eventsCollection = db.collection("events")

    /**
     * Obtiene todos los eventos del usuario actual en tiempo real
     * @param userId ID del usuario autenticado
     * @return Flow que emite listas de eventos cuando hay cambios
     */
    fun getEventsByUserId(userId: String): Flow<List<Event>> = callbackFlow {
        val listenerRegistration = eventsCollection
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val events = snapshot?.documents?.mapNotNull { document ->
                    try {
                        document.toEvent(document.id)
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()

                trySend(events)
            }

        // Cancelar el listener cuando el Flow se cancele
        awaitClose {
            listenerRegistration.remove()
        }
    }

    /**
     * Crea un nuevo evento en Firestore
     * @param event Evento a crear
     * @return Resultado de la operación (success o error)
     */
    suspend fun createEvent(event: Event): Result<String> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("Usuario no autenticado"))
            }

            val eventData = hashMapOf(
                "userId" to currentUser.uid,
                "title" to event.title,
                "date" to event.date,
                "description" to event.description,
                "createdAt" to Timestamp.now()
            )

            val documentRef = eventsCollection.add(eventData).await()
            Result.success(documentRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Actualiza un evento existente en Firestore
     * @param eventId ID del evento a actualizar
     * @param event Datos actualizados del evento
     * @return Resultado de la operación
     */
    suspend fun updateEvent(eventId: String, event: Event): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("Usuario no autenticado"))
            }

            val eventData = hashMapOf(
                "title" to event.title,
                "date" to event.date,
                "description" to event.description
            )

            eventsCollection.document(eventId).update(eventData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Elimina un evento de Firestore
     * @param eventId ID del evento a eliminar
     * @return Resultado de la operación
     */
    suspend fun deleteEvent(eventId: String): Result<Unit> {
        return try {
            eventsCollection.document(eventId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene un evento por su ID
     * @param eventId ID del evento
     * @return Evento encontrado o null
     */
    suspend fun getEventById(eventId: String): Event? {
        return try {
            val document = eventsCollection.document(eventId).get().await()
            if (document.exists()) {
                document.toEvent(eventId)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Extensión para convertir un DocumentSnapshot a Event
     */
    private fun com.google.firebase.firestore.DocumentSnapshot.toEvent(id: String): Event {
        return Event(
            id = id,
            userId = getString("userId") ?: "",
            title = getString("title") ?: "",
            date = getString("date") ?: "",
            description = getString("description") ?: "",
            createdAt = getTimestamp("createdAt")
        )
    }
}
