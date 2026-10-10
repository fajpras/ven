package com.ven.app.data.api

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.ven.app.data.api.models.UserModel

class SessionManager(context: Context) {

    private val preferences: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveToken(token: String?) {
        preferences.edit().putString(KEY_TOKEN, token).apply()
    }

    fun getToken(): String? {
        return preferences.getString(KEY_TOKEN, null)
    }

    fun saveUser(user: UserModel?) {
        val userJson = if (user != null) gson.toJson(user) else null
        preferences.edit().putString(KEY_USER, userJson).apply()
    }

    fun getUser(): UserModel? {
        val userJson = preferences.getString(KEY_USER, null) ?: return null
        return try {
            gson.fromJson(userJson, UserModel::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun clearSession() {
        preferences.edit().clear().apply()
    }

    fun isLoggedIn(): Boolean {
        return !getToken().isNullOrBlank()
    }

    companion object {
        private const val PREF_NAME = "ven_auth_pref"
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_USER = "auth_user"

        @Volatile
        private var INSTANCE: SessionManager? = null

        fun getInstance(context: Context): SessionManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SessionManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
