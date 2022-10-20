package com.test.applicationtest.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.test.applicationtest.model.Ticket
import com.test.applicationtest.helper.CoroutinesHelper.ioSafe
import com.test.applicationtest.helper.DataConverter.objectToContentValues
import com.test.applicationtest.helper.DataConverter.toTicket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import javax.inject.Inject

data class SQLResponse(val executed:Boolean, val reason:Exception? = null, val rowId:Long? = null)

class SQLiteHelper @Inject constructor(context: Context, val ticketProvider: TicketProvider) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION){

    private var currentDbJob: Job? = null

    override fun onCreate(db: SQLiteDatabase?) {
        val createTable =
                "CREATE TABLE $TICKETS_TABLE"+
                "(id INTEGER PRIMARY KEY AUTOINCREMENT,"+
                " subtitle VARCHAR(250),"+
                " customerAddress VARCHAR(500),"+
                " customerName VARCHAR(300),"+
                " customerPhone VARCHAR(250),"+
                " myNote VARCHAR(500),"+
                " eventId LONG DEFAULT 1,"+
                " ticketDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"

        db!!.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

    }


    /**
     * Add Ticket with Coroutines
     */
    fun addTicket(ticket: Ticket, response: suspend (CoroutineScope.(SQLResponse) -> Unit)){
        currentDbJob?.cancel()

       currentDbJob = ioSafe {
           val db = this@SQLiteHelper.writableDatabase
           val data = objectToContentValues(ticket)
           try {
               val rowId = db.insert(TICKETS_TABLE, null, data)
               val status = rowId != FAIL_STATUS

               response(SQLResponse(status, null, rowId))
           }catch (e:Exception){
               response(SQLResponse(false, e))
           }finally {
               db.close()
           }
       }
    }

    /**
    * Get all tickets in Database
     */
    fun getTickets(list: suspend (CoroutineScope.(List<Ticket>) -> Unit)) {

        currentDbJob?.cancel()

        currentDbJob = ioSafe {
            val db = this@SQLiteHelper.readableDatabase
            val cursor = db.rawQuery("SELECT * FROM $TICKETS_TABLE", null)
            val listValues = mutableListOf<Ticket>()
            if(cursor.moveToFirst()){
                do {
                    val ticket = cursor.toTicket()
                    listValues.add(ticket)
                }while (cursor.moveToNext())
            }
            ticketProvider.totalTicketList = listValues
            list(listValues)
            cursor.close()
            db.close()
        }
    }


    /**
     * Get ticket by id in Database
     */
    fun getTicketById(id:Int,result: suspend (CoroutineScope.(Ticket?) -> Unit)) {

        currentDbJob?.cancel()

        currentDbJob = ioSafe {
            val db = this@SQLiteHelper.readableDatabase
            val cursor = db.rawQuery("SELECT * FROM $TICKETS_TABLE WHERE id = $id", null)

            if(cursor.moveToFirst()){
                val ticket = cursor.toTicket()
                result(ticket)
            }else{
                result(null)
            }
            cursor.close()
            db.close()
        }
    }

    /**
     * Delete ticket by id
     */
    fun deleteTicket(id:Int?, result: suspend (CoroutineScope.(Boolean) -> Unit)){
        currentDbJob?.cancel()

        currentDbJob = ioSafe {
            if(id == null){
                result(false)
            }
            val db = this@SQLiteHelper.writableDatabase
            val args = arrayOf(id.toString())
            val execResult = db.delete(TICKETS_TABLE, "id = ?", args) != 0
            result(execResult)
            db.close()
        }
    }

    /**
     * Modify ticket by id
     */
    fun modifyTicket(ticket:Ticket, result: suspend (CoroutineScope.(Boolean) -> Unit)){

        currentDbJob?.cancel()

        currentDbJob = ioSafe {
            val db = this@SQLiteHelper.writableDatabase
            val args = arrayOf(ticket.id.toString())
            val newValues = objectToContentValues(ticket)
           val resultValue = db.update(TICKETS_TABLE, newValues, "id = ?", args) != 0
            db.close()
            result(resultValue)
        }

    }

    companion object{
        /** DB VALUES **/
        const val DB_NAME = "app_test.db"
        const val DB_VERSION = 1
        const val TICKETS_TABLE = "tickets"
        const val FAIL_STATUS:Long = -1
    }
}