package com.ven.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ven.app.ui.navigation.AppNavHost
import com.ven.app.ui.theme.ThemeMode
import com.ven.app.ui.theme.VexTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VexTheme(mode = ThemeMode.Dark) {
                AppNavHost()
            }
        }
    }
}
