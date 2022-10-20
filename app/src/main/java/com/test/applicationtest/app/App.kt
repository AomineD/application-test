package com.test.applicationtest.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import es.dmoral.toasty.Toasty

@HiltAndroidApp
class App: Application() {
    override fun onCreate() {
        super.onCreate()
        // Toast configurations
        Toasty.Config.getInstance()
            .tintIcon(false)
            .setTextSize(24)
            .allowQueue(true)
            .supportDarkTheme(true)
            .apply()
    }
}