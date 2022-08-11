package com.zehraatessonmez.limey

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import android.view.inputmethod.EditorInfo
import com.zehraatessonmez.limey.R
import java.util.*
import androidx.fragment.app.FragmentPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.zehraatessonmez.limey.fragments.*
import com.zehraatessonmez.limey.listeners.HomeCallback
import com.zehraatessonmez.limey.util.APP_USERS
import com.zehraatessonmez.limey.util.AppUser
import com.zehraatessonmez.limey.util.loadUrl
import kotlinx.android.synthetic.main.activity_home_page.*
import com.zehraatessonmez.limey.util.*

//zehra ateşsönmez
//b1805.090033


class HomePageActivity : AppCompatActivity(), HomeCallback {

    private var sectionsPagerAdapter: SectionPageAdapter? = null


    private val firebaseAuth = FirebaseAuth.getInstance()

    private val firebaseDB = FirebaseFirestore.getInstance()

    private val homeFrag = HomeFragment()
    private val searchFrag = SearchFragment()
    private val userActivityFrag = UserActivityFragment()

    private var userId = FirebaseAuth.getInstance().currentUser?.uid
    private var user: AppUser? = null



    private var currentFragment: LimeyFragment = homeFrag

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)


        sectionsPagerAdapter = SectionPageAdapter(supportFragmentManager)
        container.adapter = sectionsPagerAdapter

        container.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        tabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(container))
        tabs.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            override fun onTabReselected(p0: TabLayout.Tab?) {
            }

            override fun onTabUnselected(p0: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                when(tab?.position) {
                    0 -> {
                        titleBar.visibility = View.VISIBLE
                        titleBar.text = "Limey"
                        search_bar.visibility = View.GONE
                        currentFragment = homeFrag
                    }
                    1 -> {
                        titleBar.visibility = View.GONE
                        search_bar.visibility = View.VISIBLE
                        currentFragment = searchFrag
                    }
                    2 -> {

                        titleBar.visibility = View.VISIBLE
                        titleBar.text = "Profile"
                        search_bar.visibility = View.GONE
                        currentFragment = userActivityFrag
                    }
                }
            }
        })
        fab.setOnClickListener {
            startActivity(PostScreenActivity.newIntent(this , userId, user?.username))
        }


        DefaultUserImage.setOnClickListener { view: View ->
            startActivity(UserProfileActivity.newIntent(this))
        }



        HomePageProgress.setOnTouchListener { v: View, event: MotionEvent -> true }

        search.setOnEditorActionListener { v, actionId, event ->
            if(actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchFrag.newHashtag(v?.text.toString())
            }
            true
        }

    }

    override fun onResume() {
        super.onResume()
        userId = FirebaseAuth.getInstance().currentUser?.uid
        if(userId == null) {
            startActivity(LoginActivity.newIntent(this))
            finish()
        }else {
            fill()
        }
    }

    override fun onUserUpdated() {
        fill()
    }

    override fun onRefresh() {
        currentFragment.updatePostList()
    }

    fun fill() {
        HomePageProgress.visibility = View.VISIBLE

        firebaseDB.collection(APP_USERS).document(userId!!).get()
            .addOnSuccessListener { documentSnapshot ->
                HomePageProgress.visibility = View.GONE
                var user = documentSnapshot.toObject(AppUser::class.java)
                user?.imageUrl?.let {
                    DefaultUserImage.loadUrl(it, R.drawable.default_user)
                }
                updateFragmentUser()
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                finish()
            }
    }

    fun updateFragmentUser() {
        homeFrag.setUser(user)
        searchFrag.setUser(user)
        userActivityFrag.setUser(user)
        currentFragment.updatePostList()
    }

    companion object{
        fun newIntent(context: Context) = Intent(context, HomePageActivity::class.java)
    }


    inner class SectionPageAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            return when(position) {
                0 -> homeFrag
                1 -> searchFrag
                else -> userActivityFrag
            }
        }

        override fun getCount() = 3

    }




}