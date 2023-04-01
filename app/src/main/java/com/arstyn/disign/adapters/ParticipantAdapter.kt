package com.arstyn.disign.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.arstyn.disign.R
import com.arstyn.disign.modal.Participant
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.participant_row_list.view.*
import java.text.SimpleDateFormat
import java.util.*

class ParticipantAdapter(var participantList: List<Participant>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class ParticipantViewHolder(view: View): RecyclerView.ViewHolder(view) {
        fun bind(participant: Participant) {
            itemView.partTitle.text = participant.name
            itemView.participantPhone.text = participant.phoneNumber

            Glide.with(itemView).load(participant.dpUrl).into(itemView.participantDp)

            val date = participant.scannedTime?.toDate()
            val sfd = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
            if (date != null) {
                val dd = sfd.format(date)
                itemView.participantDate.text = dd
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.participant_row_list, parent, false)
        return ParticipantViewHolder(view)
    }

    override fun getItemCount(): Int {
        return participantList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ParticipantViewHolder).bind(participantList[position])
    }
}