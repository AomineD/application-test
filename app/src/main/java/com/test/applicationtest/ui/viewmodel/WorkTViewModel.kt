package com.test.applicationtest.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.test.applicationtest.db.SQLiteHelper
import com.test.applicationtest.model.Ticket
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class WorkTViewModel @Inject constructor(private val db: SQLiteHelper) : ViewModel(){

    private val _ticket = MutableLiveData<Ticket?>()
    val ticket:LiveData<Ticket?> = _ticket


    fun onCreate(id:Int){
        getTicketById(id)
    }

    fun getTicketById(id: Int) {
        db.getTicketById(id){
                _ticket.postValue(it)
        }
    }

    fun saveTicket(ticket: Ticket){
        db.modifyTicket(ticket){
            if(it){
                getTicketById(ticket.id!!)
            }
        }
    }

}