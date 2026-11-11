package com.ven.app.util

import android.content.Context
import android.content.Intent
import com.unity3d.player.UnityPlayerGameActivity
import com.ven.app.core.config.AppConfig
import com.ven.app.data.api.SessionManager

object UnityLauncher {

    /**
     * Memetakan ID template dari format VEN mobile ke format template Unity yang ada di TemplateLoader:
     * - tpl_small  -> template01 (9 panels)
     * - tpl_medium -> template02 (14 panels)
     * - tpl_big    -> template03 (21 panels)
     * - tpl_event  -> template04 (60 panels)
     */
    fun mapTemplateId(templateId: String): String {
        return when (templateId.trim().lowercase()) {
            "tpl_small", "small", "template01" -> "template01"
            "tpl_medium", "medium", "template02" -> "template02"
            "tpl_big", "big", "template03" -> "template03"
            "tpl_event", "event", "template04" -> "template04"
            else -> templateId.ifBlank { "template01" }
        }
    }

    fun createEditorIntent(
        context: Context,
        templateId: String = "template01",
        title: String = "",
        description: String = "",
    ): Intent {
        val sessionManager = SessionManager.getInstance(context)
        val token = sessionManager.getToken().orEmpty()
        val baseUrl = AppConfig.baseUrl
        val finalTemplateId = mapTemplateId(templateId)

        return Intent(context, UnityPlayerGameActivity::class.java).apply {
            putExtra("templateId", finalTemplateId)
            putExtra("template_id", finalTemplateId)
            putExtra("title", title)
            putExtra("description", description)
            putExtra("authToken", token)
            putExtra("token", token)
            putExtra("apiBaseUrl", baseUrl)
            putExtra("api_base_url", baseUrl)
        }
    }

    fun createViewerIntent(context: Context, roomIdOrExhibitId: String): Intent {
        val sessionManager = SessionManager.getInstance(context)
        val token = sessionManager.getToken().orEmpty()
        val baseUrl = AppConfig.baseUrl
        val finalTemplateId = mapTemplateId(roomIdOrExhibitId)

        return Intent(context, UnityPlayerGameActivity::class.java).apply {
            putExtra("templateId", finalTemplateId)
            putExtra("template_id", finalTemplateId)
            putExtra("roomId", roomIdOrExhibitId)
            putExtra("room_id", roomIdOrExhibitId)
            putExtra("authToken", token)
            putExtra("token", token)
            putExtra("apiBaseUrl", baseUrl)
            putExtra("api_base_url", baseUrl)
        }
    }

    fun launchEditor(
        context: Context,
        templateId: String = "template01",
        title: String = "",
        description: String = "",
    ) {
        val intent = createEditorIntent(context, templateId, title, description).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    fun launchViewer(context: Context, roomIdOrExhibitId: String) {
        val intent = createViewerIntent(context, roomIdOrExhibitId).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}
