package com.zehraatessonmez.limey.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.auth.User
import com.zehraatessonmez.limey.R
import com.zehraatessonmez.limey.adapters.PostListAdapter
import com.zehraatessonmez.limey.listeners.LimeyListenerImpl
import com.zehraatessonmez.limey.listeners.PostListener
import com.zehraatessonmez.limey.util.*
import com.zehraatessonmez.limey.util.Posts
import kotlinx.android.synthetic.main.fragment_search.*
import com.zehraatessonmez.limey.util.AppUser

import java.lang.Exception
import java.util.ArrayList


class SearchFragment : LimeyFragment() {

    private var currentHashtag = ""
    private var hashtagFollowed = false



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        listener = LimeyListenerImpl(postList,currentUser,callback)

        posts_Adapter = PostListAdapter(userId!!, arrayListOf())

        posts_Adapter?.setListener(listener)

        postList?.apply {

            layoutManager = LinearLayoutManager(context)
            adapter = posts_Adapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        }

        refreshSwipe.setOnRefreshListener {
            refreshSwipe.isRefreshing = false
            updatePostList()
        }

        hashtagFollow.setOnClickListener {
            hashtagFollow.isClickable = false
            val followed: ArrayList<String>? = currentUser?.followHashtags
            if(hashtagFollowed) {
                followed?.remove(currentHashtag)
            } else {
                followed?.add(currentHashtag)
            }

            firebaseDB.collection(APP_USERS).document(userId!!).update(APP_USER_HASTAGS, followed)
                .addOnSuccessListener {
                    callback?.onUserUpdated()
                    hashtagFollow.isClickable = true
                }
                .addOnFailureListener {e: Exception ->
                    e.printStackTrace()
                    hashtagFollow.isClickable = true
                }
        }
    }


    fun newHashtag(term: String) {
        currentHashtag = term
        hashtagFollow.visibility = View.VISIBLE
        updatePostList()
    }

    override fun updatePostList() {
        postList?.visibility = View.GONE

        firebaseDB.collection(APP_POSTS).whereArrayContains(APP_POSTS_HASHTAGS, currentHashtag).get()
            .addOnSuccessListener { list: QuerySnapshot ->
                postList?.visibility = View.VISIBLE
                val postsApp: ArrayList<Posts> = arrayListOf<Posts>()
                for(document in list.documents) {
                    val post: Posts? = document.toObject(Posts::class.java)
                    post?.let { postsApp.add(it) }
                }
                val sortedPosts: List<Posts> = postsApp.sortedWith(compareByDescending { it.timestamp })
                posts_Adapter?.updatePost(sortedPosts)
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }

        updateTopicFollow()
    }

    private fun updateTopicFollow() {
        hashtagFollowed = currentUser?.followHashtags?.contains(currentHashtag) == true

        context?.let {
            if (hashtagFollowed) {
                hashtagFollow.setImageDrawable(ContextCompat.getDrawable(it, R.drawable.followtopic))
            } else {
                hashtagFollow.setImageDrawable(ContextCompat.getDrawable(it, R.drawable.followtopic_inactive))
            }
        }
    }

}