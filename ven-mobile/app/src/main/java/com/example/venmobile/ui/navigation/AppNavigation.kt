package com.example.venmobile.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.venmobile.ui.login.LoginRoute
import com.example.venmobile.ui.register.RegisterRoute

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
}

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.LOGIN) {

        composable(Routes.LOGIN) {
            LoginRoute(
                onNavigateToHome = {
                    navController.navigate(Routes.HOME) {
                        // Hapus Login dari back stack supaya tombol Back tidak kembali ke Login
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) },
                onForgotPassword = { /* TODO: navigate ke halaman lupa password */ },
            )
        }

        composable(Routes.REGISTER) {
            RegisterRoute(
                onNavigateToHome = {
                    navController.navigate(Routes.HOME) {
                        // Hapus Login + Register dari back stack
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                // Register dibuka dari Login, jadi cukup kembali (bukan membuat Login baru)
                onNavigateToLogin = { navController.popBackStack() },
            )
        }

        composable(Routes.HOME) {
            PlaceholderScreen("Home")
        }
    }
}

// Ganti dengan layar asli nanti
@Composable
private fun PlaceholderScreen(title: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(title)
    }
}