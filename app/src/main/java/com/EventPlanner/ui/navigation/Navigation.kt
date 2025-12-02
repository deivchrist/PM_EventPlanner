package com.EventPlanner.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.EventPlanner.ui.auth.LoginScreen
import com.EventPlanner.ui.auth.RegisterScreen
import com.EventPlanner.ui.events.EventListScreen
import com.EventPlanner.ui.events.CreateEventScreen
import com.EventPlanner.ui.events.EditEventScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {

        // ---------------------------------------------------------
        // RUTAS DE AUTENTICACIÓN
        // ---------------------------------------------------------

        composable("login") {
            LoginScreen(navController = navController)
        }

        composable("register") {
            RegisterScreen(navController = navController)
        }

        // ---------------------------------------------------------
        // RUTAS DE EVENTOS
        // ---------------------------------------------------------

        composable("event_list") {
            EventListScreen(navController = navController)
        }

        composable("create_event") {
            CreateEventScreen(navController = navController)
        }

        composable("edit_event/{eventId}") { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            EditEventScreen(navController = navController, eventId = eventId)
        }
    }
}