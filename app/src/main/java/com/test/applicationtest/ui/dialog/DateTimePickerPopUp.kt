package com.test.applicationtest.ui.dialog

import android.os.Build
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.test.applicationtest.R
import com.test.applicationtest.databinding.PopUpTimepickerBinding
import com.test.applicationtest.helper.CoroutinesHelper.ioSafe
import com.test.applicationtest.helper.DataConverter.toDate
import com.test.applicationtest.helper.DataConverter.toDateAndTime
import com.wineberryhalley.bclassapp.BottomBaseShet
import kotlinx.coroutines.CoroutineScope
import java.util.*


class DateTimePickerPopUp(private val dateChanged:suspend (CoroutineScope.(Long) -> Unit)) :
    BottomBaseShet() {

    private lateinit var binding:PopUpTimepickerBinding
    private var currentCalendar:Calendar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.Theme_InputAddTheme)
    }

    override fun layoutID(): Int {
        return R.layout.pop_up_timepicker
    }

    override fun OnStart(){
        binding = PopUpTimepickerBinding.bind(requireView())
        //Instance calendar
        val today = Calendar.getInstance()

        binding.datePicker.init(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH)){ _, year, month, day ->
            currentCalendar = Calendar.getInstance()
            currentCalendar!!.set(Calendar.YEAR, year)
            currentCalendar!!.set(Calendar.MONTH, month)
            currentCalendar!!.set(Calendar.DAY_OF_MONTH, day)

            binding.dateText.text = currentCalendar!!.timeInMillis.toDate()
            binding.datePicker.isVisible = false
            binding.timePicker.isVisible = true
        }

        binding.timePicker.setIs24HourView(false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            binding.timePicker.hour = today.get(Calendar.HOUR_OF_DAY)
            binding.timePicker.minute = today.get(Calendar.MINUTE)
        }else{
            binding.timePicker.currentHour = today.get(Calendar.HOUR_OF_DAY)
            binding.timePicker.currentMinute = today.get(Calendar.MINUTE)
        }

        binding.timePicker.setOnTimeChangedListener { _, hourOfDay, minute ->

            currentCalendar?.let { calendar ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                binding.dateText.text = calendar.timeInMillis.toDateAndTime()
                validateButton(true)
            }
        }

        validateButton(false)
        binding.btnSave.setOnClickListener {
            currentCalendar?.let { calendar ->
                ioSafe {
                    dateChanged(calendar.timeInMillis)
                }
                dismissAllowingStateLoss()
            }
        }



    }

    fun validateButton(enabled:Boolean) = with(binding){
        btnSave.isEnabled = enabled
        val colorBtn = ContextCompat.getColor(requireContext(), if(enabled) R.color.primary else R.color.medium_gray)
        btnSave.setCardBackgroundColor(colorBtn)
    }
}