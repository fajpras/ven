package com.ven.app.ui.screens.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ven.app.R
import com.ven.app.VenApplication
import com.ven.app.core.localization.AppStrings
import com.ven.app.data.api.ApiAiChat
import com.ven.app.data.api.SessionManager
import com.ven.app.data.preference.AiChatHistoryPreferences
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel untuk halaman AI Assistant (DESIGN.md §8.12.6, UC-18 Tanya AI).
 * Mengelola state percakapan, drawer riwayat lokal persisten per pengguna, serta interaksi API online / fallback offline.
 */
class AiChatViewModel(
    private val historyPrefs: AiChatHistoryPreferences? = try {
        AiChatHistoryPreferences.getInstance(VenApplication.instance)
    } catch (_: Exception) {
        null
    },
) : ViewModel() {

    private val currentUserId: String
        get() = try {
            SessionManager.getInstance(VenApplication.instance).getUser()?.idPengguna ?: "guest"
        } catch (_: Exception) {
            "guest"
        }

    private val _uiState = MutableStateFlow(
        AiChatUiState(
            chatHistory = historyPrefs?.loadHistory(
                try {
                    SessionManager.getInstance(VenApplication.instance).getUser()?.idPengguna ?: "guest"
                } catch (_: Exception) {
                    "guest"
                }
            ) ?: emptyList(),
        )
    )
    val uiState: StateFlow<AiChatUiState> = _uiState.asStateFlow()

    fun onInputChange(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun onSendMessage() {
        val currentInput = _uiState.value.inputText.trim()
        if (currentInput.isBlank() || _uiState.value.isAiTyping) return

        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            sender = ChatSender.User,
            text = currentInput,
            timestamp = System.currentTimeMillis(),
            status = MessageStatus.Sent,
        )

        val updatedMessages = _uiState.value.messages + userMessage
        val activeChatId = _uiState.value.currentChatId ?: UUID.randomUUID().toString()

        _uiState.update { current ->
            current.copy(
                currentChatId = activeChatId,
                messages = updatedMessages,
                inputText = "",
                isAiTyping = true,
                errorMessage = null,
            )
        }

        viewModelScope.launch {
            val result = ApiAiChat.tanya(currentInput)
            val rawResponse = result.getOrNull()

            val aiResponseText = if (rawResponse != null && !rawResponse.contains("belum dikonfigurasi")) {
                rawResponse
            } else {
                getFallbackResponse(currentInput)
            }

            val aiMessage = ChatMessage(
                id = UUID.randomUUID().toString(),
                sender = ChatSender.Ai,
                text = aiResponseText,
                timestamp = System.currentTimeMillis(),
                status = MessageStatus.Sent,
            )

            _uiState.update { current ->
                val finalMessages = current.messages + aiMessage
                val existingHistory = current.chatHistory.toMutableList()
                val index = existingHistory.indexOfFirst { it.id == activeChatId }

                val conversationTitle = if (currentInput.length > 28) {
                    currentInput.take(28) + "..."
                } else {
                    currentInput
                }

                if (index != -1) {
                    existingHistory[index] = existingHistory[index].copy(
                        lastUpdated = System.currentTimeMillis(),
                        messages = finalMessages,
                    )
                } else {
                    existingHistory.add(
                        0,
                        ChatConversation(
                            id = activeChatId,
                            title = conversationTitle,
                            lastUpdated = System.currentTimeMillis(),
                            messages = finalMessages,
                        )
                    )
                }

                historyPrefs?.saveHistory(currentUserId, existingHistory)

                current.copy(
                    messages = finalMessages,
                    isAiTyping = false,
                    chatHistory = existingHistory,
                )
            }
        }
    }

    fun onNewChat() {
        _uiState.update { current ->
            current.copy(
                currentChatId = null,
                messages = emptyList(),
                inputText = "",
                isAiTyping = false,
                isDrawerOpen = false,
                errorMessage = null,
            )
        }
    }

    fun onSelectConversation(conversation: ChatConversation) {
        _uiState.update { current ->
            current.copy(
                currentChatId = conversation.id,
                messages = conversation.messages,
                inputText = "",
                isAiTyping = false,
                isDrawerOpen = false,
                errorMessage = null,
            )
        }
    }

    fun onToggleDrawer(isOpen: Boolean) {
        _uiState.update { it.copy(isDrawerOpen = isOpen) }
    }

    fun onDismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun getFallbackResponse(input: String): String {
        val lower = input.lowercase()
        val isAboutVen = lower.contains("karya") || lower.contains("pameran") ||
            lower.contains("seni") || lower.contains("art") || lower.contains("exhibit") ||
            lower.contains("expo") || lower.contains("kreator") || lower.contains("creator") ||
            lower.contains("3d") || lower.contains("virtual") || lower.contains("ven")

        return if (isAboutVen) {
            "Layanan AI sedang tidak dapat dijangkau. Mohon periksa koneksi internet Anda atau coba beberapa saat lagi."
        } else {
            AppStrings.get(R.string.ai_chat_out_of_scope_rejection)
        }
    }
}
