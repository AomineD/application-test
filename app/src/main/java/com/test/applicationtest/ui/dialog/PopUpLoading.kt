package com.test.applicationtest.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import com.airbnb.lottie.LottieDrawable
import com.test.applicationtest.R
import com.test.applicationtest.databinding.PopUpLoadingBinding
import com.test.applicationtest.helper.CoroutinesHelper.main
import kotlinx.coroutines.delay

class PopUpLoading(context: Context) : Dialog(context) {

    private lateinit var binding:PopUpLoadingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        binding = PopUpLoadingBinding.inflate(layoutInflater)
        setContentView(binding.root)


        setCanceledOnTouchOutside(false)
        setCancelable(false)

    }

     fun showDefault() {
         main {
             super.show()
             binding.tvInfo.text = context.getString(R.string.loading_info)
             configAnim(true, R.string.loading_json)
         }
     }

    fun showSuccess(resId:Int, count: Int){
        main {
            super.show()

            var str = context.getString(resId)
            //check if string contain %s for format
            if(str.contains("%s")){
                str = String.format(str, count)
            }

            binding.tvInfo.text = str
            configAnim(false, R.string.loading_success_json)
            dismissDelayed()
        }

    }

    fun showError(resId:Int) {
        main {
            super.show()
            binding.tvInfo.text = context.getString(resId)
            configAnim(false, R.string.loading_error_json)
            dismissDelayed()
        }
    }

    private fun dismissDelayed(){
        main {
            // wait until animation is properly set, and get duration
            delay(100)
            val millis = binding.lottieAnim.duration + 200
            // wait until end animation and dismiss
            delay(millis)
            dismiss()
        }
    }

    private fun configAnim(repeat:Boolean, resId:Int){

        binding.lottieAnim.setAnimation(context.getString(resId))
        binding.lottieAnim.repeatCount = if(repeat) LottieDrawable.INFINITE else 0
        binding.lottieAnim.playAnimation()
    }
}