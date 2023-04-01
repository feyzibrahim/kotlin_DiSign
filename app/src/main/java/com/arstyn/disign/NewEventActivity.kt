package com.arstyn.disign

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.arstyn.disign.adapters.EventAdapter
import com.arstyn.disign.modal.Event
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_new_event.*

class NewEventActivity : AppCompatActivity(), (Event) -> Unit {

    private lateinit var db: FirebaseFirestore
    private var eventList: List<Event> = ArrayList()
    private val eventAdapter: EventAdapter =
        EventAdapter(eventList, this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_event)
        db = Firebase.firestore
        val uid = intent.getStringExtra("uid")
        if (uid != null) {
            getEventFromFirebase(uid)
        }

        history_recyclerView.layoutManager = LinearLayoutManager(this)
        history_recyclerView.adapter = eventAdapter
        history_recyclerView.addItemDecoration(DividerItemDecoration(history_recyclerView.context, DividerItemDecoration.VERTICAL))

        add_new_event.setOnClickListener {
            val intent = Intent(this, AddEventActivity::class.java)
            startActivity(intent)
        }
    }

    private fun getEventFromFirebase(uid: String) {
        db.collection("Event")
            .whereEqualTo("createdBy", uid)
            .addSnapshotListener { snapShot, _ ->
                if (snapShot != null) {
                    eventList = snapShot.toObjects(Event::class.java)
                    eventAdapter.eventList = eventList
                    if(eventList.isNotEmpty()) {
                        eventListEmptyText.visibility = View.GONE
                    }
                    eventAdapter.notifyDataSetChanged()
                    historyRecyclerProgress.visibility = View.GONE
                }
            }
    }

    override fun invoke(event: Event) {
        val intent = Intent(this, EventActivity::class.java)
        intent.putExtra("name", event.eventName)
        intent.putExtra("eventId", event.eventId)
        intent.putExtra("organizer", event.eventOrganizer)
        intent.putExtra("place", event.eventPlace)
        startActivity(intent)
    }
}
