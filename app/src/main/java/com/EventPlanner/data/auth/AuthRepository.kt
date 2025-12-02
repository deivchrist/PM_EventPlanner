package com.EventPlanner.data.auth

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    // Iniciar sesión
    suspend fun login(email: String, password: String): Result<Boolean> {
        return try {
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Registrar usuario nuevo
    suspend fun register(email: String, password: String): Result<Boolean> {
        return try {
            firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Cerrar sesión
    fun logout() {
        firebaseAuth.signOut()
    }

    // Obtener ID del usuario actual (útil para guardar eventos a su nombre)
    fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }

    // Verificar si ya hay un usuario logueado (para saltar la pantalla de login)
    fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }
}