package com.test.applicationtest.ui.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import com.airbnb.lottie.LottieDrawable
import com.test.applicationtest.R
import com.test.applicationtest.databinding.PopUpLoadingBinding
import com.test.applicationtest.databinding.PopUpSelectionBinding
import com.test.applicationtest.helper.CoroutinesHelper.ioSafe
import com.test.applicationtest.helper.CoroutinesHelper.main
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay

class PopUpSelectMap(context: Context, private val callBack: suspend (CoroutineScope.(Int) -> Unit)) : AlertDialog(context) {

    private lateinit var binding:PopUpSelectionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        binding = PopUpSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)


        setCanceledOnTouchOutside(false)
        setCancelable(false)

        binding.mapSdkBtn.setOnClickListener {
            ioSafe {
                callBack(0)
            }
            dismiss()
        }

        binding.webViewBtn.setOnClickListener {
            ioSafe {
                callBack(1)
            }
            dismiss()
        }
    }

}