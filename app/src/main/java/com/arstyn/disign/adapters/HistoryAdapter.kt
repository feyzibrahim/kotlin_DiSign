package com.arstyn.disign.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.arstyn.disign.R
import com.arstyn.disign.modal.HistoryEvent
import kotlinx.android.synthetic.main.history_list_item.view.*
import java.text.SimpleDateFormat
import java.util.*

class HistoryAdapter(var historyList: List<HistoryEvent>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class HistoryViewHolder(view: View): RecyclerView.ViewHolder(view) {
        fun bind(historyEvent: HistoryEvent) {
            itemView.history_event_name.text = historyEvent.name
            itemView.history_event_place.text = historyEvent.place

            val date = historyEvent.timestamp?.toDate()
            val sfd = SimpleDateFormat("hh:mm a · dd-MM-YYYY", Locale.ENGLISH)
            if (date != null) {
                val dd = sfd.format(date)
                itemView.history_event_date.text = dd
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.history_list_item, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as HistoryViewHolder).bind(historyList[position])
    }

    override fun getItemCount(): Int {
        return  historyList.size
    }

}