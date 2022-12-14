package com.zehraatessonmez.limey

import android.content.ClipData.newIntent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.firebase.auth.FirebaseAuth

import android.content.Context
import android.content.Intent
import com.zehraatessonmez.limey.util.AppUser
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.activity_login.*




class LoginActivity : AppCompatActivity() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseAuthListener = FirebaseAuth.AuthStateListener {
        val user = firebaseAuth.currentUser?.uid
        user?.let {
            startActivity(HomePageActivity.newIntent(this))
            finish()
        }
    }

    companion object {
        fun newIntent(context: Context) = Intent(context, LoginActivity::class.java)
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        setTextChangeListener(emailGetTI, emailGet)
        setTextChangeListener(passwordET, passwordTIL)

        LoginProgress.setOnTouchListener { v, event -> true }

    }

    fun setTextChangeListener(et: EditText, til: TextInputLayout) {
        et.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                til.isErrorEnabled = false
            }

        })
    }

    fun onLogin(view: View) {
        var proceed = true
        if(emailGetTI.text.isNullOrEmpty()) {
            emailGet.error = "Email is required"
            emailGet.isErrorEnabled = true
            proceed = false
        }
        if(passwordET.text.isNullOrEmpty()) {
            passwordTIL.error = "Password is required"
            passwordTIL.isErrorEnabled = true
            proceed = false
        }
        if(proceed) {
            LoginProgress.visibility = View.VISIBLE
            firebaseAuth.signInWithEmailAndPassword(emailGetTI.text.toString(), passwordET.text.toString())
                .addOnCompleteListener { task ->
                    if(!task.isSuccessful) {
                        LoginProgress.visibility = View.GONE
                        Toast.makeText(this@LoginActivity, "Login error: ${task.exception?.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                    LoginProgress.visibility = View.GONE
                }
        }
    }


    fun goToSignup(view: View) {
        startActivity(RegisterActivity.newIntent(this))
        finish()
    }

    override fun onStart() {
        super.onStart()
        firebaseAuth.addAuthStateListener(firebaseAuthListener)
    }

    override fun onStop() {
        super.onStop()
        firebaseAuth.removeAuthStateListener(firebaseAuthListener)
    }



}