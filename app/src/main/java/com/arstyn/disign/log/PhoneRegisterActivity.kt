package com.arstyn.disign.log

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.arstyn.disign.R
import com.arstyn.disign.WelcomeActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_phone_register.*
import kotlinx.android.synthetic.main.activity_phone_register.progressBar
import kotlinx.android.synthetic.main.activity_phone_register.signUp_login_button
import java.util.concurrent.TimeUnit

class PhoneRegisterActivity : AppCompatActivity() {

    private lateinit var code: String
    private lateinit var uid: String
    private lateinit var auth: FirebaseAuth
    private lateinit var phoneNumber: String
    private lateinit var db : FirebaseFirestore
    private lateinit var storedVerificationId :String
    private lateinit var credential: PhoneAuthCredential
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_register)

        auth = FirebaseAuth.getInstance()
        db = Firebase.firestore

        nextButtonAtPhone.setOnClickListener {
            val cCode = ccp.selectedCountryCode.toString()
            val number = phoneNumberForRegistration.text.toString()
            phoneNumber = "+$cCode$number"
            if (number.isNotEmpty()) {
                showDailogeBox()
            } else {
                errorCode(phoneNumberForRegistration)
            }
        }

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                code = p0.smsCode.toString()

                if (code.isNotEmpty()) {
                    verificationCodeAtOTP.setText(code)
                }
            }

            override fun onVerificationFailed(p0: FirebaseException) {
                progressBar.visibility = View.INVISIBLE
            }

            override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
                storedVerificationId = p0
                signUp_login_button.isEnabled = true
                signUp_login_button.background = ContextCompat.getDrawable(applicationContext, R.drawable.round_button_yellow)
            }

            override fun onCodeAutoRetrievalTimeOut(p0: String) {
                super.onCodeAutoRetrievalTimeOut(p0)
                verify_code_text_atOtp.text = getString(R.string.autoverifyError)
                progressBar.visibility = View.INVISIBLE
            }
        }

        verificationCodeAtOTP.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val otp = verificationCodeAtOTP.text.toString()
                if (s != null && s.length == 6) {
                    signUp_login_button.visibility = View.GONE
                    credential = PhoneAuthProvider.getCredential(storedVerificationId, otp)
                    registerPhone()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        })
    }

    private fun showDailogeBox() {
        AlertDialog.Builder(this)
            .setMessage("We will be verifying your phone number. Is this OK, or would yuo like to edit the number?")
            .setTitle(phoneNumber)
            .setPositiveButton(R.string.ok) { _, _ ->
                numberEntryFormInPhone.visibility = View.GONE
                otpForm.visibility = View.VISIBLE
                startNumberVerification()
            }
            .setNegativeButton(R.string.no){ _, _ ->
            }
            .setCancelable(true)
            .create()
            .show()
    }

    private fun startNumberVerification() {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(120L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun registerPhone() {
        auth.currentUser?.updatePhoneNumber(credential)
            ?.addOnSuccessListener {
                updateFirebaseDatabase()
            }
            ?.addOnFailureListener {
                Toast.makeText(this, "Phone Registration Failed", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateFirebaseDatabase() {
        db.collection("User").document(uid)
            .update("phone", phoneNumber)
            .addOnSuccessListener {
                val intent = Intent(this, WelcomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK.or(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            }
    }

    private fun errorCode( editBox: EditText) {
        editBox.error = "Enter phone number"
        editBox.requestFocus()
    }
}