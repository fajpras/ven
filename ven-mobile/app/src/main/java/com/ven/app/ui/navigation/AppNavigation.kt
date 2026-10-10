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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ven.app.ui.components.layout.AppScaffold
import com.ven.app.ui.screens.auth.ForgotPasswordEvent
import com.ven.app.ui.screens.auth.ForgotPasswordScreen
import com.ven.app.ui.screens.auth.ForgotPasswordViewModel
import com.ven.app.ui.screens.auth.LoginRoute
import com.ven.app.ui.screens.auth.RegisterRoute
import com.ven.app.ui.screens.auth.ResetPasswordScreen
import com.ven.app.ui.screens.auth.VerifyOtpScreen
import com.ven.app.ui.screens.home.HomeScreen
import com.ven.app.ui.screens.home.HomeUiState
import com.ven.app.ui.screens.home.SampleHomeUiState
import com.ven.app.ui.screens.onboarding.InterestScreen
import com.ven.app.ui.theme.VexTheme

/**
 * Host navigasi utama aplikasi VEN (Virtual Exhibition).
 * Menghubungkan seluruh alur autentikasi, onboarding, dan destinasi utama.
 */
@Composable
fun AppNavHost() {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager.getInstance(context) }
    val startDestination = if (sessionManager.isLoggedIn()) Screen.Home.route else Screen.Login.route
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
