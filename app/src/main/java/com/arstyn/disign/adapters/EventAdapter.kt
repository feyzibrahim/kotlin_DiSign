package com.arstyn.disign.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.arstyn.disign.NewEventActivity
import com.arstyn.disign.R
import com.arstyn.disign.modal.Event
import kotlinx.android.synthetic.main.row_list_item.view.*

class EventAdapter(var eventList: List<Event>, val clickListener: NewEventActivity):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class EventViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(event: Event, clickListener: (Event) -> Unit) {
            itemView.event_name.text = event.eventName
            itemView.event_date.text = event.eventLimit
            itemView.event_limit.text = event.eventDate

            itemView.setOnClickListener{
                clickListener(event)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_list_item, parent, false)
        return EventViewHolder(view)
    }

    override fun getItemCount(): Int {
        return eventList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as EventViewHolder).bind(eventList[position], clickListener)
    }
}