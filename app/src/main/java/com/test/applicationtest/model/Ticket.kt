package com.test.applicationtest.model

data
class
Ticket(val id:Int?,
       val subtitle:String?,
       val customerName:String?,
       val customerPhone:String?,
       val customerAddress: String?,
       val myNote:String?,
       val eventId:Long = 1, // Calendar purposes
       val ticketDate:Long = System.currentTimeMillis())
