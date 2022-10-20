package com.test.applicationtest.ui.adapter.viewholder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.test.applicationtest.databinding.ItemTicketBinding
import com.test.applicationtest.helper.DataConverter.toDate
import com.test.applicationtest.helper.DataConverter.toTime
import com.test.applicationtest.model.Ticket

class TicketViewHolder(val view:View): RecyclerView.ViewHolder(view){
    private val binding = ItemTicketBinding.bind(view)

    fun bind(ticket:Ticket, callback: View.OnClickListener){
        binding.tvTicketTitle.text = ticket.subtitle ?: "No subtitle"
        binding.tvAddress.text = ticket.customerAddress ?: "No address"
        val idTxt = "Ticket #${ticket.id}"
        binding.tvId.text = idTxt
        binding.tvDate.text = ticket.ticketDate.toDate()
        binding.tvTime.text = ticket.ticketDate.toTime()
        binding.btnViewTicket.setOnClickListener(callback)
    }

}