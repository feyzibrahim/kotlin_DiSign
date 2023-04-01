package com.arstyn.disign.log

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.arstyn.disign.R
import com.arstyn.disign.WelcomeActivity
import com.arstyn.disign.modal.User
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_sign_up.*
import java.util.*
import java.util.concurrent.TimeUnit

class SignUpActivity : AppCompatActivity() {

    private lateinit var name:String
    private lateinit var code: String
    private lateinit var email: String
    private lateinit var password: String
    private var uid: String? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var phoneNumber: String
    private var selectedPicUri: Uri? = null
    private lateinit var db : FirebaseFirestore
    private lateinit var storedVerificationId :String
    private lateinit var credential: PhoneAuthCredential
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        auth = FirebaseAuth.getInstance()

        db = Firebase.firestore

        signUp_back_bu.setOnClickListener {
            finish()
        }

        details_next_button.setOnClickListener {
            val view = this.currentFocus
            view?.let { v ->
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.hideSoftInputFromWindow(v.windowToken, 0)
            }
            detailsButtonClick()
        }

        password_confirm_button.setOnClickListener {
            val view = this.currentFocus
            view?.let { v ->
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.hideSoftInputFromWindow(v.windowToken, 0)
            }
            passwordConfirm()
        }

        signUp_dp.setOnClickListener {
            CropImage.activity()
                .setOutputCompressQuality(40)
                .setAllowRotation(false)
                .setAllowFlipping(false)
                .setAspectRatio(1,1)
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(this)
        }

        signUp_login_button.setOnClickListener {
            val otp = verification.text.toString()
            if (otp.isNotEmpty()) {
                signUp_login_button.visibility = View.GONE
                credential = PhoneAuthProvider.getCredential(storedVerificationId, otp)
                signUpWithEmail()
            }

        }

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                code = p0.smsCode.toString()

                if (code.isNotEmpty()) {
                    verification.setText(code)
                }
            }

            override fun onVerificationFailed(p0: FirebaseException) {
                Toast.makeText(this@SignUpActivity, "Error at verification", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.INVISIBLE
            }

            override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
                storedVerificationId = p0
                signUp_login_button.isEnabled = true
                signUp_login_button.background = ContextCompat.getDrawable(applicationContext, R.drawable.round_button_yellow)
            }

            override fun onCodeAutoRetrievalTimeOut(p0: String) {
                super.onCodeAutoRetrievalTimeOut(p0)
                verify_code_text.text = getString(R.string.autoverifyError)
                progressBar.visibility = View.INVISIBLE
            }
        }

        verification.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                val otp = verification.text.toString()
                if (s != null && s.length == 6) {
                    signUp_login_button.visibility = View.GONE
                    credential = PhoneAuthProvider.getCredential(storedVerificationId, otp)
                    signUpWithEmail()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        })

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                selectedPicUri = result.uri
                signUp_dp.alpha = 0F
                signUp_dp_selected.setImageURI(selectedPicUri)
            } else {
                Toast.makeText(this, "Error at cropping", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun errorCode(errorMsg:String, editBox:EditText) {
        editBox.error = errorMsg
        editBox.requestFocus()
        return
    }

    private fun startPhoneNumberVerification() {
        Toast.makeText(this, phoneNumber, Toast.LENGTH_SHORT).show()
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(120L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
//        PhoneAuthProvider.getInstance().verifyPhoneNumber(
//            phoneNumber,
//            120,
//            TimeUnit.SECONDS,
//            this,
//            callbacks
//        )
    }

    private fun passwordConfirm() {
        val pass1 = signUp_password.text.toString()
        val pass2 = signUp_passAgain.text.toString()
        if (pass1.isEmpty()){
            errorCode("Enter password", signUp_password)
            return
        } else if(pass1.length < 8) {
            errorCode("Password must be 8 digits", signUp_password)
            return
        }
        if (pass2.isEmpty()){
            errorCode("Enter password again", signUp_password)
            return
        }
        else if(pass1.length < 8) {
            errorCode("Password must be 8 digits", signUp_passAgain)
            return
        }
        if (pass1 == pass2) {
            password = pass1
            showDailogeBox()
        } else {
            errorCode("Not Match", signUp_passAgain)
            return
        }
    }

    private fun showDailogeBox() {
        AlertDialog.Builder(this)
            .setMessage("We will be verifying your phone number. Is this ok, or would yuo like to edit the number?")
            .setTitle(phoneNumber)
            .setPositiveButton(R.string.ok) { _, _ ->
                toggleVisibility(password_confirm, phoneVerify, "3/3")
                startPhoneNumberVerification()
            }
            .setNegativeButton(R.string.no){ _, _ ->
                toggleVisibility(password_confirm, detailsEnter, "1/3")
            }
            .setCancelable(true)
            .create()
            .show()
    }

    private fun detailsButtonClick() {
        val ccc = countryCodePicker.selectedCountryCode.toString()
        val plus = "+"
        val phone = signUp_phone.text.toString()
        phoneNumber = "$plus$ccc$phone"
        name = signUp_name.text.toString()
        email = signUp_email.text.toString()

        if(name.isEmpty()) {
            errorCode("Enter a name", signUp_name)
            return
        }
        if(email.isEmpty()) {
            errorCode("Enter email", signUp_email)
            return
        }
        if (phone.isEmpty() || phone.length != 10) {
            errorCode("Enter a valid Number", signUp_phone)
            return
        }

        toggleVisibility(detailsEnter, password_confirm, "2/3")
    }

    private fun signUpWithEmail() {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                uid = it.user?.uid.toString()
                registerPhone()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Sign up Failed", Toast.LENGTH_LONG).show()
            }
    }

    private fun toggleVisibility(toBeGone: ConstraintLayout, toBeShown: ConstraintLayout, text:String) {
        toBeGone.visibility = View.GONE
        toBeShown.visibility = View.VISIBLE
        signup_stepCount.text = text
    }

    private fun registerPhone() {
        auth.currentUser?.updatePhoneNumber(credential)
            ?.addOnSuccessListener {
                saveDpToStorage()
            }
            ?.addOnFailureListener {
                Toast.makeText(this, "Phone Registration Failed", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveDpToStorage() {
        if (selectedPicUri != null) {
            val fileName = UUID.randomUUID().toString()
            val ref = Firebase.storage.reference.child("/profileImages/$fileName")
            ref.putFile(selectedPicUri!!)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener {
                        saveToFirebase(it.toString())
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "DP failed", Toast.LENGTH_SHORT).show()
                }
        } else {
            val img = ""
            saveToFirebase(img)
        }
    }

    private fun saveToFirebase(uri:String) {
        val user = uid?.let { User(name, phoneNumber, email, uri, it) }
        if (user != null) {
            db.collection("User").document("$uid")
                .set(user)
                .addOnSuccessListener {
                    val intent = Intent(this, WelcomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK.or(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                }
        }
    }
}
