package com.ven.app.ui.screens.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ForgotPasswordViewModelTest {

    @Test
    fun initialState_hasDefaultEmptyValues() {
        val state = ForgotPasswordUiState()
        assertEquals("", state.email)
        assertEquals("", state.otp)
        assertEquals("", state.newPassword)
        assertEquals("", state.confirmPassword)
        assertEquals(0, state.resendCooldown)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertFalse(state.isOtpComplete)
    }

    @Test
    fun isOtpComplete_trueOnlyWhenSixDigits() {
        val stateShort = ForgotPasswordUiState(otp = "12345")
        assertFalse(stateShort.isOtpComplete)

        val stateExact = ForgotPasswordUiState(otp = "123456")
        assertTrue(stateExact.isOtpComplete)

        val stateLong = ForgotPasswordUiState(otp = "1234567")
        assertFalse(stateLong.isOtpComplete)
    }

    @Test
    fun isPasswordMatch_trueOnlyWhenMatchingAndNotEmpty() {
        val stateEmpty = ForgotPasswordUiState(newPassword = "", confirmPassword = "")
        assertTrue(stateEmpty.isPasswordMatch)

        val stateMismatch = ForgotPasswordUiState(newPassword = "Pass1234!", confirmPassword = "Pass")
        assertFalse(stateMismatch.isPasswordMatch)

        val stateMatch = ForgotPasswordUiState(newPassword = "Pass1234!", confirmPassword = "Pass1234!")
        assertTrue(stateMatch.isPasswordMatch)
    }

    @Test
    fun canReset_requiresStrongPasswordAndMatchAndNotLoading() {
        // Weak password
        val weakState = ForgotPasswordUiState(
            newPassword = "weak",
            confirmPassword = "weak",
            isLoading = false,
        )
        assertFalse(weakState.canReset)

        // Strong password matching
        val strongState = ForgotPasswordUiState(
            newPassword = "Password123!",
            confirmPassword = "Password123!",
            isLoading = false,
        )
        assertTrue(strongState.canReset)

        // Strong password but loading
        val loadingState = strongState.copy(isLoading = true)
        assertFalse(loadingState.canReset)
    }

    @Test
    fun viewModel_stateUpdatesCorrectly() {
        val vm = ForgotPasswordViewModel()

        vm.onEmailChange("test@example.com")
        assertEquals("test@example.com", vm.uiState.value.email)

        vm.onOtpChange("654321")
        assertEquals("654321", vm.uiState.value.otp)
        assertTrue(vm.uiState.value.isOtpComplete)

        vm.onNewPasswordChange("NewSecret1!")
        assertEquals("NewSecret1!", vm.uiState.value.newPassword)

        vm.onConfirmPasswordChange("NewSecret1!")
        assertEquals("NewSecret1!", vm.uiState.value.confirmPassword)
        assertTrue(vm.uiState.value.canReset)
    }
}
