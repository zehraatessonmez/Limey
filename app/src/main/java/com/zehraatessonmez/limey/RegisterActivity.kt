package com.zehraatessonmez.limey

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.zehraatessonmez.limey.util.APP_USERS
import com.zehraatessonmez.limey.util.AppUser
import kotlinx.android.synthetic.main.activity_register.*



class RegisterActivity : AppCompatActivity() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseDB = FirebaseFirestore.getInstance()

    private val firebaseAuthListener = FirebaseAuth.AuthStateListener {
        val user = firebaseAuth.currentUser?.uid
        user?.let {
            startActivity(HomePageActivity.newIntent(this))
            finish()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        setTextChangeListener(usernameGetTI,usernameGet)

        setTextChangeListener(emailGetTI, emailGet)
        setTextChangeListener(passwordET, passwordTIL)

        RegisterProgress.setOnTouchListener { v, event -> true }

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

    fun onRegister(view: View){
        var proceed = true
        if(usernameGetTI.text.isNullOrEmpty()) {
            usernameGet.error = "Username is required"
            usernameGet.isErrorEnabled = true
            proceed = false
        }
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
            RegisterProgress.visibility = View.VISIBLE
            firebaseAuth.createUserWithEmailAndPassword(emailGetTI.text.toString(), passwordET.text.toString())
                .addOnCompleteListener { task ->
                    if(!task.isSuccessful) {
                        Toast.makeText(this@RegisterActivity, "Signup error: ${task.exception?.localizedMessage}", Toast.LENGTH_SHORT).show()
                    } else {
                        val email = emailGetTI.text.toString()
                        val username = usernameGetTI.text.toString()
                        val user = AppUser(email, username, "", arrayListOf(), arrayListOf())
                        firebaseDB.collection(APP_USERS).document(firebaseAuth.uid!!).set(user)
                    }
                    RegisterProgress.visibility = View.GONE
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                    RegisterProgress.visibility = View.GONE
                }
        }
    }

    fun backToLoginPage(view: View){
        startActivity(LoginActivity.newIntent(this))
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

    companion object{
        fun newIntent(context: Context) = Intent(context, RegisterActivity::class.java)
    }
}