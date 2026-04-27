package com.example.hronline

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.hronline.data.api.TokenManager
import com.example.hronline.navigation.AppNavigation
import com.example.hronline.ui.theme.HROnlineTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TokenManager.init(applicationContext)
        enableEdgeToEdge()
        setContent {
            HROnlineTheme {
                AppNavigation()
            }
        }
    }
}
