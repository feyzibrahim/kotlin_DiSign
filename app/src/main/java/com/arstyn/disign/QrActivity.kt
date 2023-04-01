package com.arstyn.disign

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import kotlinx.android.synthetic.main.activity_qr.*
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

class QrActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr)
        qrTitle.text = intent.getStringExtra("name")
        qrOrganizer.text = intent.getStringExtra("organizer")
        qrPlace.text = intent.getStringExtra("place")
        val qrData = intent.getStringExtra("dataToEncode")
        val qrEncoder = QRGEncoder(qrData, null, QRGContents.Type.TEXT, 1000)
        val bitmap = qrEncoder.bitmap
        qrView.setImageBitmap(bitmap)
        saveQr_Button.setOnClickListener {
            shareTheQr()
        }
    }

    private fun shareTheQr() {
        checkPermission()
        val view = layoutToBeExported
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
