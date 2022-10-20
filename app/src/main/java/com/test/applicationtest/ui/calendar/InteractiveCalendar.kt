package com.test.applicationtest.ui.calendar

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import com.events.calendar.utils.EventsCalendarUtil.SINGLE_SELECTION
import com.events.calendar.views.EventsCalendar
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.test.applicationtest.R
import com.test.applicationtest.databinding.InteractiveCalendarBinding
import com.test.applicationtest.helper.CoroutinesHelper.ioSafe
import com.test.applicationtest.helper.DataConverter.toDate
import com.test.applicationtest.model.Ticket
import com.wineberryhalley.bclassapp.BottomBaseShet
import kotlinx.coroutines.CoroutineScope
import java.util.*


class InteractiveCalendar(private val ticketList:List<Ticket>,private val dateChanged:suspend (CoroutineScope.(Long?) -> Unit)) :
    BottomBaseShet() {

    private lateinit var binding:InteractiveCalendarBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.Theme_InputAddTheme)
    }

    override fun layoutID(): Int {
        return R.layout.interactive_calendar
    }

    override fun OnStart() {
        binding = InteractiveCalendarBinding.bind(requireView())
        //Instance calendar
        val today = Calendar.getInstance()
        binding.eventsCalendar.setSelectionMode(SINGLE_SELECTION)
            .setToday(today)
            .setMonthRange(Calendar.getInstance(), Calendar.getInstance())
            .setWeekStartDay(Calendar.SUNDAY, false)

        // Add tickets to due
        ticketList.forEach {
            val millis = it.ticketDate
            if(millis >= System.currentTimeMillis()) {
                val date = Calendar.getInstance()
                date.timeInMillis = millis
                binding.eventsCalendar
                    .addEvent(date)
            }
        }


        binding.eventsCalendar.setCallback(object: EventsCalendar.Callback{
            override fun onDayLongPressed(selectedDate: Calendar?) {}
            override fun onMonthChanged(monthStartDate: Calendar?) {}
            override fun onDaySelected(selectedDate: Calendar?) {
                ioSafe {
                    if (selectedDate != null) {
                        dismiss()
                        val checkBetweenDates = savedDate.timeInMillis.toDate() != selectedDate.timeInMillis.toDate()
                        dateChanged(if (checkBetweenDates) selectedDate.timeInMillis else null)
                        savedDate = selectedDate
                    }else{
                        Log.e("MAIN", "onDaySelected: date is null, dismissing" )
                        dismiss()
                    }
                }

            }
        })

        binding.eventsCalendar.setCurrentSelectedDate(savedDate)

        binding.eventsCalendar.build()


        binding.btnClose.setOnClickListener{
            dismiss()
        }
        binding.btnReset.setOnClickListener {
            savedDate = today
            binding.eventsCalendar.setCurrentSelectedDate(savedDate)
            dismiss()
        }
    }


    companion object{
        // We have to save selected date
        var savedDate:Calendar = Calendar.getInstance()
    }
}