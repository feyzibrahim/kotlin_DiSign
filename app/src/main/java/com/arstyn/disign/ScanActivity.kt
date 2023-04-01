package com.arstyn.disign

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.arstyn.disign.modal.*
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_scan.*
import java.sql.Timestamp
import java.text.DecimalFormat
import java.util.*

class ScanActivity : AppCompatActivity() {

    private lateinit var codeScanner: CodeScanner
    private lateinit var eventId: String
    private lateinit var currentUser:User
    private lateinit var db: FirebaseFirestore
    private lateinit var date: String
    private lateinit var adLoader: AdLoader

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)

        checkPermissions()

        db = Firebase.firestore

        adLoader = AdLoader.Builder(this, "ca-app-pub-9184680471174773/7441038062")
            .forUnifiedNativeAd{unifiedNativeAd ->
                scanResultAdView.setNativeAd(unifiedNativeAd)
            }
            .build()
        adLoader.loadAd(AdRequest.Builder().build())
        loadDate()

        codeScanner = CodeScanner(this, scannerView)
        codeScanner.camera = CodeScanner.CAMERA_BACK
        codeScanner.formats = CodeScanner.TWO_DIMENSIONAL_FORMATS
        codeScanner.autoFocusMode = AutoFocusMode.SAFE
        codeScanner.scanMode = ScanMode.SINGLE
        codeScanner.isAutoFocusEnabled = true
        codeScanner.isFlashEnabled = false
        codeScanner.decodeCallback = DecodeCallback {
            runOnUiThread {
                codeScanner.stopPreview()
                scannerViewProgress.visibility = View.VISIBLE
                scannerView.visibility = View.GONE

                eventId = it.text
                Toast.makeText(this, eventId, Toast.LENGTH_LONG).show()
                verifyCode(eventId)
            }
        }

        codeScanner.errorCallback = ErrorCallback {
            runOnUiThread {
                Toast.makeText(this, "Camera initialization error: ${it.message}",
                    Toast.LENGTH_LONG).show()
                codeScanner.stopPreview()
                codeScanner.releaseResources()
            }
        }

        codeScanner.startPreview()

        scan_result_close.setOnClickListener {
            finish()
            codeScanner.stopPreview()
            codeScanner.releaseResources()
        }
    }

    override fun onStart() {
        super.onStart()
        loadUserDetails()
    }

    override fun onPause() {
        super.onPause()
        codeScanner.releaseResources()
        codeScanner.stopPreview()
    }

    private fun verifyCode(eventId: String){
        if (eventId.contains(".")) {
            updateUiOnFail()
        } else if(eventId.contains("/")) {
            updateUiOnFail()
        } else if (eventId.contains(" ")) {
            updateUiOnFail()
        } else {
            if(eventId.length == 32) {
                verifyEvent(eventId)
            } else {
                updateUiOnFail()
            }
        }
    }

    private fun verifyEvent(eventId: String) {

        try {
            db.collection("Event").document(eventId)
                .get()
                .addOnSuccessListener {
                    verifyParticipantDate(eventId)
                }
        } catch (e: Exception) {
            updateUiOnFail()
            codeScanner.stopPreview()
            codeScanner.releaseResources()
        }
    }

    private fun verifyParticipantDate(eventId:String) {
        db.collection("ParticDate")
            .whereEqualTo("eventId", eventId)
            .whereEqualTo("eventDate", date)
            .get()
            .addOnSuccessListener {
                if(it.isEmpty) {
                    createParticDate(eventId)
                }
                else {
                    registerEventInFirestore(eventId)
                }
            }
    }

    private fun createParticDate(eventId: String) {
        val time = Timestamp(System.currentTimeMillis())
        val times = com.google.firebase.Timestamp(time)
        val eventDate = EventDate(date, eventId, times)
        db.collection("ParticDate")
            .add(eventDate)
            .addOnSuccessListener {
                registerEventInFirestore(eventId)
            }
    }

    private fun registerEventInFirestore(eventId:String) {
        val time = Timestamp(System.currentTimeMillis())
        val times = com.google.firebase.Timestamp(time)
        val parti = Participant(eventId, times, date, currentUser.name, currentUser.dpUri, currentUser.phone)
        db.collection("Partic")
            .add(parti)
            .addOnSuccessListener {
                updateUIOnComplete(eventId)
            }
    }

    private fun loadUserDetails() {
        val uid = FirebaseAuth.getInstance().uid
        if (uid != null) {
            db.collection("User").document(uid)
                .get()
                .addOnSuccessListener {
                    currentUser = it.toObject<User>()!!
                }
        }
    }

    private fun updateUIOnComplete(eventId: String) {
        scannerResultConstraint.visibility = View.VISIBLE
        db.collection("Event").document(eventId)
            .get()
            .addOnSuccessListener {
                val event = it.toObject<Event>()
                if (event != null) {
                    saveHistoryForUser(event)
                    scan_result_name.text = event.eventName
                    scan_result_place.text = event.eventPlace
                }
            }
        scan_result_time.text = date
        scannerViewProgress.visibility = View.GONE
    }

    private fun saveHistoryForUser(event: Event) {
        val time = Timestamp(System.currentTimeMillis())
        val times = com.google.firebase.Timestamp(time)
        val id = db.collection("History").document().id
        val historyEvent = HistoryEvent(id, currentUser.uid, event.eventName, event.eventOrganizer, times, event.eventPlace)
        db.collection("History").document(id).set(historyEvent)
    }

    private fun updateUiOnFail() {
        scannerResultConstraint.visibility = View.VISIBLE
        scan_success.text = getString(R.string.failed)
        scan_result_name.visibility = View.GONE
        scan_result_place.visibility = View.GONE
        scan_result_time.visibility = View.GONE
        welcomeTo.visibility = View.GONE
        scan_at.visibility = View.GONE
        scan_time.visibility = View.GONE
        scan_success_tick.background = ContextCompat.getDrawable(this, R.drawable.ic_close)
        scannerViewProgress.visibility = View.GONE
    }

    private fun loadDate() {
        val calendar = Calendar.getInstance()
        val yy = calendar.get(Calendar.YEAR)
        val mmm = calendar.get(Calendar.MONTH) + 1
        val mm = DecimalFormat("00").format(mmm).toString()
        val ddd = calendar.get(Calendar.DAY_OF_MONTH)
        val dd = DecimalFormat("00").format(ddd).toString()
        date = "$dd/$mm/$yy"
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
