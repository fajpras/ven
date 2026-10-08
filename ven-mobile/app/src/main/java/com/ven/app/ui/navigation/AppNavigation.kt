package com.ven.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ven.app.ui.components.layout.AppScaffold
import com.ven.app.ui.login.LoginRoute
import com.ven.app.ui.register.RegisterRoute
import com.ven.app.ui.screens.home.HomeScreen
import com.ven.app.ui.screens.home.HomeUiState
import com.ven.app.ui.screens.home.SampleHomeUiState
import com.ven.app.ui.theme.VexTheme

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route,
    ) {
        composable(Screen.Login.route) {
            LoginRoute(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onForgotPassword = {
                    navController.navigate(Screen.ForgotPassword.route)
                },
            )
        }

        composable(Screen.Register.route) {
            RegisterRoute(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                },
            )
        }

        composable(Screen.ForgotPassword.route) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Forgot Password Screen",
                    style = MaterialTheme.typography.titleLarge,
                    color = VexTheme.colors.text,
                )
            }
        }

        composable(Screen.Home.route) {
            MainHomeScreen()
        }
    }
}

@Composable
fun MainHomeScreen(
    uiState: HomeUiState = SampleHomeUiState,
) {
    var currentTab by remember { mutableStateOf(BottomNavTab.Home) }

    when (currentTab) {
        BottomNavTab.Home -> {
            HomeScreen(
                uiState = uiState,
                onTabSelected = { currentTab = it },
                onCreateClick = { /* TODO: handle create click */ },
                onRequestMeetingClick = { /* TODO: handle request meeting */ },
                onView3dClick = { /* TODO: handle view 3D */ },
                onBannerClick = { /* TODO: handle banner click */ },
            )
        }
        else -> {
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
                        text = "${currentTab.title} Screen",
                        style = MaterialTheme.typography.titleLarge,
                        color = VexTheme.colors.text,
                    )
                }
            }
        }
    }
}
