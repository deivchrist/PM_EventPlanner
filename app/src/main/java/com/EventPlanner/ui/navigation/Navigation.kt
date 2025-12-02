package com.EventPlanner.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.EventPlanner.ui.auth.LoginScreen
import com.EventPlanner.ui.auth.RegisterScreen

// Aquí importaremos las pantallas de Alexander cuando estén listas
// import com.EventPlanner.ui.events.EventListScreen
// import com.EventPlanner.ui.events.CreateEventScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {

        // ---------------------------------------------------------
        // RUTAS DE AUTENTICACIÓN (Tu parte - Deivid)
        // ---------------------------------------------------------

        composable("login") {
            LoginScreen(navController = navController)
        }

        composable("register") {
            RegisterScreen(navController = navController)
        }

        // ---------------------------------------------------------
        // RUTAS DE EVENTOS (Parte de Alexander)
        // ---------------------------------------------------------

        composable("event_list") {
            // TODO: Cuando Alexander termine, reemplaza este Box por: EventListScreen(navController)
            PlaceholderScreen("Pantalla Lista de Eventos (Pendiente de Alexander)")
        }

        composable("create_event") {
            // TODO: Reemplazar por CreateEventScreen(navController)
            PlaceholderScreen("Pantalla Crear Evento")
        }

        composable("edit_event/{eventId}") { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId")
            // TODO: Reemplazar por EditEventScreen(navController, eventId)
            PlaceholderScreen("Pantalla Editar Evento: $eventId")
        }
    }
}

// Composable temporal para evitar errores de compilación mientras Alexander trabaja
@Composable
fun PlaceholderScreen(text: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text)
    }
}