package com.ven.app.ui.screens.ai.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ven.app.R
import com.ven.app.ui.icons.VexIcon
import com.ven.app.ui.icons.VexIcons
import com.ven.app.ui.screens.ai.AiChatDummyData
import com.ven.app.ui.screens.ai.ChatConversation
import com.ven.app.ui.theme.ElmsSans
import com.ven.app.ui.theme.ThemeMode
import com.ven.app.ui.theme.VexSize
import com.ven.app.ui.theme.VexSpace
import com.ven.app.ui.theme.VexTheme

/**
 * Konten Drawer Laci Riwayat Percakapan AI (DESIGN.md §8.12.5).
 * Sesuai wireframe `Wireframe - AI Chat-2.png`:
 * - Lebar laci: 300dp (VexSize.drawerWidth).
 * - Latar Belakang: Warna dasar #1F1F1F (colors.bg).
 * - Baris "New chat": Ikon pensil kotak (VexIcons.NewChat) + teks "New chat".
 * - Label Seksi: "Recents" (colors.textMuted).
 * - Daftar percakapan terdahulu (ChatHistoryItem).
 */
@Composable
fun ChatHistoryDrawer(
    chatHistory: List<ChatConversation>,
    selectedChatId: String?,
    onNewChatClick: () -> Unit,
    onSelectChat: (ChatConversation) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = VexTheme.colors

    Box(
        modifier = modifier
            .width(VexSize.drawerWidth)
            .fillMaxHeight()
            .background(colors.bg)
            .statusBarsPadding(),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Spacer(modifier = Modifier.height(VexSpace.s4))

            // 1. Aksi Sesi Baru ("New chat")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = VexSize.minTouch)
                    .clickable(onClick = onNewChatClick)
                    .padding(horizontal = VexSpace.s6),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                VexIcon(
                    id = VexIcons.NewChat,
                    contentDescription = stringResource(R.string.ai_chat_new_chat),
                    tint = colors.text,
                    size = VexSize.icon,
                )

                Spacer(modifier = Modifier.width(VexSpace.s4))

                Text(
                    text = stringResource(R.string.ai_chat_new_chat),
                    style = TextStyle(
                        fontFamily = ElmsSans,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.text,
                    ),
                )
            }

            Spacer(modifier = Modifier.height(VexSpace.s4))

            // 2. Label Seksi "Recents"
            Text(
                text = stringResource(R.string.ai_chat_recents),
                style = TextStyle(
                    fontFamily = ElmsSans,
                    fontSize = 14.sp,
                    color = colors.textMuted,
                ),
                modifier = Modifier.padding(horizontal = VexSpace.s6, vertical = VexSpace.s2),
            )

            // 3. Daftar Riwayat Percakapan
            if (chatHistory.isEmpty()) {
                Text(
                    text = stringResource(R.string.ai_chat_empty_history),
                    style = TextStyle(
                        fontFamily = ElmsSans,
                        fontSize = 14.sp,
                        color = colors.textDisabled,
                    ),
                    modifier = Modifier.padding(horizontal = VexSpace.s6, vertical = VexSpace.s3),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    items(
                        items = chatHistory,
                        key = { it.id },
                    ) { conversation ->
                        ChatHistoryItem(
                            title = conversation.title,
                            isSelected = conversation.id == selectedChatId,
                            onClick = { onSelectChat(conversation) },
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "ChatHistoryDrawer - Dark")
@Composable
private fun ChatHistoryDrawerDarkPreview() {
    VexTheme(mode = ThemeMode.Dark) {
        ChatHistoryDrawer(
            chatHistory = AiChatDummyData.sampleConversations,
            selectedChatId = "chat_1",
            onNewChatClick = {},
            onSelectChat = {},
        )
    }
}

@Preview(name = "ChatHistoryDrawer - Light")
@Composable
private fun ChatHistoryDrawerLightPreview() {
    VexTheme(mode = ThemeMode.Light) {
        ChatHistoryDrawer(
            chatHistory = AiChatDummyData.sampleConversations,
            selectedChatId = "chat_1",
            onNewChatClick = {},
            onSelectChat = {},
        )
    }
}
