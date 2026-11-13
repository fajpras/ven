package com.ven.app.ui.screens.ai

import androidx.compose.runtime.Immutable

enum class ChatSender {
    User,
    Ai,
}

enum class MessageStatus {
    Sent,
    Sending,
    Error,
}

@Immutable
data class ChatMessage(
    val id: String,
    val sender: ChatSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: MessageStatus = MessageStatus.Sent,
)

@Immutable
data class ChatConversation(
    val id: String,
    val title: String,
    val lastUpdated: Long = System.currentTimeMillis(),
    val messages: List<ChatMessage> = emptyList(),
)

enum class AiScreenState {
    Empty,
    Conversation,
}

@Immutable
data class AiChatUiState(
    val currentChatId: String? = null,
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isAiTyping: Boolean = false,
    val chatHistory: List<ChatConversation> = emptyList(),
    val isDrawerOpen: Boolean = false,
    val errorMessage: String? = null,
) {
    val screenState: AiScreenState
        get() = if (messages.isEmpty()) AiScreenState.Empty else AiScreenState.Conversation

    val canSend: Boolean
        get() = inputText.isNotBlank() && !isAiTyping
}
