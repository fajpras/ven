package com.ven.app.core.localization

import android.content.Context
import androidx.annotation.StringRes
import java.util.Locale

object AppStrings {
    private var context: Context? = null

    fun init(context: Context) {
        this.context = context.applicationContext
    }

    fun get(@StringRes id: Int, vararg args: Any): String {
        return context?.getString(id, *args).orEmpty()
    }

    fun locale(): Locale {
        return context?.resources?.configuration?.locales?.get(0) ?: Locale.getDefault()
    }
}
