package com.ven.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ven.app.data.api.SessionManager
import com.ven.app.data.preference.ThemePreferences
import com.ven.app.ui.components.layout.AppScaffold
import com.ven.app.ui.screens.ai.AiChatScreen
import com.ven.app.ui.screens.auth.ForgotPasswordEvent
import com.ven.app.ui.screens.auth.ForgotPasswordScreen
import com.ven.app.ui.screens.auth.ForgotPasswordViewModel
import com.ven.app.ui.screens.auth.LoginRoute
import com.ven.app.ui.screens.auth.RegisterRoute
import com.ven.app.ui.screens.auth.ResetPasswordScreen
import com.ven.app.ui.screens.auth.VerifyOtpScreen
import com.ven.app.ui.screens.create.exhibit.CreateExhibitTemplateScreen
import com.ven.app.ui.screens.create.exhibit.editor.CreateExhibitEditorScreen
import com.ven.app.ui.screens.create.post.CreatePostScreen
import com.ven.app.ui.screens.explore.ExploreScreen
import com.ven.app.ui.screens.explore.PostsFeedScreen
import com.ven.app.ui.screens.explore.dummyPostsFeed
import com.ven.app.ui.screens.home.HomeScreen
import com.ven.app.ui.screens.home.HomeUiState
import com.ven.app.ui.screens.home.SampleHomeUiState
import com.ven.app.ui.screens.onboarding.InterestScreen
import com.ven.app.ui.screens.profile.ProfileScreen
import com.ven.app.ui.screens.profile.dummyProfileUiState
import com.ven.app.ui.screens.requestmeeting.RequestMeetingEvent
import com.ven.app.ui.screens.requestmeeting.RequestMeetingScreen
import com.ven.app.ui.screens.requestmeeting.RequestMeetingViewModel
import com.ven.app.ui.screens.schedule.ScheduleScreen
import com.ven.app.ui.screens.settings.AccountStatusScreen
import com.ven.app.ui.screens.settings.ChangeEmailScreen
import com.ven.app.ui.screens.settings.ChangePasswordScreen
import com.ven.app.ui.screens.settings.HistoryScreen
import com.ven.app.ui.screens.settings.LanguageScreen
import com.ven.app.ui.screens.settings.ProfileSettingsScreen
import com.ven.app.ui.screens.settings.SettingsPlaceholderScreen
import com.ven.app.ui.screens.settings.SettingsScreen
import com.ven.app.ui.screens.settings.ThemeScreen
import com.ven.app.ui.screens.settings.VerifyEmailScreen
import com.ven.app.ui.screens.settings.dummyHistoryEntries
import com.ven.app.ui.screens.settings.dummySettingsProfile
import com.ven.app.ui.theme.VexTheme

/**
 * Host navigasi utama aplikasi VEN (Virtual Exhibition).
 * Menghubungkan seluruh alur autentikasi, onboarding, dan destinasi utama.
 */
