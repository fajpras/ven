package com.ven.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ven.app.data.api.ApiClient
import com.ven.app.data.api.SessionManager
import com.ven.app.data.preference.ThemePreferences
import com.ven.app.ui.navigation.AppNavHost
import com.ven.app.ui.theme.ThemeMode
import com.ven.app.ui.theme.VexTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val sessionManager = SessionManager.getInstance(applicationContext)
        val themePreferences = ThemePreferences.getInstance(applicationContext)
        ApiClient.init { sessionManager.getToken() }

        enableEdgeToEdge()
        setContent {
            val themeMode by themePreferences.themeMode.collectAsStateWithLifecycle()
            VexTheme(mode = themeMode) {
                AppNavHost(
                    showExplore = BuildConfig.DEBUG &&
                        intent.getBooleanExtra("ven.showExplore", false),
                )
            }
        }
    }
}
