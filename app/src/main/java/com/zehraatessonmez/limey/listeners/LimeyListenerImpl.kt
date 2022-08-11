package com.zehraatessonmez.limey.listeners

import android.app.AlertDialog
import android.content.DialogInterface
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.zehraatessonmez.limey.util.*
import java.util.ArrayList

class LimeyListenerImpl(val postList: RecyclerView, var user: AppUser?, val callback: HomeCallback?): PostListener {

    private val firebaseDB = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onLayoutClick(posts: Posts) {
        posts.let {
            val owner = posts.userIds?.get(0)
            if (owner != userId) {
                if (user?.followUsers?.contains(owner) == true) {
                    AlertDialog.Builder(postList.context)

                        .setTitle("Unfollow ${posts.username}?")
                        .setPositiveButton("yes") { dialog, which ->
                            postList.isClickable = false
                            var followedUsers = user?.followUsers
                            if(followedUsers == null) {
                                followedUsers = arrayListOf()
                            }

                            followedUsers?.remove(owner)
                            firebaseDB.collection(APP_USERS).document(userId!!).update(
                                APP_USER_FOLLOW, followedUsers)
                                .addOnSuccessListener {
                                    postList.isClickable = true
                                    callback?.onUserUpdated()
                                }
                                .addOnFailureListener {
                                    postList.isClickable = true
                                }
                        }
                        .setNegativeButton("cancel") { dialog: DialogInterface, which -> }
                        .show()
                } else {
                    AlertDialog.Builder(postList.context)
                        .setTitle("Follow ${posts.username}?")
                        .setPositiveButton("yes") { dialog: DialogInterface, which ->
                            postList.isClickable = false
                            var followedUsers = user?.followUsers
                            if(followedUsers == null) {
                                followedUsers = arrayListOf()
                            }
                            owner?.let {
                                followedUsers?.add(owner)
                                firebaseDB.collection(APP_USERS).document(userId!!)
                                    .update(APP_USER_FOLLOW, followedUsers)
                                    .addOnSuccessListener {
                                        postList.isClickable = true
                                        callback?.onUserUpdated()
                                    }
                                    .addOnFailureListener {
                                        postList.isClickable = true
                                    }
                            }
                        }
                        .setNegativeButton("cancel") { dialog: DialogInterface, which -> }
                        .show()
                }
            }
        }
    }

    override fun onLike(posts: Posts) {

        posts.let {
            postList.isClickable = false
            val likes: ArrayList<String>? = posts.likes
            if (posts.likes?.contains(userId) == true) {
                likes?.remove(userId)
            } else {
                likes?.add(userId!!)
            }
            firebaseDB.collection(APP_POSTS).document(posts.postId!!).update(APP_POSTS_LIKES,likes)
                .addOnSuccessListener {
                    postList.isClickable = true
                    callback?.onRefresh()
                }
                .addOnFailureListener {
                    postList.isClickable = true
                }
        }

    }

    override fun onRepost(posts: Posts) {

        posts.let {
            postList.isClickable = false
            val reposts: ArrayList<String>? = posts.userIds
            if (reposts?.contains(userId) == true) {
                reposts?.remove(userId)
            } else {
                reposts?.add(userId!!)
            }
            firebaseDB.collection(APP_POSTS).document(posts.postId!!).update(APP_POSTS_USER_IDS, reposts)
                .addOnSuccessListener {
                    postList.isClickable = true
                    callback?.onRefresh()
                }
                .addOnFailureListener {
                    postList.isClickable = true
                }
        }
        
    }
}