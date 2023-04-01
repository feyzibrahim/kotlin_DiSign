package com.arstyn.disign

import android.view.View
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.arstyn.disign.adapters.EventDateAdapter
import com.arstyn.disign.modal.EventDate
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_event.*
import java.util.*

class EventActivity : AppCompatActivity(), (EventDate) -> Unit {

    private lateinit var db: FirebaseFirestore
    private var eventDateList: List<EventDate> = ArrayList()
    private val eventDateAdapter: EventDateAdapter = EventDateAdapter(eventDateList, this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event)

        db = Firebase.firestore

        val eventId = intent.getStringExtra("eventId")
        val name = intent.getStringExtra("name")
        val organizer = intent.getStringExtra("organizer")
        val place = intent.getStringExtra("place")
        viewEvent_name.text = name
        if (eventId != null) {
            loadDate(eventId)
        }

        viewQr.setOnClickListener {
            val intent = Intent(this, QrActivity::class.java)
            intent.putExtra("dataToEncode", eventId)
            intent.putExtra("name", name)
            intent.putExtra("place", place)
            intent.putExtra("organizer", organizer)
            startActivity(intent)
        }

        viewEventRecycler.layoutManager = LinearLayoutManager(this)
        viewEventRecycler.adapter = eventDateAdapter
        viewEventRecycler.addItemDecoration(DividerItemDecoration(
            viewEventRecycler.context, DividerItemDecoration.VERTICAL
        ))
    }

    private fun loadDate(eventId: String) {
        db.collection("ParticDate")
            .whereEqualTo("eventId", eventId)
            .get()
            .addOnSuccessListener{ snapshot ->
                if (snapshot != null) {
                    val dateList = snapshot.toObjects(EventDate::class.java)
                    eventDateList = dateList.sortedWith(compareByDescending{it.timestamp})
                    eventDateAdapter.eventDateList = eventDateList
                    eventDateAdapter.notifyDataSetChanged()
                    eventRecyclerProgress.visibility = View.GONE
                } else {
                    Toast.makeText(this, "No data found", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun invoke(eventDate:  EventDate) {
        val intent = Intent(this, PeopleListActivity::class.java)
        intent.putExtra("eventDate", eventDate.eventDate)
        intent.putExtra("eventId", eventDate.eventId)
        startActivity(intent)
    }
}
