package com.EventPlanner

import android.app.Application
import com.google.firebase.FirebaseApp

class EventPlannerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Inicializar Firebase
        FirebaseApp.initializeApp(this)
    }
}