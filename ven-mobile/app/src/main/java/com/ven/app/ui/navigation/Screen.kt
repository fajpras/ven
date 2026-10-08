package com.ven.app.ui.navigation

/**
 * Definisi route navigasi aplikasi VEN (Virtual Exhibition).
 * Sesuai arsitektur navigasi di DESIGN.md §3.2 & §7.
 */
sealed class Screen(val route: String) {
    // Auth Flow
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object ForgotPassword : Screen("forgot_password")

    // Onboarding Flow
    data object OnboardingInterest : Screen("onboarding_interest")

    // Main 5-tab Destinations
    data object Home : Screen("home")
    data object Schedule : Screen("schedule")
    data object AiChat : Screen("ai_chat")
    data object Explore : Screen("explore")
    data object Profile : Screen("profile")

    // Create Content Flow
    data object CreatePost : Screen("create_post")
    data object CreateExhibit : Screen("create_exhibit")

    // Settings Flow
    data object Settings : Screen("settings")
}
