package com.zehraatessonmez.limey.listeners

import com.zehraatessonmez.limey.util.Posts

interface PostListener {
    fun onLayoutClick(posts: Posts)
    fun onLike(posts: Posts)
    fun onRepost(posts: Posts)
}