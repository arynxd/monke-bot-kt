package me.arynxd.monke.objects.web

import net.dv8tion.jda.api.utils.data.DataObject

class RedditPost(val data: DataObject) {

    fun getSubreddit(): String? {
        return if (data.hasKey("subreddit_name_prefixed")) data.getString("subreddit_name_prefixed") else null
    }

    fun isMedia(): Boolean {
        return getURL() != null && (getURL()!!.endsWith(".png") || getURL()!!.endsWith(".gif") || getURL()!!.endsWith(".jpg"))
    }

    fun getUpvotes(): String? {
        return if (data.hasKey("ups")) data.getString("ups") else null
    }

    fun getDownvotes(): String? {
        return if (data.hasKey("downs")) data.getString("downs") else null
    }

    fun isNSFW(): Boolean? {
        return if (data.hasKey("over_18")) data.getBoolean("over_18") else null
    }

    fun isSpoiled(): Boolean? {
        return if (data.hasKey("spoiler")) data.getBoolean("spoiler") else null
    }

    fun getURL(): String? {
        return if (data.hasKey("url")) data.getString("url") else null
    }

    fun getAuthor(): String? {
        return if (data.hasKey("author")) "u/" + data.getString("author") else null
    }

    fun isStickied(): Boolean? {
        return if (data.hasKey("stickied")) data.getBoolean("stickied") else null
    }

    fun isPinned(): Boolean? {
        return if (data.hasKey("pinned")) data.getBoolean("pinned") else null
    }

    fun getTitle(): String? {
        return if (data.hasKey("title")) data.getString("title") else null
    }
}