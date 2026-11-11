package com.ven.app.unity

import android.content.Context
import android.content.Intent
import com.unity3d.player.UnityPlayerGameActivity
import com.ven.app.BuildConfig

object UnityLauncher {
    fun openEditor(context: Context, templateId: String, authToken: String) {
        val intent = Intent(context, UnityPlayerGameActivity::class.java).apply {
            putExtra("templateId", templateId)
            putExtra("authToken", authToken)
            putExtra("apiBaseUrl", BuildConfig.BASE_URL)
        }
        context.startActivity(intent)
    }
}
