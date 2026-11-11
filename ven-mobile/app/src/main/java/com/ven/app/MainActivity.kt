package com.ven.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.getValue
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ven.app.data.api.ApiClient
import com.ven.app.data.api.SessionManager
import com.ven.app.data.preference.AppLanguage
import com.ven.app.data.preference.ThemePreferences
import com.ven.app.ui.navigation.AppNavHost
import com.ven.app.ui.theme.ThemeMode
import com.ven.app.ui.theme.VexTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (AppCompatDelegate.getApplicationLocales().isEmpty) {
            AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(AppLanguage.English.tag),
            )
        }

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
