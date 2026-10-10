package com.ven.app.ui.screens.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class LoginViewModelTest {

    @Test
    fun initialState_hasDefaultEmptyValues() {
        val state = LoginUiState()
        assertEquals("", state.identifier)
        assertEquals("", state.password)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
    }

    @Test
    fun viewModel_stateUpdatesCorrectly() {
        val vm = LoginViewModel()

        vm.onIdentifierChange("testuser@example.com")
        assertEquals("testuser@example.com", vm.state.identifier)

        vm.onPasswordChange("Secret123!")
        assertEquals("Secret123!", vm.state.password)
    }
}
