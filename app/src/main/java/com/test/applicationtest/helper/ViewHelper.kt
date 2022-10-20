package com.test.applicationtest.helper

import android.app.AlertDialog
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.app.ActivityCompat
import com.airbnb.lottie.LottieAnimationView
import com.test.applicationtest.R

object ViewHelper {

    fun AlertDialog.fixBackground() {
        if (window != null) {
            val w = window!!
            w.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
            w.setLayout(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
            w.setBackgroundDrawable(ActivityCompat.getDrawable(context, R.color.transparent))
        }
    }

    fun LottieAnimationView.playLimit(from:Int, to:Int){
        frame = from
        setMinAndMaxFrame(from, to)
        playAnimation()
    }

}