package com.test.applicationtest.app

import android.app.Application
import com.test.applicationtest.R
import com.test.applicationtest.helper.DataConverter.pixelsToSp
import dagger.hilt.android.HiltAndroidApp
import es.dmoral.toasty.Toasty

@HiltAndroidApp
class App: Application() {
    override fun onCreate() {
        super.onCreate()


        //Since getDimension() receive pixels, we need to convert it to Sp
        val sizeSp = resources.getDimension(R.dimen.txt_size_titles).pixelsToSp(this)
        // Toast configurations
        Toasty.Config.getInstance()
            .tintIcon(false)
            .setTextSize(sizeSp)
            .allowQueue(true)
            .supportDarkTheme(true)
            .apply()
    }

}