@Composable
fun AppNavHost(showExplore: Boolean = false) {
    val context = LocalContext.current
    val themePreferences = remember { ThemePreferences.getInstance(context) }
    val sessionManager = remember { SessionManager.getInstance(context) }
    val currentTheme by themePreferences.themeMode.collectAsStateWithLifecycle()
    var profileData by remember { mutableStateOf(dummySettingsProfile) }
    var selectedLanguage by remember { mutableStateOf("English") }
    var isAccountDisabled by remember { mutableStateOf(false) }

    val startDestination = if (showExplore) Screen.Home.route else Screen.Login.route
    val navController = rememberNavController()
    val forgotViewModel: ForgotPasswordViewModel = viewModel()
    val forgotState by forgotViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        forgotViewModel.events.collect { event ->
            when (event) {
                ForgotPasswordEvent.NavigateToVerifyOtp -> {
                    navController.navigate(Screen.VerifyOtp.route)
                }
                ForgotPasswordEvent.NavigateToResetPassword -> {
                    navController.navigate(Screen.ResetPassword.route)
                }
                ForgotPasswordEvent.NavigateToLogin -> {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
                ForgotPasswordEvent.NavigateToHome -> {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        // -------------------------------------------------------------
        // 1. Alur Login
        // -------------------------------------------------------------
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
                onGoogleClick = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
            )
        }

        // -------------------------------------------------------------
        // 2. Alur Register
        // -------------------------------------------------------------
        composable(Screen.Register.route) {
            RegisterRoute(
                onNavigateToOnboarding = {
                    navController.navigate(Screen.OnboardingInterest.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    if (!navController.popBackStack()) {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    }
                },
                onGoogleClick = {
                    navController.navigate(Screen.OnboardingInterest.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
            )
        }

        // -------------------------------------------------------------
        // 3. Alur Onboarding (Pemilihan Kategori Minat)
        // -------------------------------------------------------------
        composable(Screen.OnboardingInterest.route) {
            InterestScreen(
                onSaveSuccess = { _ ->
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onMaybeLater = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
            )
        }

        // -------------------------------------------------------------
        // 4. Alur Lupa Kata Sandi (3 Halaman: Email -> OTP -> Buat Password Baru)
        // -------------------------------------------------------------
        // Halaman 1: Masukkan Email terdaftar
        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                state = forgotState,
                onEmailChange = forgotViewModel::onEmailChange,
                onSendOtpClick = forgotViewModel::sendOtp,
                onBackClick = { navController.popBackStack() },
            )
        }

        // Halaman 2: Masukkan Kode OTP 6-Digit
        composable(Screen.VerifyOtp.route) {
            VerifyOtpScreen(
                state = forgotState,
                onOtpChange = forgotViewModel::onOtpChange,
                onVerifyClick = forgotViewModel::verifyOtp,
                onResendClick = forgotViewModel::resendOtp,
                onBackClick = { navController.popBackStack() },
            )
        }

        // Halaman 3: Buat Kata Sandi Baru
        composable(Screen.ResetPassword.route) {
            ResetPasswordScreen(
                state = forgotState,
                onNewPasswordChange = forgotViewModel::onNewPasswordChange,
                onConfirmPasswordChange = forgotViewModel::onConfirmPasswordChange,
                onResetClick = { forgotViewModel.resetPassword(toHome = true) },
                onBackClick = { navController.popBackStack() },
            )
        }

        // -------------------------------------------------------------
        // 5. Destinasi Utama (Home)
        // -------------------------------------------------------------
        composable(Screen.Home.route) {
            MainHomeScreen(
                initialTab = if (showExplore) BottomNavTab.Explore else BottomNavTab.Home,
                onRequestMeetingClick = { postId ->
                    navController.navigate(Screen.RequestMeeting.createRoute(postId))
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToPostsFeed = { postId ->
                    navController.navigate(Screen.PostsFeed.createRoute(postId))
                },
                onNavigateToCreatePost = {
                    navController.navigate(Screen.CreatePost.route)
                },
            )
        }

        // -------------------------------------------------------------
        // 5a. Create Content Flow: Create Post (Media Picker & Details Form)
        // -------------------------------------------------------------
        composable(Screen.CreatePost.route) {
            CreatePostScreen(
                onCloseClick = { navController.popBackStack() },
                onPostSuccess = {
                    navController.popBackStack()
                },
                onNavigateToExhibit = {
                    navController.navigate(Screen.CreateExhibit.route) {
                        popUpTo(Screen.CreatePost.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }

        // -------------------------------------------------------------
        // 5a-2. Create Content Flow: Create Exhibit (Template Picker)
        // -------------------------------------------------------------
        composable(Screen.CreateExhibit.route) {
            CreateExhibitTemplateScreen(
                onCloseClick = { navController.popBackStack() },
                onNavigateToPost = {
                    navController.navigate(Screen.CreatePost.route) {
                        popUpTo(Screen.CreateExhibit.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onTemplateSelected = { template ->
                    navController.navigate(Screen.CreateExhibitEditor.createRoute(template.id))
                },
            )
        }

        // -------------------------------------------------------------
        // 5a-3. Create Content Flow: Create Exhibit (3D Canvas Editor & 4 Tool Sheets)
        // -------------------------------------------------------------
        composable(
            route = Screen.CreateExhibitEditor.route,
            arguments = listOf(
                navArgument("templateId") {
                    type = NavType.StringType
                    defaultValue = "tpl_small"
                },
            ),
        ) {
            CreateExhibitEditorScreen(
                onBackClick = { navController.popBackStack() },
                onPublishSuccess = {
                    navController.popBackStack(Screen.CreateExhibit.route, inclusive = true)
                },
            )
        }

        // -------------------------------------------------------------
        // 5b. SubPage: Explore Post Feed (scrollable)
        // -------------------------------------------------------------
        composable(
            route = Screen.PostsFeed.route,
            arguments = listOf(
                navArgument("postId") {
                    type = NavType.StringType
                    defaultValue = ""
                },
            ),
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId").orEmpty()
            PostsFeedScreen(
                posts = dummyPostsFeed,
                onBackClick = { navController.popBackStack() },
                onRequestMeetingClick = { id ->
                    navController.navigate(Screen.RequestMeeting.createRoute(id))
                },
                onView3dClick = { /* TODO: handle view 3D */ },
                initialPostId = postId.ifBlank { null },
            )
        }

        // -------------------------------------------------------------
        // 5c. Destination: AI Assistant
        // -------------------------------------------------------------
        composable(Screen.AiChat.route) {
            AiChatScreen(
                onTabSelected = { tab ->
                    when (tab) {
                        BottomNavTab.Home -> navController.navigate(Screen.Home.route)
                        BottomNavTab.Schedule -> navController.navigate(Screen.Schedule.route)
                        BottomNavTab.Explore -> navController.navigate(Screen.Explore.route)
                        BottomNavTab.Profile -> navController.navigate(Screen.Profile.route)
                        BottomNavTab.Ai -> { /* Already here */ }
                    }
                },
            )
        }

        // -------------------------------------------------------------
        // 6. SubPage: Request Meeting
        // -------------------------------------------------------------
        composable(Screen.RequestMeeting.route) {
            val requestViewModel: RequestMeetingViewModel = viewModel()
            val requestState by requestViewModel.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(Unit) {
                requestViewModel.events.collect { event ->
                    when (event) {
                        RequestMeetingEvent.RequestSuccess -> {
                            navController.popBackStack()
                        }
                    }
                }
            }

            RequestMeetingScreen(
                state = requestState,
                onBackClick = { navController.popBackStack() },
                onDaySelected = requestViewModel::onDaySelected,
                onPrevMonth = requestViewModel::onPrevMonth,
                onNextMonth = requestViewModel::onNextMonth,
                onHourChange = requestViewModel::onHourChange,
                onMinuteChange = requestViewModel::onMinuteChange,
                onPeriodChange = requestViewModel::onPeriodChange,
                onMessageChange = requestViewModel::onMessageChange,
                onSendRequestClick = requestViewModel::sendRequest,
            )
        }

        // -------------------------------------------------------------
        // 7. SubPage: Settings and Activity Flow
        // -------------------------------------------------------------
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToProfile = { navController.navigate(Screen.SettingsProfile.route) },
                onNavigateToChangeEmail = { navController.navigate(Screen.SettingsEmail.route) },
                onNavigateToChangePassword = { navController.navigate(Screen.SettingsPassword.route) },
                onNavigateToAccountStatus = { navController.navigate(Screen.SettingsAccountStatus.route) },
                onNavigateToHistory = { navController.navigate(Screen.SettingsHistory.route) },
                onNavigateToTheme = { navController.navigate(Screen.SettingsTheme.route) },
                onNavigateToLanguage = { navController.navigate(Screen.SettingsLanguage.route) },
                onNavigateToPrivacyPolicy = { navController.navigate(Screen.SettingsPrivacy.route) },
                onNavigateToHelp = { navController.navigate(Screen.SettingsHelp.route) },
                onLogoutConfirmed = {
                    sessionManager.clearSession()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }

        composable(Screen.SettingsProfile.route) {
            ProfileSettingsScreen(
                initialData = profileData,
                onBackClick = { navController.popBackStack() },
                onSaveProfile = { updated ->
                    profileData = updated
                    navController.popBackStack()
                },
            )
        }

        composable(Screen.SettingsEmail.route) {
            ChangeEmailScreen(
                onBackClick = { navController.popBackStack() },
                onSendOtpClick = { newEmail ->
                    navController.navigate(Screen.SettingsVerifyEmail.createRoute(newEmail))
                },
            )
        }

        composable(
            route = Screen.SettingsVerifyEmail.route,
            arguments = listOf(
                navArgument("email") {
                    type = NavType.StringType
                    defaultValue = ""
                },
            ),
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email").orEmpty()
            VerifyEmailScreen(
                email = email,
                onBackClick = { navController.popBackStack() },
                onVerifySuccess = {
                    navController.popBackStack(Screen.Settings.route, inclusive = false)
                },
            )
        }

        composable(Screen.SettingsPassword.route) {
            ChangePasswordScreen(
                onBackClick = { navController.popBackStack() },
                onSendOtpClick = {
                    navController.popBackStack()
                },
            )
        }

        composable(Screen.SettingsTheme.route) {
            ThemeScreen(
                currentTheme = currentTheme,
                onThemeSelected = { newTheme ->
                    themePreferences.setThemeMode(newTheme)
                },
                onBackClick = { navController.popBackStack() },
            )
        }

        composable(Screen.SettingsLanguage.route) {
            LanguageScreen(
                currentLanguage = selectedLanguage,
                onLanguageSelected = { selectedLanguage = it },
                onBackClick = { navController.popBackStack() },
            )
        }

        composable(Screen.SettingsAccountStatus.route) {
            AccountStatusScreen(
                isDisabledInitial = isAccountDisabled,
                onStatusChange = { isAccountDisabled = it },
                onBackClick = { navController.popBackStack() },
            )
        }

        composable(Screen.SettingsHistory.route) {
            HistoryScreen(
                entries = dummyHistoryEntries,
                onBackClick = { navController.popBackStack() },
            )
        }

        composable(Screen.SettingsPrivacy.route) {
            SettingsPlaceholderScreen(
                title = "Privacy Policy",
                onBackClick = { navController.popBackStack() },
            )
        }

        composable(Screen.SettingsHelp.route) {
            SettingsPlaceholderScreen(
                title = "Help",
                onBackClick = { navController.popBackStack() },
            )
        }
    }
}

@Composable
fun MainHomeScreen(
    uiState: HomeUiState = SampleHomeUiState,
    initialTab: BottomNavTab = BottomNavTab.Home,
    onRequestMeetingClick: (postId: String) -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToPostsFeed: (postId: String) -> Unit = {},
    onNavigateToCreatePost: () -> Unit = {},
) {
    var currentTab by remember { mutableStateOf(initialTab) }
    var profileState by remember { mutableStateOf(dummyProfileUiState) }

    when (currentTab) {
        BottomNavTab.Home -> {
            HomeScreen(
                uiState = uiState,
                onTabSelected = { currentTab = it },
                onCreateClick = onNavigateToCreatePost,
                onRequestMeetingClick = onRequestMeetingClick,
                onView3dClick = { /* TODO: handle view 3D */ },
                onBannerClick = { /* TODO: handle banner click */ },
            )
        }
        BottomNavTab.Schedule -> {
            ScheduleScreen(
                onTabSelected = { currentTab = it },
            )
        }
        BottomNavTab.Ai -> {
            AiChatScreen(
                onTabSelected = { currentTab = it },
            )
        }
        BottomNavTab.Explore -> {
            ExploreScreen(
                onTabSelected = { currentTab = it },
                onMediaClick = { media -> onNavigateToPostsFeed(media.id) },
            )
        }
        BottomNavTab.Profile -> {
            ProfileScreen(
                uiState = profileState,
                onTabSelected = { currentTab = it },
                onCreateClick = onNavigateToCreatePost,
                onMenuClick = onNavigateToSettings,
                onProfileTabChange = { profileState = profileState.copy(selectedTab = it) },
                onSkillSheetToggle = { profileState = profileState.copy(isSkillSheetOpen = it) },
                onMediaClick = { /* TODO: handle media click */ },
            )
        }
    }
}
