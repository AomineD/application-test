package com.test.applicationtest.db

import com.test.applicationtest.model.Ticket
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TicketProvider @Inject constructor(){
    var totalTicketList = mutableListOf<Ticket>()
}