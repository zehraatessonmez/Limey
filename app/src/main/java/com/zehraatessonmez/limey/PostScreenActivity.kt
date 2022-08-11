package com.zehraatessonmez.limey

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.zehraatessonmez.limey.util.*
import kotlinx.android.synthetic.main.activity_post_screen.*


class PostScreenActivity : AppCompatActivity() {

    private var imageUrl: String? = null
    private var userId: String? = null
    private var userName: String? = null
    private val firebaseDB = FirebaseFirestore.getInstance()
    private val firebaseStorage = FirebaseStorage.getInstance().reference




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_screen)

        if(intent.hasExtra(VAL_USER_ID) && intent.hasExtra(VAL_USER_NAME)) {

            userId = intent.getStringExtra(VAL_USER_ID)

            userName = intent.getStringExtra(VAL_USER_NAME)
        } else {
            Toast.makeText(this, "Error creating post", Toast.LENGTH_SHORT).show()
            finish()
        }

        PostPageProgress.setOnTouchListener { v, event: MotionEvent -> true }


    }


    fun addImage(v: View) {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_PHOTO)
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
            PostPageProgress.visibility = View.VISIBLE

            val filePath = firebaseStorage.child(APP_IMAGES).child(userId!!)

            filePath.putFile(imageUri)
                .addOnSuccessListener {
                    filePath.downloadUrl
                        .addOnSuccessListener {uri ->
                            imageUrl = uri.toString()
                            PostImage.loadUrl(imageUrl, R.drawable.home_inactive)


                            PostPageProgress.visibility = View.GONE
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
        Toast.makeText(this, "Image upload failed. Please try again later.", Toast.LENGTH_SHORT).show()
        PostPageProgress.visibility = View.GONE
    }


    fun createPost(v: View){
        PostPageProgress.visibility = View.VISIBLE
        val text = postText.text.toString()
        val hashtags = getHashtags(text)

        val postId = firebaseDB.collection(APP_POSTS).document()


        val post = Posts(postId.id, arrayListOf(userId!!),  userName , text, imageUrl, System.currentTimeMillis(), hashtags, arrayListOf())

        postId.set(post)
            .addOnCompleteListener { finish() }
            .addOnFailureListener { e ->
                e.printStackTrace()
                PostPageProgress.visibility = View.GONE
                Toast.makeText(this, "Failed to post the tweet.", Toast.LENGTH_SHORT).show()
            }
    }

    fun getHashtags(source: String): ArrayList<String> {
        val hashtags = arrayListOf<String>()
        var text = source

        while (text.contains("#")) {
            var hashtag = ""
            val hash = text.indexOf("#")
            text = text.substring(hash + 1)

            val firstSpace = text.indexOf(" ")
            val firstHash = text.indexOf("#")

            if(firstSpace == -1 && firstHash == -1) {
                hashtag = text.substring(0)
            } else if (firstSpace != -1 && firstSpace < firstHash) {
                hashtag = text.substring(0, firstSpace)
                text = text.substring(firstSpace + 1)
            } else {
                hashtag = text.substring(0, firstHash)
                text = text.substring(firstHash)
            }

            if(!hashtag.isNullOrEmpty()) {
                hashtags.add(hashtag)
            }
        }

        return hashtags
    }



    companion object{
        val VAL_USER_ID = "UserId"
        val VAL_USER_NAME: String = "UserName"

        fun newIntent(context: Context, userId: String?, userName: String?): Intent {
            val intent = Intent(context, PostScreenActivity::class.java)
            intent.putExtra(VAL_USER_ID, userId)
            intent.putExtra(VAL_USER_NAME, userName)
            return intent
        }
    }
}