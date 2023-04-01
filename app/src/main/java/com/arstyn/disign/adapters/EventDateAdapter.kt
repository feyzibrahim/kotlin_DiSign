package com.arstyn.disign.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.arstyn.disign.R
import com.arstyn.disign.modal.EventDate
import kotlinx.android.synthetic.main.date_list_item.view.*

class EventDateAdapter(
    var eventDateList: List<EventDate>,
    val clickListener: (EventDate) -> Unit
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class EventDateViewHolder(view: View): RecyclerView.ViewHolder(view) {
        fun bind(eventDate: EventDate, clickListener: (EventDate) -> Unit) {
            itemView.date_event.text = eventDate.eventDate
            itemView.setOnClickListener {
                clickListener(eventDate)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.date_list_item, parent, false)
        return EventDateViewHolder(view)
    }

    override fun getItemCount(): Int {
        return eventDateList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as EventDateViewHolder).bind(eventDateList[position], clickListener)
    }
}