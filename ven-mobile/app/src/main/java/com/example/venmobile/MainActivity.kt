package com.example.venmobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.venmobile.ui.navigation.AppNavHost
import com.example.venmobile.ui.theme.VenMobileTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VenMobileTheme {
                AppNavHost()
            }
        }
    }
}
