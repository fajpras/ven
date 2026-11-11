package com.ven.app.ui.navigation

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ven.app.data.api.ApiClient
import com.ven.app.data.api.ApiLogin
import com.ven.app.data.api.ApiPencarian
import com.ven.app.data.api.SessionManager
import com.ven.app.util.DateFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.ven.app.data.preference.ThemePreferences
import com.ven.app.ui.components.feature.CreateOptionsBottomSheet
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
import com.ven.app.unity.UnityLauncher
import com.ven.app.ui.screens.create.post.CreatePostScreen
import com.ven.app.ui.screens.explore.ExploreScreen
import com.ven.app.ui.screens.explore.ExploreViewModel
import com.ven.app.ui.screens.explore.PostsFeedItem
import com.ven.app.ui.screens.explore.PostsFeedScreen
import com.ven.app.ui.screens.home.HomeScreen
import com.ven.app.ui.screens.home.HomeUiState
import com.ven.app.ui.screens.home.HomeViewModel
import com.ven.app.ui.screens.home.toFeedItem
import com.ven.app.ui.screens.onboarding.InterestRoute
import com.ven.app.ui.screens.profile.ProfileScreen
import com.ven.app.ui.screens.profile.ProfileViewModel
import com.ven.app.ui.screens.profile.UserProfileDetailScreen
import com.ven.app.ui.screens.requestmeeting.RequestMeetingEvent
import com.ven.app.ui.screens.requestmeeting.RequestMeetingScreen
import com.ven.app.ui.screens.requestmeeting.RequestMeetingViewModel
import com.ven.app.ui.screens.schedule.ScheduleScreen
import com.ven.app.ui.screens.settings.AccountStatusRoute
import com.ven.app.ui.screens.settings.ChangeEmailRoute
import com.ven.app.ui.screens.settings.ChangeEmailViewModel
import com.ven.app.ui.screens.settings.ChangePasswordRoute
import com.ven.app.ui.screens.settings.HistoryScreen
import com.ven.app.ui.screens.settings.LanguageScreen
import com.ven.app.ui.screens.settings.ProfileSettingsRoute
import com.ven.app.ui.screens.settings.SettingsPlaceholderScreen
import com.ven.app.ui.screens.settings.SettingsScreen
import com.ven.app.ui.screens.settings.ThemeScreen
import com.ven.app.ui.screens.settings.VerifyEmailRoute
import com.ven.app.ui.screens.settings.dummyHistoryEntries
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
    val coroutineScope = rememberCoroutineScope()
    val currentTheme by themePreferences.themeMode.collectAsStateWithLifecycle()
    var selectedLanguage by remember { mutableStateOf("English") }
    var isAccountDisabled by remember { mutableStateOf(false) }

    val startDestination = if (showExplore) Screen.Home.route else Screen.Login.route
    val navController = rememberNavController()
    val forgotViewModel: ForgotPasswordViewModel = viewModel()
    val homeViewModel: HomeViewModel = viewModel()
    val exploreViewModel: ExploreViewModel = viewModel()
    val forgotState by forgotViewModel.uiState.collectAsStateWithLifecycle()
    val changeEmailViewModel: ChangeEmailViewModel = viewModel()

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
        enterTransition = { fadeIn(tween(220)) },
        exitTransition = { fadeOut(tween(180)) },
        popEnterTransition = { fadeIn(tween(180)) },
        popExitTransition = { fadeOut(tween(220)) },
    ) {
        // -------------------------------------------------------------
        // 1. Alur Login
        // -------------------------------------------------------------
        composable(Screen.Login.route) {
            LoginRoute(
                onNavigateToHome = {
                    homeViewModel.muatUlang(filterSeen = false)
                    exploreViewModel.muatUlang(filterSeen = false)
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToOnboarding = {
                    navController.navigate(Screen.OnboardingInterest.route) {
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
                onNavigateToHome = {
                    homeViewModel.muatUlang(filterSeen = false)
                    exploreViewModel.muatUlang(filterSeen = false)
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    if (!navController.popBackStack()) {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    }
                },
            )
        }

        // -------------------------------------------------------------
        // 3. Alur Onboarding (Pemilihan Kategori Minat)
        // -------------------------------------------------------------
        composable(Screen.OnboardingInterest.route) {
            InterestRoute(
                onFinished = {
                    sessionManager.getUser()?.let { pengguna ->
                        sessionManager.updateUser(pengguna.copy(onboardingSelesai = true))
                    }
                    homeViewModel.muatUlang(filterSeen = false)
                    exploreViewModel.muatUlang(filterSeen = false)
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onSessionExpired = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
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
                onResetClick = { forgotViewModel.resetPassword(toHome = false) },
                onBackClick = { navController.popBackStack() },
            )
        }

        // -------------------------------------------------------------
        // 5. Destinasi Utama (Home)
        // -------------------------------------------------------------
        composable(Screen.Home.route) {
            MainHomeScreen(
                homeViewModel = homeViewModel,
                exploreViewModel = exploreViewModel,
                initialTab = if (showExplore) BottomNavTab.Explore else BottomNavTab.Home,
                onRequestMeetingClick = { postId ->
                    navController.navigate(Screen.RequestMeeting.createRoute(postId))
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToPostsFeed = { postId, userId ->
                    navController.navigate(Screen.PostsFeed.createRoute(postId = postId, userId = userId))
                },
                onNavigateToCreatePost = {
                    navController.navigate(Screen.CreatePost.route)
                },
                onNavigateToCreateExhibit = {
                    navController.navigate(Screen.CreateExhibit.route)
                },
                onNavigateToExhibitEditor = { id ->
                    navController.navigate(Screen.CreateExhibitEditor.createRoute(id))
                },
                onNavigateToUserProfile = { userId ->
                    navController.navigate(Screen.UserProfile.createRoute(userId))
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
            val context = LocalContext.current
            CreateExhibitTemplateScreen(
                onCloseClick = { navController.popBackStack() },
                onNavigateToPost = {
                    navController.navigate(Screen.CreatePost.route) {
                        popUpTo(Screen.CreateExhibit.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onTemplateSelected = { template ->
                    val authToken = sessionManager.getToken().orEmpty()
                    UnityLauncher.openEditor(
                        context = context,
                        templateId = template.unityTemplateId.ifBlank { template.id },
                        authToken = authToken,
                    )
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
                    if (!navController.popBackStack(Screen.CreateExhibit.route, inclusive = true)) {
                        navController.popBackStack()
                    }
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
                navArgument("userId") {
                    type = NavType.StringType
                    defaultValue = ""
                },
            ),
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId").orEmpty()
            val userId = backStackEntry.arguments?.getString("userId").orEmpty()
            val exploreFeed by exploreViewModel.uiState.collectAsStateWithLifecycle()
            val homeFeed by homeViewModel.uiState.collectAsStateWithLifecycle()
            val profileViewModel: ProfileViewModel = viewModel()
            val profileState by profileViewModel.uiState.collectAsStateWithLifecycle()

            val apiPencarian = remember { ApiClient.createService(ApiPencarian::class.java) }
            var userPosts by remember(userId) { mutableStateOf<List<PostsFeedItem>>(emptyList()) }
            var isUserLoading by remember(userId) { mutableStateOf(false) }

            val scope = rememberCoroutineScope()
            val loadUserPosts: () -> Unit = {
                if (userId.isNotBlank() && userId != "me") {
                    scope.launch {
                        isUserLoading = true
                        try {
                            val res = apiPencarian.getDetailPengguna(userId)
                            if (res.isSuccessful) {
                                val detail = res.body()?.data
                                val mapped = detail?.karya?.map { k ->
                                    val author = detail.pengguna?.namaPanggilan ?: detail.pengguna?.nama ?: "Kreator"
                                    PostsFeedItem(
                                        id = k.idKarya,
                                        username = "@$author",
                                        title = k.judul,
                                        category = k.kategori?.nama ?: "Karya",
                                        timestampText = DateFormatter.formatRelativeTime(k.createdAt),
                                        description = k.deskripsi.orEmpty(),
                                        imageUrl = k.fileUrl ?: k.file,
                                        userId = detail.pengguna?.idPengguna,
                                        userAvatarUrl = detail.pengguna?.fotoProfil,
                                        pameranId = k.idPameran?.toString(),
                                    )
                                } ?: emptyList()
                                userPosts = mapped
                            }
                        } catch (_: Exception) { }
                        isUserLoading = false
                    }
                }
            }

            LaunchedEffect(userId) {
                if (userId == "me") {
                    profileViewModel.muat()
                } else if (userId.isNotBlank()) {
                    loadUserPosts()
                }
            }

            val finalPosts = when {
                userId == "me" -> profileState.feedPosts
                userId.isNotBlank() -> userPosts
                else -> (exploreFeed.feedPosts + homeFeed.posts.map { it.toFeedItem() }).distinctBy { it.id }
            }

            PostsFeedScreen(
                posts = finalPosts,
                isRefreshing = when {
                    userId == "me" -> profileState.isLoading
                    userId.isNotBlank() -> isUserLoading
                    else -> exploreFeed.isRefreshing || homeFeed.isRefreshing
                },
                onRefresh = {
                    when {
                        userId == "me" -> profileViewModel.muat()
                        userId.isNotBlank() -> loadUserPosts()
                        else -> {
                            exploreViewModel.muatUlang(filterSeen = true)
                            homeViewModel.muatUlang(filterSeen = true)
                        }
                    }
                },
                onBackClick = { navController.popBackStack() },
                onRequestMeetingClick = { id ->
                    navController.navigate(Screen.RequestMeeting.createRoute(id))
                },
                onView3dClick = { id ->
                    navController.navigate(Screen.CreateExhibitEditor.createRoute(id.ifBlank { "tpl_small" }))
                },
                initialPostId = postId.ifBlank { null },
                onUserClick = { clickedUserId ->
                    navController.navigate(Screen.UserProfile.createRoute(clickedUserId))
                },
            )
        }

        // -------------------------------------------------------------
        // 5b-2. SubPage: Detail Profil Pengguna Publik
        // -------------------------------------------------------------
        composable(
            route = Screen.UserProfile.route,
            arguments = listOf(
                navArgument("userId") {
                    type = NavType.StringType
                    defaultValue = ""
                },
            ),
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId").orEmpty()
            UserProfileDetailScreen(
                userId = userId,
                onBackClick = { navController.popBackStack() },
                onMediaClick = { media ->
                    navController.navigate(Screen.PostsFeed.createRoute(postId = media.id, userId = userId))
                },
                onExhibitClick = { exhibitId ->
                    navController.navigate(Screen.CreateExhibitEditor.createRoute(exhibitId))
                },
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
                    coroutineScope.launch {
                        runCatching { ApiLogin.logout() }
                        sessionManager.clearSession()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
            )
        }

        composable(Screen.SettingsProfile.route) {
            ProfileSettingsRoute(
                onBackClick = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
                onSessionExpired = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }

        composable(Screen.SettingsEmail.route) {
            ChangeEmailRoute(
                viewModel = changeEmailViewModel,
                onBackClick = { navController.popBackStack() },
                onOtpSent = {
                    navController.navigate(Screen.SettingsVerifyEmail.route)
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
        ) {
            VerifyEmailRoute(
                viewModel = changeEmailViewModel,
                onBackClick = { navController.popBackStack() },
                onVerified = {
                    navController.popBackStack(Screen.Settings.route, inclusive = false)
                },
            )
        }

        composable(Screen.SettingsPassword.route) {
            ChangePasswordRoute(
                onBackClick = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
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
            AccountStatusRoute(
                onBackClick = { navController.popBackStack() },
                onDeactivated = {
                    sessionManager.clearSession()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
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
    homeViewModel: HomeViewModel = viewModel(),
    exploreViewModel: ExploreViewModel = viewModel(),
    uiState: HomeUiState = HomeUiState(),
    initialTab: BottomNavTab = BottomNavTab.Home,
    onRequestMeetingClick: (postId: String) -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToPostsFeed: (postId: String, userId: String) -> Unit = { _, _ -> },
    onNavigateToCreatePost: () -> Unit = {},
    onNavigateToCreateExhibit: () -> Unit = {},
    onNavigateToExhibitEditor: (templateIdOrExhibitId: String) -> Unit = {},
    onNavigateToUserProfile: (userId: String) -> Unit = {},
) {
    var currentTab by rememberSaveable { mutableStateOf(initialTab) }
    val homeListState = rememberLazyListState()
    val exploreGridState = rememberLazyGridState()
    val scope = rememberCoroutineScope()
    var lastHomeTapTime by remember { mutableStateOf(0L) }
    var lastExploreTapTime by remember { mutableStateOf(0L) }
    val profileViewModel: ProfileViewModel = viewModel()
    val profileState by profileViewModel.uiState.collectAsStateWithLifecycle()
    val liveHomeState by homeViewModel.uiState.collectAsStateWithLifecycle()
    var isCreateSheetOpen by remember { mutableStateOf(false) }

    LaunchedEffect(currentTab) {
        if (currentTab == BottomNavTab.Home && liveHomeState.posts.isEmpty() && !liveHomeState.isLoading) {
            homeViewModel.muatUlang(filterSeen = false)
        }
        if (currentTab == BottomNavTab.Profile) {
            profileViewModel.muat()
        }
    }

    LifecycleResumeEffect(Unit) {
        if (currentTab == BottomNavTab.Home && liveHomeState.posts.isEmpty() && !liveHomeState.isLoading) {
            homeViewModel.muatUlang(filterSeen = false)
        }
        if (currentTab == BottomNavTab.Profile) {
            profileViewModel.muat()
        }
        onPauseOrDispose { }
    }

    val handleCreateClick = {
        isCreateSheetOpen = true
    }

    if (isCreateSheetOpen) {
        CreateOptionsBottomSheet(
            onDismissRequest = { isCreateSheetOpen = false },
            onCreatePostClick = {
                isCreateSheetOpen = false
                onNavigateToCreatePost()
            },
            onCreateExhibitClick = {
                isCreateSheetOpen = false
                onNavigateToCreateExhibit()
            },
        )
    }

    Crossfade(
        targetState = currentTab,
        animationSpec = tween(durationMillis = 200),
        label = "MainHomeScreenTabCrossfade",
    ) { tab ->
        when (tab) {
            BottomNavTab.Home -> {
                HomeScreen(
                    uiState = liveHomeState,
                    listState = homeListState,
                    onTabSelected = { currentTab = it },
                    onTabReselected = { reselectedTab ->
                        if (reselectedTab == BottomNavTab.Home) {
                            val now = System.currentTimeMillis()
                            if (now - lastHomeTapTime <= 450L) {
                                scope.launch {
                                    homeListState.animateScrollToItem(0)
                                }
                                homeViewModel.muatUlang(filterSeen = true)
                                lastHomeTapTime = 0L
                            } else {
                                lastHomeTapTime = now
                                scope.launch {
                                    homeListState.animateScrollToItem(0)
                                }
                            }
                        }
                    },
                    onRefresh = { homeViewModel.muatUlang(filterSeen = true) },
                    onCreateClick = handleCreateClick,
                    onRequestMeetingClick = onRequestMeetingClick,
                    onView3dClick = { pameranId -> onNavigateToExhibitEditor(pameranId ?: "tpl_small") },
                    onBannerClick = { eventId -> onNavigateToExhibitEditor(eventId) },
                    onLoadMore = homeViewModel::loadNextChunk,
                    onUserClick = onNavigateToUserProfile,
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
                    gridState = exploreGridState,
                    onTabReselected = { reselectedTab ->
                        if (reselectedTab == BottomNavTab.Explore) {
                            val now = System.currentTimeMillis()
                            if (now - lastExploreTapTime <= 450L) {
                                scope.launch {
                                    exploreGridState.animateScrollToItem(0)
                                }
                                exploreViewModel.muatUlang(filterSeen = true)
                                lastExploreTapTime = 0L
                            } else {
                                lastExploreTapTime = now
                                scope.launch {
                                    exploreGridState.animateScrollToItem(0)
                                }
                            }
                        }
                    },
                    onRefresh = { exploreViewModel.muatUlang(filterSeen = true) },
                    onMediaClick = { media -> onNavigateToPostsFeed(media.id, "") },
                    onExhibitClick = { exhibit -> onNavigateToExhibitEditor(exhibit.id) },
                    onAccountClick = onNavigateToUserProfile,
                    viewModel = exploreViewModel,
                )
            }
            BottomNavTab.Profile -> {
                ProfileScreen(
                    uiState = profileState,
                    onTabSelected = { currentTab = it },
                    onCreateClick = handleCreateClick,
                    onMenuClick = onNavigateToSettings,
                    onProfileTabChange = profileViewModel::pilihTab,
                    onSkillSheetToggle = profileViewModel::aturSkillSheet,
                    onRetryClick = profileViewModel::muat,
                    onMediaClick = { media ->
                        if (profileState.selectedTab == com.ven.app.ui.screens.profile.ProfileTab.Exhibits) {
                            onNavigateToExhibitEditor(media.id)
                        } else {
                            onNavigateToPostsFeed(media.id, "me")
                        }
                    },
                )
            }
        }
    }
}
