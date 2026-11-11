package com.ven.app

import android.app.Application
import com.ven.app.core.localization.AppStrings

class VenApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppStrings.init(this)
    }
}
