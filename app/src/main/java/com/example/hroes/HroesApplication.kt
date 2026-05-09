package com.example.hroes


import android.app.Application
import android.util.Log
import com.example.hroes.data.api.TokenManager
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging

class HroesApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize encrypted token storage before any API call.
        TokenManager.init(applicationContext)

        try {
            // Check if Firebase is initialized before accessing FirebaseMessaging.
            // If google-services.json is missing, FirebaseApp.getApps() will be empty.
            if (FirebaseApp.getApps(this).isNotEmpty()) {
                // Fetch FCM token and store for push notification registration.
                FirebaseMessaging.getInstance().token.addOnSuccessListener { fcmToken ->
                    TokenManager.fcmToken = fcmToken
                }
            } else {
                Log.w("HroesApplication", "FirebaseApp is not initialized. Notifications will be disabled. Please add google-services.json to the app folder.")
            }
        } catch (e: Exception) {
            Log.e("HroesApplication", "Error initializing Firebase: ${e.message}")
        }
    }
}






















