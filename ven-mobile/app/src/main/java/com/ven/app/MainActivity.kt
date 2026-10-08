package com.ven.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.ven.app.ui.components.layout.AppScaffold
import com.ven.app.ui.navigation.BottomNavTab
import com.ven.app.ui.screens.onboarding.InterestScreen
import com.ven.app.ui.theme.ThemeMode
import com.ven.app.ui.theme.VexTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var currentTab by remember { mutableStateOf(BottomNavTab.Home) }
            var showOnboarding by remember { mutableStateOf(true) }

            VexTheme(mode = ThemeMode.Dark) {
                if (showOnboarding) {
                    InterestScreen(
                        onSaveSuccess = {
                            showOnboarding = false
                        },
                        onMaybeLater = {
                            showOnboarding = false
                        },
                    )
                } else {
                    AppScaffold(
                        currentTab = currentTab,
                        onTabSelected = { currentTab = it },
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "Active Screen: ${currentTab.title}",
                                color = VexTheme.colors.text,
                            )
                        }
                    }
                }
            }
        }
    }
}
