package com.zehraatessonmez.limey.fragments

import android.content.Context
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.zehraatessonmez.limey.adapters.PostListAdapter
import com.zehraatessonmez.limey.listeners.HomeCallback
import com.zehraatessonmez.limey.listeners.LimeyListenerImpl
import com.zehraatessonmez.limey.listeners.PostListener
import com.zehraatessonmez.limey.util.AppUser

abstract class LimeyFragment : Fragment() {
    protected var posts_Adapter: PostListAdapter? = null

    protected var userId = FirebaseAuth.getInstance().currentUser?.uid
    protected val firebaseDB = FirebaseFirestore.getInstance()


    protected var currentUser: AppUser? = null
    protected var listener: LimeyListenerImpl? = null
    protected var callback: HomeCallback? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is HomeCallback) {
            callback = context
        } else {
            throw RuntimeException(context.toString() + " error in homecallbak")
        }
    }


    abstract fun updatePostList()


    fun setUser(user: AppUser?) {
        this.currentUser = user

    }

    override fun onResume() {
        super.onResume()
        updatePostList()
    }


}