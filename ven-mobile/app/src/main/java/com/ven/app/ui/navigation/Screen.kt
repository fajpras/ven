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
    data object VerifyOtp : Screen("verify_otp")
    data object ResetPassword : Screen("reset_password")

    // Onboarding Flow
    data object OnboardingInterest : Screen("onboarding_interest")

    // Main 5-tab Destinations
    data object Home : Screen("home")
    data object Schedule : Screen("schedule")
    data object AiChat : Screen("ai_chat")
    data object Explore : Screen("explore")
    data object Profile : Screen("profile")

    // Explore Sub-Page: Scrollable Post Feed
    data object PostsFeed : Screen("posts_feed?postId={postId}&userId={userId}") {
        fun createRoute(postId: String = "", userId: String = "") = "posts_feed?postId=$postId&userId=$userId"
    }

    // Detail Profil Pengguna Publik (Instagram Style)
    data object UserProfile : Screen("user_profile/{userId}") {
        fun createRoute(userId: String) = "user_profile/$userId"
    }

    // Create Content Flow
    data object CreatePost : Screen("create_post")
    data object CreateExhibit : Screen("create_exhibit")
    data object CreateExhibitEditor : Screen("create_exhibit_editor/{templateId}?title={title}&description={description}") {
        fun createRoute(templateId: String, title: String = "", description: String = ""): String {
            val encodedTitle = java.net.URLEncoder.encode(title, "UTF-8")
            val encodedDesc = java.net.URLEncoder.encode(description, "UTF-8")
            return "create_exhibit_editor/$templateId?title=$encodedTitle&description=$encodedDesc"
        }
    }

    // SubPage Flow
    data object RequestMeeting : Screen("request_meeting/{postId}") {
        fun createRoute(postId: String) = "request_meeting/$postId"
    }

    // Settings Flow
    data object Settings : Screen("settings")
    data object SettingsProfile : Screen("settings/profile")
    data object SettingsEmail : Screen("settings/email")
    data object SettingsVerifyEmail : Screen("settings/verify_email/{email}") {
        fun createRoute(email: String) = "settings/verify_email/$email"
    }
    data object SettingsPassword : Screen("settings/password")
    data object SettingsTheme : Screen("settings/theme")
    data object SettingsLanguage : Screen("settings/language")
    data object SettingsAccountStatus : Screen("settings/account_status")
    data object SettingsHistory : Screen("settings/history")
    data object SettingsPrivacy : Screen("settings/privacy")
    data object SettingsHelp : Screen("settings/help")
}
