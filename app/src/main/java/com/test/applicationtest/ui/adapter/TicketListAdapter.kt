package com.test.applicationtest.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.test.applicationtest.R
import com.test.applicationtest.`interface`.OnClickTicketListener
import com.test.applicationtest.model.Ticket
import com.test.applicationtest.ui.adapter.viewholder.TicketViewHolder

class TicketListAdapter(private val list:List<Ticket>, private val clickListener: OnClickTicketListener):RecyclerView.Adapter<TicketViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ticket, parent, false)
        return TicketViewHolder(view)
    }

    override fun onBindViewHolder(holder: TicketViewHolder, position: Int) {
        holder.bind(list[position]){
            clickListener.click(list[position])
        }
    }

    override fun getItemCount(): Int {
       return list.size
    }
}