package com.binahr


import com.binahr.BuildConfig
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import com.binahr.navigation.AppNavigation
import com.binahr.ui.theme.BinaHrTheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // TokenManager is already initialised in BinahrApplication.onCreate()
        enableEdgeToEdge()
        setContent {
            BinaHrTheme {
                AppNavigation()
            }
        }
    }
}
