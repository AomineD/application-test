package com.test.applicationtest.helper

import android.content.ContentResolver
import android.content.ContentValues
import android.database.Cursor
import android.provider.CalendarContract
import androidx.core.database.getStringOrNull
import com.test.applicationtest.model.Ticket
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.*


object DataConverter {

    /**
     * Convert any Kotlin or Java class to ContentValues for SQLite
     */
    @Throws(IllegalAccessException::class)
    fun objectToContentValues(o: Any): ContentValues {
        val cv = ContentValues()
         for (field in o.javaClass.declaredFields) {
         //   Log.e("MAIN", "objectToContentValues: si ${field.name}" )
            field.isAccessible = true
            val value = field[o]
            //check if compatible with contentvalues
            if (value is Double || value is Int || value is String || value is Boolean
                || value is Long || value is Float || value is Short
            ) {
                cv.put(field.name, value.toString())
            } else if (value is Date) {
                cv.put(field.name, SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(value))
            }
        }
        return cv
    }

    /**
     * Convert Cursor to Ticket class easily
     */
    fun Cursor.toTicket():Ticket =
        Ticket(getInt(0),
            getStringOrNull(1),
            getStringOrNull(3),
            getStringOrNull(4),
            getStringOrNull(2),
            getStringOrNull(5),
            getLong(6),
            getLong(7))

    /**
     * Only for test purposes
     */
    fun getExampleTicket():Ticket{

        val millis = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = millis
        calendar.add(Calendar.DAY_OF_MONTH, (1..13).shuffled().last())

        return Ticket(
            id = null,
            subtitle = "Sink Repair",
            customerName = "Diego G",
            customerAddress = "Calle 8A#4-96",
            customerPhone = "3113653547",
            myNote = "None",
            ticketDate = calendar.timeInMillis,
        )
    }


    /**
     * Easy converters to date, time and compare dates
     */
    fun String.toMillis():Long? = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).parse(this)?.time

    fun Long.toDate(): String = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(Date(this))

    fun Long.toTime(): String = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(this)).uppercase()

    fun Long.toFormat(): String =  SimpleDateFormat("EEEE, MMM d, yyyy h:mm a", Locale.getDefault()).format(Date(this)).capitalized()

    fun Long.dateEqualTo(millis:Long):Boolean = this.toDate() == millis.toDate()

    fun Long.to30Minutes():Long{
        return this + 1800000
    }

    /**
     * Check if event exist in calendar
     */

    fun Ticket.checkIfEventExist(contentResolver: ContentResolver):Boolean{
        val begin = ticketDate
        val end = ticketDate.to30Minutes()
        val proj = arrayOf(
            CalendarContract.Instances._ID,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.END,
            CalendarContract.Instances.EVENT_ID)
        val cursor = CalendarContract.Instances.query(contentResolver, proj, begin, end, subtitle)
        val count = cursor.count
        cursor.close()
       return count > 0
    }


    /**
     * Capitalize is deprecated in kotlin strings, i'll use this
     */

    fun String.capitalized(): String {
        return this.replaceFirstChar {
            if (it.isLowerCase())
                it.titlecase(Locale.getDefault())
            else it.toString()
        }
    }
}