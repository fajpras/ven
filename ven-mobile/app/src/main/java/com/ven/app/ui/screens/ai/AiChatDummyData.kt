package com.ven.app.ui.screens.ai

/**
 * Dataset dummy lokal untuk layar AI Assistant (DESIGN.md §8.12.6).
 * Memuat percakapan mockup sesuai wireframe `Wireframe - AI Chat-1.png` dan `Wireframe - AI Chat-2.png`.
 */
object AiChatDummyData {

    const val INITIAL_USER_MESSAGE =
        "carikan saya 10 karya terkeren yang sesuai dengan kategori yang saya pilih"

    const val INITIAL_AI_RESPONSE =
        "Tentu! Aku sudah menemukan 10 karya yang paling menarik untuk kamu jelajahi. Berikut beberapa karya pilihan yang bisa kamu lihat:\n\n" +
            "1. Poster Seni Abstrak Modern\n" +
            "2. Desain Karakter Sci-Fi 3D\n" +
            "3. Tipografi Kreatif Nusantara\n" +
            "4. Lukisan Digital Lanskap Cyberpunk\n" +
            "5. UI/UX Konsep Smart Home Dashboard\n" +
            "6. Animasi Karakter Isometrik\n" +
            "7. Eksplorasi Brand Identity Kopi Lokal\n" +
            "8. Ilustrasi Vektor Minimalis Alam\n" +
            "9. Model Arsitektur Paviliun Futuristik\n" +
            "10. Desain Kemasan Ramah Lingkungan\n\n" +
            "Kamu bisa memilih salah satu karya untuk melihat detailnya atau langsung menjelajahi ruang 3D-nya."

    val sampleMessages: List<ChatMessage> = listOf(
        ChatMessage(
            id = "msg_1",
            sender = ChatSender.User,
            text = INITIAL_USER_MESSAGE,
            timestamp = 1727164800000L,
            status = MessageStatus.Sent,
        ),
        ChatMessage(
            id = "msg_2",
            sender = ChatSender.Ai,
            text = INITIAL_AI_RESPONSE,
            timestamp = 1727164805000L,
            status = MessageStatus.Sent,
        ),
    )

    val sampleConversations: List<ChatConversation> = listOf(
        ChatConversation(
            id = "chat_1",
            title = "Rekomendasi karya terkeren",
            lastUpdated = 1727164805000L,
            messages = sampleMessages,
        ),
        ChatConversation(
            id = "chat_2",
            title = "Pameran yang sesuai den...",
            lastUpdated = 1727078400000L,
            messages = listOf(
                ChatMessage(
                    id = "msg_2_1",
                    sender = ChatSender.User,
                    text = "Ada pameran yang sesuai dengan minat desain grafis?",
                ),
                ChatMessage(
                    id = "msg_2_2",
                    sender = ChatSender.Ai,
                    text = "Tentu! Ada pameran 'PBL EXPO 2026' dan 'Open Source Design Festival' yang sedang berlangsung minggu ini.",
                ),
            ),
        ),
        ChatConversation(
            id = "chat_3",
            title = "List karya milik Gra._.aph",
            lastUpdated = 1726992000000L,
            messages = listOf(
                ChatMessage(
                    id = "msg_3_1",
                    sender = ChatSender.User,
                    text = "Bisa tunjukkan karya-karya terbaru dari Gra._.aph?",
                ),
                ChatMessage(
                    id = "msg_3_2",
                    sender = ChatSender.Ai,
                    text = "Gra._.aph memiliki 12 karya unggulan seputar ilustrasi 3D dan poster digital. Anda dapat melihatnya langsung di ruang virtual miliknya.",
                ),
            ),
        ),
    )

    val initialEmptyUiState = AiChatUiState(
        messages = emptyList(),
        chatHistory = sampleConversations,
    )

    val initialConversationUiState = AiChatUiState(
        currentChatId = "chat_1",
        messages = sampleMessages,
        chatHistory = sampleConversations,
    )
}
