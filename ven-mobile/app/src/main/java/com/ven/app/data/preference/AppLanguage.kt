package com.ven.app.data.preference

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

enum class AppLanguage(val tag: String) {
    English("en"),
    Indonesian("in"),
    ;

    companion object {
        fun fromTag(tag: String?): AppLanguage {
            return when {
                tag == null -> English
                tag.startsWith("in") || tag.startsWith("id") -> Indonesian
                else -> English
            }
        }

        fun current(): AppLanguage {
            return fromTag(AppCompatDelegate.getApplicationLocales().toLanguageTags())
        }

        fun select(language: AppLanguage) {
            AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(language.tag),
            )
        }
    }
}
