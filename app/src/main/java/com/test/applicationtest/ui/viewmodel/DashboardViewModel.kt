package com.test.applicationtest.ui.viewmodel

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.provider.CalendarContract
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.test.applicationtest.db.SQLiteHelper
import com.test.applicationtest.helper.CoroutinesHelper.ioSafe
import com.test.applicationtest.helper.DataConverter.checkIfEventExist
import com.test.applicationtest.helper.DataConverter.dateEqualTo
import com.test.applicationtest.helper.DataConverter.to30Minutes
import com.test.applicationtest.model.Ticket
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import java.util.*
import javax.inject.Inject


@HiltViewModel
class DashboardViewModel @Inject constructor(private val db:SQLiteHelper) :ViewModel(){

    private val _ticketList = MutableLiveData<List<Ticket>>()
    val ticketList:LiveData<List<Ticket>> = _ticketList

    fun onCreate(){
        getTickets()
    }

    fun getTickets(){
        db.getTickets {
            _ticketList.postValue(it)
        }
    }

    fun getTicketsFromProvider():List<Ticket>{
        return db.ticketProvider.totalTicketList
    }

    fun getTicketsByDate(millis:Long){
        db.getTickets { retrievedTickets ->
            val tickets = mutableListOf<Ticket>()
            retrievedTickets.filterTo(tickets){it.ticketDate.dateEqualTo(millis)}
            _ticketList.postValue(tickets)
        }
    }

    fun addTicket(ticket:Ticket, result: suspend (CoroutineScope.(Boolean, Long?) -> Unit)){
        db.addTicket(ticket){ response ->
            result(response.executed, response.rowId)
            if(response.executed){
                getTickets()
            }else{
                Log.e("MAIN", "addTicket error: ${response.reason?.message}" )
            }
        }
    }

    fun deleteTicket(id:Int?, result: suspend (CoroutineScope.(Boolean) -> Unit)){
        db.deleteTicket(id){ deleted ->
            result(deleted)
            if(deleted){
                getTickets()
            }
        }
    }

    fun modifyTicket(ticket: Ticket, result: suspend (CoroutineScope.(Boolean) -> Unit)){
        db.modifyTicket(ticket){ success ->
            if(success){
                getTickets()
            }
            result(success)
        }
    }

    /**
     * Sync calendar and get response when complete
     */
    fun syncCalendar(contentResolver:ContentResolver, result: suspend (CoroutineScope.(Int, Int) -> Unit)){
        ioSafe {

            var rowAdded = 0
            var rowUpdate = 0
            getTicketsFromProvider().forEach {

                val calID = getCalendarId(contentResolver)
                val startMillis = it.ticketDate
                val endMillis = it.ticketDate.to30Minutes()
                val existEvent = it.checkIfEventExist(contentResolver)
                val values = ContentValues().apply {
                    put(CalendarContract.Events.DTSTART, startMillis)
                    put(CalendarContract.Events.DTEND, endMillis)
                    put(CalendarContract.Events.TITLE, it.subtitle)
                    put(CalendarContract.Events.DESCRIPTION, it.myNote)
                    put(CalendarContract.Events.CALENDAR_ID, calID)
                    put(CalendarContract.Events.EVENT_TIMEZONE, Locale.getDefault().displayName)
                }
                if(existEvent) {
                   val updateUri =
                        ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, it.eventId)
                    val rows = contentResolver.update(updateUri, values, null, null)
                    rowUpdate += rows
                }else{
                    val uri = contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
                    val eventID = uri?.lastPathSegment?.toLong()

                    if(eventID != null) {
                        modifyTicket(it.copy(eventId = eventID)) {
                            // if true id was success updated
                            rowAdded++
                        }
                    }
                }
            }.run {
                result(rowAdded, rowUpdate)
            }
        }
    }


    /**
     * Get the first Calendar Id, but i can make a calendar event selector to get ID by user selection
     */
    private fun getCalendarId(contentResolver: ContentResolver): Int {
        val calendars = CalendarContract.Calendars.CONTENT_URI
        val eventProjection = arrayOf(
            CalendarContract.Calendars._ID,  // 0
            CalendarContract.Calendars.ACCOUNT_NAME,  // 1
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,  // 2
            CalendarContract.Calendars.OWNER_ACCOUNT,  // 3
            CalendarContract.Calendars.IS_PRIMARY // 4
        )
        val projectionToIndex = 0
        val cursor = contentResolver.query(calendars, eventProjection, null, null, null)
        if (cursor!!.moveToFirst()) {
                val calId = cursor.getLong(projectionToIndex)
                cursor.close()
                return calId.toInt()
        }
        return 1
    }

}