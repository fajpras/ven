package com.ven.app

import android.app.Application
import com.ven.app.core.localization.AppStrings

class VenApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
        AppStrings.init(this)
    }

    companion object {
        lateinit var instance: VenApplication
            private set
    }
}
