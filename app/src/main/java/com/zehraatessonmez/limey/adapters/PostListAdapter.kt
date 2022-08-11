package com.zehraatessonmez.limey.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.zehraatessonmez.limey.R
import com.zehraatessonmez.limey.listeners.PostListener
import com.zehraatessonmez.limey.util.Posts
import com.zehraatessonmez.limey.util.getDate
import com.zehraatessonmez.limey.util.loadUrl

class PostListAdapter(val userId: String, val limey_posts: ArrayList<Posts>): RecyclerView.Adapter<PostListAdapter.PostViewHolder>() {

    private var listener: PostListener? = null

    fun setListener(listener: PostListener?) {
        this.listener = listener
    }

    fun updatePost(newPosts: List<Posts>) {
        limey_posts.clear()
        limey_posts.addAll(newPosts)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int) = PostViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
    )

    override fun getItemCount() = limey_posts.size

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(userId, limey_posts[position], listener)
    }

    class PostViewHolder(v: View) : RecyclerView.ViewHolder(v) {

        private val pagelayout = v.findViewById<ViewGroup>(R.id.post_Layout)
        private val username = v.findViewById<TextView>(R.id.PostUsername)
        private val text = v.findViewById<TextView>(R.id.PostText)
        private val likepost = v.findViewById<ImageView>(R.id.postLike)
        private val postlikeCount = v.findViewById<TextView>(R.id.postLikeCount)
        private val repost = v.findViewById<ImageView>(R.id.postRePost)
        private val repostCount = v.findViewById<TextView>(R.id.postRepostCount)
        private val image = v.findViewById<ImageView>(R.id.PostImage)
        private val date = v.findViewById<TextView>(R.id.PostDate)


        fun bind(userId: String, posts: Posts, listener: PostListener?) {
            username.text = posts.username
            text.text = posts.text
            if(posts.imageUrl.isNullOrEmpty()) {
                image.visibility = View.GONE
            } else {
                image.visibility = View.VISIBLE
                image.loadUrl(posts.imageUrl)
            }
            date.text = getDate(posts.timestamp)

            postlikeCount.text = posts.likes?.size.toString()
            repostCount.text = posts.userIds?.size?.minus(1).toString()

            pagelayout.setOnClickListener { listener?.onLayoutClick(posts) }

            likepost.setOnClickListener { listener?.onLike(posts) }


            repost.setOnClickListener { listener?.onRepost(posts) }

            if(posts.likes?.contains(userId) == true) {
                likepost.setImageDrawable(ContextCompat.getDrawable(likepost.context, R.drawable.like))
            }
            else {
                likepost.setImageDrawable(ContextCompat.getDrawable(likepost.context, R.drawable.like_inactive))
            }

            if(posts.userIds?.get(0).equals(userId)) {
                repost.setImageDrawable(ContextCompat.getDrawable(likepost.context, R.drawable.original))
                repost.isClickable = false
            }
            else if (posts.userIds?.contains(userId) == true) {
                repost.setImageDrawable(ContextCompat.getDrawable(likepost.context, R.drawable.repost))
            }
            else {
                repost.setImageDrawable(ContextCompat.getDrawable(likepost.context, R.drawable.repost_inactive))
            }
        }
    }
}

