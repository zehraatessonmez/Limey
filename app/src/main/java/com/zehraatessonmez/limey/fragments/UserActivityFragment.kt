package com.zehraatessonmez.limey.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.zehraatessonmez.limey.R
import com.zehraatessonmez.limey.adapters.PostListAdapter
import com.zehraatessonmez.limey.listeners.LimeyListenerImpl
import com.zehraatessonmez.limey.util.APP_POSTS
import com.zehraatessonmez.limey.util.APP_POSTS_USER_IDS
import com.zehraatessonmez.limey.util.Posts
import kotlinx.android.synthetic.main.fragment_user_activity.*


class UserActivityFragment : LimeyFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_user_activity, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listener = LimeyListenerImpl(postList, currentUser, callback)

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
    }

    override fun updatePostList() {

        postList?.visibility = View.GONE
        val posts: ArrayList<Posts> = arrayListOf<Posts>()

        firebaseDB.collection(APP_POSTS).whereArrayContains(APP_POSTS_USER_IDS, userId!!).get()
            .addOnSuccessListener { list ->
                for (document in list.documents) {
                    val post = document.toObject(Posts::class.java)
                    post?.let {
                        posts.add(post)
                    }
                }

                val sortedList = posts.sortedWith(compareByDescending { it.timestamp })

                posts_Adapter?.updatePost(sortedList)

                postList?.visibility = View.VISIBLE
            }
            .addOnFailureListener { e ->
                e.printStackTrace()

                postList?.visibility = View.VISIBLE
            }

    }


}