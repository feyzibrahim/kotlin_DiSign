package com.arstyn.disign

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.arstyn.disign.modal.Event
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_add_event.*
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.DecimalFormat
import java.util.*

class AddEventActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_event)

        db = Firebase.firestore

        addEventBackButton.setOnClickListener {
            finish()
        }

        dateChooser.setOnClickListener {
            pickDate()
        }
        @Suppress("NAME_SHADOWING")
        timeChooser.setOnClickListener {
            pickTime()
        }

        addEvent.setOnClickListener {
            checkTextBoxes()
        }

        addEventDate.addTextChangedListener(object: TextWatcher{
            var prvl = 0
            override fun afterTextChanged(s: Editable?) {
                if(s != null) {
                    val len = s.length
                    if ((prvl < len) && (len == 2 || len == 5)) {
                        s.append("/")
                    }
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                prvl = addEventDate.text.toString().length
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        addEventTime.addTextChangedListener(object: TextWatcher{
            var prvl = 0
            override fun afterTextChanged(s: Editable?) {
                if (s != null) {
                    val len = s.length
                    if ((prvl < len) && (len == 2)) {
                        s.append(":")
                    }
                    if ((prvl < len) && (len == 5)) {
                        s.append(' ')
                    }
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                prvl = addEventTime.text.toString().length
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        saveQr_Button_inEvent.setOnClickListener {
            shareTheQr()
        }

    }

    private fun checkTextBoxes() {
        val eventId = UUID.randomUUID().toString().replace("-", "")
        val eventLimit = addEventLimit.text.toString()
        val eventName = addEventName.text.toString()
        val eventOrganizer = addEventOrganizer.text.toString()
        val eventPlace = addEventPlace.text.toString()
        val eventStatus = "Not Started"
        val date = addEventDate.text.toString()
        val time = addEventTime.text.toString()
        val createdBy = FirebaseAuth.getInstance().currentUser?.uid

        if (eventName.isEmpty()) {
            errorCode("Enter a name", addEventName)
            return
        }
        if (eventOrganizer.isEmpty()) {
            errorCode("Enter the name of organizer", addEventOrganizer)
            return
        }
        if (eventPlace.isEmpty()) {
            errorCode("Enter the Location", addEventPlace)
            return
        }
        if (date.isEmpty()) {
            errorCode("Select a date", addEventDate)
            return
        }
        if(time.isEmpty()) {
            errorCode("Select a time", addEventTime)
            return
        }
        if (eventLimit.isEmpty()) {
            errorCode("Set the number of persons", addEventLimit)
            return
        }
        if (createdBy!!.isEmpty()) {
            return
        }

        val event = Event(eventId, eventLimit, eventName, eventOrganizer, eventPlace, eventStatus, date, time, createdBy)
        val view = this.currentFocus
        view?.let { v ->
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(v.windowToken, 0)
        }
        updateUI(event)
        addToFirebase(event, eventId)
    }

    private fun errorCode(errorMsg:String, editBox: EditText) {
        editBox.error = errorMsg
        editBox.requestFocus()
        return
    }

    private fun addToFirebase(event: Event, eventId:String) {
        db.collection("Event").document(eventId)
            .set(event)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK.or(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }
    }

    private fun updateUI(event: Event) {
        addEventLayout.visibility = View.GONE
        qrLayoutInEvent.visibility = View.VISIBLE
        qrTitleInEvent.text = event.eventName
        qrOrganizerInEvent.text = event.eventOrganizer
        qrPlaceInEvent.text = event.eventPlace
        val qrData = event.eventId
        val qrEncoder = QRGEncoder(qrData, null, QRGContents.Type.TEXT, 1000)
        val bitmap = qrEncoder.bitmap
        qrViewInEvent.setImageBitmap(bitmap)
        qrProgress_inEvent.visibility = View.GONE
    }

    private fun pickDate() {
        val calendar = Calendar.getInstance()
        val yy = calendar.get(Calendar.YEAR)
        val mm = calendar.get(Calendar.MONTH)
        val dd = calendar.get(Calendar.DAY_OF_MONTH)
        @Suppress("NAME_SHADOWING")
        val datePicker = DatePickerDialog(this, DatePickerDialog.OnDateSetListener{
                _, year, month, dayOfMonth ->

            val mmm = month + 1
            val dd = DecimalFormat("00").format(dayOfMonth).toString()
            val mm:String = DecimalFormat("00").format(mmm).toString()

            val date = "$dd/$mm/$year"
            addEventDate.setText(date)
        }, yy, mm, dd)
        datePicker.show()
    }

    private fun pickTime() {
        val cal = Calendar.getInstance()
        val hh = cal.get(Calendar.HOUR_OF_DAY)
        val tt = cal.get(Calendar.MINUTE)
        @Suppress("NAME_SHADOWING")
        val timePicker = TimePickerDialog(this, TimePickerDialog.OnTimeSetListener {
                _, hourOfDay, minute ->

            var hhh = hourOfDay

            val ampm: String
            when {
                hourOfDay == 0 -> {
                    hhh = 12
                    ampm = "AM"
                }
                hourOfDay < 12 -> {
                    ampm = "AM"
                }
                hourOfDay == 12 -> {
                    ampm = "PM"
                }
                else -> {
                    hhh -= 12
                    ampm = "PM"
                }
            }
            val hh = DecimalFormat("00").format(hhh).toString()
            val tt = DecimalFormat("00").format(minute).toString()

            val time = "$hh:$tt $ampm"
            addEventTime.setText(time)

        }, hh, tt, false)
        timePicker.show()
    }

    private fun shareTheQr() {
        checkPermission()

        val view = layoutToBeExportedFromEvent
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        view.draw(canvas)
        val intent = Intent(Intent.ACTION_SEND)
        val file = File(externalCacheDir, "qrcode.png")
        val fout = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fout)
        fout.close()
        fout.flush()
        intent.type = "image/png"
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val uri = FileProvider.getUriForFile(this, "com.arstyn.disign.fileProvider", file)
                intent.setDataAndType(uri, contentResolver.getType(uri))
                intent.putExtra(Intent.EXTRA_STREAM, uri)
                intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                startActivity(Intent.createChooser(intent, "Share Qr Code"))
            } else {
                intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file))
                startActivity(Intent.createChooser(intent, "Share Qr Code"))
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e:FileNotFoundException) {
            e.printStackTrace()
        }
    }

    private fun checkPermission() {
        if(ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode != 1 && grantResults.isEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission Not Granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
        }
    }
}
