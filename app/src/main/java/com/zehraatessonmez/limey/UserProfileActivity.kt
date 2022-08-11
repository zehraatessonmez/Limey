package com.zehraatessonmez.limey


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.zehraatessonmez.limey.util.AppUser
import android.widget.TextView
import android.widget.Toast
import com.zehraatessonmez.limey.util.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


import com.google.firebase.storage.FirebaseStorage
import com.zehraatessonmez.limey.util.APP_USERS
import kotlinx.android.synthetic.main.activity_user_profile.*

class UserProfileActivity : AppCompatActivity() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseDB = FirebaseFirestore.getInstance()
    private val firebaseStorage = FirebaseStorage.getInstance().reference
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private var imageUrl: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        if(userId == null) {
            finish()
        }

        ProfilePageProgress.setOnTouchListener { v, event -> true }

        photoIV.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_PHOTO)
        }


        populateInfo()
    }

    fun populateInfo(){
        ProfilePageProgress.visibility = View.VISIBLE
        firebaseDB.collection(APP_USERS).document(userId!!).get()
            .addOnSuccessListener { documentSnapshot ->
                val user = documentSnapshot.toObject(AppUser::class.java)
                usernameET.setText(user?.username, TextView.BufferType.EDITABLE)
                emailET.setText(user?.email, TextView.BufferType.EDITABLE)
                imageUrl = user?.imageUrl
                imageUrl?.let {
                    photoIV.loadUrl(user?.imageUrl, R.drawable.default_user)
                }
                ProfilePageProgress.visibility = View.GONE
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                finish()
            }
    }

    fun onApply(view: View){
        ProfilePageProgress.visibility = View.VISIBLE
        val username = usernameET.text.toString()
        val email = emailET.text.toString()
        val map = HashMap<String, Any>()
        map[APP_USER_USERNAME] = username
        map[APP_USER_EMAIL] = email

        firebaseDB.collection(APP_USERS).document(userId!!).update(map)
            .addOnSuccessListener {
                Toast.makeText(this, "Update successful", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                Toast.makeText(this, "Update failed. Please try again.", Toast.LENGTH_SHORT).show()
                ProfilePageProgress.visibility = View.GONE
            }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == REQUEST_PHOTO) {
            storeImage(data?.data)
        }
    }

    fun storeImage(imageUri: Uri?) {
        imageUri?.let {
            Toast.makeText(this, "Uploading...", Toast.LENGTH_SHORT).show()
            ProfilePageProgress.visibility = View.VISIBLE

            val filePath = firebaseStorage.child(APP_IMAGES).child(userId!!)
            filePath.putFile(imageUri)
                .addOnSuccessListener {
                    filePath.downloadUrl
                        .addOnSuccessListener {uri ->
                            val url = uri.toString()
                            firebaseDB.collection(APP_USERS).document(userId!!).update(APP_USER_IMAGE_URL, url)
                                .addOnSuccessListener {
                                    imageUrl = url
                                    photoIV.loadUrl(imageUrl, R.drawable.default_user)
                                }
                            ProfilePageProgress.visibility = View.GONE
                        }
                        .addOnFailureListener {
                            onUploadFailure()
                        }
                }
                .addOnFailureListener {
                    onUploadFailure()
                }
        }
    }

    fun onUploadFailure() {
        Toast.makeText(this, "Image upload failed. Please try agail later.", Toast.LENGTH_SHORT).show()
        ProfilePageProgress.visibility = View.GONE
    }
    fun onSignout(view: View) {
        firebaseAuth.signOut()
        finish()
    }


    companion object{
        fun newIntent(context: Context) = Intent(context, UserProfileActivity::class.java)
    }

}