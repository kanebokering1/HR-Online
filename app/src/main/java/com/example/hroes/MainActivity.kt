package com.example.hroes


import com.example.hroes.BuildConfig
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.hroes.navigation.AppNavigation
import com.example.hroes.ui.theme.BinaHrTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // TokenManager is already initialised in HroesApplication.onCreate()
        enableEdgeToEdge()
        setContent {
            BinaHrTheme {
                AppNavigation()
            }
        }
    }
}






















