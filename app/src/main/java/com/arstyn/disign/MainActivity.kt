package com.arstyn.disign

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.arstyn.disign.adapters.HistoryAdapter
import com.arstyn.disign.log.LogOrSignActivity
import com.arstyn.disign.modal.HistoryEvent
import com.arstyn.disign.modal.User
import com.bumptech.glide.Glide
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.nav_header.*

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var currentUser: User
    private var historyList: List<HistoryEvent> = ArrayList()
    private val historyAdapter: HistoryAdapter = HistoryAdapter(historyList)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        MobileAds.initialize(this){}

        checkPermissions()
        FirebaseApp.initializeApp(this)

        db = Firebase.firestore
        auth = FirebaseAuth.getInstance()
        val uid = auth.uid

        if (uid != null) {
            loadUserDetails(uid)
            loadEventData(uid)
        }

        main_recyclerView.layoutManager = LinearLayoutManager(this)
        main_recyclerView.adapter = historyAdapter
        main_recyclerView.addItemDecoration(DividerItemDecoration(
            main_recyclerView.context, DividerItemDecoration.VERTICAL
        ))

        floatingHome.setOnClickListener{
            val intent = Intent(this, ScanActivity::class.java)
            startActivity(intent)
        }

        drawer_button.setOnClickListener {
            drawer_layout.openDrawer(GravityCompat.START)
        }

        navigation_view.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.nav_events -> {
                    drawer_layout.closeDrawer(GravityCompat.START)
                    val intent = Intent(this, NewEventActivity::class.java)
                    intent.putExtra("uid", uid)
                    startActivity(intent)
                }

                R.id.nav_logout -> {
                    auth.signOut()
                    val intent = Intent(this, LogOrSignActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }

                R.id.nav_help -> {
                    drawer_layout.closeDrawer(GravityCompat.START)
                    val intent = Intent(this, WelcomeActivity::class.java)
                    startActivity(intent)
                }

                R.id.nav_invite -> {
                    val intent = Intent(Intent.ACTION_SEND)
                    intent.type = "text/plain"
                    intent.putExtra(Intent.EXTRA_TEXT, "Disign \n\nhttps://play.google.com/store/apps/details?id=com.arstyn.disign \n\ndownload the app from playstore")
                    startActivity(Intent.createChooser(intent, "Share.."))
                }
            }
            false
        }

        Handler(Looper.getMainLooper()).postDelayed({
            arrowForScan.visibility = View.GONE
            textForScan.visibility = View.GONE
        }, 3000)
    }

    override fun onStart() {
        super.onStart()
        val currentUser:FirebaseUser? = auth.currentUser
        if (currentUser == null) {
            signUp()
        }
    }

    private fun loadEventData(uid: String) {
        db.collection("History")
            .whereEqualTo("uid", uid)
            .addSnapshotListener { snapshot, _ ->
                if(snapshot != null) {
                    val subHistoryList = snapshot.toObjects(HistoryEvent::class.java)
                    historyList = subHistoryList.sortedWith(compareByDescending { it.timestamp })
                    historyAdapter.historyList = historyList
                    historyAdapter.notifyDataSetChanged()
                    mainRecyclerProgress.visibility = View.GONE

                    if(historyList.isNotEmpty()) {
                        recyclerEmptyText.visibility = View.GONE
                    }
                }
            }
    }

    private fun loadUserDetails(uid: String) {
        db.collection("User").document(uid)
            .get()
            .addOnSuccessListener {
                currentUser = it.toObject<User>()!!
                navUserName.text = currentUser.name
                navEmail.text = currentUser.email
                val number = currentUser.phone
                navPhoneNo.text = number
                Glide
                    .with(this)
                    .load(currentUser.dpUri)
                    .centerCrop()
                    .into(nav_drawer_dp)
                showDailgueBoxForNumberConformation(number)
            }
    }
    
    private fun showDailgueBoxForNumberConformation(number: String?) {
        if(number == "null") {
            val text = "Phone number is not registered"
            navPhoneNo.text = text
//            AlertDialog.Builder(this)
//                .setTitle("Phone number is not verified")
//                .setMessage("Phone number is required for the proper working of this app. So please click the okay button and register your phone number. OTP verification is required")
//                .setPositiveButton(R.string.ok) { _, _ ->
//                    val intent = Intent(this, PhoneRegisterActivity::class.java)
//                    startActivity(intent)
//                }
//                .setNegativeButton(R.string.cancel) { _, _ ->
//                    finishAffinity()
//                }
//                .create()
//                .show()
        }
    }

    private fun signUp() {
        val intent = Intent(this, LogOrSignActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK.or(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), 10)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode != 10 && grantResults.isEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission Not Granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
        }
    }
}
