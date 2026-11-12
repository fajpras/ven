package com.ven.app.data.preference

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ven.app.ui.screens.ai.ChatConversation

/**
 * Penyimpanan riwayat obrolan AI secara persisten di aplikasi Kreator (UC-18).
 */
class AiChatHistoryPreferences private constructor(context: Context) {

    private val preferences: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    private val listType = object : TypeToken<List<ChatConversation>>() {}.type

    fun loadHistory(userId: String = "guest"): List<ChatConversation> {
        val key = "${KEY_CHAT_HISTORY}_$userId"
        val json = preferences.getString(key, null) ?: return emptyList()
        return try {
            gson.fromJson<List<ChatConversation>>(json, listType) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveHistory(userId: String = "guest", history: List<ChatConversation>) {
        try {
            val key = "${KEY_CHAT_HISTORY}_$userId"
            val json = gson.toJson(history, listType)
            preferences.edit().putString(key, json).apply()
        } catch (_: Exception) {
        }
    }

    fun clearHistory(userId: String = "guest") {
        val key = "${KEY_CHAT_HISTORY}_$userId"
        preferences.edit().remove(key).apply()
    }

    companion object {
        private const val PREF_NAME = "ven_ai_chat_pref"
        private const val KEY_CHAT_HISTORY = "key_ai_chat_history"

        @Volatile
        private var INSTANCE: AiChatHistoryPreferences? = null

        fun getInstance(context: Context): AiChatHistoryPreferences {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AiChatHistoryPreferences(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
