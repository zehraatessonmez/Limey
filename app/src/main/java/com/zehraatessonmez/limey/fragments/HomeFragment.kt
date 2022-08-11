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
import com.zehraatessonmez.limey.util.APP_POSTS_HASHTAGS
import com.zehraatessonmez.limey.util.APP_POSTS_USER_IDS
import com.zehraatessonmez.limey.util.Posts
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.postList
import kotlinx.android.synthetic.main.fragment_home.refreshSwipe
import kotlinx.android.synthetic.main.fragment_search.*
import java.util.ArrayList


class HomeFragment : LimeyFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_home, container, false)
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


        currentUser?.let {

            val posts: ArrayList<Posts> = arrayListOf()

            for(hashtag in it.followHashtags!!) {
                firebaseDB.collection(APP_POSTS).whereArrayContains(APP_POSTS_HASHTAGS, hashtag).get()
                    .addOnSuccessListener { list ->
                        for(document in list.documents) {
                            val tweet = document.toObject(Posts::class.java)
                            tweet?.let { posts.add(it) }
                        }
                        updateAdapter(posts)
                        postList?.visibility = View.VISIBLE
                    }
                    .addOnFailureListener { e ->
                        e.printStackTrace()
                        postList?.visibility = View.VISIBLE
                    }
            }


            for (followedUser in it.followUsers!!) {

                firebaseDB.collection(APP_POSTS).whereArrayContains(APP_POSTS_USER_IDS, followedUser).get()
                    .addOnSuccessListener { list ->
                        for(document in list.documents) {
                            val tweet = document.toObject(Posts::class.java)
                            tweet?.let { posts.add(it) }
                        }
                        updateAdapter(posts)
                        postList?.visibility = View.VISIBLE
                    }
                    .addOnFailureListener { e ->
                        e.printStackTrace()
                        postList?.visibility = View.VISIBLE
                    }
            }
        }

    }

    private fun removeDuplicates(originalList: List<Posts>) = originalList.distinctBy { it.postId }


    private fun updateAdapter(posts: List<Posts>) {
        val sortedTweets = posts.sortedWith(compareByDescending { it.timestamp })
        posts_Adapter?.updatePost(removeDuplicates(sortedTweets))
    }



}