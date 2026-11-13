package com.ven.app.ui.screens.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ven.app.data.api.ApiAiChat
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel untuk halaman AI Assistant (DESIGN.md §8.12.6).
 * Mengelola state percakapan, drawer riwayat, serta interaksi API online / fallback offline mockup.
 */
class AiChatViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        AiChatUiState(
            chatHistory = AiChatDummyData.sampleConversations,
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
            val aiResponseText = result.getOrElse {
                // Fallback graceful untuk demo offline / jika API key backend belum diisi
                AiChatDummyData.INITIAL_AI_RESPONSE
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
}
