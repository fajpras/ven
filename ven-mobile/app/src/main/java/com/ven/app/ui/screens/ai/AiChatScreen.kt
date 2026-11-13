package com.ven.app.ui.screens.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ven.app.ui.components.layout.AppScaffold
import com.ven.app.ui.navigation.BottomNavTab
import com.ven.app.ui.screens.ai.components.AiChatEmptyState
import com.ven.app.ui.screens.ai.components.AiChatInput
import com.ven.app.ui.screens.ai.components.AiChatTopBar
import com.ven.app.ui.screens.ai.components.AiTypingIndicator
import com.ven.app.ui.screens.ai.components.ChatBubbleUser
import com.ven.app.ui.screens.ai.components.ChatHistoryDrawer
import com.ven.app.ui.screens.ai.components.ChatMessageAi
import com.ven.app.ui.theme.ThemeMode
import com.ven.app.ui.theme.VexSpace
import com.ven.app.ui.theme.VexTheme
import kotlinx.coroutines.launch

/**
 * Layar utama AI Assistant / AI Chat untuk VEN (Virtual Exhibition).
 * Sesuai spesifikasi DESIGN.md §8.12 dan observasi 4 wireframe resmi:
 * - Wireframe - AI Chat.png (Empty state: watermark sparkle, input pill di tengah kanvas vertikal, label).
 * - Wireframe - AI Chat-1.png (Conversation state: bubble user kanan ber-ekor, pesan AI netral justify).
 * - Wireframe - AI Chat-2.png (History Drawer 300dp: New chat, Recents, daftar percakapan).
 * - Wireframe - AI Chat-3.png (Input active: tombol kirim dinamis, responsif keyboard via imePadding).
 */
@Composable
fun AiChatScreen(
    onTabSelected: (BottomNavTab) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AiChatViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    AiChatScreenContent(
        uiState = uiState,
        onTabSelected = onTabSelected,
        onInputChange = viewModel::onInputChange,
        onSendMessage = viewModel::onSendMessage,
        onNewChat = viewModel::onNewChat,
        onSelectConversation = viewModel::onSelectConversation,
        modifier = modifier,
    )
}

/**
 * Composable Stateless untuk konten layar AI Assistant (memungkinkan pengujian & pratinjau fleksibel).
 */
@Composable
fun AiChatScreenContent(
    uiState: AiChatUiState,
    onTabSelected: (BottomNavTab) -> Unit,
    onInputChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onNewChat: () -> Unit,
    onSelectConversation: (ChatConversation) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = VexTheme.colors
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Otomatis auto-scroll ke pesan terbaru saat percakapan bertambah
    LaunchedEffect(uiState.messages.size, uiState.isAiTyping) {
        if (uiState.messages.isNotEmpty()) {
            val targetIndex = if (uiState.isAiTyping) uiState.messages.size else uiState.messages.size - 1
            if (targetIndex >= 0) {
                listState.animateScrollToItem(targetIndex)
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        scrimColor = colors.scrim,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color.Transparent,
                drawerShape = RoundedCornerShape(0.dp),
            ) {
                ChatHistoryDrawer(
                    chatHistory = uiState.chatHistory,
                    selectedChatId = uiState.currentChatId,
                    onNewChatClick = {
                        onNewChat()
                        coroutineScope.launch { drawerState.close() }
                    },
                    onSelectChat = { conversation ->
                        onSelectConversation(conversation)
                        coroutineScope.launch { drawerState.close() }
                    },
                )
            }
        },
        modifier = modifier,
    ) {
        AppScaffold(
            currentTab = BottomNavTab.Ai,
            onTabSelected = onTabSelected,
            topBar = {
                AiChatTopBar(
                    onMenuClick = {
                        coroutineScope.launch { drawerState.open() }
                    },
                )
            },
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(colors.bg),
            ) {
                when (uiState.screenState) {
                    AiScreenState.Empty -> {
                        // State Kosong: Sparkle + input pill tengah + label "AI assistant"
                        AiChatEmptyState(
                            inputText = uiState.inputText,
                            onInputChange = onInputChange,
                            onSend = onSendMessage,
                            modifier = Modifier.imePadding(),
                        )
                    }

                    AiScreenState.Conversation -> {
                        // State Percakapan: Daftar pesan scrollable + input pill di atas BottomNav
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .imePadding(),
                        ) {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentPadding = PaddingValues(vertical = VexSpace.s4),
                                verticalArrangement = Arrangement.spacedBy(20.dp),
                            ) {
                                items(
                                    items = uiState.messages,
                                    key = { it.id },
                                ) { message ->
                                    when (message.sender) {
                                        ChatSender.User -> {
                                            ChatBubbleUser(text = message.text)
                                        }

                                        ChatSender.Ai -> {
                                            ChatMessageAi(text = message.text)
                                        }
                                    }
                                }

                                if (uiState.isAiTyping) {
                                    item(key = "typing_indicator") {
                                        AiTypingIndicator()
                                    }
                                }
                            }

                            // Input pill melayang di dasar layar
                            AiChatInput(
                                value = uiState.inputText,
                                onValueChange = onInputChange,
                                onSend = onSendMessage,
                                modifier = Modifier
                                    .padding(horizontal = 24.dp)
                                    .padding(bottom = 12.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------
// Previews untuk Mode Dark dan Light
// ---------------------------------------------------------

@Preview(name = "AiChatScreen - Empty State - Dark")
@Composable
private fun AiChatScreenEmptyDarkPreview() {
    VexTheme(mode = ThemeMode.Dark) {
        AiChatScreenContent(
            uiState = AiChatDummyData.initialEmptyUiState,
            onTabSelected = {},
            onInputChange = {},
            onSendMessage = {},
            onNewChat = {},
            onSelectConversation = {},
        )
    }
}

@Preview(name = "AiChatScreen - Conversation State - Dark")
@Composable
private fun AiChatScreenConversationDarkPreview() {
    VexTheme(mode = ThemeMode.Dark) {
        AiChatScreenContent(
            uiState = AiChatDummyData.initialConversationUiState,
            onTabSelected = {},
            onInputChange = {},
            onSendMessage = {},
            onNewChat = {},
            onSelectConversation = {},
        )
    }
}

@Preview(name = "AiChatScreen - Conversation State - Light")
@Composable
private fun AiChatScreenConversationLightPreview() {
    VexTheme(mode = ThemeMode.Light) {
        AiChatScreenContent(
            uiState = AiChatDummyData.initialConversationUiState,
            onTabSelected = {},
            onInputChange = {},
            onSendMessage = {},
            onNewChat = {},
            onSelectConversation = {},
        )
    }
}
