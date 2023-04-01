package com.arstyn.disign.log

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import com.arstyn.disign.MainActivity
import com.arstyn.disign.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var email: String
    private lateinit var password: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        login_back_button.setOnClickListener {
            finish()
        }

        login_button.setOnClickListener {
            email = login_email.text.toString()
            password = login_password.text.toString()
            if (email.isEmpty()) {
                errorCode("Enter email address", login_email)
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                errorCode("Enter password", login_password)
                return@setOnClickListener
            }
            if (password.length < 6) {
                errorCode("Enter 6 digit password", login_password)
                return@setOnClickListener
            }
            loginProgress.visibility = View.VISIBLE
            login_button.visibility = View.INVISIBLE
            loginWithEmail()
        }

        toSignUpButton.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        passwordForget.setOnClickListener {
            if(email.isEmpty()) {
                errorCode("Enter a gmail id", login_email)
                return@setOnClickListener
            } else {
                showDailogeBox()
            }

        }
    }

    private fun showDailogeBox() {
        AlertDialog.Builder(this)
            .setMessage("Reset code will be sent to your email Id:\n$email")
            .setTitle("Reset Password")
            .setPositiveButton(R.string.ok) { _, _ ->
                auth.sendPasswordResetEmail(email)
                    .addOnSuccessListener {
                    }
            }
            .setNegativeButton(R.string.noEmail){ _, _ ->

            }
            .setCancelable(true)
            .create()
            .show()
    }

    private fun loginWithEmail() {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK.or(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            }
            .addOnFailureListener {
                if (it.message == "The password is invalid or the user does not have a password.") {
                    errorCode(it.message, login_password)
                } else {
                    errorCode(it.message, login_email)
                }
                loginProgress.visibility = View.INVISIBLE
                login_button.visibility = View.VISIBLE
                passwordForget.visibility = View.VISIBLE
            }
    }

    private fun errorCode(errorMsg:String?, editBox: EditText) {
        editBox.error = errorMsg
        editBox.requestFocus()
        return
    }

}
