package com.ven.app.unity

import android.content.Context
import com.ven.app.util.UnityLauncher as UtilUnityLauncher

object UnityLauncher {
    fun openEditor(
        context: Context,
        templateId: String,
        authToken: String = "",
        title: String = "",
        description: String = "",
        bannerUrl: String = "",
    ) {
        UtilUnityLauncher.launchEditor(context, templateId, title, description, bannerUrl)
    }
}
