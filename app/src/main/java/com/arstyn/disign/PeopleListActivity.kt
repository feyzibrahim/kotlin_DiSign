package com.arstyn.disign

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.arstyn.disign.adapters.ParticipantAdapter
import com.arstyn.disign.modal.Participant
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_people_list.*

class PeopleListActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private var participantList: List<Participant> = ArrayList()
    private var participantAdapter: ParticipantAdapter = ParticipantAdapter(participantList)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_people_list)

        db = Firebase.firestore

        val eventId = intent.getStringExtra("eventId")
        val eventDate = intent.getStringExtra("eventDate")
        if (eventId != null && eventDate != null) {
            visitingDate.text = eventDate
            loadParticipant(eventId, eventDate)
        }

        particRecyclerView.layoutManager = LinearLayoutManager(this)
        particRecyclerView.adapter = participantAdapter
        particRecyclerView.addItemDecoration(DividerItemDecoration(
            particRecyclerView.context, DividerItemDecoration.VERTICAL
        ))
    }

    private fun loadParticipant(eventId: String, eventDate: String) {
        db.collection("Partic")
            .whereEqualTo("eventId", eventId)
            .whereEqualTo("scannedDate", eventDate)
            .get()
            .addOnSuccessListener {snapShot ->
                val partitList = snapShot.toObjects(Participant::class.java)
                participantList = partitList.sortedWith(compareBy { it.scannedTime })
                participantAdapter.participantList = participantList
                participantAdapter.notifyDataSetChanged()
                peopleRecyclerProgress.visibility = View.GONE
                visitorCount.text = participantList.size.toString()
            }
    }
}
