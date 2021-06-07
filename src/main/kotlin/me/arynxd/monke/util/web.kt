package me.arynxd.monke.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.arynxd.monke.MONKE_VERSION
import me.arynxd.monke.Monke
import me.arynxd.monke.handlers.translate
import me.arynxd.monke.handlers.translateInternal
import me.arynxd.monke.objects.command.threads.CommandReply
import me.arynxd.monke.objects.command.CommandEvent
import me.arynxd.monke.objects.handlers.LOGGER
import me.arynxd.monke.objects.web.RedditPost
import me.arynxd.monke.objects.web.WIKIPEDIA_API
import me.arynxd.monke.objects.web.WikipediaPage
import net.dv8tion.jda.api.utils.data.DataObject
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import ru.gildor.coroutines.okhttp.await

const val HASTEBIN_SERVER = "https://hastebin.monkebot.ml/"

suspend fun getPosts(subreddit: String, monke: Monke): List<RedditPost> {
    val request: Request = Request.Builder()
        .url("https://www.reddit.com/r/$subreddit/.json")
        .addHeader("User-Agent", "Monkebot/${MONKE_VERSION} (Discord bot)")
        .build()

    monke.handlers.okHttpClient.newCall(request).await().use {
        val body = it.body()
        val posts = mutableListOf<RedditPost>()

        if (!it.isSuccessful || body == null) {
            LOGGER.error(
                translateInternal {
                    path = "internal_error.web_service_error"
                    values = arrayOf("Reddit")
                }
            )
            return emptyList()
        }

        val redditJson = DataObject.fromJson(withContext(Dispatchers.IO) { body.string() })

        if (!redditJson.hasKey("data")) {
            return emptyList()
        }

        if (!redditJson.getObject("data").hasKey("children")) {
            return emptyList()
        }

        val jsonArray = redditJson
            .getObject("data")
            .getArray("children")

        for (i in 0 until jsonArray.length()) {
            val meme = jsonArray.getObject(i)
            if (!meme.hasKey("data")) {
                continue
            }

            posts.add(RedditPost(meme.getObject("data")))
        }

        return posts
    }

}

fun checkAndSendPost(event: CommandEvent, post: RedditPost) {
    val language = event.language
    if (!event.channel.isNSFW && (post.isNSFW() != false || post.isSpoiled() != false)) {
        event.replyAsync {
            type(CommandReply.Type.EXCEPTION)
            title(
                translate {
                    lang = language
                    path = "command_error.nsfw_reddit_post"
                }
            )
            footer()
            event.thread.post(this)
        }
        return
    }

    val description = translate {
        lang = language
        path = "command_response.reddit_description"
        values = arrayOf(
            post.getSubreddit().toString(),
            post.getAuthor().toString()
        )
    }

    val footer = translate {
        lang = language
        path = "command_response.reddit_footer"
        values = arrayOf(
            post.getUpvotes() ?: "0",
            post.getDownvotes() ?: "0"
        )
    }

    event.replyAsync {
        type(CommandReply.Type.SUCCESS)
        title(post.getTitle())
        description(description)
        image(post.getURL())
        footer(footer)
        event.thread.post(this)
    }
}

suspend fun getWikipediaPage(event: CommandEvent, subject: String): WikipediaPage? {
    val request: Request = Request.Builder()
        .url(WIKIPEDIA_API + subject)
        .build()

    event.monke.handlers.okHttpClient.newCall(request).await().use { response ->
        val body = response.body()

        if (!response.isSuccessful || body == null) {
            LOGGER.error(
                translateInternal {
                    path = "internal_error.web_service_error"
                    values = arrayOf("Wikipedia")
                }
            )
            return null
        }
        val page = WikipediaPage(DataObject.fromJson(withContext(Dispatchers.IO) { body.string() }))

        return page.takeIf { it.getType() == WikipediaPage.PageType.STANDARD } //Returns null if the page isn't standard
    }
}

suspend fun postBin(text: String, client: OkHttpClient): String? {
    val request = Request.Builder()
        .post(RequestBody.create(MediaType.parse("text/plain"), text))
        .url(HASTEBIN_SERVER + "documents")
        .header("User-Agent", "Mozilla/5.0 Monke")
        .build()

    client.newCall(request).await().use { resp ->
        if (!resp.isSuccessful) {
            return null
        }

        val charStream = resp.body()?: return null

        charStream.use { body ->
            val json = DataObject.fromJson(body.charStream())

            if (!json.hasKey("key")) {
                return null
            }

            return HASTEBIN_SERVER + json.getString("key")
        }
    }
}
