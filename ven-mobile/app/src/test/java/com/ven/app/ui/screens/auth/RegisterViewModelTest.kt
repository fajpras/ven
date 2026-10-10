package com.ven.app.ui.screens.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RegisterViewModelTest {

    @Test
    fun passwordValidator_checksAllRules() {
        // Weak password - missing uppercase, digit, symbol, < 8 chars
        val weak = "abc"
        val weakRules = PasswordValidator.rules(weak)
        assertEquals(5, weakRules.size)
        assertFalse(PasswordValidator.isStrong(weak))

        // Strong password - meets all requirements
        val strong = "Pass1234!"
        val strongRules = PasswordValidator.rules(strong)
        assertTrue(strongRules.all { it.isMet })
        assertTrue(PasswordValidator.isStrong(strong))
    }

    @Test
    fun registerUiState_canSubmit_returnsTrueOnlyWhenValid() {
        val invalidState = RegisterUiState(
            email = "invalid-email",
            username = "ab",
            password = "123",
            confirmPassword = "123"
        )
        assertFalse(invalidState.canSubmit)

        val validState = RegisterUiState(
            email = "user@example.com",
            username = "username",
            password = "Password123!",
            confirmPassword = "Password123!"
        )
        assertTrue(validState.isUsernameValid)
        assertTrue(validState.isPasswordStrong)
        assertTrue(validState.isPasswordMatch)
        assertTrue(validState.canSubmit)
    }

    @Test
    fun registerUiState_canSubmit_falseWhenLoading() {
        val loadingState = RegisterUiState(
            email = "user@example.com",
            username = "username",
            password = "Password123!",
            confirmPassword = "Password123!",
            isLoading = true
        )
        assertFalse(loadingState.canSubmit)
    }
}
